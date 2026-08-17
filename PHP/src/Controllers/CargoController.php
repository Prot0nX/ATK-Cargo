<?php
// PHP/src/Controllers/CargoController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
use App\Core\AuthenticatesRequests;
use App\Core\Database;
use App\Core\MicroCache;
use App\Core\Request;
use App\Core\Response;
use App\Core\Logger;
use App\Exceptions\ApiException;
use App\Services\CargoService;
use App\Services\PasswordGateService;
use App\Repositories\CargoRepository;

class CargoController {
    use AuthenticatesRequests;

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

        $this->requireAuthenticatedSession();

        $params = $this->request->all();

        // username/userType هرگز از کلاینت پذیرفته نمی‌شوند؛ هویت واقعی از
        // نشست معتبرشده گرفته می‌شود تا زنجیره‌ی ردیابی (audit trail) با هدر
        // جعلی قابل دستکاری نباشد.
        $params['username'] = $this->authenticatedUsername;
        $params['userType'] = $this->authenticatedUserType;

        $requiredFields = ['shipName', 'loadingWarehouse', 'cargoType', 'shippingCompany', 'loadingQuotaNumber', 'trackingNumber'];
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
            Response::json($result['data'], $result['code'] ?? 200);
        } catch (ApiException $e) {
            // ConflictException (و مشابه آن) کد HTTP معنادار خودش را حمل
            // می‌کند (۴۰۹ برای تداخل هم‌زمانی)؛ نباید مثل خطای داخلی سرور با
            // ۵۰۰ عمومی پوشانده شود.
            $this->logger->error("Error in CargoController saveOrUpdate: " . $e->getMessage());
            Response::json(['error' => true, 'message' => $e->getMessage()], $e->getStatusCode());
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
            Response::json(['error' => true, 'message' => 'روش درخواست نامعتبر است. فقط POST مجاز است.'], 405);
        }

        $this->requireAuthenticatedSession();
        $this->requirePermission('edit_cargo');

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
            // username/userType از نشست معتبرشده گرفته می‌شود، نه از بدنه‌ی
            // درخواست، تا زنجیره‌ی ردیابی با هدر جعلی قابل دستکاری نباشد.
            $username = (string)$this->authenticatedUsername;
            $userType = (string)$this->authenticatedUserType;
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

    /**
     * تأیید حواله توسط بارشمار (confirm_cargo.php)
     */
    public function confirmCargo(): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            Response::json(["status" => "error", "message" => "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید."], 405);
        }

        $this->requireAuthenticatedSession();
        // تأیید حواله یک عملیات نوشتنی با اثر مالی/عملیاتی است؛ باید مثل
        // سایر عملیات نوشتنِ این کنترلر (edit_cargo، delete_cargo) پشت یک
        // مجوز مشخص قفل شود، نه فقط لاگین بودن.
        $this->requirePermission('cargo_counter');

        $data = json_decode((string)file_get_contents('php://input'), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
            Response::json(["status" => "error", "message" => "فرمت JSON نامعتبر است"], 400);
        }

        if (!isset($data['id']) || (is_string($data['id']) && trim($data['id']) === '')) {
            Response::json(["status" => "error", "message" => "فیلدهای ضروری وجود ندارند: id"], 400);
        }

        $cargoId = (int)$data['id'];
        // username/userType از نشست معتبرشده گرفته می‌شود، نه از بدنه‌ی
        // درخواست، تا معلوم شود واقعاً چه کسی حواله را تأیید کرده است.
        $username = (string)$this->authenticatedUsername;
        $userType = (string)$this->authenticatedUserType;

        // بدون این دو، هر کاربر مجاز می‌توانست با شماره‌گذاری متوالی id
        // حواله‌های خارج از کوتاژ/کشتی خودش را هم تأیید کند (IDOR).
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
                // JSON_NUMERIC_CHECK قبلاً trackingNumber/loadingQuotaNumber
                // را که رشته‌اند (و می‌توانند صفر ابتدایی داشته باشند) به
                // عدد تبدیل می‌کرد؛ sendJsonResponse مثل بقیه‌ی endpointهای
                // این کنترلر بدون آن فلگ و با gzip ارسال می‌کند.
                Response::json($response, 200);
            } else {
                $exists = $this->cargoRepo->findCargoById($cargoId) !== null;
                if ($exists) {
                    // این یک خطای واقعی است (تأیید تکراری یا رقابت هم‌زمانی)،
                    // نه موفقیت؛ کد ۲۰۰ باعث می‌شد کلاینت آن را success بداند
                    // و علاوه بر نمایش این پیام به‌عنوان موفقیت، وضعیت محلی را
                    // هم به‌اشتباه «تأیید شده» علامت بزند
                    // (CargoDetailsScreen.handleCargoConfirmation).
                    Response::json(["status" => "error", "message" => "حواله قبلاً تأیید شده است یا تغییری اعمال نشد"], 409);
                } else {
                    Response::json(["status" => "error", "message" => "حواله با شناسه ارسالی یافت نشد"], 404);
                }
            }
        } catch (Exception $e) {
            $this->logger->error("Error in confirmCargo: " . $e->getMessage());
            // پیام داخلی mysqli/exception (که می‌تواند نام جدول/ستون را
            // فاش کند) فقط در لاگ ثبت می‌شود، نه در پاسخ به کلاینت.
            Response::json(["status" => "error", "message" => "خطایی در سیستم رخ داده است. لطفاً بعداً تلاش کنید."], 500);
        }
    }

    /**
     * حذف اطلاعات حواله (deleteCargoInfo.php)
     */
    public function deleteCargoInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            Response::json(["status" => "error", "message" => "روش درخواست مجاز نیست. لطفاً از روش POST استفاده کنید."], 405);
        }

        $this->requireAuthenticatedSession();
        $this->requirePermission('delete_cargo');

        $data = json_decode((string)file_get_contents("php://input"), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data) || !isset($data['id']) || empty($data['id'])) {
            Response::json(["status" => "error", "message" => "فیلد ضروری وجود ندارد: id"], 400);
        }

        $cargoId = (int)$data['id'];
        if ($cargoId <= 0) {
            Response::json(["status" => "error", "message" => "شناسه حواله نامعتبر است"], 400);
        }

        // بررسی رمز عبور حذف اینجا و در همین درخواست انجام می‌شود، نه در یک
        // فراخوانی جداگانه‌ی checkPassword.php قبل از این endpoint؛ چون دو
        // درخواست HTTP مستقل هیچ تضمینی نمی‌دهند که رمز واقعاً برای همین
        // عملیات حذف بررسی شده باشد (کافی بود مستقیماً همین endpoint را صدا
        // بزنند). شمارنده‌ی تلاش ناموفق روی هویت نشست معتبرشده کلید می‌خورد.
        $password = (string)($data['password'] ?? '');
        if ($password === '') {
            Response::json(["status" => "error", "message" => "رمز عبور الزامی است"], 400);
        }

        $gateResult = (new PasswordGateService())->verify('delete_info', $password, (string)$this->authenticatedUsername);
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
            Response::json(["status" => "error", "message" => "خطا در حذف حواله: " . $e->getMessage()], 500);
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
            Response::json(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        $this->requireAuthenticatedSession();

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

    /**
     * جستجوی حواله با شماره رهگیری (search_by_tracking.php)
     */
    public function searchByTracking(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if (!$this->request->isGet()) {
            Response::json(['error' => 'روش درخواست نامعتبر است'], 405);
        }

        $this->requireAuthenticatedSession();

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

    /**
     * دریافت اطلاعات اولیه کشتی و آمار بارهای قبلی (getInitialInfo.php)
     */
    public function getInitialInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        $this->requireAuthenticatedSession();

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

            // ستون‌های cargoWeight/loadingQuotaNumber و ۴ ستون بعدی همان‌هایی
            // هستند که مدل InitialInfo کلاینت (CargoModels.kt) واقعاً مصرف
            // می‌کند؛ remainingWeight/totalNetWeight/averageNetWeight/
            // remainingServices از جدول خوانده نمی‌شوند چون چند خط پایین‌تر
            // با مقدار محاسبه‌شده بازنویسی می‌شوند، و percentage/is_enabled
            // اصلاً در پاسخ استفاده نمی‌شوند.
            $stmt = $this->conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, cargoOwner, cargoWeight, loadingQuotaNumber, isActive, temp_tonnage_status, temp_tonnage_amount FROM InitialInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? LIMIT 1");
            $stmt->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
            $stmt->execute();
            $initialResult = $stmt->get_result();

            if ($initialResult->num_rows === 0) {
                $stmt->close();
                Response::json([
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

            // ستون‌های زیر دقیقاً همان‌هایی هستند که data class CargoInfo در
            // کلاینت (از جمله id، که کلید LazyColumn روی آن است) می‌خواند؛
            // confirm_username/confirm_usertype/updated_at در پاسخ این
            // endpoint مصرف نمی‌شوند.
            //
            // تمام ستون‌های این جدول (به‌جز id) در دیتابیس NULLABLE هستند
            // (schema.sql)، اما مدل Kotlin سمت کلاینت اغلب آن‌ها را non-null
            // تعریف کرده — به‌خصوص netWeight/shortageWeight/excessWeight که
            // برای رکوردهای «ورود» (هنوز باسکول نشده) واقعاً NULL هستند.
            // Gson یک non-null String را بدون خطای فوری با null پر می‌کند و
            // کرش وقتی رخ می‌دهد که همان مقدار به یک پارامتر non-null در
            // لایه‌ی UI برسد (دقیقاً چیزی که در دیالوگ «بررسی و تأیید حواله»
            // با رکوردهای در انتظار وزن‌کشی اتفاق می‌افتاد). COALESCE همینجا
            // مقدار پیش‌فرض امن می‌دهد تا با قرارداد non-null کلاینت سازگار
            // بماند.
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
                FROM CargoInfo WHERE loadingQuotaNumber = ? AND shippingCompany = ? AND loadingWarehouse = ? AND cargoType = ? AND (status = 'ورود' OR (status = 'خروج' AND exitDate >= ? AND exitDate <= ?)) ORDER BY CASE WHEN status = 'ورود' THEN 1 ELSE 2 END, entryTime DESC");
            $cargoStmt->bind_param("ssssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType, $yesterday, $today);
            $cargoStmt->execute();
            $cargoResult = $cargoStmt->get_result();

            $cargoInfoList = [];
            while ($row = $cargoResult->fetch_assoc()) {
                $cargoInfoList[] = $row;
            }
            $cargoStmt->close();

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

    /**
     * ثبت اطلاعات اولیه جدید (saveInitialInfo.php)
     */
    public function saveInitialInfo(): void {
        header('Content-Type: application/json; charset=UTF-8');

        $this->requireAuthenticatedSession();
        $this->requirePermission('initial_info');

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
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);

            Response::json(["status" => "success", "message" => "اطلاعات با موفقیت ثبت شد."]);
        } catch (Exception $e) {
            $this->logger->error("Error in saveInitialInfo: " . $e->getMessage());
            Response::json(["status" => "error", "message" => $e->getMessage()], 400);
        }
    }

    /**
     * دریافت لیست کشتی‌های فعال (getActiveShips.php)
     */
    public function getActiveShips(): void {
        header('Content-Type: application/json; charset=UTF-8');

        $this->requireAuthenticatedSession();

        try {
            $activeShips = $this->cargoService->getActiveShips();
            Response::json($activeShips);
        } catch (Exception $e) {
            $this->logger->error("Error in getActiveShips: " . $e->getMessage());
            Response::json(["status" => "error", "message" => $e->getMessage()], 400);
        }
    }

    /**
     * بررسی تکراری بودن شماره قبض باسکول (check_scale_receipt.php)
     */
    public function checkScaleReceipt(): void {
        header('Content-Type: application/json; charset=utf-8');

        $this->requireAuthenticatedSession();

        $scaleReceiptNumber = $this->sanitizeString((string)$this->request->get('scaleReceiptNumber', ''));

        try {
            $res = $this->cargoService->checkScaleReceipt($scaleReceiptNumber);
            $code = $res['code'] ?? 200;
            unset($res['code']);
            Response::json($res, $code);
        } catch (Exception $e) {
            $this->logger->error("Error in checkScaleReceipt: " . $e->getMessage());
            Response::json(['error' => $e->getMessage()], 500);
        }
    }

    // این مقدار در ستون‌های WHERE (findCargoByKeys، findTempTonnage و ...)
    // برای مقایسه‌ی دقیق استفاده می‌شود. htmlspecialchars اینجا اشتباه بود:
    // چون این API فقط توسط اپ اندروید (نه مرورگر) مصرف می‌شود، escape کردن
    // ورودی هیچ محافظتی ایجاد نمی‌کرد و فقط باعث می‌شد مقادیر دارای &/'/"
    // در InitialInfo (که escape نمی‌شود) با نسخه‌ی escape‌شده در CargoInfo
    // مطابقت نداشته باشند و رکورد به اشتباه «جدید» تشخیص داده شود. SQL
    // Injection از قبل با prepared statement بسته است؛ اینجا فقط trim لازم
    // است.
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

    private function validateStringField($value, string $fieldName, bool $required = true): string {
        $strValue = ($value === null) ? '' : (string)$value;
        $sanitized = htmlspecialchars(trim($strValue), ENT_QUOTES, 'UTF-8');
        if ($required && empty($sanitized)) {
            throw new InvalidArgumentException("فیلد {$fieldName} الزامی است.");
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
