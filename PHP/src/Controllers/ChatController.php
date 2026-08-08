<?php
// PHP/src/Controllers/ChatController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use mysqli;
use mysqli_stmt;
use App\Core\Database;
use App\Core\Logger;
use App\Core\Request;

class ChatController {
    private mysqli $conn;
    private Logger $logger;
    private Request $request;
    private const MAX_MESSAGE_LENGTH = 1000;
    private const MESSAGE_FETCH_LIMIT = 100;

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->logger = Logger::getInstance();
        $this->request = new Request();
    }

    public function handleChatRequest(): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        try {
            if ($this->request->isGet()) {
                $action = (string)$this->request->get('action', '');
                $username = (string)$this->request->get('username', '');

                if ($action === 'getMessages') {
                    $lastMessageId = (int)$this->request->get('lastMessageId', 0);
                    $olderThanId = (int)$this->request->get('olderThanId', 0);
                    $limit = (int)$this->request->get('limit', self::MESSAGE_FETCH_LIMIT);

                    $this->sendJsonResponse([
                        'success' => true,
                        'messages' => $this->getMessages($lastMessageId, $olderThanId, $limit, $username)
                    ]);
                } elseif ($action === 'getUnreadCount') {
                    $this->sendJsonResponse([
                        'success' => true,
                        'unreadCount' => $this->getUnreadCount($username)
                    ]);
                } else {
                    throw new Exception('Invalid Action');
                }
            } elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
                $input = json_decode((string)file_get_contents('php://input'), true);
                $action = $input['action'] ?? '';
                $username = $input['username'] ?? '';

                switch ($action) {
                    case 'sendMessage':
                        $this->sendJsonResponse($this->sendMessage($username, $input['message'] ?? ''));
                        break;
                    case 'editMessage':
                        $this->sendJsonResponse($this->editMessage((int)($input['messageId'] ?? 0), $username, $input['message'] ?? ''));
                        break;
                    case 'deleteMessage':
                        $this->sendJsonResponse($this->deleteMessage((int)($input['messageId'] ?? 0), $username));
                        break;
                    case 'markAsRead':
                        $this->sendJsonResponse($this->markAsRead((int)($input['messageId'] ?? 0), $username));
                        break;
                    default:
                        throw new Exception('Invalid Action');
                }
            }
        } catch (Exception $e) {
            http_response_code(400);
            echo json_encode(['success' => false, 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    private function getMessages(int $lastMessageId = 0, int $olderThanId = 0, int $limit = self::MESSAGE_FETCH_LIMIT, string $username = ''): array {
        if (!empty($username) && !$this->isAdmin($username)) {
            throw new Exception('فقط ادمین‌ها می‌توانند پیام‌ها را مشاهده کنند');
        }

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
            $stmt->close();
            return array_reverse($results);
        } elseif ($lastMessageId > 0) {
            $query = "SELECT $baseFields $join WHERE c.id > ? AND $whereAdmin ORDER BY c.id ASC";
            $stmt = $this->prepareAndExecute($query, 'si', $username, $lastMessageId);
            $results = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
            $stmt->close();
            return $results;
        } else {
            $query = "SELECT $baseFields $join WHERE $whereAdmin ORDER BY c.id DESC LIMIT ?";
            $stmt = $this->prepareAndExecute($query, 'si', $username, $limit);
            $results = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
            $stmt->close();
            return array_reverse($results);
        }
    }

    private function sendMessage(string $username, string $message): array {
        if (!$this->isAdmin($username)) {
            return ['success' => false, 'message' => 'فقط ادمین‌ها می‌توانند پیام ارسال کنند'];
        }

        $message = $this->sanitizeInput($message);
        if (empty($message)) {
            return ['success' => false, 'message' => 'پیام نمی‌تواند خالی باشد'];
        }

        if (mb_strlen($message) > self::MAX_MESSAGE_LENGTH) {
            return ['success' => false, 'message' => 'پیام بیش از حد طولانی است'];
        }

        $query = "INSERT INTO admin_chat_messages (username, message) VALUES (?, ?)";
        
        try {
            $stmt = $this->prepareAndExecute($query, 'ss', $username, $message);
            $messageId = $stmt->insert_id;
            $stmt->close();

            $stmt2 = $this->prepareAndExecute("INSERT INTO admin_chat_reads (message_id, username) VALUES (?, ?)", 'is', $messageId, $username);
            $stmt2->close();

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
            $this->logger->error("Error sending chat message: " . $e->getMessage());
            return ['success' => false, 'message' => 'خطا در ارسال پیام'];
        }
    }

    private function editMessage(int $messageId, string $username, string $newMessage): array {
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
            $stmt = $this->prepareAndExecute($query, 'si', $newMessage, $messageId);
            $stmt->close();
            return [
                'success' => true,
                'message' => 'پیام ویرایش شد',
                'updated_at' => date('Y-m-d H:i:s')
            ];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در ویرایش پیام'];
        }
    }

    private function deleteMessage(int $messageId, string $username): array {
        if (!$this->isAdmin($username)) {
            return ['success' => false, 'message' => 'دسترسی غیرمجاز'];
        }

        if (!$this->isMessageOwner($messageId, $username)) {
            return ['success' => false, 'message' => 'شما فقط می‌توانید پیام‌های خود را حذف کنید'];
        }

        $query = "UPDATE admin_chat_messages SET is_deleted = 1 WHERE id = ?";
        
        try {
            $stmt = $this->prepareAndExecute($query, 'i', $messageId);
            $stmt->close();
            return ['success' => true, 'message' => 'پیام حذف شد'];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا در حذف پیام'];
        }
    }

    private function markAsRead(int $messageId, string $username): array {
        if (!$this->isAdmin($username)) return ['success' => false, 'message' => 'دسترسی غیرمجاز'];
        
        try {
            $stmt = $this->prepareAndExecute("INSERT IGNORE INTO admin_chat_reads (message_id, username) VALUES (?, ?)", 'is', $messageId, $username);
            $stmt->close();
            return ['success' => true];
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'خطا'];
        }
    }

    private function getUnreadCount(string $username): int {
        if (!$this->isAdmin($username)) return 0;
        
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
            $stmt = $this->prepareAndExecute($query, 'ss', $username, $username);
            $result = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return (int)($result['count'] ?? 0);
        } catch (Exception $e) {
            return 0;
        }
    }

    private function isMessageOwner(int $messageId, string $username): bool {
        $query = "SELECT username FROM admin_chat_messages WHERE id = ? LIMIT 1";
        $stmt = $this->prepareAndExecute($query, 'i', $messageId);
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $result && $result['username'] === $username;
    }

    private function getUserFullName(string $username): string {
        $stmt = $this->prepareAndExecute("SELECT fullName FROM Users WHERE username = ? LIMIT 1", 's', $username);
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $result['fullName'] ?? $username;
    }

    private function isAdmin(string $username): bool {
        $stmt = $this->prepareAndExecute("SELECT userType FROM Users WHERE username = ? LIMIT 1", 's', $username);
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
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

    private function sendJsonResponse(array $data, int $statusCode = 200): void {
        http_response_code($statusCode);
        if (extension_loaded('zlib') && !ini_get('zlib.output_compression') && !in_array('ob_gzhandler', ob_list_handlers(), true)) {
            ob_start('ob_gzhandler');
        }
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
        exit;
    }
}
