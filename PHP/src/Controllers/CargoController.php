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
use App\Services\CargoService;
use App\Repositories\CargoRepository;

class CargoController {
    private mysqli $conn;
    private Request $request;
    private Logger $logger;
    private CargoService $cargoService;
    private CargoRepository $cargoRepo;

    public function __construct(?CargoService $cargoService = null, ?CargoRepository $cargoRepo = null) {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->request = new Request();
        $this->logger = Logger::getInstance();
        $this->cargoRepo = $cargoRepo ?? new CargoRepository();
        $this->cargoService = $cargoService ?? new CargoService($this->cargoRepo);
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

        try {
            $result = $this->cargoService->saveOrUpdateCargo($params);
            $this->sendJsonResponse($result['data'], $result['code'] ?? 200);
        } catch (Exception $e) {
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

            $existing = $this->cargoRepo->findCargoById((int)$id);
            if (!$existing) {
                $this->sendJsonResponse(['error' => true, 'message' => 'رکوردی با این شناسه یافت نشد.'], 404);
            }

            if ($this->cargoRepo->isScaleReceiptDuplicate($scaleReceiptNumber, (int)$id)) {
                $this->sendJsonResponse(['error' => true, 'message' => 'شماره قبض باسکول تکراری است.'], 400);
            }

            $updateData = [
                'trackingNumber' => $trackingNumber,
                'numberOfPeople' => $numberOfPeople,
                'username' => $username,
                'userType' => $userType,
                'entryTime' => $entryTime,
                'netWeight' => $netWeight,
                'scaleReceiptNumber' => $scaleReceiptNumber,
                'shortageWeight' => $shortageWeight,
                'excessWeight' => $excessWeight,
                'exitTime' => $exitTime,
                'exitDate' => $exitDate,
                'status' => $status,
                'confirm' => $confirm
            ];

            $updated = $this->cargoRepo->updateCargoFull((int)$id, $updateData);
            if ($updated) {
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
            $res = $this->cargoRepo->confirmCargo($cargoId, $username, $userType);
            if ($res['affected'] > 0) {
                $cargoData = $this->cargoRepo->findCargoById($cargoId);
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
                $exists = $this->cargoRepo->findCargoById($cargoId) !== null;
                if ($exists) {
                    $this->sendJsonResponse(["status" => "error", "message" => "حواله قبلاً تأیید شده است یا تغییری اعمال نشد"], 200);
                } else {
                    $this->sendJsonResponse(["status" => "error", "message" => "حواله با شناسه ارسالی یافت نشد"], 404);
                }
            }
        } catch (Exception $e) {
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
            $res = $this->cargoService->deleteCargoInfo($cargoId);
            $code = $res['code'] ?? 200;
            unset($res['code']);
            $this->sendJsonResponse($res, $code);
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

        if (!$this->request->isGet()) {
            $this->sendJsonResponse(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        try {
            $receipt = trim((string)$this->request->get('receipt', ''));
            if ($receipt === '') {
                throw new InvalidArgumentException('لطفاً شماره قبض باسکول را وارد کنید.');
            }

            $cargoInfo = $this->cargoRepo->searchByScaleReceipt($receipt);
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

        if (!$this->request->isGet()) {
            $this->sendJsonResponse(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        try {
            $tracking = trim((string)$this->request->get('tracking', ''));
            if ($tracking === '') {
                throw new InvalidArgumentException('لطفاً شماره حواله را وارد کنید.');
            }

            $rows = $this->cargoRepo->searchByTracking($tracking);
            $cargoInfoList = [];
            foreach ($rows as $row) {
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
            $val = $this->request->get($param);
            if ($val === null || trim((string)$val) === '') {
                $missingParams[] = $param;
            }
        }

        if (!empty($missingParams)) {
            $this->sendJsonResponse([
                "status" => "error",
                "message" => "پارامترهای زیر الزامی هستند: " . implode(', ', $missingParams)
            ], 400);
        }

        $quotaNumber = $this->sanitizeString((string)$this->request->get('quotaNumber'));
        $shippingCompany = $this->sanitizeString((string)$this->request->get('shippingCompany'));
        $warehouse = $this->sanitizeString((string)$this->request->get('warehouse'));
        $cargoType = $this->sanitizeString((string)$this->request->get('cargoType'));

        try {
            $today = jdate('Y/m/d');
            $yesterday = jdate('Y/m/d', time() - 86400);

            $stmt = $this->conn->prepare("SELECT *, temp_tonnage_status, temp_tonnage_amount FROM InitialInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? LIMIT 1");
            $stmt->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
            $stmt->execute();
            $initialResult = $stmt->get_result();

            if ($initialResult->num_rows === 0) {
                $stmt->close();
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

            $this->sendJsonResponse([
                "status" => "success",
                "initialInfo" => $initialInfo,
                "cargoInfoList" => $cargoInfoList,
                "allTrackingNumbers" => $allTrackingNumbers
            ]);
        } catch (Exception $e) {
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

    /**
     * دریافت لیست کشتی‌های فعال (getActiveShips.php)
     */
    public function getActiveShips(): void {
        header('Content-Type: application/json; charset=UTF-8');

        try {
            $activeShips = $this->cargoService->getActiveShips();
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

        $scaleReceiptNumber = $this->sanitizeString((string)$this->request->get('scaleReceiptNumber', ''));

        try {
            $res = $this->cargoService->checkScaleReceipt($scaleReceiptNumber);
            $code = $res['code'] ?? 200;
            unset($res['code']);
            $this->sendJsonResponse($res, $code);
        } catch (Exception $e) {
            $this->logger->error("Error in checkScaleReceipt: " . $e->getMessage());
            $this->sendJsonResponse(['error' => $e->getMessage()], 500);
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

    private function sendJsonResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }

    private function sendErrorResponse(string $message): void {
        http_response_code(500);
        echo json_encode([
            'error' => true,
            'message' => $message
        ], JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }
}
