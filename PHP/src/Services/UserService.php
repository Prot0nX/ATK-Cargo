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

    /**
     * بررسی اعتبار کاربر و احراز هویت
     *
     * استراتژی هشینگ دوگانه (Dual-Hash Migration):
     * ۱. اگر پسورد دیتابیس با password_hash (bcrypt) ذخیره شده: password_verify
     * ۲. اگر پسورد دیتابیس به‌صورت SHA-256 ذخیره شده (سیستم قدیم):
     *    - مقایسه hash_equals
     *    - در صورت موفقیت: پسورد در دیتابیس به bcrypt ارتقا داده می‌شود (Silent Migration)
     */
    public function verifyCredentials(string $username, string $password): ?array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return null;
        }

        $storedPassword = $user['password'];
        $hashInfo = password_get_info($storedPassword);

        if ($hashInfo['algo'] !== 0) {
            // ===== حالت ۱: bcrypt — مقایسه مستقیم =====
            if (!password_verify($password, $storedPassword)) {
                return null;
            }

            // بررسی نیاز به rehash (در صورت تغییر cost factor)
            if (password_needs_rehash($storedPassword, PASSWORD_BCRYPT, ['cost' => 12])) {
                $this->userRepository->updatePassword($user['id'], password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]));
            }

            return $user;
        }

        // ===== حالت ۲: SHA-256 یا متن خام (سیستم قدیم) — مقایسه ایمن =====
        if (!hash_equals($storedPassword, $password)) {
            return null;
        }

        // ===== Silent Migration: ارتقا پسورد به bcrypt در پس‌زمینه =====
        // پسورد دریافتی از کلاینت هم‌اکنون SHA-256 است — bcrypt روی همان ذخیره می‌شود
        // تا مقایسه‌های آینده از مسیر bcrypt عبور کنند
        $upgradedHash = password_hash($storedPassword, PASSWORD_BCRYPT, ['cost' => 12]);
        $this->userRepository->updatePassword($user['id'], $upgradedHash);

        return $user;
    }

    public function getAllUsers(): array {
        return $this->userRepository->getAll();
    }

    /**
     * دریافت تمامی کاربران به همراه وضعیت آنلاین و آخرین فعالیت.
     *
     * منطق آنلاین بودن: اگر `last_activity` در ۵ دقیقه گذشته ثبت شده باشد،
     * کاربر آنلاین در نظر گرفته می‌شود (هماهنگ با OnlineUsersController).
     */
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

    public function createUser(array $data): array {
        // فیلتر کردن و اعتبارسنجی مقادیر
        $username = trim($data['username']);
        $fullName = trim($data['fullName']);
        $password = $data['password'];
        $userType = trim($data['userType']);

        // بررسی تکراری نبودن نام کاربری
        if ($this->userRepository->getByUsername($username) !== null) {
            throw new ApiException('این نام کاربری قبلاً ثبت شده است', 400);
        }

        // ===== ذخیره پسورد با bcrypt برای کاربران جدید =====
        // پسورد ورودی در زمان ایجاد از پنل ادمین به‌عنوان متن خام دریافت می‌شود
        $hashedPassword = password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);

        $userId = $this->userRepository->create([
            'username' => $username,
            'fullName' => $fullName,
            'password' => $hashedPassword,
            'userType' => $userType
        ]);

        return [
            'success' => true,
            'message' => 'کاربر جدید با موفقیت ایجاد شد',
            'userId' => $userId
        ];
    }

    public function updateUser(int $id, array $data): array {
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

        // پس از ویرایش موفق، جلسات قبلی کاربر را غیرفعال می‌کنیم
        $sessionRepo = new \App\Repositories\SessionRepository();
        $sessionRepo->deactivateAllSessions($user['username']);

        return [
            'success' => true,
            'message' => 'اطلاعات کاربر با موفقیت به‌روزرسانی شد'
        ];
    }

    public function deleteUser(int $id): array {
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

        return [
            'success' => true,
            'message' => 'کاربر با موفقیت حذف شد'
        ];
    }
}


    public function getAllUsers(): array {
        return $this->userRepository->getAll();
    }

    public function createUser(array $data): array {
        // فیلتر کردن و اعتبارسنجی مقادیر
        $username = trim($data['username']);
        $fullName = trim($data['fullName']);
        $password = $data['password'];
        $userType = trim($data['userType']);

        // بررسی تکراری نبودن نام کاربری
        if ($this->userRepository->getByUsername($username) !== null) {
            throw new ApiException('این نام کاربری قبلاً ثبت شده است', 400);
        }

        $userId = $this->userRepository->create([
            'username' => $username,
            'fullName' => $fullName,
            'password' => $password, // برای هماهنگی با کلاینت فعلاً به همان فرم قبلی ذخیره می‌شود
            'userType' => $userType
        ]);

        return [
            'success' => true,
            'message' => 'کاربر جدید با موفقیت ایجاد شد',
            'userId' => $userId
        ];
    }

    public function updateUser(int $id, array $data): array {
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
            $updates['password'] = $data['password'];
        }

        if (isset($data['userType'])) {
            $updates['userType'] = trim($data['userType']);
        }

        if (empty($updates)) {
            throw new ApiException('هیچ داده‌ای برای به‌روزرسانی ارائه نشده است', 400);
        }

        $this->userRepository->update($id, $updates);

        // پس از ویرایش موفق، جلسات قبلی کاربر را غیرفعال می‌کنیم
        $sessionRepo = new \App\Repositories\SessionRepository();
        $sessionRepo->deactivateAllSessions($user['username']);

        return [
            'success' => true,
            'message' => 'اطلاعات کاربر با موفقیت به‌روزرسانی شد'
        ];
    }

    public function deleteUser(int $id): array {
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

        return [
            'success' => true,
            'message' => 'کاربر با موفقیت حذف شد'
        ];
    }
}
