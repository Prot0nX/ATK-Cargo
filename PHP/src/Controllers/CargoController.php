<?php
// PHP/src/Controllers/CargoController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
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
        header('Content-Type: application/json; charset=utf-8');
        ini_set('memory_limit', '64M');
        ini_set('max_execution_time', '15');

        $params = $this->request->all();

        $requiredFields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber', 'username', 'userType'];
        $optionalFields = ['entryTime', 'netWeight', 'scaleReceiptNumber', 'shortageWeight', 'excessWeight', 'exitTime', 'exitDate', 'status', 'confirmation', 'numberOfPeople', 'duplicateConfirmation'];

        foreach ($requiredFields as $field) {
            if (!isset($params[$field]) || trim((string)$params[$field]) === '') {
                $this->sendErrorResponse("پارامتر $field الزامی است.");
            }
            $params[$field] = $this->sanitizeString((string)$params[$field]);
        }

        foreach ($optionalFields as $field) {
            $params[$field] = isset($params[$field]) ? $this->sanitizeString((string)$params[$field]) : '';
        }

        $shipName = $params['shipName'];
        $trackingNumber = $params['trackingNumber'];
        $loadingQuotaNumber = $params['loadingQuotaNumber'];

        try {
            $this->conn->autocommit(FALSE);
            $this->conn->query("SET SESSION sql_mode = 'STRICT_TRANS_TABLES'");
            $this->conn->query("SET SESSION innodb_lock_wait_timeout = 5");

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

    /**
     * به‌روزرسانی کامل اطلاعات حواله بار (updateCargoInfo.php)
     */
    public function updateCargoInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            $this->sendJsonResponse(['error' => true, 'message' => 'روش درخواست نامعتبر است. فقط POST مجاز است.'], 405);
        }

        try {
            $jsonInput = file_get_contents('php://input');
            $data = json_decode((string)$jsonInput, true);

            if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
                throw new InvalidArgumentException('فرمت JSON نامعتبر است.');
            }

            $id = filter_var($data['id'] ?? null, FILTER_VALIDATE_INT);
            if (!$id || $id <= 0) {
                throw new InvalidArgumentException('شناسه رکورد نامعتبر است.');
            }

            $trackingNumber = $this->validateStringField($data['trackingNumber'] ?? null, 'شماره حواله');
            $numberOfPeople = $this->validateStringField($data['numberOfPeople'] ?? null, 'تعداد افراد', false);
            $username = $this->validateStringField($data['username'] ?? null, 'نام کاربری', false);
            $userType = $this->validateStringField($data['userType'] ?? null, 'نوع کاربر', false);
            $entryTime = $this->validateStringField($data['entryTime'] ?? null, 'زمان ورود');
            $netWeight = $this->validateNumericField($data['netWeight'] ?? null, 'وزن خالص');
            $scaleReceiptNumber = $this->validateStringField($data['scaleReceiptNumber'] ?? null, 'شماره قبض باسکول');
            $shortageWeight = $this->validateNumericField($data['shortageWeight'] ?? null, 'وزن کسری');
            $excessWeight = $this->validateNumericField($data['excessWeight'] ?? null, 'وزن اضافی');
            $exitTime = $this->validateStringField($data['exitTime'] ?? null, 'زمان خروج', false);
            $exitDate = $this->validateStringField($data['exitDate'] ?? null, 'تاریخ خروج', false);
            $status = $this->validateStringField($data['status'] ?? null, 'وضعیت');
            $confirm = $this->validateStringField($data['confirm'] ?? null, 'تایید', false);

            // بررسی وجود رکورد
            $checkStmt = $this->conn->prepare("SELECT id FROM CargoInfo WHERE id = ? LIMIT 1");
            $checkStmt->bind_param("i", $id);
            $checkStmt->execute();
            $checkResult = $checkStmt->get_result();
            if ($checkResult->num_rows === 0) {
                $checkStmt->close();
                $this->sendJsonResponse(['error' => true, 'message' => 'رکوردی با این شناسه یافت نشد.'], 404);
            }
            $checkStmt->close();

            // بررسی تکراری نبودن قبض باسکول
            $dupStmt = $this->conn->prepare("SELECT id FROM CargoInfo WHERE scaleReceiptNumber = ? AND id != ? LIMIT 1");
            $dupStmt->bind_param("si", $scaleReceiptNumber, $id);
            $dupStmt->execute();
            if ($dupStmt->get_result()->num_rows > 0) {
                $dupStmt->close();
                $this->sendJsonResponse(['error' => true, 'message' => 'شماره قبض باسکول تکراری است.'], 400);
            }
            $dupStmt->close();

            $updateQuery = "UPDATE CargoInfo SET 
                trackingNumber = ?, numberOfPeople = ?, username = ?, userType = ?, 
                entryTime = ?, netWeight = ?, scaleReceiptNumber = ?, shortageWeight = ?, 
                excessWeight = ?, exitTime = ?, exitDate = ?, status = ?, confirm = ? 
                WHERE id = ?";

            $updateStmt = $this->conn->prepare($updateQuery);
            $updateStmt->bind_param("sssssssssssssi", 
                $trackingNumber, $numberOfPeople, $username, $userType, 
                $entryTime, $netWeight, $scaleReceiptNumber, $shortageWeight, 
                $excessWeight, $exitTime, $exitDate, $status, $confirm, $id
            );

            if ($updateStmt->execute()) {
                $affected = $updateStmt->affected_rows;
                $updateStmt->close();
                if ($affected > 0) {
                    $this->sendJsonResponse([
                        'error' => false,
                        'status' => 'success',
                        'message' => 'اطلاعات با موفقیت بروزرسانی شد.'
                    ]);
                } else {
                    $this->sendJsonResponse([
                        'error' => false,
                        'status' => 'no_change',
                        'message' => 'تغییری در اطلاعات ایجاد نشد.'
                    ]);
                }
            } else {
                $err = $updateStmt->error;
                $updateStmt->close();
                throw new Exception("خطا در اجرای کوئری بروزرسانی: " . $err);
            }
        } catch (InvalidArgumentException $e) {
            $this->sendJsonResponse(['error' => true, 'message' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in updateCargoInfo: " . $e->getMessage());
            $this->sendJsonResponse(['error' => true, 'message' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    /**
     * تأیید حواله توسط بارشمار (confirm_cargo.php)
     */
    public function confirmCargo(): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            $this->sendJsonResponse(["status" => "error", "message" => "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید."], 405);
        }

        $data = json_decode((string)file_get_contents('php://input'), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
            $this->sendJsonResponse(["status" => "error", "message" => "فرمت JSON نامعتبر است"], 400);
        }

        $requiredFields = ['id', 'username', 'userType'];
        $missingFields = [];
        foreach ($requiredFields as $field) {
            if (!isset($data[$field]) || (is_string($data[$field]) && trim($data[$field]) === '')) {
                $missingFields[] = $field;
            }
        }

        if (!empty($missingFields)) {
            $this->sendJsonResponse(["status" => "error", "message" => "فیلدهای ضروری وجود ندارند: " . implode(', ', $missingFields)], 400);
        }

        $cargoId = (int)$data['id'];
        $username = $this->sanitizeString((string)$data['username']);
        $userType = $this->sanitizeString((string)$data['userType']);

        if ($cargoId <= 0) {
            $this->sendJsonResponse(["status" => "error", "message" => "شناسه حواله نامعتبر است"], 400);
        }

        try {
            $this->conn->begin_transaction();

            $query = "UPDATE CargoInfo 
                      SET confirm = 'تائید شده', 
                          confirm_username = ?, 
                          confirm_usertype = ?, 
                          updated_at = NOW() 
                      WHERE id = ? AND (confirm IS NULL OR confirm != 'تائید شده')";

            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("ssi", $username, $userType, $cargoId);
            $stmt->execute();

            if ($stmt->affected_rows > 0) {
                $stmt->close();
                $infoStmt = $this->conn->prepare("SELECT trackingNumber, loadingQuotaNumber FROM CargoInfo WHERE id = ?");
                $infoStmt->bind_param("i", $cargoId);
                $infoStmt->execute();
                $cargoData = $infoStmt->get_result()->fetch_assoc();
                $infoStmt->close();

                $this->conn->commit();

                $confirmTime = date('H:i:s');
                $confirmDate = date('Y/m/d');

                $responseData = [
                    'id' => $cargoId,
                    'trackingNumber' => $cargoData['trackingNumber'] ?? '',
                    'loadingQuotaNumber' => $cargoData['loadingQuotaNumber'] ?? '',
                    'confirmTime' => $confirmTime,
                    'confirmDate' => $confirmDate,
                    'confirmUsername' => $username,
                    'confirmUserType' => $userType
                ];

                $response = [
                    "status" => "success",
                    "message" => "حواله شماره {$cargoData['trackingNumber']} با کوتاژ {$cargoData['loadingQuotaNumber']} در ساعت {$confirmTime} توسط {$username} با موفقیت تأیید شد",
                    "data" => $responseData
                ];
                http_response_code(200);
                echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_NUMERIC_CHECK);
                exit;
            } else {
                $stmt->close();
                $checkStmt = $this->conn->prepare("SELECT id FROM CargoInfo WHERE id = ?");
                $checkStmt->bind_param("i", $cargoId);
                $checkStmt->execute();
                $exists = $checkStmt->get_result()->num_rows > 0;
                $checkStmt->close();

                $this->conn->commit();

                if ($exists) {
                    $this->sendJsonResponse(["status" => "error", "message" => "حواله قبلاً تأیید شده است یا تغییری اعمال نشد"], 200);
                } else {
                    $this->sendJsonResponse(["status" => "error", "message" => "حواله با شناسه ارسالی یافت نشد"], 404);
                }
            }
        } catch (Exception $e) {
            $this->conn->rollback();
            $this->logger->error("Error in confirmCargo: " . $e->getMessage());
            $this->sendJsonResponse(["status" => "error", "message" => $e->getMessage()], 500);
        }
    }

    /**
     * حذف اطلاعات حواله (deleteCargoInfo.php)
     */
    public function deleteCargoInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            $this->sendJsonResponse(["status" => "error", "message" => "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید."], 405);
        }

        $data = json_decode((string)file_get_contents("php://input"), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data) || !isset($data['id']) || empty($data['id'])) {
            $this->sendJsonResponse(["status" => "error", "message" => "فیلد ضروری وجود ندارد: id"], 400);
        }

        $cargoId = (int)$data['id'];
        if ($cargoId <= 0) {
            $this->sendJsonResponse(["status" => "error", "message" => "شناسه حواله نامعتبر است"], 400);
        }

        try {
            $selectStmt = $this->conn->prepare("SELECT netWeight, status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM CargoInfo WHERE id = ?");
            $selectStmt->bind_param("i", $cargoId);
            $selectStmt->execute();
            $cargoData = $selectStmt->get_result()->fetch_assoc();
            $selectStmt->close();

            if (!$cargoData) {
                $this->sendJsonResponse(["status" => "error", "message" => "حواله یافت نشد یا قبلاً حذف شده است"], 404);
            }

            $deleteStmt = $this->conn->prepare("DELETE FROM CargoInfo WHERE id = ?");
            $deleteStmt->bind_param("i", $cargoId);
            $deleteStmt->execute();
            $affectedRows = $deleteStmt->affected_rows;
            $deleteStmt->close();

            if ($affectedRows > 0 && $cargoData['status'] === 'خروج' && !empty($cargoData['netWeight'])) {
                $netWeightValue = (float)$cargoData['netWeight'];
                $tempStmt = $this->conn->prepare("SELECT temp_tonnage_status, temp_tonnage_amount FROM InitialInfo WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ? AND loadingQuotaNumber = ? LIMIT 1");
                $tempStmt->bind_param("sssss", $cargoData['shipName'], $cargoData['loadingWarehouse'], $cargoData['cargoType'], $cargoData['shippingCompany'], $cargoData['loadingQuotaNumber']);
                $tempStmt->execute();
                $tempData = $tempStmt->get_result()->fetch_assoc();
                $tempStmt->close();

                if ($tempData && (int)$tempData['temp_tonnage_status'] === 1) {
                    $newTemp = (float)$tempData['temp_tonnage_amount'] + $netWeightValue;
                    $updateTempStmt = $this->conn->prepare("UPDATE InitialInfo SET temp_tonnage_amount = ? WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ? AND loadingQuotaNumber = ?");
                    $updateTempStmt->bind_param("dsssss", $newTemp, $cargoData['shipName'], $cargoData['loadingWarehouse'], $cargoData['cargoType'], $cargoData['shippingCompany'], $cargoData['loadingQuotaNumber']);
                    $updateTempStmt->execute();
                    $updateTempStmt->close();
                }
            }

            if ($affectedRows === 0) {
                $this->sendJsonResponse(["status" => "error", "message" => "حواله یافت نشد یا قبلاً حذف شده است"], 404);
            } else {
                $this->sendJsonResponse(["status" => "success", "message" => "حواله با موفقیت حذف شد"]);
            }
        } catch (Exception $e) {
            $this->logger->error("Error in deleteCargoInfo: " . $e->getMessage());
            $this->sendJsonResponse(["status" => "error", "message" => "خطا در حذف حواله: " . $e->getMessage()], 500);
        }
    }

    /**
     * جستجوی حواله با شماره قبض باسکول (search_by_scaleReceipt.php)
     */
    public function searchByScaleReceipt(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
            $this->sendJsonResponse(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        try {
            $receipt = isset($_GET['receipt']) ? trim((string)$_GET['receipt']) : '';
            if ($receipt === '') {
                throw new InvalidArgumentException('لطفاً شماره قبض باسکول را وارد کنید.');
            }

            $query = "SELECT id, trackingNumber, numberOfPeople, username, userType, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status, confirm, confirmation, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM CargoInfo WHERE scaleReceiptNumber = ? LIMIT 1";
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("s", $receipt);
            $stmt->execute();
            $cargoInfo = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            if ($cargoInfo) {
                $formattedCargoInfo = [
                    'id' => (int)$cargoInfo['id'],
                    'trackingNumber' => htmlspecialchars((string)$cargoInfo['trackingNumber']),
                    'numberOfPeople' => htmlspecialchars((string)($cargoInfo['numberOfPeople'] ?? '')),
                    'username' => htmlspecialchars((string)($cargoInfo['username'] ?? '')),
                    'userType' => htmlspecialchars((string)($cargoInfo['userType'] ?? '')),
                    'entryTime' => htmlspecialchars((string)$cargoInfo['entryTime']),
                    'netWeight' => htmlspecialchars((string)$cargoInfo['netWeight']),
                    'scaleReceiptNumber' => htmlspecialchars((string)$cargoInfo['scaleReceiptNumber']),
                    'shortageWeight' => htmlspecialchars((string)$cargoInfo['shortageWeight']),
                    'excessWeight' => htmlspecialchars((string)$cargoInfo['excessWeight']),
                    'exitTime' => htmlspecialchars((string)($cargoInfo['exitTime'] ?? '')),
                    'exitDate' => htmlspecialchars((string)($cargoInfo['exitDate'] ?? '')),
                    'status' => htmlspecialchars((string)$cargoInfo['status']),
                    'shipName' => htmlspecialchars((string)$cargoInfo['shipName']),
                    'loadingWarehouse' => htmlspecialchars((string)$cargoInfo['loadingWarehouse']),
                    'cargoType' => htmlspecialchars((string)$cargoInfo['cargoType']),
                    'shippingCompany' => htmlspecialchars((string)$cargoInfo['shippingCompany']),
                    'loadingQuotaNumber' => htmlspecialchars((string)$cargoInfo['loadingQuotaNumber']),
                    'confirm' => htmlspecialchars((string)($cargoInfo['confirm'] ?? '')),
                    'confirmation' => htmlspecialchars((string)($cargoInfo['confirmation'] ?? 'no'))
                ];
                $this->sendJsonResponse(['cargoInfo' => $formattedCargoInfo]);
            } else {
                $this->sendJsonResponse(['error' => 'هیچ نتیجه‌ای یافت نشد.'], 404);
            }
        } catch (InvalidArgumentException $e) {
            $this->sendJsonResponse(['error' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in searchByScaleReceipt: " . $e->getMessage());
            $this->sendJsonResponse(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    /**
     * جستجوی حواله با شماره رهگیری (search_by_tracking.php)
     */
    public function searchByTracking(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
            $this->sendJsonResponse(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        try {
            $tracking = isset($_GET['tracking']) ? trim((string)$_GET['tracking']) : '';
            if ($tracking === '') {
                throw new InvalidArgumentException('لطفاً شماره حواله را وارد کنید.');
            }

            $query = "SELECT id, trackingNumber, numberOfPeople, username, userType, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status, confirm, confirmation, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM CargoInfo WHERE trackingNumber = ? ORDER BY entryTime DESC";
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("s", $tracking);
            $stmt->execute();
            $result = $stmt->get_result();

            $cargoInfoList = [];
            while ($row = $result->fetch_assoc()) {
                $cargoInfoList[] = [
                    'cargoInfo' => [
                        'id' => (int)$row['id'],
                        'trackingNumber' => htmlspecialchars((string)$row['trackingNumber']),
                        'numberOfPeople' => htmlspecialchars((string)($row['numberOfPeople'] ?? '')),
                        'username' => htmlspecialchars((string)($row['username'] ?? '')),
                        'userType' => htmlspecialchars((string)($row['userType'] ?? '')),
                        'entryTime' => htmlspecialchars((string)$row['entryTime']),
                        'netWeight' => htmlspecialchars((string)$row['netWeight']),
                        'scaleReceiptNumber' => htmlspecialchars((string)$row['scaleReceiptNumber']),
                        'shortageWeight' => htmlspecialchars((string)$row['shortageWeight']),
                        'excessWeight' => htmlspecialchars((string)$row['excessWeight']),
                        'exitTime' => htmlspecialchars((string)($row['exitTime'] ?? '')),
                        'exitDate' => htmlspecialchars((string)($row['exitDate'] ?? '')),
                        'status' => htmlspecialchars((string)$row['status']),
                        'shipName' => htmlspecialchars((string)$row['shipName']),
                        'loadingWarehouse' => htmlspecialchars((string)$row['loadingWarehouse']),
                        'cargoType' => htmlspecialchars((string)$row['cargoType']),
                        'shippingCompany' => htmlspecialchars((string)$row['shippingCompany']),
                        'loadingQuotaNumber' => htmlspecialchars((string)$row['loadingQuotaNumber']),
                        'confirm' => htmlspecialchars((string)($row['confirm'] ?? '')),
                        'confirmation' => htmlspecialchars((string)($row['confirmation'] ?? 'no'))
                    ]
                ];
            }
            $stmt->close();

            if (!empty($cargoInfoList)) {
                $this->sendJsonResponse([
                    'cargoInfoList' => $cargoInfoList,
                    'totalCount' => count($cargoInfoList)
                ]);
            } else {
                $this->sendJsonResponse(['error' => 'هیچ نتیجه‌ای برای این شماره حواله یافت نشد.'], 404);
            }
        } catch (InvalidArgumentException $e) {
            $this->sendJsonResponse(['error' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in searchByTracking: " . $e->getMessage());
            $this->sendJsonResponse(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    /**
     * دریافت اطلاعات اولیه کشتی و آمار بارهای قبلی (getInitialInfo.php)
     */
    public function getInitialInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        $requiredParams = ['quotaNumber', 'shippingCompany', 'warehouse', 'cargoType'];
        $missingParams = [];
        foreach ($requiredParams as $param) {
            if (!isset($_GET[$param]) || trim((string)$_GET[$param]) === '') {
                $missingParams[] = $param;
            }
        }

        if (!empty($missingParams)) {
            $this->sendJsonResponse([
                "status" => "error",
                "message" => "پارامترهای زیر الزامی هستند: " . implode(', ', $missingParams)
            ], 400);
        }

        $quotaNumber = $this->sanitizeString((string)$_GET['quotaNumber']);
        $shippingCompany = $this->sanitizeString((string)$_GET['shippingCompany']);
        $warehouse = $this->sanitizeString((string)$_GET['warehouse']);
        $cargoType = $this->sanitizeString((string)$_GET['cargoType']);

        try {
            $this->conn->begin_transaction();

            $today = jdate('Y/m/d');
            $yesterday = jdate('Y/m/d', time() - 86400);

            $stmt = $this->conn->prepare("SELECT *, temp_tonnage_status, temp_tonnage_amount FROM InitialInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? LIMIT 1");
            $stmt->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
            $stmt->execute();
            $initialResult = $stmt->get_result();

            if ($initialResult->num_rows === 0) {
                $stmt->close();
                $this->conn->rollback();
                $this->sendJsonResponse([
                    "status" => "error",
                    "message" => "اطلاعات وارد شده (شامل نوع کالا) مطابقت ندارد. لطفاً مقادیر را بررسی کنید."
                ], 404);
            }

            $initialInfo = $initialResult->fetch_assoc();
            $stmt->close();

            $statsStmt = $this->conn->prepare("SELECT 
                COUNT(*) as totalVouchers,
                COALESCE(SUM(CASE WHEN status = 'خروج' THEN netWeight ELSE 0 END), 0) as totalNetWeight,
                COUNT(CASE WHEN status = 'خروج' THEN 1 END) as exitedVouchers,
                COUNT(CASE WHEN status = 'ورود' THEN 1 END) as remainingVouchers,
                COALESCE(AVG(CASE WHEN status = 'خروج' AND netWeight > 0 THEN netWeight END), 0) as avgNetWeight
                FROM CargoInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ?");
            $statsStmt->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
            $statsStmt->execute();
            $stats = $statsStmt->get_result()->fetch_assoc();
            $statsStmt->close();

            $totalNetWeight = (int)$stats['totalNetWeight'];
            $totalVouchers = (int)$stats['totalVouchers'];
            $averageNetWeight = (float)$stats['avgNetWeight'];

            $cargoWeight = (int)$initialInfo['cargoWeight'];
            $remainingWeight = max(0, $cargoWeight - $totalNetWeight);
            $remainingServices = $averageNetWeight > 0 ? (int)floor($remainingWeight / $averageNetWeight) : 0;

            $initialInfo['remainingWeight'] = $remainingWeight;
            $initialInfo['averageNetWeight'] = round($averageNetWeight, 2);
            $initialInfo['remainingServices'] = $remainingServices;
            $initialInfo['totalVoucherCount'] = $totalVouchers;
            $initialInfo['totalNetWeight'] = $totalNetWeight;
            $initialInfo['tempTonnageStatus'] = isset($initialInfo['temp_tonnage_status']) ? (bool)$initialInfo['temp_tonnage_status'] : false;
            $initialInfo['tempTonnageAmount'] = isset($initialInfo['temp_tonnage_amount']) ? (float)$initialInfo['temp_tonnage_amount'] : null;

            $cargoStmt = $this->conn->prepare("SELECT * FROM CargoInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? AND (status = 'ورود' OR (status = 'خروج' AND exitDate >= ? AND exitDate <= ?)) ORDER BY CASE WHEN status = 'ورود' THEN 1 ELSE 2 END, entryTime DESC");
            $cargoStmt->bind_param("ssssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType, $yesterday, $today);
            $cargoStmt->execute();
            $cargoResult = $cargoStmt->get_result();

            $cargoInfoList = [];
            while ($row = $cargoResult->fetch_assoc()) {
                $cargoInfoList[] = $row;
            }
            $cargoStmt->close();

            $trackStmt = $this->conn->prepare("SELECT DISTINCT trackingNumber FROM CargoInfo WHERE shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ?");
            $trackStmt->bind_param("sss", $shippingCompany, $warehouse, $cargoType);
            $trackStmt->execute();
            $trackResult = $trackStmt->get_result();

            $allTrackingNumbers = [];
            while ($row = $trackResult->fetch_assoc()) {
                $allTrackingNumbers[] = $row['trackingNumber'];
            }
            $trackStmt->close();

            $this->conn->commit();

            $this->sendJsonResponse([
                "status" => "success",
                "initialInfo" => $initialInfo,
                "cargoInfoList" => $cargoInfoList,
                "allTrackingNumbers" => $allTrackingNumbers
            ]);
        } catch (Exception $e) {
            $this->conn->rollback();
            $this->logger->error("Error in getInitialInfo: " . $e->getMessage());
            $this->sendJsonResponse([
                "status" => "error",
                "message" => "خطایی در سیستم رخ داده است. لطفاً بعداً تلاش کنید."
            ], 500);
        }
    }

    /**
     * ثبت اطلاعات اولیه جدید (saveInitialInfo.php)
     */
    public function saveInitialInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');

        $requiredFields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'cargoWeight', 'loadingQuotaNumber', 'remainingWeight', 'totalNetWeight', 'averageNetWeight', 'remainingServices', 'cargoOwner'];

        try {
            $data = json_decode((string)file_get_contents('php://input'), true);
            if (!$data || !is_array($data)) {
                throw new Exception("داده‌های ورودی نامعتبر هستند.");
            }

            foreach ($requiredFields as $field) {
                if (!isset($data[$field]) || trim((string)$data[$field]) === '') {
                    throw new Exception("فیلد $field الزامی است.");
                }
            }

            $stmt = $this->conn->prepare("INSERT INTO InitialInfo (shipName, loadingWarehouse, cargoType, shippingCompany, cargoWeight, loadingQuotaNumber, remainingWeight, totalNetWeight, averageNetWeight, remainingServices, cargoOwner, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)");
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی دستور SQL: " . $this->conn->error);
            }

            $stmt->bind_param("ssssdddddds", 
                $data['shipName'],
                $data['loadingWarehouse'],
                $data['cargoType'],
                $data['shippingCompany'],
                $data['cargoWeight'],
                $data['loadingQuotaNumber'],
                $data['remainingWeight'],
                $data['totalNetWeight'],
                $data['averageNetWeight'],
                $data['remainingServices'],
                $data['cargoOwner']
            );

            if (!$stmt->execute()) {
                throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
            }
            $stmt->close();

            $this->sendJsonResponse(["status" => "success", "message" => "اطلاعات با موفقیت ثبت شد."]);
        } catch (Exception $e) {
            $this->logger->error("Error in saveInitialInfo: " . $e->getMessage());
            $this->sendJsonResponse(["status" => "error", "message" => $e->getMessage()], 400);
        }
    }

    private function sanitizeString(string $input): string {
        return htmlspecialchars(trim($input), ENT_QUOTES, 'UTF-8');
    }

    private function validateNumericField($value, string $fieldName): string {
        if ($value === null || $value === '') {
            return '0';
        }
        $strValue = (string)$value;
        $sanitized = filter_var(trim($strValue), FILTER_SANITIZE_NUMBER_FLOAT, FILTER_FLAG_ALLOW_FRACTION);
        if (!is_numeric($sanitized)) {
            throw new InvalidArgumentException("فیلد {$fieldName} باید عددی باشد.");
        }
        return (string)$sanitized;
    }

    private function validateStringField($value, string $fieldName, bool $required = true): string {
        $strValue = ($value === null) ? '' : (string)$value;
        $sanitized = htmlspecialchars(trim($strValue), ENT_QUOTES, 'UTF-8');
        if ($required && empty($sanitized)) {
            throw new InvalidArgumentException("فیلد {$fieldName} الزامی است.");
        }
        return $sanitized;
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

    private function sendJsonResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    private function sendSuccessResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    /**
     * دریافت لیست کشتی‌های فعال (getActiveShips.php)
     */
    public function getActiveShips(): void {
        header('Content-Type: application/json; charset=UTF-8');

        try {
            $stmt = $this->conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM InitialInfo WHERE isActive = 1");
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی دستور SQL: " . $this->conn->error);
            }

            if (!$stmt->execute()) {
                throw new Exception("خطا در اجرای دستور SQL: " . $stmt->error);
            }

            $result = $stmt->get_result();
            $activeShips = [];

            while ($row = $result->fetch_assoc()) {
                $activeShips[] = [
                    'shipName' => $row['shipName'],
                    'loadingWarehouse' => $row['loadingWarehouse'],
                    'cargoType' => $row['cargoType'],
                    'shippingCompany' => $row['shippingCompany'],
                    'loadingQuotaNumber' => $row['loadingQuotaNumber']
                ];
            }
            $stmt->close();

            $this->sendJsonResponse($activeShips);
        } catch (Exception $e) {
            $this->logger->error("Error in getActiveShips: " . $e->getMessage());
            $this->sendJsonResponse(["status" => "error", "message" => $e->getMessage()], 400);
        }
    }

    /**
     * بررسی تکراری بودن شماره قبض باسکول (check_scale_receipt.php)
     */
    public function checkScaleReceipt(): void {
        header('Content-Type: application/json; charset=utf-8');

        $scaleReceiptNumber = $this->sanitizeString($_GET['scaleReceiptNumber'] ?? '');

        if (empty($scaleReceiptNumber)) {
            $this->sendJsonResponse(['error' => 'شماره قبض باسکول الزامی است.'], 400);
        }

        if (!ctype_digit($scaleReceiptNumber) || strlen($scaleReceiptNumber) !== 8) {
            $this->sendJsonResponse(['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.'], 400);
        }

        $firstTwoDigits = substr($scaleReceiptNumber, 0, 2);
        if ($firstTwoDigits < '44' || $firstTwoDigits > '55') {
            $this->sendJsonResponse(['error' => 'شماره قبض باسکول معتبر نیست. لطفاً دوباره اسکن کنید.'], 400);
        }

        try {
            $stmt = $this->conn->prepare("SELECT trackingNumber, netWeight, loadingQuotaNumber FROM CargoInfo WHERE scaleReceiptNumber = ? LIMIT 1");
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی دستور SQL");
            }

            $stmt->bind_param("s", $scaleReceiptNumber);
            if (!$stmt->execute()) {
                throw new Exception("خطا در اجرای دستور SQL");
            }

            $result = $stmt->get_result();

            if ($row = $result->fetch_assoc()) {
                $stmt->close();
                $this->sendJsonResponse([
                    'exists' => true,
                    'message' => 'شماره قبض باسکول تکراری است.',
                    'trackingNumber' => $row['trackingNumber'],
                    'netWeight' => $row['netWeight'],
                    'loadingQuotaNumber' => $row['loadingQuotaNumber']
                ]);
            } else {
                $stmt->close();
                $this->sendJsonResponse(['exists' => false, 'message' => 'شماره قبض باسکول معتبر است.']);
            }
        } catch (Exception $e) {
            $this->logger->error("Error in checkScaleReceipt: " . $e->getMessage());
            $this->sendJsonResponse(['error' => $e->getMessage()], 500);
        }
    }

    private function sendErrorResponse(string $message): void {
        $this->sendSuccessResponse([
            'error' => true,
            'message' => $message
        ], 500);
    }
}


