<?php
// تنظیمات اولیه و هدرهای امنیتی
date_default_timezone_set('Asia/Tehran');
ini_set('display_errors', 0);
error_reporting(0);

// تنظیم هدرهای CORS و امنیتی
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: DENY');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'; script-src \'self\' https://cdn.jsdelivr.net; style-src \'self\' https://cdn.jsdelivr.net \'unsafe-inline\'; font-src \'self\' https://cdn.jsdelivr.net data:;');
header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
header('Referrer-Policy: strict-origin-when-cross-origin');
header('Permissions-Policy: geolocation=(), microphone=(), camera=()');

// پاسخ به درخواست OPTIONS (preflight)
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// اصلاح مسیر فایل config.php
require_once __DIR__ . '/../config/config.php';

// بررسی متد درخواست
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // فقط عملیات list برای متد GET مجاز است
    if (isset($_GET['action']) && $_GET['action'] === 'list') {
        try {
            $conn = getDbConnection();
            $stmt = $conn->prepare("SELECT * FROM licenses ORDER BY created_at DESC");
            if (!$stmt->execute()) {
                throw new Exception("خطا در اجرای کوئری");
            }
            
            $result = $stmt->get_result();
            $licenses = [];
            
            while ($row = $result->fetch_assoc()) {
                $licenses[] = [
                    'license_key' => htmlspecialchars($row['license_key']),
                    'company_name' => htmlspecialchars($row['company_name']),
                    'is_active' => (bool)$row['is_active'],
                    'created_at' => $row['created_at'],
                    'activation_date' => $row['activation_date'],
                    'last_check' => $row['last_check']
                ];
            }
            
            echo json_encode([
                'success' => true,
                'licenses' => $licenses
            ]);
            $conn->close();
            exit;
        } catch (Exception $e) {
            error_log("خطا در دریافت لیست لایسنس‌ها: " . $e->getMessage());
            echo json_encode([
                'success' => false,
                'message' => 'خطا در دریافت لیست لایسنس‌ها'
            ]);
            exit;
        }
    }
}

// برای سایر عملیات، فقط متد POST مجاز است
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'روش درخواست معتبر نیست']);
    exit;
}

// دریافت داده‌های POST
$postData = json_decode(file_get_contents('php://input'), true);
if ($postData) {
    $_POST = $postData;
}

// تابع تولید کلید لایسنس منحصر به فرد
function generateLicenseKey() {
    return strtoupper(bin2hex(random_bytes(16)));
}

function isLicenseKeyUnique($conn, $licenseKey) {
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM licenses WHERE license_key = ?");
    $stmt->bind_param("s", $licenseKey);
    $stmt->execute();
    $result = $stmt->get_result();
    $row = $result->fetch_assoc();
    return $row['count'] == 0;
}

function generateUniqueLicenseKey($conn) {
    $maxAttempts = 10;
    $attempt = 0;
    
    do {
        $licenseKey = generateLicenseKey();
        if (isLicenseKeyUnique($conn, $licenseKey)) {
            return $licenseKey;
        }
        $attempt++;
    } while ($attempt < $maxAttempts);
    
    throw new Exception("خطا در تولید کلید لایسنس یکتا");
}

$action = $_POST['action'] ?? '';
$conn = getDbConnection();

switch ($action) {
    case 'create':
        $companyName = trim($_POST['company_name'] ?? '');
        if (empty($companyName)) {
            echo json_encode(['success' => false, 'message' => 'نام شرکت الزامی است']);
            exit;
        }

        // بررسی تکراری نبودن نام شرکت
        $stmt = $conn->prepare("SELECT COUNT(*) as count FROM licenses WHERE company_name = ?");
        $stmt->bind_param("s", $companyName);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        
        if ($row['count'] > 0) {
            echo json_encode(['success' => false, 'message' => 'این نام شرکت قبلاً ثبت شده است']);
            exit;
        }

        try {
            $licenseKey = generateUniqueLicenseKey($conn);
            
            $stmt = $conn->prepare("INSERT INTO licenses (license_key, company_name) VALUES (?, ?)");
            $stmt->bind_param("ss", $licenseKey, $companyName);
            
            if ($stmt->execute()) {
                echo json_encode([
                    'success' => true,
                    'message' => 'لایسنس با موفقیت ایجاد شد',
                    'license_key' => $licenseKey
                ]);
            } else {
                error_log("خطا در ایجاد لایسنس: " . $stmt->error);
                echo json_encode(['success' => false, 'message' => 'خطا در ایجاد لایسنس']);
            }
        } catch (Exception $e) {
            error_log("خطای سیستمی در ایجاد لایسنس: " . $e->getMessage());
            echo json_encode(['success' => false, 'message' => 'خطای سیستمی رخ داده است']);
        }
        break;

    case 'delete':
        $licenseKey = $_POST['license_key'] ?? '';
        if (empty($licenseKey)) {
            echo json_encode(['success' => false, 'message' => 'کلید لایسنس الزامی است']);
            exit;
        }

        try {
            $stmt = $conn->prepare("DELETE FROM licenses WHERE license_key = ?");
            $stmt->bind_param("s", $licenseKey);
            
            if ($stmt->execute()) {
                if ($stmt->affected_rows > 0) {
                    echo json_encode(['success' => true, 'message' => 'لایسنس با موفقیت حذف شد']);
                } else {
                    echo json_encode(['success' => false, 'message' => 'لایسنس مورد نظر یافت نشد']);
                }
            } else {
                echo json_encode(['success' => false, 'message' => 'خطا در حذف لایسنس']);
            }
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => $e->getMessage()]);
        }
        break;

    case 'edit':
        $licenseKey = $_POST['license_key'] ?? '';
        $companyName = $_POST['company_name'] ?? '';
        if (empty($licenseKey) || empty($companyName)) {
            echo json_encode(['success' => false, 'message' => 'کلید لایسنس و نام شرکت الزامی است']);
            exit;
        }

        try {
            // ابتدا بررسی می‌کنیم که آیا لایسنس وجود دارد
            $checkStmt = $conn->prepare("SELECT company_name FROM licenses WHERE license_key = ?");
            $checkStmt->bind_param("s", $licenseKey);
            $checkStmt->execute();
            $result = $checkStmt->get_result();

            if ($result->num_rows === 0) {
                echo json_encode(['success' => false, 'message' => 'لایسنس مورد نظر یافت نشد']);
                exit;
            }

            $currentData = $result->fetch_assoc();
            
            // اگر نام شرکت تغییر نکرده باشد
            if ($currentData['company_name'] === $companyName) {
                echo json_encode(['success' => true, 'message' => 'اطلاعات لایسنس بدون تغییر ذخیره شد']);
                exit;
            }

            $stmt = $conn->prepare("UPDATE licenses SET company_name = ? WHERE license_key = ?");
            $stmt->bind_param("ss", $companyName, $licenseKey);
            
            if ($stmt->execute()) {
                echo json_encode(['success' => true, 'message' => 'نام شرکت با موفقیت ویرایش شد']);
            } else {
                echo json_encode(['success' => false, 'message' => 'خطا در ویرایش نام شرکت']);
            }
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => $e->getMessage()]);
        }
        break;

    case 'toggle':
        $licenseKey = $_POST['license_key'] ?? '';
        if (empty($licenseKey)) {
            echo json_encode(['success' => false, 'message' => 'کلید لایسنس الزامی است']);
            exit;
        }

        try {
            $stmt = $conn->prepare("UPDATE licenses SET is_active = NOT is_active WHERE license_key = ?");
            $stmt->bind_param("s", $licenseKey);
            
            if ($stmt->execute()) {
                echo json_encode(['success' => true, 'message' => 'وضعیت لایسنس با موفقیت تغییر کرد']);
            } else {
                echo json_encode(['success' => false, 'message' => 'خطا در تغییر وضعیت لایسنس']);
            }
        } catch (Exception $e) {
            echo json_encode(['success' => false, 'message' => $e->getMessage()]);
        }
        break;

    default:
        echo json_encode(['success' => false, 'message' => 'عملیات نامعتبر']);
}

$conn->close();