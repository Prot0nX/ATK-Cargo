<?php
// PHP/src/Controllers/UtilityController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
use App\Core\AuthenticatesRequests;
use App\Core\Database;
use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;
use App\Services\PasswordGateService;
use App\Services\PermissionService;

class UtilityController {
    use AuthenticatesRequests;

    private mysqli $conn;
    private Logger $logger;
    private Request $request;

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->logger = Logger::getInstance();
        $this->request = new Request();
    }

    /**
     * بررسی امضای اپلیکیشن (check_signature.php)
     */
    public function checkSignature(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');
        header('Referrer-Policy: strict-origin-when-cross-origin');
        header('Cache-Control: no-cache, no-store, must-revalidate');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            Response::json(['error' => 'روش درخواست غیرمجاز'], 405);
        }

        $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
        if (strpos($contentType, 'application/json') === false) {
            Response::json(['error' => 'نوع محتوای نامعتبر'], 400);
        }

        try {
            $rawInput = file_get_contents('php://input');
            if (empty($rawInput)) {
                Response::json(['error' => 'بدنه درخواست خالی است'], 400);
            }

            $input = json_decode($rawInput, true);
            if (json_last_error() !== JSON_ERROR_NONE || !is_array($input)) {
                Response::json(['error' => 'فرمت JSON نامعتبر'], 400);
            }

            if (!isset($input['app_signature']) || !is_string($input['app_signature'])) {
                Response::json(['error' => 'امضای برنامه ارسال نشده است'], 400);
            }

            $receivedSignature = trim($input['app_signature']);
            if (strlen($receivedSignature) !== 64 || !ctype_xdigit($receivedSignature)) {
                Response::json(['error' => 'فرمت امضای نامعتبر'], 400);
            }

            $packageName = $input['app_package'] ?? null;
            if ($packageName !== null && !preg_match('/^[a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z][a-zA-Z0-9_]*)*$/', (string)$packageName)) {
                Response::json(['error' => 'نام بسته نامعتبر'], 400);
            }

            $stmt = $this->conn->prepare("SELECT 1 FROM SignChecker WHERE app_signature = ? LIMIT 1");
            $stmt->bind_param("s", $receivedSignature);
            $stmt->execute();
            $result = $stmt->get_result();
            $isValid = $result->num_rows > 0;
            $stmt->close();

            Response::json(['is_valid' => $isValid]);
        } catch (Exception $e) {
            $this->logger->error("Signature check error: " . $e->getMessage());
            Response::json(['error' => 'خطای سرور رخ داده است'], 500);
        }
    }

    /**
     * بررسی رمز عبور (check_password.php)
     */
    public function checkPassword(): void {
        header('Content-Type: application/json; charset=utf-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            http_response_code(405);
            echo json_encode(['success' => false, 'message' => 'Method Not Allowed']);
            exit;
        }

        // بدون هویت نشست، این endpoint یک oracle حدس‌زنی رمز باز است. علاوه
        // بر این، شمارنده‌ی تلاش ناموفق باید روی هویت واقعی کلید بخورد نه
        // $_SESSION — کلاینت اندروید کوکی نگه نمی‌دارد، پس شمارنده‌ی قبلی
        // هرگز عملاً به ۵ نمی‌رسید.
        $this->requireAuthenticatedSession();

        $receivedPassword = isset($_POST['password']) ? trim((string)$_POST['password']) : '';
        $passwordType = isset($_POST['passwordType']) ? trim((string)$_POST['passwordType']) : '';

        try {
            $gateResult = (new PasswordGateService())->verify($passwordType, $receivedPassword, (string)$this->authenticatedUsername);
            if ($gateResult['locked']) {
                http_response_code(429);
            }
            echo json_encode(['success' => $gateResult['success'], 'message' => $gateResult['message']], JSON_UNESCAPED_UNICODE);
        } catch (Exception $e) {
            $this->logger->error("Check password error: " . $e->getMessage());
            echo json_encode(['success' => false, 'message' => 'خطایی رخ داده است. لطفا بعدا تلاش کنید.'], JSON_UNESCAPED_UNICODE);
        }
        exit;
    }

    /**
     * بررسی وجود اطلاعات (checkExistence.php)
     */
    public function checkExistence(): void {
        header('Content-Type: application/json; charset=UTF-8');

        // بدون احراز هویت، این endpoint یک oracle برای شمارش/کشف
        // loadingQuotaNumberهای ثبت‌شده بود (S-11).
        $this->requireAuthenticatedSession();

        try {
            $input = file_get_contents('php://input');
            $data = json_decode((string)$input, true);

            if (!$data || !isset($data['loadingQuotaNumber']) || !is_int($data['loadingQuotaNumber']) ||
                !isset($data['shipName']) || !isset($data['loadingWarehouse']) || 
                !isset($data['cargoType']) || !isset($data['shippingCompany'])) {
                Response::json(["status" => "error", "message" => "داده‌های ورودی نامعتبر است"]);
            }

            $loadingQuotaNumber = (int)$data['loadingQuotaNumber'];
            $shipName = (string)$data['shipName'];
            $loadingWarehouse = (string)$data['loadingWarehouse'];
            $cargoType = (string)$data['cargoType'];
            $shippingCompany = (string)$data['shippingCompany'];

            $stmt = $this->conn->prepare("SELECT id FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ?");
            $stmt->bind_param("issss", $loadingQuotaNumber, $shipName, $loadingWarehouse, $cargoType, $shippingCompany);
            $stmt->execute();
            $result = $stmt->get_result();

            if ($result->num_rows > 0) {
                $stmt->close();
                Response::json(["status" => "exists", "message" => "اطلاعات وارد شده قبلاً ثبت شده است."]);
            } else {
                $stmt->close();
                $stmtPartial = $this->conn->prepare("SELECT id FROM InitialInfo WHERE loadingQuotaNumber = ?");
                $stmtPartial->bind_param("i", $loadingQuotaNumber);
                $stmtPartial->execute();
                $resultPartial = $stmtPartial->get_result();

                if ($resultPartial->num_rows > 0) {
                    $stmtPartial->close();
                    Response::json(["status" => "partial_match", "message" => "شماره کوتاژ قبلاً ثبت شده، اما با مشخصات متفاوت. ثبت اطلاعات جدید مجاز است."]);
                } else {
                    $stmtPartial->close();
                    Response::json(["status" => "not_exists", "message" => "اطلاعات وارد شده قابل ثبت است."]);
                }
            }
        } catch (Exception $e) {
            $this->logger->error("Check existence error: " . $e->getMessage());
            Response::json(["status" => "error", "message" => $e->getMessage()]);
        }
    }

    /**
     * بررسی نسخه جدید اپلیکیشن (check_update.php)
     */
    public function checkUpdate(): void {
        header('Content-Type: application/json');
        header('X-Content-Type-Options: nosniff');
        header('Cache-Control: no-store, no-cache, must-revalidate');

        // کلید در هدر (نه query string که در لاگ دسترسی وب‌سرور/پروکسی ثبت می‌شود)
        // و مقایسه‌ی ثابت‌زمان با hash_equals به‌جای === (S-3). پشتیبانی از
        // پارامتر GET قدیمی api_key هم نگه داشته شده تا نسخه‌های نصب‌شده‌ی
        // قدیمی‌تر کلاینت که هنوز هدر نمی‌فرستند، فوراً از کار نیفتند
        $apiKey = (string)($this->request->getHeader('X-Api-Key') ?? $this->request->get('api_key', ''));
        // UPDATE_CHECK_API_KEY === '' یعنی روی سرور پیکربندی نشده (S-13)؛
        // hash_equals('', '') خودش true برمی‌گرداند، پس این حالت باید صریحاً
        // قبل از مقایسه رد شود، وگرنه یک درخواست بدون هدر کلید هم عبور می‌کرد.
        if (UPDATE_CHECK_API_KEY === '' || !hash_equals(UPDATE_CHECK_API_KEY, $apiKey)) {
            http_response_code(403);
            echo json_encode(['error' => 'دسترسی غیرمجاز'], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $configFile = APP_ROOT . '/update_config.php';
        if (!file_exists($configFile)) {
            http_response_code(500);
            echo json_encode(['error' => 'پیکربندی آپدیت یافت نشد'], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $config = include $configFile;
        $currentVersion = (string)$this->request->get('current_version', '');
        if (empty($currentVersion)) {
            http_response_code(400);
            echo json_encode(['error' => 'نسخه فعلی مشخص نشده است.'], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $hasUpdate = version_compare((string)$currentVersion, (string)$config['latest_version'], '<');
        $sha256 = $hasUpdate ? ($config['sha256'] ?? '') : '';
        // fail-closed: بدون هش قابل‌محاسبه (hash_file شکست خورده یا فایل
        // گم است)، تأیید یکپارچگی سمت کلاینت ممکن نیست — پس download_url
        // اصلاً برگردانده نمی‌شود تا کلاینت بی‌صدا یک APK تأییدنشده نصب
        // نکند (DEEP_CODE_AUDIT.md #Phase1.5).
        $downloadUrl = ($hasUpdate && $sha256 !== '') ? $config['download_url'] : '';

        $response = [
            // 'hasUpdate'/'has_update' — کلاینت فعلی خودش hasUpdate را از مقایسه‌ی
            // latestVersion/currentVersion محاسبه می‌کند (تصمیم سرور را نادیده می‌گرفت)؛
            // has_update اضافه شد تا کلاینت بتواند به تصمیم سرور (منبع حقیقت) اعتماد کند
            'hasUpdate' => $hasUpdate,
            'has_update' => $hasUpdate,
            'latestVersion' => $config['latest_version'],
            'downloadUrl' => $downloadUrl,
            'changeLog' => $hasUpdate ? ($config['change_log'] ?? []) : [],
            'minRequiredVersion' => $config['min_required_version'],
            'minAllowedVersion' => $config['min_allowed_version'] ?? $config['min_required_version'],
            'sha256' => $sha256,
            // فیلدهای مسطح snake_case زیر در سطح ریشه — کلاینت اندروید همه‌ی فیلدهای
            // آپدیت را از ریشه‌ی پاسخ می‌خواند، نه از 'updateInfo' تودرتو (S-1)؛ قبلاً
            // این فیلدها فقط زیر updateInfo بودند و کلاینت همیشه مقدار پیش‌فرض
            // خودش (پیام خالی، اولویت normal، forceUpdate=false و...) را می‌گرفت
            'update_priority' => $hasUpdate ? ($config['update_priority'] ?? 'normal') : null,
            'update_message' => $hasUpdate ? ($config['update_message'] ?? '') : null,
            'force_update' => $hasUpdate ? ($config['force_update'] ?? false) : null,
            'update_size' => $hasUpdate ? ($config['update_size'] ?? '0') : null,
            'release_date' => $hasUpdate ? ($config['release_date'] ?? '') : null,
            'version_constraints' => $hasUpdate ? [
                'min_android_version' => $config['version_constraints']['min_android_version'] ?? 21,
                'min_app_version' => $config['version_constraints']['min_app_version'] ?? '1.0',
                'excluded_versions' => $config['version_constraints']['excluded_versions'] ?? [],
            ] : null,
            // ساختار قدیمی تودرتو برای سازگاری با نسخه‌های نصب‌شده‌ی قدیمی‌تر کلاینت
            // که ممکن است هنوز از این ساختار بخوانند — حذف نشد، فقط دیگر تنها منبع نیست
            'updateInfo' => $hasUpdate ? [
                'priority' => $config['update_priority'] ?? 'normal',
                'message' => $config['update_message'] ?? '',
                'forceUpdate' => $config['force_update'] ?? false,
                'size' => $config['update_size'] ?? '0',
                'releaseDate' => $config['release_date'] ?? '',
                'minAndroidVersion' => $config['version_constraints']['min_android_version'] ?? 21,
            ] : null
        ];

        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit;
    }

    /**
     * همگام‌سازی دسترسی‌ها (sync_permissions.php)
     */
    public function syncPermissions(): void {
        header('Content-Type: application/json; charset=UTF-8');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('Cache-Control: no-store, no-cache, must-revalidate');

        if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
            $this->sendSyncResponse(false, 'Only POST method is allowed.', [], 405);
        }

        // قبلاً هویت را از SessionManager::isSessionActive($username, $deviceId)
        // می‌گرفت که فقط بررسی می‌کرد آیا نشستی برای این username/deviceId فعال
        // است، بدون بررسی X-Session-Token — و هیچ‌کدام از username/deviceId هم
        // سرّی نیستند (S-20). requireAuthenticatedSession همان گیت مبتنی‌بر
        // توکن است که بقیه‌ی endpointهای این کنترلر استفاده می‌کنند؛ userType
        // هم از همان نتیجه در دسترس است، پس کوئری جداگانه‌ی SELECT userType
        // هم دیگر لازم نیست.
        $this->requireAuthenticatedSession();

        try {
            $userType = (string)$this->authenticatedUserType;
            // منطق خواندن/کش permissions.json دیگر اینجا تکرار نمی‌شود — همان
            // PermissionService::getUserPermissions مورد استفاده‌ی
            // AuthController/AppApiController، با همان کش (MicroCache،
            // DEEP_CODE_AUDIT.md #Phase2.7).
            $userPermissions = (new PermissionService())->getUserPermissions(
                (string)$this->authenticatedUsername,
                $userType
            );

            $this->sendSyncResponse(true, 'Permissions synced successfully.', [
                'userType' => $userType,
                'permissions' => $userPermissions,
            ]);
        } catch (Exception $e) {
            $this->logger->error("Sync permissions error: " . $e->getMessage());
            $this->sendSyncResponse(false, 'خطای داخلی سرور.', [], 500);
        }
    }

    private function sendSyncResponse(bool $success, string $message, array $extra = [], int $code = 200): void {
        Response::json(array_merge(['success' => $success, 'message' => $message], $extra), $code);
    }
}
