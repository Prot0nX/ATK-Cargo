<?php
// PHP/src/Controllers/UtilityController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use InvalidArgumentException;
use mysqli;
use App\Core\Database;
use App\Core\Logger;
use App\Core\MicroCache;
use App\Core\Request;
use SessionManager;

class UtilityController {
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
            $this->sendJsonResponse(['error' => 'روش درخواست غیرمجاز'], 405);
        }

        $contentType = $_SERVER['CONTENT_TYPE'] ?? '';
        if (strpos($contentType, 'application/json') === false) {
            $this->sendJsonResponse(['error' => 'نوع محتوای نامعتبر'], 400);
        }

        try {
            $rawInput = file_get_contents('php://input');
            if (empty($rawInput)) {
                $this->sendJsonResponse(['error' => 'بدنه درخواست خالی است'], 400);
            }

            $input = json_decode($rawInput, true);
            if (json_last_error() !== JSON_ERROR_NONE || !is_array($input)) {
                $this->sendJsonResponse(['error' => 'فرمت JSON نامعتبر'], 400);
            }

            if (!isset($input['app_signature']) || !is_string($input['app_signature'])) {
                $this->sendJsonResponse(['error' => 'امضای برنامه ارسال نشده است'], 400);
            }

            $receivedSignature = trim($input['app_signature']);
            if (strlen($receivedSignature) !== 64 || !ctype_xdigit($receivedSignature)) {
                $this->sendJsonResponse(['error' => 'فرمت امضای نامعتبر'], 400);
            }

            $packageName = $input['app_package'] ?? null;
            if ($packageName !== null && !preg_match('/^[a-zA-Z][a-zA-Z0-9_]*(?:\.[a-zA-Z][a-zA-Z0-9_]*)*$/', (string)$packageName)) {
                $this->sendJsonResponse(['error' => 'نام بسته نامعتبر'], 400);
            }

            $stmt = $this->conn->prepare("SELECT 1 FROM SignChecker WHERE app_signature = ? LIMIT 1");
            $stmt->bind_param("s", $receivedSignature);
            $stmt->execute();
            $result = $stmt->get_result();
            $isValid = $result->num_rows > 0;
            $stmt->close();

            $this->sendJsonResponse(['is_valid' => $isValid]);
        } catch (Exception $e) {
            $this->logger->error("Signature check error: " . $e->getMessage());
            $this->sendJsonResponse(['error' => 'خطای سرور رخ داده است'], 500);
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

        $receivedPassword = isset($_POST['password']) ? trim((string)$_POST['password']) : '';
        $passwordType = isset($_POST['passwordType']) ? trim((string)$_POST['passwordType']) : '';

        $response = [
            'success' => false,
            'message' => 'رمز عبور اشتباه است!'
        ];

        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }

        if (!isset($_SESSION['attempt_count'])) {
            $_SESSION['attempt_count'] = 0;
        }

        if ($_SESSION['attempt_count'] >= 5) {
            http_response_code(429);
            echo json_encode(['success' => false, 'message' => 'تعداد تلاش‌های ناموفق بیش از حد مجاز است. لطفا بعدا تلاش کنید.']);
            exit;
        }

        try {
            $stmt = $this->conn->prepare("SELECT password FROM Passwords WHERE passwordType = ?");
            $stmt->bind_param("s", $passwordType);
            $stmt->execute();
            $result = $stmt->get_result();

            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                if (password_verify($receivedPassword, $row['password'])) {
                    $response['success'] = true;
                    $response['message'] = 'رمز عبور صحیح است!';
                    $_SESSION['attempt_count'] = 0;
                } else {
                    $_SESSION['attempt_count']++;
                }
            } else {
                $_SESSION['attempt_count']++;
            }
            $stmt->close();
        } catch (Exception $e) {
            $this->logger->error("Check password error: " . $e->getMessage());
            $response['message'] = 'خطایی رخ داده است. لطفا بعدا تلاش کنید.';
        }

        echo json_encode($response, JSON_UNESCAPED_UNICODE);
        exit;
    }

    /**
     * بررسی وجود اطلاعات (checkExistence.php)
     */
    public function checkExistence(): void {
        header('Content-Type: application/json; charset=UTF-8');

        try {
            $input = file_get_contents('php://input');
            $data = json_decode((string)$input, true);

            if (!$data || !isset($data['loadingQuotaNumber']) || !is_int($data['loadingQuotaNumber']) ||
                !isset($data['shipName']) || !isset($data['loadingWarehouse']) || 
                !isset($data['cargoType']) || !isset($data['shippingCompany'])) {
                $this->sendJsonResponse(["status" => "error", "message" => "داده‌های ورودی نامعتبر است"]);
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
                $this->sendJsonResponse(["status" => "exists", "message" => "اطلاعات وارد شده قبلاً ثبت شده است."]);
            } else {
                $stmt->close();
                $stmtPartial = $this->conn->prepare("SELECT id FROM InitialInfo WHERE loadingQuotaNumber = ?");
                $stmtPartial->bind_param("i", $loadingQuotaNumber);
                $stmtPartial->execute();
                $resultPartial = $stmtPartial->get_result();

                if ($resultPartial->num_rows > 0) {
                    $stmtPartial->close();
                    $this->sendJsonResponse(["status" => "partial_match", "message" => "شماره کوتاژ قبلاً ثبت شده، اما با مشخصات متفاوت. ثبت اطلاعات جدید مجاز است."]);
                } else {
                    $stmtPartial->close();
                    $this->sendJsonResponse(["status" => "not_exists", "message" => "اطلاعات وارد شده قابل ثبت است."]);
                }
            }
        } catch (Exception $e) {
            $this->logger->error("Check existence error: " . $e->getMessage());
            $this->sendJsonResponse(["status" => "error", "message" => $e->getMessage()]);
        }
    }

    /**
     * بررسی نسخه جدید اپلیکیشن (check_update.php)
     */
    public function checkUpdate(): void {
        header('Content-Type: application/json');

        $apiKey = (string)$this->request->get('api_key', '');
        if ($apiKey !== 'atk_nk_9290VV42-38XQ02DI-F2WY4L2K-EJA7V682') {
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

        $response = [
            'hasUpdate' => $hasUpdate,
            'latestVersion' => $config['latest_version'],
            'downloadUrl' => $hasUpdate ? $config['download_url'] : '',
            'changeLog' => $hasUpdate ? $config['change_log'] : [],
            'minRequiredVersion' => $config['min_required_version'],
            'minAllowedVersion' => $config['min_allowed_version'] ?? $config['min_required_version'],
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

        $input = json_decode((string)file_get_contents('php://input'), true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($input)) {
            $this->sendSyncResponse(false, 'Invalid JSON payload.', [], 400);
        }

        $username = isset($input['username']) ? trim((string)$input['username']) : '';
        $deviceId = isset($input['deviceId']) ? trim((string)$input['deviceId']) : '';

        if (empty($username)) {
            $this->sendSyncResponse(false, 'نام کاربری الزامی است.', [], 400);
        }

        try {
            if (class_exists('SessionManager')) {
                $sessionManager = new SessionManager();
                if (!$sessionManager->isSessionActive($username, $deviceId)) {
                    $this->sendSyncResponse(false, 'نشست کاربر منقضی شده است.', [], 401);
                }
            }

            $stmt = $this->conn->prepare("SELECT userType FROM Users WHERE username = ? LIMIT 1");
            $stmt->bind_param('s', $username);
            $stmt->execute();
            $row = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            if (!$row) {
                $this->sendSyncResponse(false, 'کاربر یافت نشد.', [], 404);
            }

            $userType = $row['userType'];
            $permissions_file = APP_ROOT . '/config/permissions.json';
            $userPermissions = [];

            // محتوای permissions.json برای همه‌ی کاربران یکسان است؛ خواندن و پارس آن
            // به مدت کوتاهی کش می‌شود تا روی هر sync دوباره از دیسک خوانده نشود.
            $allData = MicroCache::remember('permissions_file_data', 15, function () use ($permissions_file) {
                if (file_exists($permissions_file)) {
                    return json_decode((string)file_get_contents($permissions_file), true) ?: [];
                }
                return [];
            });

            if (isset($allData['roles'])) {
                if (isset($allData['users'][$username])) {
                    $userPermissions = $allData['users'][$username];
                } elseif (isset($allData['roles'][$userType])) {
                    $userPermissions = $allData['roles'][$userType];
                }
            } else {
                $userPermissions = $allData[$userType] ?? [];
            }

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
        http_response_code($code);
        if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
            ob_start('ob_gzhandler');
        }
        echo json_encode(array_merge(['success' => $success, 'message' => $message], $extra), JSON_UNESCAPED_UNICODE);
        exit;
    }

    private function sendJsonResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
            ob_start('ob_gzhandler');
        }
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }
}
