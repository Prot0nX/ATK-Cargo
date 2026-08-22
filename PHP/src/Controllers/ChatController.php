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
use App\Core\Response;
use App\Exceptions\ApiException;
use App\Validators\InputValidator;

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

    // $username از Router::dispatch (auth=>true) می‌آید — هویت همیشه از نشست احرازشده گرفته می‌شود، نه از پارامتر
    // ورودی که رازی نیست و قابل جعل بود (S-03، DEEP_CODE_AUDIT.md فاز۳ #۲۵)
    public function handleChatRequest(?string $username): void {
        header('Content-Type: application/json; charset=UTF-8');
        date_default_timezone_set('Asia/Tehran');

        try {
            $username = (string)$username;

            if ($this->request->isGet()) {
                $action = (string)$this->request->get('action', '');

                if ($action === 'getMessages') {
                    $lastMessageId = (int)$this->request->get('lastMessageId', 0);
                    $olderThanId = (int)$this->request->get('olderThanId', 0);
                    // کلمپ limit به MESSAGE_FETCH_LIMIT تا subquery همبسته‌ی read_by_names روی بازه‌ی بزرگ مشغول نماند (Phase1.10)
                    $limit = max(1, min((int)$this->request->get('limit', self::MESSAGE_FETCH_LIMIT), self::MESSAGE_FETCH_LIMIT));

                    Response::json([
                        'success' => true,
                        'messages' => $this->getMessages($lastMessageId, $olderThanId, $limit, $username)
                    ]);
                } else {
                    throw new ApiException('عملیات نامعتبر است', 400);
                }
            } elseif ($this->request->isWrite()) {
                // isWrite() هر سه فعل POST/PATCH/DELETE را پوشش می‌دهد و از همان Request::get() استفاده می‌کند، هم‌راستا با شاخه‌ی GET (Phase4 #33)
                $action = (string)$this->request->get('action', '');

                switch ($action) {
                    case 'sendMessage':
                        Response::json($this->sendMessage($username, (string)$this->request->get('message', '')));
                        break;
                    case 'editMessage':
                        Response::json($this->editMessage((int)$this->request->get('messageId', 0), $username, (string)$this->request->get('message', '')));
                        break;
                    case 'deleteMessage':
                        Response::json($this->deleteMessage((int)$this->request->get('messageId', 0), $username));
                        break;
                    default:
                        throw new ApiException('عملیات نامعتبر است', 400);
                }
            }
        } catch (ApiException $e) {
            http_response_code($e->getStatusCode());
            echo json_encode(['success' => false, 'message' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
            exit;
        } catch (\Throwable $e) {
            // فقط ApiException به کلاینت می‌رود؛ بقیه‌ی خطاها فقط لاگ می‌شوند تا ساختار جدول/کوئری افشا نشود (Phase2.4)
            $this->logger->error('ChatController: ' . $e->getMessage());
            http_response_code(500);
            echo json_encode(['success' => false, 'message' => 'خطای داخلی سرور رخ داده است.'], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    private function getMessages(int $lastMessageId = 0, int $olderThanId = 0, int $limit = self::MESSAGE_FETCH_LIMIT, string $username = ''): array {
        if (!empty($username) && !$this->isAdmin($username)) {
            throw new ApiException('فقط ادمین‌ها می‌توانند پیام‌ها را مشاهده کنند', 403);
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
            // کد ۲۰۰ فقط برای کلاینت‌های قدیمی؛ هم‌راستا با ۴۰۳ صریح getMessages برای همین شرط (DEEP_CODE_AUDIT.md فاز۳ #۲۸)
            Response::versionGatedJson(['success' => false, 'message' => 'فقط ادمین‌ها می‌توانند پیام ارسال کنند'], 200, 403);
        }

        $message = InputValidator::sanitize($message);
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
            Response::versionGatedJson(['success' => false, 'message' => 'دسترسی غیرمجاز'], 200, 403);
        }

        if (!$this->isMessageOwner($messageId, $username)) {
            Response::versionGatedJson(['success' => false, 'message' => 'شما فقط می‌توانید پیام‌های خود را ویرایش کنید'], 200, 403);
        }

        $newMessage = InputValidator::sanitize($newMessage);
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
            Response::versionGatedJson(['success' => false, 'message' => 'دسترسی غیرمجاز'], 200, 403);
        }

        if (!$this->isMessageOwner($messageId, $username)) {
            Response::versionGatedJson(['success' => false, 'message' => 'شما فقط می‌توانید پیام‌های خود را حذف کنید'], 200, 403);
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

    private function prepareAndExecute(string $query, string $types, ...$params): mysqli_stmt {
        $stmt = $this->conn->prepare($query);
        if (!$stmt) throw new Exception('SQL Error: ' . $this->conn->error);
        if (!empty($types)) $stmt->bind_param($types, ...$params);
        if (!$stmt->execute()) throw new Exception('SQL Exec Error: ' . $stmt->error);
        return $stmt;
    }

}
