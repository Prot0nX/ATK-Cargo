<?php
// PHP/src/Services/CargoService.php

declare(strict_types=1);

namespace App\Services;

use Exception;
use InvalidArgumentException;
use App\Repositories\CargoRepository;
use App\Core\Logger;
use App\Core\MicroCache;
use App\Exceptions\ApiException;
use App\Exceptions\ConflictException;
use App\Enums\CargoStatus;
use App\Enums\CargoConfirmStatus;

class CargoService {

    private const NEW_ENTRY_TONNAGE_BUFFER_KG = 7000.0;

    private CargoRepository $repo;
    private Logger $logger;

    public function __construct(?CargoRepository $repo = null) {
        $this->repo = $repo ?? new CargoRepository();
        $this->logger = Logger::getInstance();
    }

    public function saveOrUpdateCargo(array $params): array {
        $shipName = $params['shipName'];
        $trackingNumber = $params['trackingNumber'];
        $loadingQuotaNumber = $params['loadingQuotaNumber'];

        $conn = $this->repo->getPdoConnection();
        $conn->exec("SET SESSION sql_mode = 'STRICT_TRANS_TABLES'");
        $conn->exec("SET SESSION innodb_lock_wait_timeout = 5");
        $conn->beginTransaction();

        try {
            $yesterdayStart = date('Y-m-d 00:00:00', strtotime('-1 day'));
            $currentTime = jdate("H:i");
            $currentDate = jdate("Y/m/d");

            $quotaControl = $this->repo->findQuotaControlData(
                $shipName,
                $params['loadingWarehouse'],
                $params['cargoType'],
                $params['shippingCompany'],
                $loadingQuotaNumber
            );

            if ($quotaControl && !(bool)$quotaControl['isActive']) {
                throw new ApiException('این کوتاژ غیرفعال است و امکان ثبت یا خروج حواله برای آن وجود ندارد.', 403);
            }

            $tempTonnageCheck = $this->repo->findTempTonnage(
                $shipName,
                $params['loadingWarehouse'],
                $params['cargoType'],
                $params['shippingCompany'],
                $loadingQuotaNumber
            );
            if ($tempTonnageCheck
                && (int)$tempTonnageCheck['temp_tonnage_status'] === 1
                && (float)$tempTonnageCheck['temp_tonnage_amount'] <= 0
            ) {
                throw new ApiException('تناژ موقت به پایان رسیده است. امکان ثبت حواله جدید یا خروج وجود ندارد. لطفاً با مسئول خود بررسی کنید.', 403);
            }

            // ۱. بررسی وجود حواله تکراری در ۲۴ ساعت گذشته برای این کشتی
            $existing24hCargo = $this->repo->find24hCargo($shipName, $trackingNumber, $yesterdayStart);

            if ($existing24hCargo && (string)$existing24hCargo['loadingQuotaNumber'] !== $loadingQuotaNumber) {
                if ($params['duplicateConfirmation'] !== "proceed") {
                    $warningParts = [
                        "شماره حواله ({$trackingNumber}) در 24 ساعت گذشته برای کشتی [ {$shipName} ] قبلاً ثبت شده است:\n\n",
                        "شماره کوتاژ ثبت شده: {$existing24hCargo['loadingQuotaNumber']}\n",
                        "انبار ثبت شده: {$existing24hCargo['loadingWarehouse']}\n"
                    ];
                    
                    $optionalLabels = [
                        'shippingCompany' => 'شرکت باربری',
                        'entryTime' => 'ساعت ورود',
                        'exitTime' => 'ساعت خروج',
                        'exitDate' => 'تاریخ خروج'
                    ];
                    
                    foreach ($optionalLabels as $field => $label) {
                        if (!empty($existing24hCargo[$field])) {
                            $warningParts[] = "$label: {$existing24hCargo[$field]}\n";
                        }
                    }
                    $warningParts[] = "وضعیت فعلی حواله: {$existing24hCargo['status']}";

                    $conn->rollBack();
                    return [
                        "code" => 409,
                        "data" => [
                            "warning" => true,
                            "message" => implode('', $warningParts),
                            "existing_cargo" => $existing24hCargo,
                            "requires_confirmation" => true
                        ]
                    ];
                }
            }

            // ۲. بررسی وجود حواله با کلیدهای اصلی
            $existingCargo = $this->repo->findCargoByKeys(
                $shipName, 
                $params['loadingWarehouse'], 
                $params['cargoType'], 
                $params['shippingCompany'], 
                $loadingQuotaNumber, 
                $trackingNumber
            );

            $shouldInsertNew = !$existingCargo;

            if ($existingCargo) {
                $isClosedService = $existingCargo['status'] === CargoStatus::EXITED->value;
                $isNewEntryAttempt = $isClosedService || (
                    empty($params['netWeight']) && empty($params['shortageWeight']) && empty($params['excessWeight'])
                );

                if ($isNewEntryAttempt) {
                    if ($params['duplicateConfirmation'] !== "proceed") {
                        $cargoStatus = $existingCargo['status'];
                        $cargoDate = $cargoStatus === CargoStatus::EXITED->value ? $existingCargo['exitDate'] : ($existingCargo['exitDate'] ?: $currentDate);
                        $cargoTime = $cargoStatus === CargoStatus::EXITED->value ? $existingCargo['exitTime'] : ($existingCargo['entryTime'] ?: $currentTime);
                        
                        $warningMessage = "شماره حواله \"{$trackingNumber}\" برای شماره کوتاژ \"{$loadingQuotaNumber}\" برای کشتی [ {$shipName} ] قبلاً در تاریخ {$cargoDate} و ساعت {$cargoTime} در وضعیت [ {$cargoStatus} ] ثبت شده است.\n\nآیا اطمینان دارید که می‌خواهید حواله جدید با همین مشخصات ثبت کنید؟";
                        
                        $conn->rollBack();
                        return [
                            "code" => 409,
                            "data" => [
                                "warning" => true,
                                "message" => $warningMessage,
                                "existing_cargo" => $existingCargo,
                                "requires_confirmation" => true
                            ]
                        ];
                    } else {
                        $shouldInsertNew = true;
                    }
                }
            }

            // ۳. درج حواله جدید یا به‌روزرسانی حواله موجود
            if ($shouldInsertNew) {
                if ($quotaControl) {
                    $isPercentageRestricted = (bool)$quotaControl['is_enabled'];
                    $percentage = $quotaControl['percentage'] !== null ? (float)$quotaControl['percentage'] : null;
                    if ($isPercentageRestricted && $percentage !== null) {
                        $totalTonnage = (float)$quotaControl['totalTonnage'];
                        $loadedTonnage = (float)$quotaControl['loadedTonnage'];
                        $remainingTonnage = $totalTonnage - $loadedTonnage;
                        $percentageAmount = $totalTonnage * ($percentage / 100);
                        $loadableTonnage = $remainingTonnage - $percentageAmount;
                        if ($loadableTonnage <= self::NEW_ENTRY_TONNAGE_BUFFER_KG) {
                            $bufferKg = number_format(self::NEW_ENTRY_TONNAGE_BUFFER_KG, 0);
                            throw new ApiException(
                                "تناژ مجاز باقی‌مانده برای این کوتاژ کمتر از {$bufferKg} کیلوگرم است (حد نصاب {$percentage}%). امکان ثبت حوالهٔ جدید وجود ندارد.",
                                403
                            );
                        }
                    }
                }

                if (!empty($params['netWeight'])) {
                    throw new ApiException("برای ثبت حوالهٔ جدید نمی‌توان وزن خالص ثبت کرد؛ ابتدا حواله ثبت و توسط بارشمار تأیید شود، سپس خروج ثبت شود.", 400);
                }

                $numberOfPeople = filter_var($params['numberOfPeople'], FILTER_VALIDATE_INT);
                if ($numberOfPeople === false || $numberOfPeople < 1) {
                    throw new ApiException("تعداد نفرات باید عددی بزرگتر از صفر باشد", 400);
                }

                $res = $this->repo->insertCargo($params, $currentTime, (int)$numberOfPeople);
                if (!$res) {
                    throw new Exception("خطا در اجرای دستور درج حواله");
                }
                $conn->commit();
                $this->invalidateShipsListCache();

                $this->logger->info("New cargo entry tracking: $trackingNumber, ship: $shipName, user: {$params['username']}");

                return [
                    "code" => 200,
                    "data" => [
                        "success" => true, 
                        "message" => "حواله جدید با شماره {$trackingNumber} و تعداد نفرات {$numberOfPeople} در ساعت $currentTime توسط کاربر {$params['username']} با نقش {$params['userType']} با موفقیت ثبت شد"
                    ]
                ];
            } else {
                $cargoId = (int)$existingCargo['id'];

                if ($existingCargo['status'] === CargoStatus::ENTERED->value) {
                    if (!empty($params['netWeight'])) {
                        if ($existingCargo['confirm'] !== CargoConfirmStatus::CONFIRMED->value) {
                            throw new ApiException("حواله مورد نظر توسط بارشمار هنوز تائید نشده است!", 400);
                        }
                        
                        $this->validateExitData($params['netWeight'], $params['scaleReceiptNumber'], $cargoId);
                        
                        // کسر تناژ موقت در صورت فعال بودن
                        $netWeightValue = floatval($params['netWeight']);
                        $tempTonnageData = $this->repo->findTempTonnage(
                            $shipName, 
                            $params['loadingWarehouse'], 
                            $params['cargoType'], 
                            $params['shippingCompany'], 
                            $loadingQuotaNumber
                        );
                        
                        if ($tempTonnageData && (int)$tempTonnageData['temp_tonnage_status'] === 1 && (float)$tempTonnageData['temp_tonnage_amount'] > 0) {
                            $newTempTonnage = max(0, (float)$tempTonnageData['temp_tonnage_amount'] - $netWeightValue);
                            $this->repo->updateTempTonnage(
                                $newTempTonnage, 
                                $shipName, 
                                $params['loadingWarehouse'], 
                                $params['cargoType'], 
                                $params['shippingCompany'], 
                                $loadingQuotaNumber
                            );
                        }
                        
                        $exitApplied = $this->repo->updateCargoExit(
                            $cargoId,
                            $params['netWeight'],
                            $params['scaleReceiptNumber'],
                            $currentTime,
                            $currentDate,
                            $params['username'],
                            $params['userType']
                        );
                        if (!$exitApplied) {
                            $conn->rollBack();
                            throw new ConflictException("این حواله هم‌زمان توسط درخواست دیگری به‌روزرسانی شد. لطفاً فهرست را بروزرسانی کنید.");
                        }
                    } elseif (!empty($params['shortageWeight']) || !empty($params['excessWeight'])) {
                        // به‌روزرسانی کسری یا اضافه بار
                        $shortageApplied = $this->repo->updateCargoShortageOrExcess(
                            $cargoId,
                            $params['shortageWeight'],
                            $params['excessWeight'],
                            $params['username'],
                            $params['userType']
                        );
                        if (!$shortageApplied) {
                            $conn->rollBack();
                            throw new ConflictException("این حواله هم‌زمان توسط درخواست دیگری به‌روزرسانی شد. لطفاً فهرست را بروزرسانی کنید.");
                        }
                    } else {
                        throw new ApiException("برای حواله در وضعیت ورود، باید وزن خالص یا کسری/اضافه بار وارد شود", 400);
                    }
                } else {
                    throw new ApiException("وضعیت نامعتبر حواله", 400);
                }

                $conn->commit();
                $this->invalidateShipsListCache();
                $this->logger->info("Cargo updated tracking: $trackingNumber, status: exit, user: {$params['username']}");

                return [
                    "code" => 200,
                    "data" => [
                        "success" => true,
                        "message" => "عملیات با موفقیت انجام شد",
                        "exitDate" => $currentDate,
                        "exitTime" => $currentTime,
                        "trackingNumber" => $trackingNumber,
                        "loadingQuotaNumber" => $loadingQuotaNumber
                    ]
                ];
            }
        } catch (Exception $e) {
            $conn->rollBack();
            $this->logger->error("Error in CargoService saveOrUpdate: " . $e->getMessage());
            throw $e;
        }
    }

    public function deleteCargoInfo(int $cargoId): array {
        $cargoData = $this->repo->findCargoById($cargoId);
        if (!$cargoData) {
            return ["status" => "error", "message" => "حواله یافت نشد یا قبلاً حذف شده است", "code" => 404];
        }

        $deleted = $this->repo->deleteCargoById($cargoId);
        if ($deleted && $cargoData['status'] === CargoStatus::EXITED->value && !empty($cargoData['netWeight'])) {
            $netWeightValue = (float)$cargoData['netWeight'];

            $quotaNumber = (string)$cargoData['loadingQuotaNumber'];
            $tempData = $this->repo->findTempTonnage(
                $cargoData['shipName'], $cargoData['loadingWarehouse'], $cargoData['cargoType'],
                $cargoData['shippingCompany'], $quotaNumber
            );

            if ($tempData && (int)$tempData['temp_tonnage_status'] === 1) {
                $newTemp = (float)$tempData['temp_tonnage_amount'] + $netWeightValue;
                $this->repo->updateTempTonnage(
                    $newTemp, $cargoData['shipName'], $cargoData['loadingWarehouse'], $cargoData['cargoType'],
                    $cargoData['shippingCompany'], $quotaNumber
                );
            }
        }

        if (!$deleted) {
            return ["status" => "error", "message" => "حواله یافت نشد یا قبلاً حذف شده است", "code" => 404];
        }

        $this->invalidateShipsListCache();
        return ["status" => "success", "message" => "حواله با موفقیت حذف شد", "code" => 200];
    }

    private function invalidateShipsListCache(): void {
        MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
        MicroCache::forget('cargo_active_ships');
    }

    public function getActiveShips(): array {
        return MicroCache::remember('cargo_active_ships', 8, function () {
            return $this->repo->getActiveShipsList();
        });
    }

    public function checkScaleReceipt(string $receipt): array {
        if (empty($receipt)) {
            return ['error' => 'شماره قبض باسکول الزامی است.', 'code' => 400];
        }
        if (!ctype_digit($receipt) || strlen($receipt) !== 8) {
            return ['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.', 'code' => 400];
        }
        $firstTwo = substr($receipt, 0, 2);
        if ($firstTwo < '44' || $firstTwo > '55') {
            return ['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.', 'code' => 400];
        }

        $row = $this->repo->findByScaleReceiptNumber($receipt);
        if ($row) {
            return [
                'exists' => true,
                'message' => 'شماره قبض باسکول تکراری است.',
                'trackingNumber' => $row['trackingNumber'],
                'netWeight' => $row['netWeight'],
                'loadingQuotaNumber' => $row['loadingQuotaNumber'],
                'code' => 200
            ];
        }

        return ['exists' => false, 'message' => 'شماره قبض باسکول معتبر است.', 'code' => 200];
    }

    private const MIN_NET_WEIGHT = 1000.0;
    private const MAX_NET_WEIGHT = 45000.0;

    private function validateExitData(string $netWeight, string $scaleReceiptNumber, int $cargoId): void {
        if (!filter_var($netWeight, FILTER_VALIDATE_FLOAT)) {
            throw new ApiException("وزن خالص باید عددی باشد.", 400);
        }
        $netWeightValue = floatval($netWeight);
        if ($netWeightValue < self::MIN_NET_WEIGHT || $netWeightValue > self::MAX_NET_WEIGHT) {
            throw new ApiException("وزن خالص باید بین " . self::MIN_NET_WEIGHT . " تا " . self::MAX_NET_WEIGHT . " کیلوگرم باشد.", 400);
        }
        if (empty($scaleReceiptNumber)) {
            throw new ApiException("شماره قبض باسکول نمی‌تواند خالی باشد.", 400);
        }

        if (!ctype_digit($scaleReceiptNumber) || strlen($scaleReceiptNumber) !== 8) {
            throw new ApiException("شماره قبض باسکول باید دقیقاً ۸ رقم باشد.", 400);
        }
        $prefix = substr($scaleReceiptNumber, 0, 2);
        if ($prefix < '44' || $prefix > '55') {
            throw new ApiException("شماره قبض باسکول معتبر نیست.", 400);
        }
        if ($this->repo->isScaleReceiptDuplicate($scaleReceiptNumber, $cargoId)) {
            throw new ApiException("شماره قبض باسکول {$scaleReceiptNumber} قبلاً برای حوالهٔ دیگری ثبت شده است.", 400);
        }
    }
}
