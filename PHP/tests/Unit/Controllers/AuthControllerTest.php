<?php
// PHP/tests/Unit/Controllers/AuthControllerTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Controllers;

use App\Controllers\AuthController;
use App\Exceptions\ResponseSentException;
use App\Services\LoginAttemptLimiter;
use App\Services\SessionService;
use App\Services\UserService;
use PHPUnit\Framework\TestCase;

// تست‌های AuthController::login: شکست ورود، قفل‌شدن حساب، ورود همزمان، ورود موفق (DEEP_CODE_AUDIT.md #۲۰).
// زیر TESTING_MODE (tests/bootstrap.php)، Response::json به‌جای exit یک ResponseSentException پرتاب می‌کند که اینجا catch می‌شود.
// LoginAttemptLimiter/PermissionService (هر دو final، غیرقابل‌mock) به‌صورت نمونه‌ی واقعی استفاده می‌شوند —
// هیچ‌کدام برای این تست‌ها به DB واقعی نیاز ندارند (fallback فایلی و fallback permissions.json).
final class AuthControllerTest extends TestCase {
    protected function setUp(): void {
        $_SERVER['REQUEST_METHOD'] = 'POST';
        $_SERVER['REMOTE_ADDR'] = '127.0.0.1';
        $_POST = [];
        $_GET = [];
    }

    private function uniqueUsername(): string {
        return 'auth_ctrl_test_' . uniqid();
    }

    private function captureResponse(AuthController $controller, string $method = 'login'): ResponseSentException {
        try {
            $controller->$method();
        } catch (ResponseSentException $e) {
            return $e;
        }

        $this->fail('انتظار می‌رفت Response::json (به‌صورت ResponseSentException) پرتاب شود.');
    }

    public function testLoginRejectsNonPostRequest(): void {
        $_SERVER['REQUEST_METHOD'] = 'GET';

        $userService = $this->createMock(UserService::class);
        $userService->expects($this->never())->method('verifyCredentials');

        $controller = new AuthController($userService, $this->createMock(SessionService::class));

        $response = $this->captureResponse($controller);

        $this->assertSame(405, $response->getStatusCode());
        $this->assertStringContainsString('روش درخواست مجاز نیست', $response->getPayload()['message']);
    }

    public function testLoginRejectsMissingCredentials(): void {
        $_POST = ['username' => 'someone']; // بدون password

        $userService = $this->createMock(UserService::class);
        $userService->expects($this->never())->method('verifyCredentials');

        $controller = new AuthController($userService, $this->createMock(SessionService::class));

        $response = $this->captureResponse($controller);

        $this->assertSame(400, $response->getStatusCode());
        $this->assertStringContainsString('نام کاربری و رمز عبور الزامی است', $response->getPayload()['message']);
    }

    public function testLoginIsBlockedAfterAccountIsLocked(): void {
        $username = $this->uniqueUsername();
        $_POST = ['username' => $username, 'password' => 'wrong-password'];

        // قفل کردن مستقیم حساب از طریق limiter واقعی
        $limiter = new LoginAttemptLimiter();
        for ($i = 0; $i < 5; $i++) {
            $limiter->registerFailedAttempt($username, '127.0.0.1');
        }

        $userService = $this->createMock(UserService::class);
        $userService->expects($this->never())->method('verifyCredentials');

        $controller = new AuthController($userService, $this->createMock(SessionService::class), null, $limiter);

        $response = $this->captureResponse($controller);

        $this->assertSame(200, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
        $this->assertStringContainsString('تعداد تلاش‌های ناموفق بیش از حد مجاز است', $response->getPayload()['message']);
    }

    public function testLoginRejectsWrongCredentials(): void {
        $username = $this->uniqueUsername();
        $_POST = ['username' => $username, 'password' => 'wrong-password'];

        $userService = $this->createMock(UserService::class);
        $userService->expects($this->once())
            ->method('verifyCredentials')
            ->willReturn(null);

        $sessionService = $this->createMock(SessionService::class);
        $sessionService->expects($this->never())->method('createMobileSession');

        $controller = new AuthController($userService, $sessionService, null, new LoginAttemptLimiter());

        $response = $this->captureResponse($controller);

        $this->assertFalse($response->getPayload()['success']);
        $this->assertStringContainsString('نام کاربری یا رمز عبور اشتباه است', $response->getPayload()['message']);
    }

    public function testLoginReturnsConflictWhenAnotherDeviceIsAlreadyLoggedIn(): void {
        $username = $this->uniqueUsername();
        $_POST = [
            'username' => $username,
            'password' => 'correct-password',
            'deviceId' => 'device-b',
        ];

        $userService = $this->createMock(UserService::class);
        $userService->method('verifyCredentials')->willReturn([
            'id' => 1,
            'username' => $username,
            'userType' => 'operator',
        ]);

        $sessionService = $this->createMock(SessionService::class);
        $sessionService->method('createMobileSession')->willReturn([
            'success' => false,
            'message' => 'شما در حال حاضر از دستگاه دیگری وارد شده‌اید. لطفاً ابتدا از آن دستگاه خارج شوید.',
        ]);

        $controller = new AuthController($userService, $sessionService, null, new LoginAttemptLimiter());

        $response = $this->captureResponse($controller);

        $this->assertSame(409, $response->getStatusCode());
        $this->assertStringContainsString('از دستگاه دیگری وارد شده‌اید', $response->getPayload()['message']);
    }

    public function testSuccessfulLoginReturnsSessionTokens(): void {
        $username = $this->uniqueUsername();
        $_POST = [
            'username' => $username,
            'password' => 'correct-password',
            'deviceId' => 'device-a',
            'deviceModel' => 'Pixel Test',
            'androidVersion' => '14',
        ];

        $userService = $this->createMock(UserService::class);
        $userService->method('verifyCredentials')->willReturn([
            'id' => 1,
            'username' => $username,
            'userType' => 'operator',
        ]);

        $sessionService = $this->createMock(SessionService::class);
        $sessionService->method('createMobileSession')->willReturn([
            'success' => true,
            'message' => 'جلسه با موفقیت ایجاد شد',
            'session_id' => 42,
            'session_token' => 'fake-access-token',
            'access_token_expires_in' => 1800,
            'refresh_token' => 'fake-refresh-token',
            'refresh_token_expires_in' => 86400,
        ]);

        $controller = new AuthController($userService, $sessionService, null, new LoginAttemptLimiter());

        $response = $this->captureResponse($controller);

        $this->assertTrue($response->getPayload()['success']);
        $this->assertSame('fake-access-token', $response->getPayload()['session_token']);
        $this->assertSame('operator', $response->getPayload()['userType']);
    }
}
