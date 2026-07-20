<?php
// PHP/src/Controllers/CargoController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use mysqli;
use mysqli_stmt;
use App\Core\Database;
use App\Core\Request;
use App\Core\Response;
use App\Core\Logger;

class CargoController {
    private mysqli $conn;
    private Request $request;
    private Logger $logger;

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->request = new Request();
        $this->logger = Logger::getInstance();
    }

    /**
     * ثبت یا به‌روزرسانی اطلاعات حواله بارگیری (saveOrUpdateCargoInfo.php)
     */
    public function saveOrUpdate(): void {
        // تنظیم هدر خروجی
        header('Content-Type: application/json; charset=utf-8');

        // تنظیمات عملکرد
        ini_set('memory_limit', '64M');
        ini_set('max_execution_time', '15');

        // دریافت پارامترها
        $params = $this->request->all();

        // تعریف فیلدهای اجباری و اختیاری
        $requiredFields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber', 'username', 'userType'];
        $optionalFields = ['entryTime', 'netWeight', 'scaleReceiptNumber', 'shortageWeight', 'excessWeight', 'exitTime', 'exitDate', 'status', 'confirmation', 'numberOfPeople', 'duplicateConfirmation'];

        // پاک‌سازی و ایجاد مقادیر پیش‌فرض
        foreach ($requiredFields as $field) {
            if (!isset($params[$field]) || trim((string)$params[$field]) === '') {
                $this->sendErrorResponse("پارامتر $field الزامی است.");
            }
            $params[$field] = $this->sanitizeInput((string)$params[$field]);
        }

        foreach ($optionalFields as $field) {
            $params[$field] = isset($params[$field]) ? $this->sanitizeInput((string)$params[$field]) : '';
        }

        // استخراج متغیرهای کلیدی
        $shipName = $params['shipName'];
        $trackingNumber = $params['trackingNumber'];
        $loadingQuotaNumber = $params['loadingQuotaNumber'];

        try {
            // شروع تراکنش
            $this->conn->autocommit(FALSE);
            $this->conn->query("SET SESSION sql_mode = 'STRICT_TRANS_TABLES'");
            $this->conn->query("SET SESSION innodb_lock_wait_timeout = 5");

            // محاسبه زمان‌های شمسی و میلادی
            $yesterdayStart = date('Y-m-d 00:00:00', strtotime('-1 day'));
            $currentTime = jdate("H:i");
            $currentDate = jdate("Y/m/d");

            // ۱. بررسی وجود حواله تکراری در ۲۴ ساعت گذشته برای این کشتی
            $check24hQuery = "SELECT loadingQuotaNumber, loadingWarehouse, shippingCompany, exitTime, exitDate, status, entryTime 
                              FROM CargoInfo 
                              WHERE shipName = ? AND trackingNumber = ? AND updated_at >= ? 
                              ORDER BY id DESC LIMIT 1";
            
            $check24hStmt = $this->conn->prepare($check24hQuery);
            if (!$check24hStmt) {
                throw new Exception("خطا در آماده‌سازی دستور بررسی ۲۴ ساعته: " . $this->conn->error);
            }
            $check24hStmt->bind_param("sss", $shipName, $trackingNumber, $yesterdayStart);
            $check24hStmt->execute();
            $existing24hCargo = $check24hStmt->get_result()->fetch_assoc();
            $check24hStmt->close();

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

                    $this->sendSuccessResponse([
                        "warning" => true,
                        "message" => implode('', $warningParts),
                        "existing_cargo" => $existing24hCargo,
                        "requires_confirmation" => true
                    ], 409);
                }
            }

            // ۲. بررسی وجود حواله با کلیدهای اصلی
            $query = "SELECT id, status, confirm, exitDate, exitTime, entryTime FROM CargoInfo WHERE 
                      shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                      shippingCompany = ? AND loadingQuotaNumber = ? AND trackingNumber = ? 
                      ORDER BY id DESC LIMIT 1";
            
            $stmt = $this->conn->prepare($query);
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی دستور بازیابی حواله: " . $this->conn->error);
            }
            $stmt->bind_param("ssssss", 
                $shipName, 
                $params['loadingWarehouse'], 
                $params['cargoType'], 
                $params['shippingCompany'], 
                $loadingQuotaNumber, 
                $trackingNumber
            );
            $stmt->execute();
            $existingCargo = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            $shouldInsertNew = !$existingCargo;

            if ($existingCargo) {
                $isNewEntryAttempt = empty($params['netWeight']) && empty($params['shortageWeight']) && empty($params['excessWeight']);
                
                if ($isNewEntryAttempt) {
                    if ($params['duplicateConfirmation'] !== "proceed") {
                        $cargoStatus = $existingCargo['status'];
                        $cargoDate = $cargoStatus === "خروج" ? $existingCargo['exitDate'] : ($existingCargo['exitDate'] ?: $currentDate);
                        $cargoTime = $cargoStatus === "خروج" ? $existingCargo['exitTime'] : ($existingCargo['entryTime'] ?: $currentTime);
                        
                        $warningMessage = "شماره حواله \"{$trackingNumber}\" برای شماره کوتاژ \"{$loadingQuotaNumber}\" برای کشتی [ {$shipName} ] قبلاً در تاریخ {$cargoDate} و ساعت {$cargoTime} در وضعیت [ {$cargoStatus} ] ثبت شده است.\n\nآیا اطمینان دارید که می‌خواهید حواله جدید با همین مشخصات ثبت کنید؟";
                        
                        $this->sendSuccessResponse([
                            "warning" => true,
                            "message" => $warningMessage,
                            "existing_cargo" => $existingCargo,
                            "requires_confirmation" => true
                        ], 409);
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

                $insertQuery = "INSERT INTO CargoInfo (
                    trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, 
                    status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, 
                    numberOfPeople, username, userType
                ) VALUES (?, ?, ?, ?, ?, ?, 'ورود', ?, ?, ?, ?, ?, ?, ?, ?)";
                
                $insertStmt = $this->conn->prepare($insertQuery);
                if (!$insertStmt) {
                    throw new Exception("خطا در آماده‌سازی دستور درج: " . $this->conn->error);
                }
                
                $insertStmt->bind_param("ssssssssssssss", 
                    $trackingNumber, 
                    $currentTime, 
                    $params['netWeight'], 
                    $params['scaleReceiptNumber'], 
                    $params['shortageWeight'], 
                    $params['excessWeight'], 
                    $shipName, 
                    $params['loadingWarehouse'], 
                    $params['cargoType'], 
                    $params['shippingCompany'], 
                    $loadingQuotaNumber, 
                    $numberOfPeople, 
                    $params['username'], 
                    $params['userType']
                );
                
                if (!$insertStmt->execute()) {
                    throw new Exception("خطا در اجرای دستور درج: " . $insertStmt->error);
                }
                $insertStmt->close();
                $this->conn->commit();

                $this->logger->info("New cargo entry tracking: $trackingNumber, ship: $shipName, user: {$params['username']}");

                $this->sendSuccessResponse([
                    "success" => true, 
                    "message" => "حواله جدید با شماره {$trackingNumber} و تعداد نفرات {$numberOfPeople} در ساعت $currentTime توسط کاربر {$params['username']} با نقش {$params['userType']} با موفقیت ثبت شد"
                ]);
            } else {
                // به‌روزرسانی حواله موجود
                $cargoId = $existingCargo['id'];
                
                if ($existingCargo['status'] === "ورود") {
                    if (!empty($params['netWeight'])) {
                        if ($existingCargo['confirm'] !== "تائید شده") {
                            throw new Exception("حواله مورد نظر توسط بارشمار هنوز تائید نشده است!");
                        }
                        
                        $this->validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
                        
                        // کسر تناژ موقت در صورت فعال بودن
                        $netWeightValue = floatval($params['netWeight']);
                        $tempTonnageQuery = "SELECT temp_tonnage_status, temp_tonnage_amount FROM InitialInfo 
                                           WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                                                 shippingCompany = ? AND loadingQuotaNumber = ? LIMIT 1";
                        $tempTonnageStmt = $this->conn->prepare($tempTonnageQuery);
                        $tempTonnageStmt->bind_param("sssss", 
                            $shipName, 
                            $params['loadingWarehouse'], 
                            $params['cargoType'], 
                            $params['shippingCompany'], 
                            $loadingQuotaNumber
                        );
                        $tempTonnageStmt->execute();
                        $tempTonnageData = $tempTonnageStmt->get_result()->fetch_assoc();
                        $tempTonnageStmt->close();
                        
                        if ($tempTonnageData && $tempTonnageData['temp_tonnage_status'] == 1 && $tempTonnageData['temp_tonnage_amount'] > 0) {
                            $newTempTonnage = max(0, $tempTonnageData['temp_tonnage_amount'] - $netWeightValue);
                            $updateTempTonnageQuery = "UPDATE InitialInfo SET temp_tonnage_amount = ? 
                                                     WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                                                           shippingCompany = ? AND loadingQuotaNumber = ?";
                            $updateTempTonnageStmt = $this->conn->prepare($updateTempTonnageQuery);
                            $updateTempTonnageStmt->bind_param("dsssss", 
                                $newTempTonnage, 
                                $shipName, 
                                $params['loadingWarehouse'], 
                                $params['cargoType'], 
                                $params['shippingCompany'], 
                                $loadingQuotaNumber
                            );
                            $updateTempTonnageStmt->execute();
                            $updateTempTonnageStmt->close();
                        }
                        
                        // به‌روزرسانی برای وضعیت خروج
                        $updateQuery = "UPDATE CargoInfo SET 
                            netWeight = ?, scaleReceiptNumber = ?, exitTime = ?, exitDate = ?, 
                            status = 'خروج', username = ?, userType = ? 
                        WHERE id = ?";
                        
                        $updateStmt = $this->conn->prepare($updateQuery);
                        $updateStmt->bind_param("ssssssi", 
                            $params['netWeight'], 
                            $params['scaleReceiptNumber'], 
                            $currentTime, 
                            $currentDate, 
                            $params['username'], 
                            $params['userType'], 
                            $cargoId
                        );
                    } elseif (!empty($params['shortageWeight']) || !empty($params['excessWeight'])) {
                        // به‌روزرسانی کسری یا اضافه بار
                        $updateQuery = "UPDATE CargoInfo SET 
                            shortageWeight = ?, excessWeight = ?, username = ?, userType = ? 
                        WHERE id = ?";
                        
                        $updateStmt = $this->conn->prepare($updateQuery);
                        $updateStmt->bind_param("ssssi", 
                            $params['shortageWeight'], 
                            $params['excessWeight'], 
                            $params['username'], 
                            $params['userType'], 
                            $cargoId
                        );
                    } else {
                        throw new Exception("برای حواله در وضعیت ورود، باید وزن خالص یا کسری/اضافه بار وارد شود");
                    }
                } elseif ($existingCargo['status'] === "خروج") {
                    if ($params['confirmation'] !== "yes") {
                        $this->sendSuccessResponse([
                            "message" => "شماره حواله {$trackingNumber} در تاریخ {$existingCargo['exitDate']} و ساعت {$existingCargo['exitTime']} خروج کرده و سرویس بسته شده است!", 
                            "status" => "confirmation_needed", 
                            "exitDate" => $existingCargo['exitDate'], 
                            "exitTime" => $existingCargo['exitTime']
                        ]);
                    }
                    
                    $this->validateExitData($params['netWeight'], $params['scaleReceiptNumber']);
                    
                    // به‌روزرسانی حواله خروج یافته
                    $updateQuery = "UPDATE CargoInfo SET 
                        netWeight = ?, scaleReceiptNumber = ?, exitTime = ?, exitDate = ?, 
                        username = ?, userType = ? 
                    WHERE id = ?";
                    
                    $updateStmt = $this->conn->prepare($updateQuery);
                    $updateStmt->bind_param("ssssssi", 
                        $params['netWeight'], 
                        $params['scaleReceiptNumber'], 
                        $currentTime, 
                        $currentDate, 
                        $params['username'], 
                        $params['userType'], 
                        $cargoId
                    );
                } else {
                    throw new Exception("وضعیت نامعتبر حواله");
                }

                if (!$updateStmt->execute()) {
                    throw new Exception("خطا در اجرای دستور به‌روزرسانی: " . $updateStmt->error);
                }
                $updateStmt->close();
                $this->conn->commit();

                $this->logger->info("Cargo updated tracking: $trackingNumber, status: exit, user: {$params['username']}");

                $this->sendSuccessResponse([
                    "success" => true,
                    "message" => "عملیات با موفقیت انجام شد",
                    "exitDate" => $currentDate,
                    "exitTime" => $currentTime,
                    "trackingNumber" => $trackingNumber,
                    "loadingQuotaNumber" => $loadingQuotaNumber
                ]);
            }
        } catch (Exception $e) {
            $this->conn->rollback();
            $this->logger->error("Error in CargoController saveOrUpdate: " . $e->getMessage());
            $this->sendErrorResponse($e->getMessage());
        }
    }

    private function sanitizeInput(string $input): string {
        return filter_var(trim($input), FILTER_SANITIZE_STRING, FILTER_FLAG_NO_ENCODE_QUOTES);
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

    private function sendSuccessResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    private function sendErrorResponse(string $message): void {
        $this->sendSuccessResponse([
            'error' => true,
            'message' => $message
        ], 500); // ۵۰۰ جهت پایداری با کلاینت قدیمی اندروید
    }
}
