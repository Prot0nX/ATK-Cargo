<?php
// PHP/src/Controllers/UserController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Core\Request;
use App\Core\Response;
use App\Services\UserService;
use App\Validators\InputValidator;
use App\Exceptions\ApiException;

class UserController {
    private UserService $userService;
    private Request $request;

    public function __construct() {
        $this->userService = new UserService();
        $this->request = new Request();
    }

    /**
     * مدیریت و مسیریابی درخواست‌های کاربران
     */
    public function handle(): void {
        try {
            $action = $this->request->get('action');
            if (!$action) {
                throw new ApiException('پارامتر action مورد نیاز است', 400);
            }

            if ($this->request->isGet()) {
                $this->handleGet((string)$action);
            } elseif ($this->request->isPost()) {
                $this->handlePost((string)$action);
            } else {
                throw new ApiException('روش درخواست نامعتبر است', 405);
            }
        } catch (ApiException $e) {
            Response::json([
                'success' => false,
                'message' => $e->getMessage()
            ], $e->getStatusCode());
        } catch (\Exception $e) {
            error_log("Error in UserController: " . $e->getMessage());
            Response::json([
                'success' => false,
                'message' => 'خطایی در پردازش درخواست رخ داده است: ' . $e->getMessage()
            ], 400);
        }
    }

    private function handleGet(string $action): void {
        switch ($action) {
            case 'getAllUsers':
                $users = $this->userService->getAllUsers();
                Response::json($users);
                break;

            case 'getActiveDeviceId':
                $username = $this->request->get('username');
                if (!$username || empty($username)) {
                    throw new ApiException('نام کاربری الزامی است', 400);
                }
                
                $sessionService = new \App\Services\SessionService();
                $activeSession = $sessionService->isSessionActive((string)$username);
                
                if ($activeSession) {
                    $sessionRepo = new \App\Repositories\SessionRepository();
                    $sessionData = $sessionRepo->getActiveSession((string)$username);
                    Response::json([
                        'success' => true,
                        'message' => 'جلسه فعال یافت شد',
                        'device_id' => $sessionData['device_id'] ?? null,
                        'last_activity' => $sessionData['last_activity'] ?? null,
                        'login_time' => $sessionData['login_time'] ?? null
                    ]);
                } else {
                    Response::json([
                        'success' => false,
                        'message' => 'هیچ جلسه فعالی برای این کاربر یافت نشد',
                        'device_id' => null
                    ]);
                }
                break;

            default:
                throw new ApiException('عملیات نامعتبر است', 400);
        }
    }

    private function handlePost(string $action): void {
        $params = $this->request->all();

        switch ($action) {
            case 'createUser':
                InputValidator::validateRequired($params, ['username', 'fullName', 'password', 'userType']);
                
                $username = InputValidator::validateUsername((string)$params['username']);
                $fullName = InputValidator::validateFullName((string)$params['fullName']);
                $password = (string)$params['password'];
                $userType = InputValidator::sanitize((string)$params['userType']);

                $result = $this->userService->createUser([
                    'username' => $username,
                    'fullName' => $fullName,
                    'password' => $password,
                    'userType' => $userType
                ]);
                Response::json($result);
                break;

            case 'updateUser':
                InputValidator::validateRequired($params, ['id']);
                $id = (int)$params['id'];
                
                $updates = [];
                if (isset($params['username'])) {
                    $updates['username'] = InputValidator::validateUsername((string)$params['username']);
                }
                if (isset($params['fullName'])) {
                    $updates['fullName'] = InputValidator::validateFullName((string)$params['fullName']);
                }
                if (isset($params['password'])) {
                    $updates['password'] = (string)$params['password'];
                }
                if (isset($params['userType'])) {
                    $updates['userType'] = InputValidator::sanitize((string)$params['userType']);
                }

                $result = $this->userService->updateUser($id, $updates);
                Response::json($result);
                break;

            case 'deleteUser':
                InputValidator::validateRequired($params, ['userId']);
                $userId = (int)$params['userId'];
                $result = $this->userService->deleteUser($userId);
                Response::json($result);
                break;

            case 'forceLogout':
                InputValidator::validateRequired($params, ['username']);
                $username = InputValidator::sanitize((string)$params['username']);
                $deviceId = isset($params['device_id']) ? InputValidator::sanitize((string)$params['device_id']) : '';

                $sessionService = new \App\Services\SessionService();
                if (!empty($deviceId)) {
                    $result = $sessionService->forceLogoutFromDevice($username, $deviceId);
                } else {
                    $result = $sessionService->deactivateSession($username);
                }
                
                // فرمت پاسخ برای هماهنگی با کلاینت اندروید
                if (isset($result['http_code'])) {
                    unset($result['http_code']);
                }
                Response::json($result);
                break;

            default:
                throw new ApiException('عملیات نامعتبر است', 400);
        }
    }
}
