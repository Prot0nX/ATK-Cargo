<?php
// ===== CHAT API FOR ADMIN MESSAGING SYSTEM =====
// این API مدیریت پیام‌های چت داخلی ادمین‌ها را انجام می‌دهد
// به‌روزرسانی شده برای پشتیبانی از ویرایش، حذف و صفحه‌بندی

declare(strict_types=1);
header('Content-Type: application/json; charset=UTF-8');
require_once __DIR__ . '/config/config.php';

date_default_timezone_set('Asia/Tehran');

// ===== CONFIGURATION & GLOBALS =====
const MAX_MESSAGE_LENGTH = 1000;
const MESSAGE_FETCH_LIMIT = 100; // پیش‌فرض برای بارگذاری اولیه

// ===== CORE LOGIC / IMPLEMENTATION =====

class ChatManager {
    private mysqli $conn;


    public function __construct(mysqli $conn) {
        $this->conn = $conn;
        $this->ensureTablesExist();
    }

    private function ensureTablesExist(): void {
        $query = "CREATE TABLE IF NOT EXISTS admin_chat_reads (
            id INT AUTO_INCREMENT PRIMARY KEY,
            message_id INT NOT NULL,
            username VARCHAR(50) NOT NULL,
            read_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            UNIQUE KEY unique_read (message_id, username),
            FOREIGN KEY (message_id) REFERENCES admin_chat_messages(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        $this->conn->query($query);
    }

    /**
     * دریافت پیام‌ها (پشتیبانی از Polling و Pagination)
     */
    public function getMessages(int $lastMessageId = 0, int $olderThanId = 0, int $limit = MESSAGE_FETCH_LIMIT, string $username = ''): array {
        if (!empty($username) && !$this->isAdmin($username)) {
            throw new Exception('فقط ادمین‌ها می‌توانند پیام‌ها را مشاهده کنند');
        }

        // Subquery for converting read list to string
        $readByQuery = "
            SELECT GROUP_CONCAT(u2.fullName SEPARATOR ', ')
            FROM admin_chat_reads r 
            JOIN Users u2 ON r.username = u2.username 
            WHERE r.message_id = c.id AND r.username != c.username
        ";

        $baseFields = "
            c.id,
            c.username,
            u.fullName,
            c.message,
            DATE_FORMAT(c.created_at, '%Y-%m-%d %H:%i:%s') as timestamp,
            ($readByQuery) as read_by_names,
            EXISTS(SELECT 1 FROM admin_chat_reads r2 WHERE r2.message_id = c.id AND r2.username = ?) as is_read_by_me,
            c.is_deleted,
            DATE_FORMAT(c.updated_at, '%Y-%m-%d %H:%i:%s') as updated_at
        ";
        
        $join = "FROM admin_chat_messages c JOIN Users u ON c.username = u.username";
        $whereAdmin = "u.userType = 'admin'";

        if ($olderThanId > 0) {
            $query = "SELECT $baseFields $join WHERE c.id < ? AND $whereAdmin ORDER BY c.id DESC LIMIT ?";
            $stmt = $this->prepareAndExecute($query, 'sii', $username, $olderThanId, $limit);
            $results = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
            return array_reverse($results);
            
        } elseif ($lastMessageId > 0) {
            $query = "SELECT $baseFields $join WHERE c.id > ? AND $whereAdmin ORDER BY c.id ASC";
            $stmt = $this->prepareAndExecute($query, 'si', $username, $lastMessageId);
            return $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
            
        } else {
            $query = "SELECT $baseFields $join WHERE $whereAdmin ORDER BY c.id DESC LIMIT ?";
            $stmt = $this->prepareAndExecute($query, 'si', $username, $limit);
            $results = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
            return array_reverse($results);
        }
    }

    /**
     * ارسال پیام جدید
     */
    public function sendMessage(string $username, string $message): array {
        if (!$this->isAdmin($username)) {
            return ['success' => false, 'message' => 'فقط ادمین‌ها می‌توانند پیام ارسال کنند'];
        }

        $message = $this->sanitizeInput($message);
        if (empty($message)) {
            return ['success' => false, 'message' => 'پیام نمی‌تواند خالی باشد'];
        }

        if (strlen($message) > MAX_MESSAGE_LENGTH) {
            return ['success' => false, 'message' => 'پیام بیش از حد طولانی است'];
        }

        $query = "INSERT INTO admin_chat_messages (username, message) VALUES (?, ?)";
        
        try {
            $stmt = $this->prepareAndExecute($query, 'ss', $username, $message);
            $messageId = $stmt->insert_id;
            
            // Mark as read by sender automatically
            $this->prepareAndExecute("INSERT INTO admin_chat_reads (message_id, username) VALUES (?, ?)", 'is', $messageId, $username);
            
            $fullName = $this->getUserFullName($username);
            
            return [
                'success' => true,
                'message' => 'پیام ارسال شد',
                'messageData' => [
                    'id' => $messageId,
                    'username' => $username,
                    'fullName' => $fullName,
                    'message' => $message,
                    'timestamp' => date('Y-m-d H:i:s'),
                    'read_by_names' => null,
                    'is_read_by_me' => 1,
                    'is_deleted' => 0,
                    'updated_at' => null
                ]
            ];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در ارسال پیام: ' . $e->getMessage()];
        }
    }

    /**
     * ویرایش پیام
     */
    public function editMessage(int $messageId, string $username, string $newMessage): array {
        if (!$this->isAdmin($username)) {
            return ['success' => false, 'message' => 'دسترسی غیرمجاز'];
        }

        if (!$this->isMessageOwner($messageId, $username)) {
            return ['success' => false, 'message' => 'شما فقط می‌توانید پیام‌های خود را ویرایش کنید'];
        }

        $newMessage = $this->sanitizeInput($newMessage);
        if (empty($newMessage)) {
            return ['success' => false, 'message' => 'متن پیام نمی‌تواند خالی باشد'];
        }

        $query = "UPDATE admin_chat_messages SET message = ?, updated_at = NOW() WHERE id = ?";
        
        try {
            $this->prepareAndExecute($query, 'si', $newMessage, $messageId);
            return [
                'success' => true,
                'message' => 'پیام ویرایش شد',
                'updated_at' => date('Y-m-d H:i:s')
            ];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در ویرایش پیام'];
        }
    }

    /**
     * حذف پیام (Soft Delete)
     */
    public function deleteMessage(int $messageId, string $username): array {
        if (!$this->isAdmin($username)) {
            return ['success' => false, 'message' => 'دسترسی غیرمجاز'];
        }

        if (!$this->isMessageOwner($messageId, $username)) {
            return ['success' => false, 'message' => 'شما فقط می‌توانید پیام‌های خود را حذف کنید'];
        }

        $query = "UPDATE admin_chat_messages SET is_deleted = 1 WHERE id = ?";
        
        try {
            $this->prepareAndExecute($query, 'i', $messageId);
            return ['success' => true, 'message' => 'پیام حذف شد'];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در حذف پیام'];
        }
    }

    public function markAsRead(int $messageId, string $username): array {
        if (!$this->isAdmin($username)) return ['success' => false, 'message' => 'دسترسی غیرمجاز'];
        
        try {
            // Use INSERT IGNORE to prevent duplicate errors
            $this->prepareAndExecute("INSERT IGNORE INTO admin_chat_reads (message_id, username) VALUES (?, ?)", 'is', $messageId, $username);
            return ['success' => true];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا'];
        }
    }

    public function getUnreadCount(string $username): int {
        if (!$this->isAdmin($username)) return 0;
        
        // Count messages that exist but are NOT in the reads table for this user
        $query = "
            SELECT COUNT(*) as count 
            FROM admin_chat_messages c 
            WHERE c.username != ? 
            AND NOT EXISTS (
                SELECT 1 FROM admin_chat_reads r 
                WHERE r.message_id = c.id AND r.username = ?
            )
        ";
        try {
            $result = $this->prepareAndExecute($query, 'ss', $username, $username)->get_result()->fetch_assoc();
            return (int)($result['count'] ?? 0);
        } catch (Exception $e) {
            return 0;
        }
    }

    private function isMessageOwner(int $messageId, string $username): bool {
        $query = "SELECT username FROM admin_chat_messages WHERE id = ? LIMIT 1";
        $result = $this->prepareAndExecute($query, 'i', $messageId)->get_result()->fetch_assoc();
        return $result && $result['username'] === $username;
    }

    private function getUserFullName(string $username): string {
        $result = $this->prepareAndExecute("SELECT fullName FROM Users WHERE username = ? LIMIT 1", 's', $username)->get_result()->fetch_assoc();
        return $result['fullName'] ?? $username;
    }

    private function isAdmin(string $username): bool {
        $result = $this->prepareAndExecute("SELECT userType FROM Users WHERE username = ? LIMIT 1", 's', $username)->get_result()->fetch_assoc();
        return $result && $result['userType'] === 'admin';
    }

    private function sanitizeInput(string $input): string {
        return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
    }

    private function prepareAndExecute(string $query, string $types, ...$params): mysqli_stmt {
        $stmt = $this->conn->prepare($query);
        if (!$stmt) throw new Exception('SQL Error: ' . $this->conn->error);
        if (!empty($types)) $stmt->bind_param($types, ...$params);
        if (!$stmt->execute()) throw new Exception('SQL Exec Error: ' . $stmt->error);
        return $stmt;
    }
}

// ===== REQUEST HANDLING =====

function handleRequest(mysqli $conn): void {
    $manager = new ChatManager($conn);

    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        $action = $_GET['action'] ?? '';
        $username = $_GET['username'] ?? '';
        
        if ($action === 'getMessages') {
            $lastMessageId = (int)($_GET['lastMessageId'] ?? 0);
            $olderThanId = (int)($_GET['olderThanId'] ?? 0);
            $limit = (int)($_GET['limit'] ?? MESSAGE_FETCH_LIMIT);
            
            echo json_encode([
                'success' => true, 
                'messages' => $manager->getMessages($lastMessageId, $olderThanId, $limit, $username)
            ], JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE);
            
        } elseif ($action === 'getUnreadCount') {
            echo json_encode(['success' => true, 'unreadCount' => $manager->getUnreadCount($username)]);
        } else {
            throw new Exception('Invalid Action');
        }
    } 
    elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $input = json_decode(file_get_contents('php://input'), true);
        $action = $input['action'] ?? '';
        $username = $input['username'] ?? '';

        switch ($action) {
            case 'sendMessage':
                echo json_encode($manager->sendMessage($username, $input['message'] ?? ''), JSON_UNESCAPED_UNICODE);
                break;
            case 'editMessage':
                echo json_encode($manager->editMessage((int)($input['messageId'] ?? 0), $username, $input['message'] ?? ''), JSON_UNESCAPED_UNICODE);
                break;
            case 'deleteMessage':
                echo json_encode($manager->deleteMessage((int)($input['messageId'] ?? 0), $username), JSON_UNESCAPED_UNICODE);
                break;
            case 'markAsRead':
                echo json_encode($manager->markAsRead((int)($input['messageId'] ?? 0), $username), JSON_UNESCAPED_UNICODE);
                break;
            default:
                throw new Exception('Invalid Action');
        }
    }
}

try {
    $conn = getDbConnection();
    if ($conn) {
        handleRequest($conn);
        $conn->close();
    } else {
        throw new Exception("DB Connection Failed");
    }
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
?>

