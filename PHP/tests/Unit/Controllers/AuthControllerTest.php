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

// تست‌های AuthController::login: شکست ورود، قفل‌شدن حساب، ورود همزمان، ورود موفق.
final class AuthControllerTest extends TestCase {
    protected function setUp(): void {
        $_SERVER['REQUEST_METHOD'] = 'POST';
        $_SERVER['REMOTE_ADDR'] = '127.0.0.1';
        // چون $_SERVER بین متدهای تست در همان پروسه باقی می‌ماند، بدون این reset تست‌های «کلاینت قدیمی» می‌توانستند به‌اشتباه هدر تست قبلی را ببینند.
        unset($_SERVER['HTTP_X_APP_VERSION']);
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

 // بدون X-App-Version یعنی کلاینت قدیمی — کد باید 200 بماند تا نشکند
        $this->assertSame(200, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
        $this->assertStringContainsString('نام کاربری یا رمز عبور اشتباه است', $response->getPayload()['message']);
    }

    // از اینجا به بعد: همان دو سناریوی بالا اما با X-App-Version >= آستانه، برای تأیید کد HTTP واقعی (فاز۳ #۲۸)

    public function testLoginIsBlockedAfterAccountIsLockedReturns429ForRecentAppVersion(): void {
        $_SERVER['HTTP_X_APP_VERSION'] = '4.1.0';

        $username = $this->uniqueUsername();
        $_POST = ['username' => $username, 'password' => 'wrong-password'];

        $limiter = new LoginAttemptLimiter();
        for ($i = 0; $i < 5; $i++) {
            $limiter->registerFailedAttempt($username, '127.0.0.1');
        }

        $userService = $this->createMock(UserService::class);
        $userService->expects($this->never())->method('verifyCredentials');

        $controller = new AuthController($userService, $this->createMock(SessionService::class), null, $limiter);

        $response = $this->captureResponse($controller);

        $this->assertSame(429, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
        $this->assertSame('rate_limited', $response->getPayload()['code']);
    }

    public function testLoginRejectsWrongCredentialsReturns401ForRecentAppVersion(): void {
        $_SERVER['HTTP_X_APP_VERSION'] = '4.1.0';

        $username = $this->uniqueUsername();
        $_POST = ['username' => $username, 'password' => 'wrong-password'];

        $userService = $this->createMock(UserService::class);
        $userService->method('verifyCredentials')->willReturn(null);

        $controller = new AuthController($userService, $this->createMock(SessionService::class), null, new LoginAttemptLimiter());

        $response = $this->captureResponse($controller);

        $this->assertSame(401, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
        $this->assertSame('invalid_credentials', $response->getPayload()['code']);
    }

    public function testLoginRejectsWrongCredentialsStaysAt200ForOlderAppVersion(): void {
        $_SERVER['HTTP_X_APP_VERSION'] = '4.0.9';

        $username = $this->uniqueUsername();
        $_POST = ['username' => $username, 'password' => 'wrong-password'];

        $userService = $this->createMock(UserService::class);
        $userService->method('verifyCredentials')->willReturn(null);

        $controller = new AuthController($userService, $this->createMock(SessionService::class), null, new LoginAttemptLimiter());

        $response = $this->captureResponse($controller);

        $this->assertSame(200, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
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

 // تست‌های checkSession — کد HTTP وقتی نشست نامعتبر است هم پشت همان گیت نسخه است

    public function testCheckSessionForInvalidSessionStaysAt200ForOlderAppVersion(): void {
        $_POST = ['username' => 'someone', 'deviceId' => 'device-a', 'sessionToken' => 'bad-token'];

        $sessionService = $this->createMock(SessionService::class);
        $sessionService->method('validateAndGetUserType')->willReturn(null);

        $controller = new AuthController($this->createMock(UserService::class), $sessionService);

        $response = $this->captureResponse($controller, 'checkSession');

        $this->assertSame(200, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
        $this->assertNull($response->getPayload()['userType']);
    }

    public function testCheckSessionForInvalidSessionReturns401ForRecentAppVersion(): void {
        $_SERVER['HTTP_X_APP_VERSION'] = '4.1.0';
        $_POST = ['username' => 'someone', 'deviceId' => 'device-a', 'sessionToken' => 'bad-token'];

        $sessionService = $this->createMock(SessionService::class);
        $sessionService->method('validateAndGetUserType')->willReturn(null);

        $controller = new AuthController($this->createMock(UserService::class), $sessionService);

        $response = $this->captureResponse($controller, 'checkSession');

        $this->assertSame(401, $response->getStatusCode());
        $this->assertFalse($response->getPayload()['success']);
        $this->assertSame('session_invalid', $response->getPayload()['code']);
    }

    public function testCheckSessionForValidSessionReturns200RegardlessOfAppVersion(): void {
        $_SERVER['HTTP_X_APP_VERSION'] = '4.1.0';
        $_POST = ['username' => 'someone', 'deviceId' => 'device-a', 'sessionToken' => 'good-token'];

        $sessionService = $this->createMock(SessionService::class);
        $sessionService->method('validateAndGetUserType')->willReturn('operator');

        $controller = new AuthController($this->createMock(UserService::class), $sessionService);

        $response = $this->captureResponse($controller, 'checkSession');

        $this->assertSame(200, $response->getStatusCode());
        $this->assertTrue($response->getPayload()['success']);
        $this->assertSame('operator', $response->getPayload()['userType']);
    }
}