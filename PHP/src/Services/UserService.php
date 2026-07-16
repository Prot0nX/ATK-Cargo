<?php

declare(strict_types=1);

namespace AtkCargo\Services;

use AtkCargo\Repositories\UserRepository;
use AtkCargo\Repositories\SessionRepository;
use Exception;

/**
 * Business Logic Service Layer for User Management
 */
class UserService
{
    private UserRepository $userRepository;
    private SessionRepository $sessionRepository;

    public function __construct()
    {
        $this->userRepository = new UserRepository();
        $this->sessionRepository = new SessionRepository();
    }

    /**
     * Get list of all users
     */
    public function getAllUsers(): array
    {
        return $this->userRepository->getAllUsers();
    }

    /**
     * Create a new system user
     * 
     * @throws Exception
     */
    public function createUser(array $data): array
    {
        $username = trim($data['username'] ?? '');
        $fullName = trim($data['fullName'] ?? '');
        $password = $data['password'] ?? '';
        $userType = trim($data['userType'] ?? '');

        if (empty($username) || empty($fullName) || empty($password) || empty($userType)) {
            throw new Exception('تمامی فیلدها الزامی هستند', 400);
        }

        $validUserTypes = ['admin', 'operator', 'verifier'];
        if (!in_array($userType, $validUserTypes, true)) {
            throw new Exception('نوع کاربر نامعتبر است', 400);
        }

        if (strlen($username) < 3) {
            throw new Exception('نام کاربری باید حداقل 3 کاراکتر باشد', 400);
        }

        if (strlen($fullName) < 3) {
            throw new Exception('نام و نام خانوادگی باید حداقل 3 کاراکتر باشد', 400);
        }

        // Clean inputs from tags
        $usernameClean = htmlspecialchars(strip_tags($username), ENT_QUOTES, 'UTF-8');
        $fullNameClean = htmlspecialchars(strip_tags($fullName), ENT_QUOTES, 'UTF-8');
        $userTypeClean = htmlspecialchars(strip_tags($userType), ENT_QUOTES, 'UTF-8');

        if ($this->userRepository->isUsernameTaken($usernameClean)) {
            throw new Exception('این نام کاربری قبلاً ثبت شده است', 400);
        }

        $userId = $this->userRepository->createUser($usernameClean, $fullNameClean, $password, $userTypeClean);

        return [
            'success' => true,
            'message' => 'کاربر جدید با موفقیت ایجاد شد',
            'userId' => $userId
        ];
    }

    /**
     * Update user details and terminate active sessions for security
     * 
     * @throws Exception
     */
    public function updateUser(array $data): array
    {
        $id = isset($data['id']) ? (int)$data['id'] : 0;
        if ($id <= 0) {
            throw new Exception('شناسه کاربر الزامی است', 400);
        }

        $currentUser = $this->userRepository->getUserById($id);
        if (!$currentUser) {
            throw new Exception('کاربری با این شناسه یافت نشد', 404);
        }

        $updates = [];

        // 1. Update Username
        if (isset($data['username'])) {
            $username = trim($data['username']);
            if (strlen($username) < 3) {
                throw new Exception('نام کاربری باید حداقل 3 کاراکتر باشد', 400);
            }
            
            $usernameClean = htmlspecialchars(strip_tags($username), ENT_QUOTES, 'UTF-8');
            if ($this->userRepository->isUsernameTaken($usernameClean, $id)) {
                throw new Exception('این نام کاربری قبلاً ثبت شده است', 400);
            }
            
            $updates['username'] = $usernameClean;
        }

        // 2. Update Full Name
        if (isset($data['fullName'])) {
            $fullName = trim($data['fullName']);
            if (strlen($fullName) < 3) {
                throw new Exception('نام و نام خانوادگی باید حداقل 3 کاراکتر باشد', 400);
            }
            $updates['fullName'] = htmlspecialchars(strip_tags($fullName), ENT_QUOTES, 'UTF-8');
        }

        // 3. Update Password
        if (isset($data['password']) && $data['password'] !== '') {
            $updates['password'] = $data['password'];
        }

        // 4. Update User Type
        if (isset($data['userType'])) {
            $userType = trim($data['userType']);
            $validUserTypes = ['admin', 'operator', 'verifier'];
            if (!in_array($userType, $validUserTypes, true)) {
                throw new Exception('نوع کاربر نامعتبر است', 400);
            }
            $updates['userType'] = htmlspecialchars(strip_tags($userType), ENT_QUOTES, 'UTF-8');
        }

        if (empty($updates)) {
            throw new Exception('هیچ داده‌ای برای به‌روزرسانی ارائه نشده است', 400);
        }

        $success = $this->userRepository->updateUser($id, $updates);
        if ($success) {
            // Force logout active sessions of this user for security reasons (like credentials change)
            $this->sessionRepository->deactivateAllSessions($currentUser['username']);
            return [
                'success' => true,
                'message' => 'اطلاعات کاربر با موفقیت به‌روزرسانی شد'
            ];
        }

        throw new Exception('خطا در به‌روزرسانی اطلاعات کاربر', 500);
    }

    /**
     * Delete a system user with precautions
     * 
     * @throws Exception
     */
    public function deleteUser(int $userId): array
    {
        $user = $this->userRepository->getUserById($userId);
        if (!$user) {
            throw new Exception('کاربری با این شناسه یافت نشد', 404);
        }

        // Prevent deleting the last administrator
        if ($user['userType'] === 'admin') {
            $adminCount = $this->userRepository->countAdmins();
            if ($adminCount <= 1) {
                throw new Exception('حذف آخرین مدیر سیستم امکان‌پذیر نیست', 400);
            }
        }

        $success = $this->userRepository->deleteUser($userId);
        if ($success) {
            // Terminate user sessions
            $this->sessionRepository->deactivateAllSessions($user['username']);
            return [
                'success' => true,
                'message' => 'کاربر با موفقیت حذف شد'
            ];
        }

        throw new Exception('خطا در حذف کاربر', 500);
    }
}
