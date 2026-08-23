<?php
// PHP/src/Services/UserService.php

declare(strict_types=1);

namespace App\Services;

use App\Repositories\UserRepository;
use App\Exceptions\ApiException;

class UserService {
    private UserRepository $userRepository;

    public function __construct() {
        $this->userRepository = new UserRepository();
    }

 // بررسی اعتبار کاربر با استراتژی هشینگ دومسیره: هم bcrypt(رمز خام) و هم bcrypt(SHA-256(رمز)) برای دستگاه‌های قدیمی امتحان می‌شود و هش با موفقیت بی‌صدا ارتقا می‌یابد
    public function verifyCredentials(string $username, string $password): ?array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return null;
        }

        $storedPassword = $user['password'];

 // ===== حالت ۱: bcrypt(رمز خام) — طرح جدید =====
        if (password_verify($password, $storedPassword)) {
            if (password_needs_rehash($storedPassword, PASSWORD_BCRYPT, ['cost' => 12])) {
                $this->userRepository->updatePassword($user['id'], password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]));
            }
            return $user;
        }

 // ===== حالت ۲: bcrypt(SHA-256(رمز)) — طرح قبلی، کلاینت هنوز آپدیت نشده =====
        if (password_verify(hash('sha256', $password), $storedPassword)) {
            $this->userRepository->updatePassword($user['id'], password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]));
            return $user;
        }

        return null;
    }

    public function getAllUsers(): array {
        return $this->userRepository->getAll();
    }

 // پروفایل خودِ کاربر احرازشده، بدون افشای کل فهرست کاربران به کاربرانی که مجوز manage_users ندارند
    public function getSelfProfile(string $username): ?array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return null;
        }
        unset($user['password']);
        return $user;
    }

 // فهرست کاربران admin، برای پرکردن مقصدهای «چت با مدیر» در دسترس هر کاربر احرازشده
    public function getAdminUsers(): array {
        return $this->userRepository->getByUserType('admin');
    }

 // دریافت تمامی کاربران به همراه وضعیت آنلاین (فعالیت در ۵ دقیقه اخیر) و آخرین فعالیت
    public function getAllUsersWithStatus(): array {
        $users = $this->userRepository->getAll();
        $sessionRepo = new \App\Repositories\SessionRepository();
        $latestSessions = $sessionRepo->getLatestSessionsForAllUsers();

 // ساخت Map از strtolower(username) → session برای تطبیق دقیق و Case-Insensitive
        $sessionMap = [];
        foreach ($latestSessions as $session) {
            if (!empty($session['username'])) {
                $sessionMap[strtolower((string)$session['username'])] = $session;
            }
        }

        $result = [];
        foreach ($users as $user) {
            $usernameKey = strtolower((string)$user['username']);
            $session     = $sessionMap[$usernameKey] ?? null;
            $isActive    = $session ? ((int)($session['is_active'] ?? 0) === 1) : false;
            $idleSeconds = $session ? (int)($session['idle_time'] ?? PHP_INT_MAX) : PHP_INT_MAX;

 // آنلاین بودن: داشتن نشست فعال + فعالیت در ۵ دقیقه اخیر (۳۰۰ ثانیه)
            $isOnline    = $isActive && ($idleSeconds <= 300);
            $idleMinutes = ($session && $idleSeconds < PHP_INT_MAX) ? (int)floor($idleSeconds / 60) : null;

 // استخراج تاریخ آخرین بازدید با فرمت شمسی در صورت امکان
            $rawLastActivity = $session['last_activity'] ?? $session['login_time'] ?? $user['updatedAt'] ?? $user['createdAt'] ?? null;
            $lastActivityFormatted = null;
            if (!empty($rawLastActivity)) {
                $timestamp = strtotime((string)$rawLastActivity);
                if ($timestamp > 0) {
                    if (function_exists('jdate')) {
                        $lastActivityFormatted = jdate('Y/m/d H:i', $timestamp);
                    } else {
                        $lastActivityFormatted = date('Y/m/d H:i', $timestamp);
                    }
                } else {
                    $lastActivityFormatted = (string)$rawLastActivity;
                }
            }

            $result[] = array_merge($user, [
                'is_online'     => $isOnline,
                'last_activity' => $lastActivityFormatted,
                'idle_minutes'  => $idleMinutes,
                'login_time'    => $session['login_time'] ?? null,
                'device_model'  => $session['device_model'] ?? null,
            ]);
        }

        return $result;
    }

    public function createUser(array $data, ?string $actorUsername = null): array {
 // فیلتر کردن و اعتبارسنجی مقادیر
        $username = trim($data['username']);
        $fullName = trim($data['fullName']);
        $password = $data['password'];
        $userType = trim($data['userType']);

 // بررسی تکراری نبودن نام کاربری
        if ($this->userRepository->getByUsername($username) !== null) {
            throw new ApiException('این نام کاربری قبلاً ثبت شده است', 400);
        }

 // ذخیره پسورد با bcrypt؛ ورودی از پنل ادمین به‌صورت متن خام دریافت می‌شود
        $hashedPassword = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

        $userId = $this->userRepository->create([
            'username' => $username,
            'fullName' => $fullName,
            'password' => $hashedPassword,
            'userType' => $userType
        ]);

        if ($actorUsername !== null) {
            AuditLogger::log($actorUsername, 'createUser', 'user', (string)$userId, [
                'username' => $username,
                'fullName' => $fullName,
                'userType' => $userType,
            ]);
        }

        return [
            'success' => true,
            'message' => 'کاربر جدید با موفقیت ایجاد شد',
            'userId' => $userId
        ];
    }

    public function updateUser(int $id, array $data, ?string $actorUsername = null): array {
        $user = $this->userRepository->getById($id);
        if (!$user) {
            throw new ApiException('کاربری با این شناسه یافت نشد', 404);
        }

        $updates = [];

        if (isset($data['username'])) {
            $newUsername = trim($data['username']);
            $existingUser = $this->userRepository->getByUsername($newUsername);
            if ($existingUser && $existingUser['id'] !== $id) {
                throw new ApiException('این نام کاربری قبلاً ثبت شده است', 400);
            }
            $updates['username'] = $newUsername;
        }

        if (isset($data['fullName'])) {
            $updates['fullName'] = trim($data['fullName']);
        }

        if (isset($data['password'])) {
 // ذخیره پسورد جدید با bcrypt
            $updates['password'] = password_hash($data['password'], PASSWORD_BCRYPT, ['cost' => 12]);
        }

        if (isset($data['userType'])) {
            $updates['userType'] = trim($data['userType']);
        }

        if (empty($updates)) {
            throw new ApiException('هیچ داده‌ای برای به‌روزرسانی ارائه نشده است', 400);
        }

        $this->userRepository->update($id, $updates);

 // فقط تغییرات امنیتی (رمز/نوع کاربری/نام کاربری) نشست‌ها را باطل می‌کنند؛ ویرایش صرفاً fullName کاربر را بی‌دلیل بیرون نمی‌اندازد
        $securitySensitiveFields = array_intersect(array_keys($updates), ['password', 'userType', 'username']);
        if (!empty($securitySensitiveFields)) {
            $sessionRepo = new \App\Repositories\SessionRepository();
            $sessionRepo->deactivateAllSessions($user['username']);
        }

        if ($actorUsername !== null) {
 // فقط نام فیلدهای تغییریافته ثبت می‌شود، نه مقدار رمز عبور
            AuditLogger::log($actorUsername, 'updateUser', 'user', (string)$id, [
                'changedFields' => array_keys($updates),
            ]);
        }

        return [
            'success' => true,
            'message' => 'اطلاعات کاربر با موفقیت به‌روزرسانی شد'
        ];
    }

    public function deleteUser(int $id, ?string $actorUsername = null): array {
        $user = $this->userRepository->getById($id);
        if (!$user) {
            throw new ApiException('کاربری با این شناسه یافت نشد', 404);
        }

 // بررسی تعداد مدیران
        if ($user['userType'] === 'admin') {
            $adminCount = $this->userRepository->getAdminCount();
            if ($adminCount <= 1) {
                throw new ApiException('حذف آخرین مدیر سیستم امکان‌پذیر نیست', 400);
            }
        }

        $this->userRepository->delete($id);

 // غیرفعال کردن تمامی جلسات کاربر حذف شده
        $sessionRepo = new \App\Repositories\SessionRepository();
        $sessionRepo->deactivateAllSessions($user['username']);

        if ($actorUsername !== null) {
            AuditLogger::log($actorUsername, 'deleteUser', 'user', (string)$id, [
                'username' => $user['username'],
                'userType' => $user['userType'],
            ]);
        }

        return [
            'success' => true,
            'message' => 'کاربر با موفقیت حذف شد'
        ];
    }
}
