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
     */
    public function verifyCredentials(string $username, string $password): ?array {
        $user = $this->userRepository->getByUsername($username);
        if (!$user) {
            return null;
        }

        // بررسی اینکه آیا پسورد در دیتابیس هش‌شده است یا متن خام است
        $hashInfo = password_get_info($user['password']);
        if ($hashInfo['algo'] !== 0) {
            // هش شده است
            if (password_verify($password, $user['password'])) {
                return $user;
            }
        } else {
            // به صورت مقایسه رشته‌ای امن (حفظ سازگاری با سیستم قدیم)
            if (hash_equals($user['password'], $password)) {
                return $user;
            }
        }

        return null;
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
