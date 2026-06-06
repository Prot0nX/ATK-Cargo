<?php
//users_api.php

declare(strict_types=1);
header('Content-Type: application/json');
require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/User/SessionManager.php';

date_default_timezone_set('Asia/Tehran');

class UserManager {
    private mysqli $conn;

    public function __construct(mysqli $conn) {
        $this->conn = $conn;
    }

    public function getAllUsers(): array {
        $query = "SELECT id, username, fullName, userType, created_at, updated_at FROM Users ORDER BY created_at DESC";

        try {
            $result = $this->conn->query($query);
            return $result->fetch_all(MYSQLI_ASSOC);
        } catch (Exception $e) {
            throw new Exception('خطا در دریافت لیست کاربران: ' . $e->getMessage());
        }
    }

    public function createUser(array $data): array {
        // بررسی وجود فیلدهای ضروری
        if (!isset($data['username']) || !isset($data['password']) || !isset($data['userType']) || !isset($data['fullName'])) {
            return ['success' => false, 'message' => 'تمامی فیلدها الزامی هستند'];
        }

        // اعتبارسنجی نوع کاربر
        $validUserTypes = ['admin', 'operator', 'verifier'];
        if (!in_array($data['userType'], $validUserTypes)) {
            return ['success' => false, 'message' => 'نوع کاربر نامعتبر است'];
        }

        $username = $this->sanitizeInput($data['username']);
        $fullName = $this->sanitizeInput($data['fullName']); // اضافه کردن فیلد جدید
        $password = $data['password'];
        $userType = $this->sanitizeInput($data['userType']);

        // بررسی حداقل طول نام کاربری و نام کامل
        if (strlen($username) < 3) {
            return ['success' => false, 'message' => 'نام کاربری باید حداقل 3 کاراکتر باشد'];
        }
        if (strlen($fullName) < 3) {
            return ['success' => false, 'message' => 'نام و نام خانوادگی باید حداقل 3 کاراکتر باشد'];
        }

        // بررسی تکراری نبودن نام کاربری
        $checkQuery = "SELECT id FROM Users WHERE username = ?";
        $stmt = $this->prepareAndExecute($checkQuery, 's', $username);
        if ($stmt->get_result()->num_rows > 0) {
            return ['success' => false, 'message' => 'این نام کاربری قبلاً ثبت شده است'];
        }

        // درج کاربر جدید
        $query = "INSERT INTO Users (username, fullName, password, userType, created_at, updated_at) 
                  VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try {
            $stmt = $this->prepareAndExecute($query, 'ssss', $username, $fullName, $password, $userType);
            return [
                'success' => true,
                'message' => 'کاربر جدید با موفقیت ایجاد شد',
                'userId' => $stmt->insert_id
            ];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در ایجاد کاربر: ' . $e->getMessage()];
        }
    }

    public function updateUser(array $data): array {
        if (!isset($data['id'])) {
            return ['success' => false, 'message' => 'شناسه کاربر الزامی است'];
        }

        $id = (int)$data['id'];
        $updates = [];
        $params = [];
        $types = '';

        // دریافت نام کاربری فعلی برای خروج از جلسه
        $getUserQuery = "SELECT username FROM Users WHERE id = ?";
        $userStmt = $this->prepareAndExecute($getUserQuery, 'i', $id);
        $userResult = $userStmt->get_result();
        
        if ($userResult->num_rows === 0) {
            return ['success' => false, 'message' => 'کاربری با این شناسه یافت نشد'];
        }
        
        $currentUsername = $userResult->fetch_assoc()['username'];

        // بررسی تکراری نبودن نام کاربری در صورت تغییر
        if (isset($data['username'])) {
            $checkQuery = "SELECT id FROM Users WHERE username = ? AND id != ?";
            $stmt = $this->prepareAndExecute($checkQuery, 'si', $data['username'], $id);
            if ($stmt->get_result()->num_rows > 0) {
                return ['success' => false, 'message' => 'این نام کاربری قبلاً ثبت شده است'];
            }
            
            $updates[] = "username = ?";
            $params[] = $this->sanitizeInput($data['username']);
            $types .= 's';
        }

        // اضافه کردن بررسی fullName
        if (isset($data['fullName'])) {
            if (strlen($data['fullName']) < 3) {
                return ['success' => false, 'message' => 'نام و نام خانوادگی باید حداقل 3 کاراکتر باشد'];
            }
            $updates[] = "fullName = ?";
            $params[] = $this->sanitizeInput($data['fullName']);
            $types .= 's';
        }

        if (isset($data['password'])) {
            $updates[] = "password = ?";
            $params[] = $data['password'];
            $types .= 's';
        }

        if (isset($data['userType'])) {
            $validUserTypes = ['admin', 'operator', 'verifier'];
            if (!in_array($data['userType'], $validUserTypes)) {
                return ['success' => false, 'message' => 'نوع کاربر نامعتبر است'];
            }
            $updates[] = "userType = ?";
            $params[] = $this->sanitizeInput($data['userType']);
            $types .= 's';
        }

        if (empty($updates)) {
            return ['success' => false, 'message' => 'هیچ داده‌ای برای به‌روزرسانی ارائه نشده است'];
        }

        $updates[] = "updated_at = CURRENT_TIMESTAMP";
        $query = "UPDATE Users SET " . implode(", ", $updates) . " WHERE id = ?";
        
        $params[] = $id;
        $types .= 'i';

        try {
            $stmt = $this->prepareAndExecute($query, $types, ...$params);
            if ($stmt->affected_rows > 0) {
                // خروج کاربر از تمام جلسات فعال بعد از ویرایش موفق
                $this->logoutUserSessions($currentUsername);
                
                return ['success' => true, 'message' => 'اطلاعات کاربر با موفقیت به‌روزرسانی شد'];
            }
            return ['success' => false, 'message' => 'کاربری با این شناسه یافت نشد'];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در به‌روزرسانی اطلاعات کاربر: ' . $e->getMessage()];
        }
    }

    /**
     * خروج کاربر از تمام جلسات فعال
     */
    private function logoutUserSessions(string $username): void {
        try {
            $query = "UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE username = ? AND is_active = 1";
            $stmt = $this->conn->prepare($query);
            if ($stmt) {
                $stmt->bind_param('s', $username);
                $stmt->execute();
                $stmt->close();
            }
        } catch (Exception $e) {
            // لاگ خطا اما عدم توقف فرآیند اصلی
            error_log("خطا در خروج خودکار کاربر: " . $e->getMessage());
        }
    }

    /**
     * خروج اجباری کاربر از تمام دستگاه‌ها
     */
    public function forceLogout(string $username, string $deviceId): array {
        try {
            if (empty($username)) {
                return ['success' => false, 'message' => 'نام کاربری الزامی است'];
            }

            $sessionManager = new SessionManager();

            if (!empty($deviceId)) {
                // خروج از دستگاه خاص
                $result = $sessionManager->forceLogoutFromDevice($username, $deviceId);
            } else {
                // خروج از تمام دستگاه‌ها
                $result = $sessionManager->deactivateSession($username);
            }

            return $result;
        } catch (Exception $e) {
            error_log("خطا در خروج اجباری کاربر: " . $e->getMessage());
            return ['success' => false, 'message' => 'خطا در خروج اجباری: ' . $e->getMessage()];
        }
    }

    public function deleteUser(int $userId): array {
        // بررسی وجود کاربر قبل از حذف
        $checkQuery = "SELECT userType FROM Users WHERE id = ?";
        $stmt = $this->prepareAndExecute($checkQuery, 'i', $userId);
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            return ['success' => false, 'message' => 'کاربری با این شناسه یافت نشد'];
        }

        $userType = $result->fetch_assoc()['userType'];

        // بررسی تعداد مدیران سیستم
        if ($userType === 'admin') {
            $adminCountQuery = "SELECT COUNT(*) as count FROM Users WHERE userType = 'admin'";
            $countResult = $this->conn->query($adminCountQuery);
            $adminCount = $countResult->fetch_assoc()['count'];
            
            if ($adminCount <= 1) {
                return ['success' => false, 'message' => 'حذف آخرین مدیر سیستم امکان‌پذیر نیست'];
            }
        }

        $query = "DELETE FROM Users WHERE id = ?";

        try {
            $stmt = $this->prepareAndExecute($query, 'i', $userId);
            if ($stmt->affected_rows > 0) {
                return ['success' => true, 'message' => 'کاربر با موفقیت حذف شد'];
            }
            return ['success' => false, 'message' => 'خطا در حذف کاربر'];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در حذف کاربر: ' . $e->getMessage()];
        }
    }

    private function sanitizeInput(string $input): string {
        return htmlspecialchars(strip_tags($input), ENT_QUOTES, 'UTF-8');
    }

    private function prepareAndExecute(string $query, string $types, ...$params): mysqli_stmt {
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception('خطا در آماده‌سازی دستور SQL');
        }
        $stmt->bind_param($types, ...$params);
        if (!$stmt->execute()) {
            throw new Exception('خطا در اجرای دستور SQL');
        }
        return $stmt;
    }
}

function handleRequest(mysqli $conn): void {
    $manager = new UserManager($conn);

    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        if (!isset($_GET['action'])) {
            throw new Exception('پارامتر action مورد نیاز است');
        }
        if ($_GET['action'] === 'getAllUsers') {
            $users = $manager->getAllUsers();
            echo json_encode($users, JSON_THROW_ON_ERROR);
        } else {
            throw new Exception('عملیات نامعتبر');
        }
    } elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $input = json_decode(file_get_contents('php://input'), true);
        if (!isset($input['action'])) {
            throw new Exception('پارامتر action مورد نیاز است');
        }
        
        switch ($input['action']) {
            case 'createUser':
                $result = $manager->createUser($input);
                echo json_encode($result, JSON_THROW_ON_ERROR);
                break;

            case 'updateUser':
                $result = $manager->updateUser($input);
                echo json_encode($result, JSON_THROW_ON_ERROR);
                break;

            case 'deleteUser':
                if (!isset($input['userId'])) {
                    throw new Exception('شناسه کاربر مورد نیاز است');
                }
                $result = $manager->deleteUser((int)$input['userId']);
                echo json_encode($result, JSON_THROW_ON_ERROR);
                break;

            case 'forceLogout':
                $username = $input['username'] ?? '';
                $deviceId = $input['device_id'] ?? '';
                if (empty($username)) {
                    throw new Exception('نام کاربری الزامی است');
                }
                $result = $manager->forceLogout($username, $deviceId);
                echo json_encode($result, JSON_THROW_ON_ERROR);
                break;

            default:
                throw new Exception('عملیات نامعتبر');
        }
    } else {
        throw new Exception('روش درخواست نامعتبر');
    }
}

try {
    $conn = getDbConnection();
    if (!$conn) {
        throw new Exception("خطا در اتصال به پایگاه داده");
    }
    handleRequest($conn);
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => $e->getMessage()], JSON_THROW_ON_ERROR);
} finally {
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->close();
    }
}
?>