<?php
// PHP/tests/Unit/Services/SessionServiceTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Exceptions\ApiException;
use App\Repositories\SessionRepository;
use App\Repositories\UserRepository;
use App\Services\SessionService;
use PHPUnit\Framework\TestCase;

// تست‌های واحد تصمیم‌های SessionService با repositoryهای mock شده
final class SessionServiceTest extends TestCase {
    private SessionRepository $sessionRepository;
    private UserRepository $userRepository;
    private SessionService $service;

    protected function setUp(): void {
        $this->sessionRepository = $this->createMock(SessionRepository::class);
        $this->userRepository = $this->createMock(UserRepository::class);
        $this->service = new SessionService($this->sessionRepository, $this->userRepository);
    }

 // ===== refreshTokens =====

    public function testRefreshTokensRejectsEmptyParameters(): void {
        $this->sessionRepository->expects($this->never())->method('getActiveSessionByDevice');

        $result = $this->service->refreshTokens('', 'device1', 'token1');

        $this->assertFalse($result['success']);
        $this->assertSame(400, $result['http_code']);
    }

    public function testRefreshTokensReturns401WhenSessionNotFound(): void {
        $this->sessionRepository->method('getActiveSessionByDevice')->willReturn(null);

        $result = $this->service->refreshTokens('user1', 'device1', 'refresh-token');

        $this->assertFalse($result['success']);
        $this->assertSame(401, $result['http_code']);
    }

    public function testRefreshTokensDeactivatesAllSessionsOnTokenReuseDetection(): void {
        $this->sessionRepository->method('getActiveSessionByDevice')->willReturn([
            'id' => 42,
            'refresh_token' => SessionRepository::hashToken('a-completely-different-token'),
            'refresh_token_expires_at' => date('Y-m-d H:i:s', time() + 3600),
        ]);

 // توکن نامنطبق نشانه‌ی reuse است و باید همه‌ی نشست‌های کاربر را باطل کند.
        $this->sessionRepository->expects($this->once())
            ->method('deactivateAllSessions')
            ->with('user1');
        $this->sessionRepository->expects($this->never())->method('rotateTokens');

        $result = $this->service->refreshTokens('user1', 'device1', 'the-real-refresh-token');

        $this->assertFalse($result['success']);
        $this->assertSame(401, $result['http_code']);
    }

    public function testRefreshTokensReturns401WhenRefreshTokenExpired(): void {
        $rawToken = 'expired-refresh-token';
        $this->sessionRepository->method('getActiveSessionByDevice')->willReturn([
            'id' => 42,
            'refresh_token' => SessionRepository::hashToken($rawToken),
            'refresh_token_expires_at' => date('Y-m-d H:i:s', time() - 3600),
        ]);
        $this->sessionRepository->expects($this->never())->method('rotateTokens');

        $result = $this->service->refreshTokens('user1', 'device1', $rawToken);

        $this->assertFalse($result['success']);
        $this->assertSame(401, $result['http_code']);
    }

    public function testRefreshTokensRotatesAndReturnsFreshTokenPairOnSuccess(): void {
        $rawToken = 'valid-refresh-token';
        $this->sessionRepository->method('getActiveSessionByDevice')->willReturn([
            'id' => 42,
            'refresh_token' => SessionRepository::hashToken($rawToken),
            'refresh_token_expires_at' => date('Y-m-d H:i:s', time() + 3600),
            'userType' => 'admin',
        ]);
        $this->sessionRepository->expects($this->once())
            ->method('rotateTokens')
            ->with(42, $this->isType('string'), $this->isType('string'), $this->isType('string'), $this->isType('string'));

        $result = $this->service->refreshTokens('user1', 'device1', $rawToken);

        $this->assertTrue($result['success']);
        $this->assertSame(200, $result['http_code']);
        $this->assertSame('admin', $result['userType']);
        $this->assertNotSame($rawToken, $result['refresh_token']);
        $this->assertArrayHasKey('session_token', $result);
    }

 // ===== isValidToken =====

    public function testIsValidTokenReturnsFalseForEmptyParametersWithoutHittingRepository(): void {
        $this->sessionRepository->expects($this->never())->method('isValidToken');

        $this->assertFalse($this->service->isValidToken('', 'device1', 'token1'));
    }

    public function testIsValidTokenTouchesLastActivityOnlyWhenValid(): void {
        $this->sessionRepository->method('isValidToken')->willReturn(true);
        $this->sessionRepository->expects($this->once())
            ->method('updateLastActivity')
            ->with('user1', 'device1');

        $this->assertTrue($this->service->isValidToken('user1', 'device1', 'token1'));
    }

    public function testIsValidTokenDoesNotTouchLastActivityWhenInvalid(): void {
        $this->sessionRepository->method('isValidToken')->willReturn(false);
        $this->sessionRepository->expects($this->never())->method('updateLastActivity');

        $this->assertFalse($this->service->isValidToken('user1', 'device1', 'wrong-token'));
    }

 // ===== validateAndGetUserType (گیت AuthenticatesRequests) =====

    public function testValidateAndGetUserTypeReturnsNullForEmptyParameters(): void {
        $this->sessionRepository->expects($this->never())->method('validateTokenAndGetUserType');

        $this->assertNull($this->service->validateAndGetUserType('', '', ''));
    }

    public function testValidateAndGetUserTypeThrottlesActivityOnlyWhenSessionValid(): void {
        $this->sessionRepository->method('validateTokenAndGetUserType')->willReturn('operator');
        $this->sessionRepository->expects($this->once())
            ->method('touchLastActivityThrottled')
            ->with('user1', 'device1');

        $this->assertSame('operator', $this->service->validateAndGetUserType('user1', 'device1', 'token1'));
    }

    public function testValidateAndGetUserTypeDoesNotThrottleActivityWhenSessionInvalid(): void {
        $this->sessionRepository->method('validateTokenAndGetUserType')->willReturn(null);
        $this->sessionRepository->expects($this->never())->method('touchLastActivityThrottled');

        $this->assertNull($this->service->validateAndGetUserType('user1', 'device1', 'bad-token'));
    }

 // ===== deactivateSession =====

    public function testDeactivateSessionReturns404WhenUserDoesNotExist(): void {
        $this->userRepository->method('getByUsername')->willReturn(null);
        $this->sessionRepository->expects($this->never())->method('getActiveSessionsForUser');

        $result = $this->service->deactivateSession('ghost-user');

        $this->assertFalse($result['success']);
        $this->assertSame(404, $result['http_code']);
    }

    public function testDeactivateSessionReturns404WhenNoActiveSessions(): void {
        $this->userRepository->method('getByUsername')->willReturn(['id' => 1, 'username' => 'user1']);
        $this->sessionRepository->method('getActiveSessionsForUser')->willReturn([]);
        $this->sessionRepository->expects($this->never())->method('deactivateSessionsByIds');

        $result = $this->service->deactivateSession('user1');

        $this->assertFalse($result['success']);
        $this->assertSame(404, $result['http_code']);
    }

    public function testDeactivateSessionDeactivatesAllActiveSessionIds(): void {
        $this->userRepository->method('getByUsername')->willReturn(['id' => 1, 'username' => 'user1']);
        $this->sessionRepository->method('getActiveSessionsForUser')->willReturn([10, 11, 12]);
        $this->sessionRepository->expects($this->once())
            ->method('deactivateSessionsByIds')
            ->with([10, 11, 12]);

        $result = $this->service->deactivateSession('user1');

        $this->assertTrue($result['success']);
        $this->assertSame(200, $result['http_code']);
        $this->assertSame(3, $result['affected_sessions']);
    }

 // ===== createMobileSession =====

    public function testCreateMobileSessionRotatesTokensForSameDeviceReLogin(): void {
        $this->sessionRepository->method('getActiveSession')->willReturn([
            'id' => 5,
            'device_id' => 'device1',
        ]);
        $this->sessionRepository->expects($this->once())->method('rotateTokens');
        $this->sessionRepository->expects($this->never())->method('createSession');

        $result = $this->service->createMobileSession('user1', 'device1', 'Pixel', '14', '1.2.3.4', 'admin');

        $this->assertTrue($result['success']);
        $this->assertSame(5, $result['session_id']);
    }

    public function testCreateMobileSessionRejectsLoginFromDifferentDeviceWhileSessionActive(): void {
        $this->sessionRepository->method('getActiveSession')->willReturn([
            'id' => 5,
            'device_id' => 'device-old',
        ]);
        $this->sessionRepository->expects($this->never())->method('rotateTokens');
        $this->sessionRepository->expects($this->never())->method('createSession');

        $result = $this->service->createMobileSession('user1', 'device-new', 'Pixel', '14', '1.2.3.4', 'admin');

        $this->assertFalse($result['success']);
    }

    public function testCreateMobileSessionThrowsWhenNoUserTypeGivenAndUserNotFound(): void {
        $this->sessionRepository->method('getActiveSession')->willReturn(null);
        $this->userRepository->method('getByUsername')->willReturn(null);

        $this->expectException(ApiException::class);

        try {
            $this->service->createMobileSession('ghost-user', 'device1', 'Pixel', '14', '1.2.3.4', null);
        } catch (ApiException $e) {
            $this->assertSame(404, $e->getStatusCode());
            throw $e;
        }
    }

    public function testCreateMobileSessionCreatesNewSessionWhenNoneActive(): void {
        $this->sessionRepository->method('getActiveSession')->willReturn(null);
        $this->sessionRepository->method('createSession')->willReturn(99);
        $this->sessionRepository->expects($this->once())->method('createSession');

        $result = $this->service->createMobileSession('user1', 'device1', 'Pixel', '14', '1.2.3.4', 'admin');

        $this->assertTrue($result['success']);
        $this->assertSame(99, $result['session_id']);
    }
}
