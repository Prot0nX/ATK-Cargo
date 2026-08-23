<?php
// PHP/src/Controllers/CargoController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use PDO;
use App\Core\Database;
use App\Core\MicroCache;
use App\Core\Request;
use App\Core\Response;
use App\Core\Logger;
use App\Exceptions\ApiException;
use App\Services\CargoService;
use App\Services\PasswordGateService;
use App\Repositories\CargoRepository;
use App\Enums\CargoStatus;

class CargoController {
    // بدون ->value چون PHP 8.1 اجازه‌ی property-fetch در class const نمی‌دهد
    private const ENTERED = CargoStatus::ENTERED;
    private const EXITED = CargoStatus::EXITED;

    private PDO $conn;
    private Request $request;
    private Logger $logger;
    private CargoService $cargoService;
    private CargoRepository $cargoRepo;

    public function __construct(?CargoService $cargoService = null, ?CargoRepository $cargoRepo = null) {
        $this->conn = Database::getInstance()->getPdoConnection();
        $this->request = new Request();
        $this->logger = Logger::getInstance();
        $this->cargoRepo = $cargoRepo ?? new CargoRepository();
        $this->cargoService = $cargoService ?? new CargoService($this->cargoRepo);
    }

    // ثبت یا به‌روزرسانی اطلاعات حواله بارگیری (saveOrUpdateCargoInfo.php)؛ $username/$userType از Router::dispatch
 // می‌آیند تا audit trail با هدر جعلی قابل دستکاری نباشد
    public function saveOrUpdate(?string $username, ?string $userType): void {
        header('Content-Type: application/json; charset=utf-8');
        ini_set('memory_limit', '64M');
        ini_set('max_execution_time', '15');

        $params = $this->request->all();
        $params['username'] = $username;
        $params['userType'] = $userType;

        $requiredFields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber'];
        $optionalFields = ['entryTime', 'netWeight', 'scaleReceiptNumber', 'shortageWeight', 'excessWeight', 'exitTime', 'exitDate', 'status', 'confirmation', 'numberOfPeople', 'duplicateConfirmation'];

        // سقف طول فیلد هم‌راستا با varchar(100) در schema.sql، برای جلوگیری از truncate بی‌صدا (Phase4.10)
        $maxFieldLength = 100;

        foreach ($requiredFields as $field) {
            if (!isset($params[$field]) || trim((string)$params[$field]) === '') {
                $this->sendErrorResponse("پارامتر $field الزامی است.");
            }
            $params[$field] = $this->sanitizeString((string)$params[$field]);
            if (mb_strlen($params[$field], 'UTF-8') > $maxFieldLength) {
                $this->sendErrorResponse("پارامتر $field نمی‌تواند بیشتر از {$maxFieldLength} کاراکتر باشد.");
            }
        }

        foreach ($optionalFields as $field) {
            $params[$field] = isset($params[$field]) ? $this->sanitizeString((string)$params[$field]) : '';
            if (mb_strlen($params[$field], 'UTF-8') > $maxFieldLength) {
                $this->sendErrorResponse("پارامتر $field نمی‌تواند بیشتر از {$maxFieldLength} کاراکتر باشد.");
            }
        }

        try {
            $result = $this->cargoService->saveOrUpdateCargo($params);
            Response::json($result['data'], $result['code'] ?? 200);
        } catch (ApiException $e) {
            // ConflictException کد HTTP معنادار خودش را حمل می‌کند (۴۰۹) و نباید با ۵۰۰ عمومی پوشانده شود
            $this->logger->error("Error in CargoController saveOrUpdate: " . $e->getMessage());
            Response::json(['error' => true, 'message' => $e->getMessage()], $e->getStatusCode());
        } catch (Exception $e) {
            // پیام خام DB/داخلی این شاخه فقط لاگ می‌شود و به کلاینت نمی‌رود (Phase2.4)
            $this->logger->error("Error in CargoController saveOrUpdate: " . $e->getMessage());
            $this->sendErrorResponse('خطای داخلی سرور رخ داده است.');
        }
    }

    // به‌روزرسانی کامل اطلاعات حواله بار (updateCargoInfo.php)؛ $username/$userType از Router::dispatch می‌آیند —
 // route این متد از قبل permission=>'edit_cargo' سطح Router دارد
    public function updateCargoInfo(?string $username, ?string $userType): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        // متد PATCH جایگزین POST قبلی شده، هم‌راستا با routes/api_v2.php و کلاینت اندروید (Phase4 #33)
        if ($_SERVER['REQUEST_METHOD'] !== 'PATCH') {
            Response::json(['error' => true, 'message' => 'روش درخواست نامعتبر است. فقط PATCH مجاز است.'], 405);
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
            $username = (string)$username;
            $userType = (string)$userType;
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
                Response::json(['error' => true, 'message' => 'رکوردی با این شناسه یافت نشد.'], 404);
            }

            if ($this->cargoRepo->isScaleReceiptDuplicate($scaleReceiptNumber, (int)$id)) {
                Response::json(['error' => true, 'message' => 'شماره قبض باسکول تکراری است.'], 400);
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
                MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
                Response::json([
                    'error' => false,
                    'status' => 'success',
                    'message' => 'اطلاعات با موفقیت بروزرسانی شد.'
                ]);
            } else {
                Response::json([
                    'error' => false,
                    'status' => 'no_change',
                    'message' => 'تغییری در اطلاعات ایجاد نشد.'
                ]);
            }
        } catch (InvalidArgumentException $e) {
            Response::json(['error' => true, 'message' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in updateCargoInfo: " . $e->getMessage());
            Response::json(['error' => true, 'message' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    // تأیید حواله توسط بارشمار (confirm_cargo.php)؛ $username/$userType از Router::dispatch می‌آیند — route این متد
 // از قبل permission=>'cargo_counter' سطح Router دارد
    public function confirmCargo(?string $username, ?string $userType): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            Response::json(["status" => "error", "message" => "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید."], 405);
        }

        $data = json_decode((string)file_get_contents('php://input'), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
            Response::json(["status" => "error", "message" => "فرمت JSON نامعتبر است"], 400);
        }

        if (!isset($data['id']) || (is_string($data['id']) && trim($data['id']) === '')) {
            Response::json(["status" => "error", "message" => "فیلدهای ضروری وجود ندارند: id"], 400);
        }

        $cargoId = (int)$data['id'];
        $username = (string)$username;
        $userType = (string)$userType;

        // بررسی loadingQuotaNumber/shipName برای جلوگیری از تأیید حواله‌های خارج از دامنه‌ی کاربر (IDOR)
        $loadingQuotaNumber = $this->sanitizeString((string)($data['loadingQuotaNumber'] ?? ''));
        $shipName = $this->sanitizeString((string)($data['shipName'] ?? ''));

        if ($cargoId <= 0) {
            Response::json(["status" => "error", "message" => "شناسه حواله نامعتبر است"], 400);
        }

        if ($loadingQuotaNumber === '' || $shipName === '') {
            Response::json(["status" => "error", "message" => "فیلدهای ضروری وجود ندارند: loadingQuotaNumber, shipName"], 400);
        }

        try {
            $res = $this->cargoRepo->confirmCargo($cargoId, $username, $userType, $loadingQuotaNumber, $shipName);
            if ($res['affected'] > 0) {
                MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
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
                // ارسال بدون JSON_NUMERIC_CHECK تا trackingNumber/loadingQuotaNumber با صفر ابتدایی خراب نشوند
                Response::json($response, 200);
            } else {
                $exists = $this->cargoRepo->findCargoById($cargoId) !== null;
                if ($exists) {
                    // کد ۴۰۹ (نه ۲۰۰) چون این خطای واقعی است (تأیید تکراری/رقابت هم‌زمانی)، نه موفقیت
                    Response::json(["status" => "error", "message" => "حواله قبلاً تأیید شده است یا تغییری اعمال نشد"], 409);
                } else {
                    Response::json(["status" => "error", "message" => "حواله با شناسه ارسالی یافت نشد"], 404);
                }
            }
        } catch (Exception $e) {
            $this->logger->error("Error in confirmCargo: " . $e->getMessage());
            // پیام داخلی PDO فقط در لاگ ثبت می‌شود تا نام جدول/ستون افشا نشود
            Response::json(["status" => "error", "message" => "خطایی در سیستم رخ داده است. لطفاً بعداً تلاش کنید."], 500);
        }
    }

    // حذف اطلاعات حواله (deleteCargoInfo.php)؛ $username از Router::dispatch می‌آید — route این متد از قبل
 // permission=>'delete_cargo' سطح Router دارد
    public function deleteCargoInfo(?string $username): void {
        header('Content-Type: application/json; charset=UTF-8');

        // متد DELETE جایگزین POST قبلی شده، هم‌راستا با routes/api_v2.php و کلاینت اندروید (Phase4 #33)
        if ($_SERVER['REQUEST_METHOD'] !== 'DELETE') {
            Response::json(["status" => "error", "message" => "روش درخواست مجاز نیست. لطفاً از روش DELETE استفاده کنید."], 405);
        }

        $data = json_decode((string)file_get_contents("php://input"), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data) || !isset($data['id']) || empty($data['id'])) {
            Response::json(["status" => "error", "message" => "فیلد ضروری وجود ندارد: id"], 400);
        }

        $cargoId = (int)$data['id'];
        if ($cargoId <= 0) {
            Response::json(["status" => "error", "message" => "شناسه حواله نامعتبر است"], 400);
        }

        // بررسی رمز عبور حذف در همین درخواست انجام می‌شود، نه در فراخوانی جداگانه‌ی checkPassword.php، تا تضمین شود رمز برای همین عملیات بررسی شده
        $password = (string)($data['password'] ?? '');
        if ($password === '') {
            Response::json(["status" => "error", "message" => "رمز عبور الزامی است"], 400);
        }

        $gateResult = (new PasswordGateService())->verify('delete_info', $password, (string)$username);
        if (!$gateResult['success']) {
            Response::json(["status" => "error", "message" => $gateResult['message']], $gateResult['locked'] ? 429 : 403);
        }

        try {
            $res = $this->cargoService->deleteCargoInfo($cargoId);
            $code = $res['code'] ?? 200;
            unset($res['code']);
            Response::json($res, $code);
        } catch (Exception $e) {
            $this->logger->error("Error in deleteCargoInfo: " . $e->getMessage());
            Response::json(["status" => "error", "message" => "خطا در حذف حواله."], 500);
        }
    }

    // جستجوی حواله با شماره قبض باسکول (search_by_scaleReceipt.php)
    public function searchByScaleReceipt(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if (!$this->request->isGet()) {
            Response::json(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        try {
            $receipt = trim((string)$this->request->get('receipt', ''));
            if ($receipt === '') {
                throw new InvalidArgumentException('لطفاً شماره قبض باسکول را وارد کنید.');
            }

            $cargoInfo = $this->cargoRepo->searchByScaleReceipt($receipt);
            if ($cargoInfo) {
 // htmlspecialchars حذف شد؛ خروجی JSON برای کلاینت اندروید است، نه HTML مرورگر
                $formattedCargoInfo = [
                    'id' => (int)$cargoInfo['id'],
                    'trackingNumber' => (string)$cargoInfo['trackingNumber'],
                    'numberOfPeople' => (string)($cargoInfo['numberOfPeople'] ?? ''),
                    'username' => (string)($cargoInfo['username'] ?? ''),
                    'userType' => (string)($cargoInfo['userType'] ?? ''),
                    'entryTime' => (string)$cargoInfo['entryTime'],
                    'netWeight' => (string)$cargoInfo['netWeight'],
                    'scaleReceiptNumber' => (string)$cargoInfo['scaleReceiptNumber'],
                    'shortageWeight' => (string)$cargoInfo['shortageWeight'],
                    'excessWeight' => (string)$cargoInfo['excessWeight'],
                    'exitTime' => (string)($cargoInfo['exitTime'] ?? ''),
                    'exitDate' => (string)($cargoInfo['exitDate'] ?? ''),
                    'status' => (string)$cargoInfo['status'],
                    'shipName' => (string)$cargoInfo['shipName'],
                    'loadingWarehouse' => (string)$cargoInfo['loadingWarehouse'],
                    'cargoType' => (string)$cargoInfo['cargoType'],
                    'shippingCompany' => (string)$cargoInfo['shippingCompany'],
                    'loadingQuotaNumber' => (string)$cargoInfo['loadingQuotaNumber'],
                    'confirm' => (string)($cargoInfo['confirm'] ?? ''),
                    'confirmation' => (string)($cargoInfo['confirmation'] ?? 'no')
                ];
                Response::json(['cargoInfo' => $formattedCargoInfo]);
            } else {
                Response::json(['error' => 'هیچ نتیجه‌ای یافت نشد.'], 404);
            }
        } catch (InvalidArgumentException $e) {
            Response::json(['error' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in searchByScaleReceipt: " . $e->getMessage());
            Response::json(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    // جستجوی حواله با شماره رهگیری (search_by_tracking.php)
    public function searchByTracking(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if (!$this->request->isGet()) {
            Response::json(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        try {
            $tracking = trim((string)$this->request->get('tracking', ''));
            if ($tracking === '') {
                throw new InvalidArgumentException('لطفاً شماره حواله را وارد کنید.');
            }

            $rows = $this->cargoRepo->searchByTracking($tracking);
            $cargoInfoList = [];
 // htmlspecialchars حذف شد؛ خروجی JSON برای کلاینت اندروید است، نه HTML مرورگر
            foreach ($rows as $row) {
                $cargoInfoList[] = [
                    'cargoInfo' => [
                        'id' => (int)$row['id'],
                        'trackingNumber' => (string)$row['trackingNumber'],
                        'numberOfPeople' => (string)($row['numberOfPeople'] ?? ''),
                        'username' => (string)($row['username'] ?? ''),
                        'userType' => (string)($row['userType'] ?? ''),
                        'entryTime' => (string)$row['entryTime'],
                        'netWeight' => (string)$row['netWeight'],
                        'scaleReceiptNumber' => (string)$row['scaleReceiptNumber'],
                        'shortageWeight' => (string)$row['shortageWeight'],
                        'excessWeight' => (string)$row['excessWeight'],
                        'exitTime' => (string)($row['exitTime'] ?? ''),
                        'exitDate' => (string)($row['exitDate'] ?? ''),
                        'status' => (string)$row['status'],
                        'shipName' => (string)$row['shipName'],
                        'loadingWarehouse' => (string)$row['loadingWarehouse'],
                        'cargoType' => (string)$row['cargoType'],
                        'shippingCompany' => (string)$row['shippingCompany'],
                        'loadingQuotaNumber' => (string)$row['loadingQuotaNumber'],
                        'confirm' => (string)($row['confirm'] ?? ''),
                        'confirmation' => (string)($row['confirmation'] ?? 'no')
                    ]
                ];
            }

            if (!empty($cargoInfoList)) {
                Response::json([
                    'cargoInfoList' => $cargoInfoList,
                    'totalCount' => count($cargoInfoList)
                ]);
            } else {
                Response::json(['error' => 'هیچ نتیجه‌ای برای این شماره حواله یافت نشد.'], 404);
            }
        } catch (InvalidArgumentException $e) {
            Response::json(['error' => $e->getMessage()], 400);
        } catch (Exception $e) {
            $this->logger->error("Error in searchByTracking: " . $e->getMessage());
            Response::json(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    // دریافت اطلاعات اولیه کشتی و آمار بارهای قبلی (getInitialInfo.php)
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
            Response::json([
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

            // انتخاب فقط ستون‌های مصرفی مدل InitialInfo کلاینت؛ فیلدهای محاسباتی چند خط پایین‌تر بازنویسی می‌شوند
            $stmt = $this->conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, cargoOwner, cargoWeight, loadingQuotaNumber, isActive, temp_tonnage_status, temp_tonnage_amount FROM InitialInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? LIMIT 1");
            $stmt->execute([$quotaNumber, $shippingCompany, $warehouse, $cargoType]);
            $initialInfo = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($initialInfo === false) {
                Response::json([
                    "status" => "error",
                    "message" => "اطلاعات وارد شده (شامل نوع کالا) مطابقت ندارد. لطفاً مقادیر را بررسی کنید."
                ], 404);
            }

            $statsStmt = $this->conn->prepare("SELECT
                COUNT(*) as totalVouchers,
                COALESCE(SUM(CASE WHEN status = '" . self::EXITED->value . "' THEN netWeight ELSE 0 END), 0) as totalNetWeight,
                COUNT(CASE WHEN status = '" . self::EXITED->value . "' THEN 1 END) as exitedVouchers,
                COUNT(CASE WHEN status = '" . self::ENTERED->value . "' THEN 1 END) as remainingVouchers,
                COALESCE(AVG(CASE WHEN status = '" . self::EXITED->value . "' AND netWeight > 0 THEN netWeight END), 0) as avgNetWeight
                FROM CargoInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ?");
            $statsStmt->execute([$quotaNumber, $shippingCompany, $warehouse, $cargoType]);
            $stats = $statsStmt->fetch(PDO::FETCH_ASSOC);

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

            // استفاده از COALESCE برای ستون‌های NULLABLE چون مدل Kotlin کلاینت آن‌ها را non-null می‌خواهد و بدون آن کرش می‌کرد
 // LIMIT 2000 سقف محافظتی است نه صفحه‌بندی؛ در عمل به ورودهای معلق + خروج ۲۴ ساعت اخیر محدود است
            $cargoStmt = $this->conn->prepare("SELECT id,
                COALESCE(trackingNumber, '') AS trackingNumber,
                COALESCE(numberOfPeople, 0) AS numberOfPeople,
                COALESCE(username, '') AS username,
                COALESCE(userType, '') AS userType,
                COALESCE(entryTime, '') AS entryTime,
                COALESCE(netWeight, 0) AS netWeight,
                COALESCE(scaleReceiptNumber, '') AS scaleReceiptNumber,
                COALESCE(shortageWeight, '0') AS shortageWeight,
                COALESCE(excessWeight, '0') AS excessWeight,
                exitTime,
                exitDate,
                COALESCE(status, '') AS status,
                COALESCE(shipName, '') AS shipName,
                COALESCE(loadingWarehouse, '') AS loadingWarehouse,
                COALESCE(cargoType, '') AS cargoType,
                COALESCE(shippingCompany, '') AS shippingCompany,
                COALESCE(loadingQuotaNumber, 0) AS loadingQuotaNumber,
                COALESCE(confirm, '') AS confirm,
                confirmation
                FROM CargoInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? AND (status = '" . self::ENTERED->value . "' OR (status = '" . self::EXITED->value . "' AND exitDate >= ? AND exitDate <= ?)) ORDER BY CASE WHEN status = '" . self::ENTERED->value . "' THEN 1 ELSE 2 END, entryTime DESC LIMIT 2000");
            $cargoStmt->execute([$quotaNumber, $shippingCompany, $warehouse, $cargoType, $yesterday, $today]);
            $cargoInfoList = $cargoStmt->fetchAll(PDO::FETCH_ASSOC);

            Response::json([
                "status" => "success",
                "initialInfo" => $initialInfo,
                "cargoInfoList" => $cargoInfoList
            ]);
        } catch (Exception $e) {
            $this->logger->error("Error in getInitialInfo: " . $e->getMessage());
            Response::json([
                "status" => "error",
                "message" => "خطایی در سیستم رخ داده است. لطفاً بعداً تلاش کنید."
            ], 500);
        }
    }

    // ثبت اطلاعات اولیه جدید (saveInitialInfo.php)
 // route این متد از قبل permission=>'initial_info' سطح Router دارد
    public function saveInitialInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');

        $requiredFields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'cargoWeight', 'loadingQuotaNumber', 'remainingWeight', 'totalNetWeight', 'averageNetWeight', 'remainingServices', 'cargoOwner'];

        try {
            $data = json_decode((string)file_get_contents('php://input'), true);
            if (!$data || !is_array($data)) {
                throw new ApiException("داده‌های ورودی نامعتبر هستند.", 400);
            }

            foreach ($requiredFields as $field) {
                if (!isset($data[$field]) || trim((string)$data[$field]) === '') {
                    throw new ApiException("فیلد $field الزامی است.", 400);
                }
            }

            $stmt = $this->conn->prepare("INSERT INTO InitialInfo (shipName, loadingWarehouse, cargoType, shippingCompany, cargoWeight, loadingQuotaNumber, remainingWeight, totalNetWeight, averageNetWeight, remainingServices, cargoOwner, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)");

            if (!$stmt->execute([
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
            ])) {
                throw new Exception("خطا در اجرای دستور SQL درج InitialInfo");
            }
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);

            Response::json(["status" => "success", "message" => "اطلاعات با موفقیت ثبت شد."]);
        } catch (ApiException $e) {
            Response::json(["status" => "error", "message" => $e->getMessage()], $e->getStatusCode());
        } catch (Exception $e) {
            // خطای خام SQL فقط لاگ می‌شود، نه در پاسخ (Phase2.4)
            $this->logger->error("Error in saveInitialInfo: " . $e->getMessage());
            Response::json(["status" => "error", "message" => "خطای داخلی سرور رخ داده است."], 500);
        }
    }

    // دریافت لیست کشتی‌های فعال (getActiveShips.php)
    public function getActiveShips(): void {
        header('Content-Type: application/json; charset=UTF-8');

        try {
            $activeShips = $this->cargoService->getActiveShips();
            Response::json($activeShips);
        } catch (Exception $e) {
            $this->logger->error("Error in getActiveShips: " . $e->getMessage());
            Response::json(["status" => "error", "message" => "خطای داخلی سرور رخ داده است."], 500);
        }
    }

    // بررسی تکراری بودن شماره قبض باسکول (check_scale_receipt.php)
    public function checkScaleReceipt(): void {
        header('Content-Type: application/json; charset=utf-8');

        $scaleReceiptNumber = $this->sanitizeString((string)$this->request->get('scaleReceiptNumber', ''));

        try {
            $res = $this->cargoService->checkScaleReceipt($scaleReceiptNumber);
            $code = $res['code'] ?? 200;
            unset($res['code']);
            Response::json($res, $code);
        } catch (Exception $e) {
            $this->logger->error("Error in checkScaleReceipt: " . $e->getMessage());
            Response::json(['error' => 'خطای داخلی سرور رخ داده است.'], 500);
        }
    }

    // فقط trim، نه htmlspecialchars، چون escape کردن باعث عدم تطابق با InitialInfo escape‌نشده می‌شد؛ SQL Injection با prepared statement بسته است
    private function sanitizeString(string $input): string {
        return trim($input);
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

    // $maxLength پیش‌فرض ۱۰۰ هم‌راستا با varchar(100) در schema.sql، برای جلوگیری از truncate بی‌صدا (Phase4.10)
 // htmlspecialchars حذف شد تا با sanitizeString هم‌راستا باشد؛ مصرف‌کننده JSON/اندروید است نه مرورگر
    private function validateStringField($value, string $fieldName, bool $required = true, ?int $maxLength = 100): string {
        $strValue = ($value === null) ? '' : (string)$value;
        $sanitized = trim($strValue);
        if ($required && empty($sanitized)) {
            throw new InvalidArgumentException("فیلد {$fieldName} الزامی است.");
        }
        if ($maxLength !== null && mb_strlen($sanitized, 'UTF-8') > $maxLength) {
            throw new InvalidArgumentException("فیلد {$fieldName} نمی‌تواند بیشتر از {$maxLength} کاراکتر باشد.");
        }
        return $sanitized;
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
