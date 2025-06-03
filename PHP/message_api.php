<?php
//message_api.php

declare(strict_types=1);
header('Content-Type: application/json');
require_once __DIR__ . '/config/config.php';

date_default_timezone_set('Asia/Tehran');

class MessageHandler {
    private mysqli $conn;

    public function __construct(mysqli $conn) {
        $this->conn = $conn;
        $this->validateTableStructure();
    }

    private function validateTableStructure(): void {
        try {
            // بررسی وجود جدول
            $tableCheck = $this->conn->query("SHOW TABLES LIKE 'Messages'");
            if ($tableCheck->num_rows === 0) {
                $this->createMessagesTable();
            }

            // بررسی وجود ستون readBy
            $query = "SHOW COLUMNS FROM Messages LIKE 'readBy'";
            $result = $this->conn->query($query);
            
            if ($result->num_rows === 0) {
                $alterQuery = "ALTER TABLE Messages ADD COLUMN readBy JSON NULL DEFAULT (JSON_ARRAY())";
                if (!$this->conn->query($alterQuery)) {
                    throw new Exception("خطا در اضافه کردن ستون readBy");
                }
            }
        } catch (Exception $e) {
            error_log("Table validation error: " . $e->getMessage());
            throw $e;
        }
    }

    private function createMessagesTable(): void {
        $query = "CREATE TABLE IF NOT EXISTS Messages (
            id INT AUTO_INCREMENT PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            body TEXT NOT NULL,
            recipients JSON NOT NULL,
            dateTime DATETIME NOT NULL,
            senderId INT NOT NULL,
            senderType VARCHAR(50) NOT NULL,
            readBy JSON NULL DEFAULT (JSON_ARRAY()),
            INDEX (dateTime)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        if (!$this->conn->query($query)) {
            throw new Exception("خطا در ایجاد جدول پیام‌ها");
        }
    }

    public function sendMessage(array $data): array {
        try {
            $requiredFields = ['title', 'body', 'recipients', 'senderId', 'senderType'];
            foreach ($requiredFields as $field) {
                if (!isset($data[$field]) || empty($data[$field])) {
                    throw new Exception("فیلد $field الزامی است");
                }
            }

            $title = $this->sanitizeInput($data['title']);
            $body = $this->sanitizeInput($data['body']);
            $recipients = json_encode($data['recipients'], JSON_UNESCAPED_UNICODE);
            $senderId = (int)$data['senderId'];
            $senderType = $this->sanitizeInput($data['senderType']);
            $dateTime = date('Y-m-d H:i:s');
            $readBy = json_encode([], JSON_UNESCAPED_UNICODE);

            $query = "INSERT INTO Messages (title, body, recipients, dateTime, senderId, senderType, readBy) 
                     VALUES (?, ?, ?, ?, ?, ?, ?)";

            $stmt = $this->conn->prepare($query);
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی کوئری: " . $this->conn->error);
            }

            $stmt->bind_param("ssssiss", $title, $body, $recipients, $dateTime, $senderId, $senderType, $readBy);
            
            if ($stmt->execute()) {
                return [
                    'success' => true,
                    'message' => 'پیام با موفقیت ارسال شد',
                    'messageId' => $this->conn->insert_id
                ];
            } else {
                throw new Exception("خطا در ارسال پیام: " . $stmt->error);
            }
        } catch (Exception $e) {
            error_log("Error in sendMessage: " . $e->getMessage());
            return [
                'success' => false,
                'message' => $e->getMessage()
            ];
        }
    }

    public function getNewMessages(string $userType, string $lastCheckTime): array {
        try {
            $dateTime = new DateTime($lastCheckTime);
            $formattedDateTime = $dateTime->format('Y-m-d H:i:s');

            $query = "SELECT id, title, body, dateTime, senderType, readBy
                     FROM Messages 
                     WHERE JSON_CONTAINS(recipients, ?)
                     AND dateTime > ? 
                     ORDER BY dateTime DESC";

            $userTypeJson = json_encode($userType);
            $stmt = $this->conn->prepare($query);
            
            if (!$stmt) {
                throw new Exception("خطا در آماده‌سازی کوئری: " . $this->conn->error);
            }

            $stmt->bind_param("ss", $userTypeJson, $formattedDateTime);
            
            if (!$stmt->execute()) {
                throw new Exception("خطا در اجرای کوئری: " . $stmt->error);
            }

            $result = $stmt->get_result();
            $messages = [];

            while ($row = $result->fetch_assoc()) {
                $row['readBy'] = json_decode($row['readBy'], true) ?? [];
                $messages[] = $row;
            }

            return $messages;
        } catch (Exception $e) {
            error_log("Error in getNewMessages: " . $e->getMessage());
            throw $e;
        }
    }

    public function markMessageAsRead(int $messageId, string $username): array {
        try {
            // بررسی وجود پیام
            $checkQuery = "SELECT id, readBy FROM Messages WHERE id = ?";
            $checkStmt = $this->conn->prepare($checkQuery);
            if (!$checkStmt) {
                throw new Exception("خطا در آماده‌سازی کوئری بررسی");
            }

            $checkStmt->bind_param("i", $messageId);
            $checkStmt->execute();
            $result = $checkStmt->get_result();
            
            if ($result->num_rows === 0) {
                return ['success' => false, 'message' => 'پیام یافت نشد'];
            }

            $row = $result->fetch_assoc();
            $readBy = json_decode($row['readBy'], true) ?? [];

            // بررسی اینکه آیا قبلاً خوانده شده است
            if (in_array($username, $readBy)) {
                return ['success' => true, 'message' => 'پیام قبلاً خوانده شده است'];
            }

            // اضافه کردن کاربر به لیست خوانندگان
            $readBy[] = $username;
            $readByJson = json_encode($readBy, JSON_UNESCAPED_UNICODE);

            $updateQuery = "UPDATE Messages SET readBy = ? WHERE id = ?";
            $updateStmt = $this->conn->prepare($updateQuery);
            if (!$updateStmt) {
                throw new Exception("خطا در آماده‌سازی کوئری به‌روزرسانی");
            }

            $updateStmt->bind_param("si", $readByJson, $messageId);
            
            if ($updateStmt->execute()) {
                return [
                    'success' => true,
                    'message' => 'وضعیت پیام با موفقیت به‌روز شد'
                ];
            } else {
                throw new Exception("خطا در به‌روزرسانی وضعیت پیام");
            }
        } catch (Exception $e) {
            error_log("Error in markMessageAsRead: " . $e->getMessage());
            return [
                'success' => false,
                'message' => $e->getMessage()
            ];
        }
    }

public function getAllMessages(string $username): array {
    try {
        // SQL برای دریافت تمام پیام‌های مربوط به نوع کاربر 
        $query = "SELECT m.*, 
                    (SELECT userType FROM Users WHERE username = ?) as currentUserType
                 FROM Messages m 
                 WHERE JSON_CONTAINS(m.recipients, CAST((
                    SELECT userType FROM Users WHERE username = ?
                 ) AS JSON))
                 ORDER BY m.dateTime DESC";
                 
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("Error in preparing query: " . $this->conn->error);
        }

        $stmt->bind_param("ss", $username, $username);
        
        if (!$stmt->execute()) {
            throw new Exception("Error executing query: " . $stmt->error);
        }

        $result = $stmt->get_result();
        $messages = [];

        while ($row = $result->fetch_assoc()) {
            // پردازش readBy برای هر پیام
            $row['readBy'] = json_decode($row['readBy'], true) ?? [];
            $row['recipients'] = json_decode($row['recipients'], true) ?? [];
            $messages[] = $row;
        }

        return $messages;
    } catch (Exception $e) {
        error_log("Error in getAllMessages: " . $e->getMessage());
        throw $e;
    }
}

    private function sanitizeInput(string $input): string {
        return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
    }
}

try {
    $conn = getDbConnection();
    if (!$conn) {
        throw new Exception("خطا در اتصال به پایگاه داده");
    }

    $handler = new MessageHandler($conn);
    
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $input = json_decode(file_get_contents('php://input'), true);
        if (json_last_error() !== JSON_ERROR_NONE) {
            throw new Exception('خطا در پردازش داده‌های ورودی JSON: ' . json_last_error_msg());
        }
        
        if (!isset($input['action'])) {
            throw new Exception('پارامتر action مشخص نشده است');
        }
        
        switch ($input['action']) {
            case 'sendMessage':
                $result = $handler->sendMessage($input);
                echo json_encode($result, JSON_UNESCAPED_UNICODE);
                break;
            
            case 'markAsRead':
                if (!isset($input['messageId'], $input['username'])) {
                    throw new Exception('پارامترهای ناقص برای markAsRead');
                }
                $result = $handler->markMessageAsRead((int)$input['messageId'], $input['username']);
                echo json_encode($result, JSON_UNESCAPED_UNICODE);
                break;

            default:
                throw new Exception('عملیات نامعتبر: ' . $input['action']);
        }
    } 
    elseif ($_SERVER['REQUEST_METHOD'] === 'GET') {
        if (!isset($_GET['action'])) {
            throw new Exception('پارامتر action مشخص نشده است');
        }
        
        switch ($_GET['action']) {
            case 'getNewMessages':
                if (!isset($_GET['userType'], $_GET['lastCheckTime'])) {
                    throw new Exception('پارامترهای ناقص برای getNewMessages');
                }
                $messages = $handler->getNewMessages($_GET['userType'], $_GET['lastCheckTime']);
                echo json_encode($messages, JSON_UNESCAPED_UNICODE);
                break;
				
	case 'getAllMessages':
		if (!isset($_GET['username'])) {
			throw new Exception('Username parameter is required');
		}
		$messages = $handler->getAllMessages($_GET['username']);
		echo json_encode($messages, JSON_UNESCAPED_UNICODE);
		break;

            default:
                throw new Exception('عملیات نامعتبر: ' . $_GET['action']);
        }
    } else {
        throw new Exception('متد درخواست نامعتبر است');
    }
} catch (Exception $e) {
    error_log("Error in message_api.php: " . $e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
} finally {
    if (isset($conn) && $conn instanceof mysqli) {
        $conn->close();
    }
}