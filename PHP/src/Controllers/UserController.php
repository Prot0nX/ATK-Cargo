<?php

declare(strict_types=1);

namespace AtkCargo\Controllers;

use AtkCargo\Core\Request;
use AtkCargo\Core\Response;
use AtkCargo\Services\UserService;
use AtkCargo\Services\AuthService;
use Exception;

/**
 * Controller to handle all user management actions
 */
class UserController
{
    private UserService $userService;
    private AuthService $authService;

    public function __construct()
    {
        $this->userService = new UserService();
        $this->authService = new AuthService();
    }

    /**
     * Get all users in the system
     */
    public function getAllUsers(Request $request): void
    {
        try {
            $users = $this->userService->getAllUsers();
            Response::json($users);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    /**
     * Create a new user account
     */
    public function createUser(Request $request): void
    {
        $data = $request->all();
        try {
            $result = $this->userService->createUser($data);
            Response::json($result);
        } catch (Exception $e) {
            $code = $e->getCode();
            $statusCode = ($code >= 400 && $code <= 500) ? $code : 400;
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $statusCode);
        }
    }

    /**
     * Update an existing user account
     */
    public function updateUser(Request $request): void
    {
        $data = $request->all();
        try {
            $result = $this->userService->updateUser($data);
            Response::json($result);
        } catch (Exception $e) {
            $code = $e->getCode();
            $statusCode = ($code >= 400 && $code <= 500) ? $code : 400;
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $statusCode);
        }
    }

    /**
     * Delete user from the system
     */
    public function deleteUser(Request $request): void
    {
        $userId = $request->getInt('userId');
        if ($userId <= 0) {
            Response::json([
                'success' => false,
                'message' => 'شناسه کاربر معتبر نیست'
            ], 400);
        }

        try {
            $result = $this->userService->deleteUser($userId);
            Response::json($result);
        } catch (Exception $e) {
            $code = $e->getCode();
            $statusCode = ($code >= 400 && $code <= 500) ? $code : 400;
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $statusCode);
        }
    }

    /**
     * Fetch active device ID for a user
     */
    public function getActiveDeviceId(Request $request): void
    {
        $username = $request->getString('username');
        if (empty($username)) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری الزامی است',
                'device_id' => null
            ], 400);
        }

        try {
            $sessionRepository = new \AtkCargo\Repositories\SessionRepository();
            $activeSession = $sessionRepository->getAnyActiveSession($username);

            if ($activeSession && !empty($activeSession['device_id'])) {
                Response::json([
                    'success' => true,
                    'message' => 'جلسه فعال یافت شد',
                    'device_id' => $activeSession['device_id'],
                    'last_activity' => $activeSession['last_activity'] ?? null,
                    'login_time' => $activeSession['login_time'] ?? null
                ]);
            } else {
                Response::json([
                    'success' => false,
                    'message' => 'هیچ جلسه فعالی برای این کاربر یافت نشد',
                    'device_id' => null
                ]);
            }
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در پردازش: ' . $e->getMessage(),
                'device_id' => null
            ], 500);
        }
    }

    /**
     * Force logout from devices
     */
    public function forceLogout(Request $request): void
    {
        $username = $request->getString('username');
        $deviceId = $request->getString('device_id');

        if (empty($username)) {
            Response::json([
                'success' => false,
                'message' => 'نام کاربری الزامی است'
            ], 400);
        }

        try {
            $result = $this->authService->forceLogout($username, $deviceId !== '' ? $deviceId : null);
            Response::json($result);
        } catch (Exception $e) {
            Response::json([
                'success' => false,
                'message' => 'خطا در خروج اجباری: ' . $e->getMessage()
            ], 500);
        }
    }
}
