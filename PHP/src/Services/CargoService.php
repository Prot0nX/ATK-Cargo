<?php
// PHP/src/Services/CargoService.php

declare(strict_types=1);

namespace App\Services;

use Exception;
use InvalidArgumentException;
use App\Repositories\CargoRepository;
use App\Core\Logger;
use App\Core\MicroCache;

class CargoService {
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

        $conn = $this->repo->getMysqliConnection();
        $conn->autocommit(FALSE);
        $conn->query("SET SESSION sql_mode = 'STRICT_TRANS_TABLES'");
        $conn->query("SET SESSION innodb_lock_wait_timeout = 5");

        try {
            $yesterdayStart = date('Y-m-d 00:00:00', strtotime('-1 day'));
            $currentTime = jdate("H:i");
            $currentDate = jdate("Y/m/d");

            // ۱. بررسی وجود حواله تکراری در ۲۴ ساعت گذشته برای این کشتی
            $existing24hCargo = $this->repo->find24hCargo($shipName, $trackingNumber, $yesterdayStart);

            if ($existing24hCargo && $existing24hCargo['loadingQuotaNumber'] !== $loadingQuotaNumber) {
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

                    $conn->rollback();
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
                $isNewEntryAttempt = empty($params['netWeight']) && empty($params['shortageWeight']) && empty($params['excessWeight']);
                
                if ($isNewEntryAttempt) {
                    if ($params['duplicateConfirmation'] !== "proceed") {
                        $cargoStatus = $existingCargo['status'];
                        $cargoDate = $cargoStatus === "خروج" ? $existingCargo['exitDate'] : ($existingCargo['exitDate'] ?: $currentDate);
                        $cargoTime = $cargoStatus === "خروج" ? $existingCargo['exitTime'] : ($existingCargo['entryTime'] ?: $currentTime);
                        
                        $warningMessage = "شماره حواله \"{$trackingNumber}\" برای شماره کوتاژ \"{$loadingQuotaNumber}\" برای کشتی [ {$shipName} ] قبلاً در تاریخ {$cargoDate} و ساعت {$cargoTime} در وضعیت [ {$cargoStatus} ] ثبت شده است.\n\nآیا اطمینان دارید که می‌خواهید حواله جدید با همین مشخصات ثبت کنید؟";
                        
                        $conn->rollback();
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
                $numberOfPeople = filter_var($params['numberOfPeople'], FILTER_VALIDATE_INT);
                if ($numberOfPeople === false || $numberOfPeople < 1) {
                    throw new Exception("تعداد نفرات باید عددی بزرگتر از صفر باشد");
                }

                $res = $this->repo->insertCargo($params, $currentTime, (int)$numberOfPeople);
                if (!$res) {
                    throw new Exception("خطا در اجرای دستور درج حواله");
                }
                $conn->commit();

                $this->logger->info("New cargo entry tracking: $trackingNumber, ship: $shipName, user: {$params['username']}");

                return [
                    "code" => 200,
                    "data" => [
                        "success" => true, 
                        "message" => "حواله جدید با شماره {$trackingNumber} و تعداد نفرات {$numberOfPeople} در ساعت $currentTime توسط کاربر {$params['username']} با نقش {$params['userType']} با موفقیت ثبت شد"
                    ]
                ];
            } else {
                // به‌روزرسانی حواله موجود
                $cargoId = (int)$existingCargo['id'];
                
                if ($existingCargo['status'] === "ورود") {
                    if (!empty($params['netWeight'])) {
                        if ($existingCargo['confirm'] !== "تائید شده") {
                            throw new Exception("حواله مورد نظر توسط بارشمار هنوز تائید نشده است!");
                        }
                        
                        $this->validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
                        
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
                        
                        // به‌روزرسانی برای وضعیت خروج
                        $this->repo->updateCargoExit(
                            $cargoId, 
                            $params['netWeight'], 
                            $params['scaleReceiptNumber'], 
                            $currentTime, 
                            $currentDate, 
                            $params['username'], 
                            $params['userType']
                        );
                    } elseif (!empty($params['shortageWeight']) || !empty($params['excessWeight'])) {
                        // به‌روزرسانی کسری یا اضافه بار
                        $this->repo->updateCargoShortageOrExcess(
                            $cargoId, 
                            $params['shortageWeight'], 
                            $params['excessWeight'], 
                            $params['username'], 
                            $params['userType']
                        );
                    } else {
                        throw new Exception("برای حواله در وضعیت ورود، باید وزن خالص یا کسری/اضافه بار وارد شود");
                    }
                } elseif ($existingCargo['status'] === "خروج") {
                    if ($params['confirmation'] !== "yes") {
                        $conn->rollback();
                        return [
                            "code" => 200,
                            "data" => [
                                "message" => "شماره حواله {$trackingNumber} در تاریخ {$existingCargo['exitDate']} و ساعت {$existingCargo['exitTime']} خروج کرده و سرویس بسته شده است!", 
                                "status" => "confirmation_needed", 
                                "exitDate" => $existingCargo['exitDate'], 
                                "exitTime" => $existingCargo['exitTime']
                            ]
                        ];
                    }
                    
                    $this->validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
                    
                    $this->repo->updateCargoExitExiting(
                        $cargoId, 
                        $params['netWeight'], 
                        $params['scaleReceiptNumber'], 
                        $currentTime, 
                        $currentDate, 
                        $params['username'], 
                        $params['userType']
                    );
                } else {
                    throw new Exception("وضعیت نامعتبر حواله");
                }

                $conn->commit();
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
            $conn->rollback();
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
        if ($deleted && $cargoData['status'] === 'خروج' && !empty($cargoData['netWeight'])) {
            $netWeightValue = (float)$cargoData['netWeight'];
            $tempData = $this->repo->findTempTonnage(
                $cargoData['shipName'], $cargoData['loadingWarehouse'], $cargoData['cargoType'], 
                $cargoData['shippingCompany'], $cargoData['loadingQuotaNumber']
            );

            if ($tempData && (int)$tempData['temp_tonnage_status'] === 1) {
                $newTemp = (float)$tempData['temp_tonnage_amount'] + $netWeightValue;
                $this->repo->updateTempTonnage(
                    $newTemp, $cargoData['shipName'], $cargoData['loadingWarehouse'], $cargoData['cargoType'], 
                    $cargoData['shippingCompany'], $cargoData['loadingQuotaNumber']
                );
            }
        }

        if (!$deleted) {
            return ["status" => "error", "message" => "حواله یافت نشد یا قبلاً حذف شده است", "code" => 404];
        }

        return ["status" => "success", "message" => "حواله با موفقیت حذف شد", "code" => 200];
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

    private function validateExitData(string $netWeight, string $scaleReceiptNumber): void {
        if (!filter_var($netWeight, FILTER_VALIDATE_FLOAT) || floatval($netWeight) <= 0) {
            throw new Exception("وزن خالص باید عددی مثبت و بزرگتر از صفر باشد.");
        }
        if (empty($scaleReceiptNumber)) {
            throw new Exception("شماره قبض باسکول نمی‌تواند خالی باشد.");
        }
        if (!ctype_digit($scaleReceiptNumber)) {
            throw new Exception("شماره قبض باسکول باید فقط شامل اعداد باشد.");
        }
    }
}
