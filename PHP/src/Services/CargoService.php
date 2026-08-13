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

class CargoService {
    // فاصله‌ی ایمنی قبل از رسیدن دقیق به حد نصاب درصدی: وقتی «تناژ مجاز»
    // باقی‌مانده به این مقدار یا کمتر برسد، دیگر حوالهٔ تازه پذیرفته نمی‌شود
    // (نه اینکه دقیقاً صفر شود) — چون یک محمولهٔ تک بعدی معمولاً چند تن است
    // و می‌تواند به‌سادگی از حد نصاب رد شود. فقط ثبت حوالهٔ جدید را می‌بندد؛
    // خروج/به‌روزرسانی حواله‌های از قبل ثبت‌شده حتی زیر این آستانه هم مجاز
    // است (کاربر باید بتواند تعهدات قبلی را تمام کند).
    private const NEW_ENTRY_TONNAGE_BUFFER_KG = 5000.0;

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

            // ۰. کنترل‌های کوتاژ (فعال بودن، تناژ موقت) — قبلاً فقط سمت کلاینت
            // (QuotaValidationUseCase) انجام می‌شدند؛ یعنی اپی که این
            // درخواست‌ها را ارسال می‌کرد کافی بود این بررسی‌ها را دور بزند.
            // اینجا روی سرور و قبل از هر نوشتنی تکرار می‌شوند تا واقعاً
            // لازم‌الاجرا باشند. بررسی حد نصاب درصدی/تناژ مجاز جداگانه و فقط
            // برای ثبت حوالهٔ تازه در بخش ۳ انجام می‌شود (رجوع کنید به
            // NEW_ENTRY_TONNAGE_BUFFER_KG).
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
                // فقط ثبت حوالهٔ تازه با بافر تناژ کنترل می‌شود؛ خروج/به‌روزرسانی
                // حواله‌های از قبل ثبت‌شده در شاخهٔ else پایین‌تر است و این
                // بررسی را نمی‌بیند، پس حتی اگر تناژ مجاز زیر بافر افتاده باشد
                // کاربر می‌تواند تعهدات قبلی را تمام کند.
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
                // به‌روزرسانی حواله موجود
                $cargoId = (int)$existingCargo['id'];
                
                if ($existingCargo['status'] === "ورود") {
                    if (!empty($params['netWeight'])) {
                        if ($existingCargo['confirm'] !== "تائید شده") {
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
                        
                        // به‌روزرسانی برای وضعیت خروج؛ خروجی false یعنی رکورد
                        // بین خواندن (findCargoByKeys با FOR UPDATE) و این
                        // UPDATE توسط یک درخواست دیگر از وضعیت «ورود» خارج شده
                        // است (مثلاً خروج هم‌زمان از دو دستگاه).
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
                            $conn->rollback();
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
                            $conn->rollback();
                            throw new ConflictException("این حواله هم‌زمان توسط درخواست دیگری به‌روزرسانی شد. لطفاً فهرست را بروزرسانی کنید.");
                        }
                    } else {
                        throw new ApiException("برای حواله در وضعیت ورود، باید وزن خالص یا کسری/اضافه بار وارد شود", 400);
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
                    
                    $this->validateExitData($params['netWeight'], $params['scaleReceiptNumber'], $cargoId);

                    $exitingApplied = $this->repo->updateCargoExitExiting(
                        $cargoId,
                        $params['netWeight'],
                        $params['scaleReceiptNumber'],
                        $currentTime,
                        $currentDate,
                        $params['username'],
                        $params['userType']
                    );
                    if (!$exitingApplied) {
                        $conn->rollback();
                        throw new ConflictException("این حواله هم‌زمان توسط درخواست دیگری به‌روزرسانی شد. لطفاً فهرست را بروزرسانی کنید.");
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
            // $cargoData از findCargoById (خواندن خام SELECT *) می‌آید، نه از
            // ورودی کنترلر که از قبل sanitizeString/(string) شده؛ loadingQuotaNumber
            // ستونی عددی (INT) است و درایور mysqli آن را int برمی‌گرداند، در
            // حالی‌که findTempTonnage/updateTempTonnage با declare(strict_types=1)
            // پارامتر $quota را string اعلام کرده‌اند — بدون این cast صریح، هر
            // حذف حوالهٔ خروج‌زده‌ی دارای تناژ موقت با TypeError (نه Exception)
            // متوقف می‌شد و چون هیچ catch(Exception) آن را نمی‌گرفت، کلاینت
            // پاسخ کاملاً خالی می‌گرفت — با اینکه DELETE پیش از این خط با
            // موفقیت اجرا شده بود.
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

    /**
     * لیست کشتی‌ها (تناژ/تعداد کوتاژ هر کشتی) بعد از هر نوشتنی که CargoInfo یا
     * InitialInfo را تغییر می‌دهد باید invalidate شود تا کاربر تا ۲۰ ثانیه
     * (TTL کش getShipsList) داده‌ی قدیمی نبیند.
     */
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

    // بررسی تکراری‌بودن قبض باسکول قبلاً فقط از طریق یک فراخوانی مشورتی و
    // جدای کلاینت (check_scale_receipt.php) انجام می‌شد که چیزی جلوی
    // فراخوانی مستقیم این مسیر (saveOrUpdateCargoInfo.php) بدون آن بررسی را
    // نمی‌گرفت. isScaleReceiptDuplicate از قبل در updateCargoInfo استفاده
    // می‌شد؛ اینجا هم در همان تراکنشی که واقعاً رکورد را می‌نویسد اجرا می‌شود.
    // محدودهٔ وزن خالص (۱۰۰۰ تا ۴۵۰۰۰) با QuotaValidationUseCase.validateInputData
    // سمت کلاینت یکی است. سرور قبلاً فقط «مثبت بودن» را چک می‌کرد، یعنی
    // درخواستی که مستقیم این endpoint را صدا می‌زد (بدون رد شدن از کلاینت)
    // می‌توانست این محدوده را دور بزند.
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
        // فرمت واقعی قبض باسکول (۸ رقم، پیشوند ۴۴ تا ۵۵) همان چیزی است که
        // CargoService::checkScaleReceipt از قبل روی آن تکیه می‌کند؛ اینجا هم
        // همان قاعده اعمال می‌شود تا هر دو مسیر یک تعریف از «قبض معتبر» داشته
        // باشند.
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
