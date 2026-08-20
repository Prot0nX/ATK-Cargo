<?php
// PHP/src/Controllers/ChatController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use mysqli;
use mysqli_stmt;
use App\Core\AuthenticatesRequests;
use App\Core\Database;
use App\Core\Logger;
use App\Core\MicroCache;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;
use App\Validators\InputValidator;

class ChatController {
    use AuthenticatesRequests;

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
            $this->requireAuthenticatedSession();
            // هویت همیشه از نشست احرازشده گرفته می‌شود، هرگز از پارامتر
            // ورودی username — قبلاً کل کنترل دسترسی چت (خواندن/ارسال/ویرایش/
            // حذف پیام‌های ادمین‌ها) روی همین پارامتر بنا شده بود که هیچ رازی
            // نیست و از users_api.php قابل استخراج بود؛ یعنی دانستن نام
            // کاربری یک ادمین برای جعل هویت کامل او در چت کافی بود (S-03).
            $username = (string)$this->authenticatedUsername;

            if ($this->request->isGet()) {
                $action = (string)$this->request->get('action', '');

                if ($action === 'getMessages') {
                    $lastMessageId = (int)$this->request->get('lastMessageId', 0);
                    $olderThanId = (int)$this->request->get('olderThanId', 0);
                    // MESSAGE_FETCH_LIMIT فقط پیش‌فرض بود نه سقف — یک کاربر
                    // می‌توانست limit دلخواه بزرگی بفرستد و subquery همبسته‌ی
                    // read_by_names را روی بازه‌ی بزرگ مشغول نگه دارد
                    // (DEEP_CODE_AUDIT.md #Phase1.10).
                    $limit = max(1, min((int)$this->request->get('limit', self::MESSAGE_FETCH_LIMIT), self::MESSAGE_FETCH_LIMIT));

                    Response::json([
                        'success' => true,
                        'messages' => $this->getMessages($lastMessageId, $olderThanId, $limit, $username)
                    ]);
                } elseif ($action === 'getUnreadCount') {
                    Response::json([
                        'success' => true,
                        'unreadCount' => $this->getUnreadCount($username)
                    ]);
                } else {
                    throw new ApiException('عملیات نامعتبر است', 400);
                }
            } elseif ($this->request->isWrite()) {
                // isWrite() نه فقط REQUEST_METHOD==='POST': این شاخه هم
                // sendMessage/markAsRead (POST) هم editMessage (PATCH) هم
                // deleteMessage (DELETE) را پوشش می‌دهد — تفکیک واقعی با
                // action انجام می‌شود (DEEP_CODE_REVIEW.md Phase4 #33).
                // قبلاً اینجا php://input جداگانه و مستقیم decode می‌شد (برخلاف
                // شاخه‌ی GET بالا که از Request::get() استفاده می‌کند)؛
                // Request::get() از قبل JSON body/POST/GET را به همین ترتیب
                // اولویت می‌خواند، پس این ناهماهنگی حذف شد — رفتار برای کلاینت
                // v1 (که همیشه JSON body می‌فرستد) دقیقاً یکسان می‌ماند، و
                // مسیرهای v2 (که ممکن است از query/route param هم استفاده
                // کنند) هم پشتیبانی می‌شوند.
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
                    case 'markAsRead':
                        Response::json($this->markAsRead((int)$this->request->get('messageId', 0), $username));
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
            // فقط ApiException (پیام‌های فارسی عمدی) به کلاینت می‌رود؛ بقیه
            // (مثل خطای خام mysqli در prepareAndExecute) فقط لاگ می‌شود تا
            // ساختار جدول/کوئری افشا نشود (DEEP_CODE_AUDIT.md #Phase2.4).
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
            return ['success' => false, 'message' => 'فقط ادمین‌ها می‌توانند پیام ارسال کنند'];
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
            return ['success' => false, 'message' => 'دسترسی غیرمجاز'];
        }

        if (!$this->isMessageOwner($messageId, $username)) {
            return ['success' => false, 'message' => 'شما فقط می‌توانید پیام‌های خود را ویرایش کنید'];
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

        // badge تعداد نخوانده معمولاً هر چند ثانیه poll می‌شود؛ کش کوتاه بار دیتابیس
        // را کم می‌کند بدون اینکه تأخیر محسوسی در نمایش badge ایجاد شود.
        return MicroCache::remember('chat_unread_' . $username, 4, function () use ($username) {
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
        });
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
