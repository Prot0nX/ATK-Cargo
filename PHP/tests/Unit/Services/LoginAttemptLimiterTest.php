<?php
// PHP/tests/Unit/Services/LoginAttemptLimiterTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Services\LoginAttemptLimiter;
use PHPUnit\Framework\TestCase;

// تست‌های واحد محدودکننده‌ی brute-force؛ sleep مسدودکننده حذف شد پس دیگر نیازی به تزریق sleeper نیست
final class LoginAttemptLimiterTest extends TestCase {
    private LoginAttemptLimiter $limiter;

    protected function setUp(): void {
        $this->limiter = new LoginAttemptLimiter();
    }

    private function uniqueUsername(): string {
        return 'test_user_' . uniqid();
    }

    private function uniqueIp(): string {
        return '10.0.' . random_int(0, 255) . '.' . random_int(0, 255);
    }

    public function testFreshUsernameAndIpAreNotLocked(): void {
        $this->assertFalse($this->limiter->isLocked($this->uniqueUsername(), $this->uniqueIp()));
    }

    public function testUsernameIsNotLockedBeforeReachingMaxAttempts(): void {
        $username = $this->uniqueUsername();
        $ip = $this->uniqueIp();

 // MAX_USER_ATTEMPTS = 5؛ چهار تلاش ناموفق هنوز نباید قفل کند.
        for ($i = 0; $i < 4; $i++) {
            $this->limiter->registerFailedAttempt($username, $ip);
        }

        $this->assertFalse($this->limiter->isLocked($username, $ip));
    }

    public function testUsernameIsLockedAfterReachingMaxAttempts(): void {
        $username = $this->uniqueUsername();
        $ip = $this->uniqueIp();

        for ($i = 0; $i < 5; $i++) {
            $this->limiter->registerFailedAttempt($username, $ip);
        }

        $this->assertTrue($this->limiter->isLocked($username, $ip));
    }

    public function testResetAttemptsClearsUsernameLockButKeepsIpCounter(): void {
        $username = $this->uniqueUsername();
        $ip = $this->uniqueIp();

        for ($i = 0; $i < 5; $i++) {
            $this->limiter->registerFailedAttempt($username, $ip);
        }
        $this->assertTrue($this->limiter->isLocked($username, $ip));

        $this->limiter->resetAttempts($username, $ip);

 // شمارنده‌ی username پاک شد ولی شمارنده‌ی IP دست‌نخورده ماند.
        $this->assertFalse($this->limiter->isLocked($username, $ip));

 // یک username دیگر از همان IP باید بخشی از سقف MAX_IP_ATTEMPTS را از قبل مصرف‌شده ببیند.
        $otherUsername = $this->uniqueUsername();
        for ($i = 0; $i < 45; $i++) {
            $this->limiter->registerFailedAttempt($otherUsername, $ip);
        }
 // ۵ (از username اول، هنوز روی شمارنده‌ی IP) + ۴۵ (از username دوم) = ۵۰ = MAX_IP_ATTEMPTS
        $this->assertTrue($this->limiter->isLocked($otherUsername, $ip));
    }

    public function testDifferentUsernamesFromSameIpShareIpAttemptCounter(): void {
        $ip = $this->uniqueIp();
        $usernameA = $this->uniqueUsername();
        $usernameB = $this->uniqueUsername();

 // تلاش‌های دو username روی یک IP باید در شمارنده‌ی مشترک IP جمع شوند تا به سقف MAX_IP_ATTEMPTS برسند.
        for ($i = 0; $i < 5; $i++) {
            $this->limiter->registerFailedAttempt($usernameA, $ip);
        }
        for ($i = 0; $i < 45; $i++) {
            $this->limiter->registerFailedAttempt($usernameB, $ip);
        }

        $this->assertTrue($this->limiter->isLocked($usernameA, $ip));
        $this->assertTrue($this->limiter->isLocked($usernameB, $ip));
    }

    public function testDifferentIpsForSameUsernameHaveIndependentIpCounters(): void {
        $username = $this->uniqueUsername();
        $ipA = $this->uniqueIp();
        $ipB = $this->uniqueIp();

        for ($i = 0; $i < 5; $i++) {
            $this->limiter->registerFailedAttempt($username, $ipA);
        }

 // username الان قفل است (سقف username رد شده)، صرف‌نظر از IP:
        $this->assertTrue($this->limiter->isLocked($username, $ipB));
    }

    public function testRegisterFailedAttemptDoesNotBlockTheCallingThread(): void {
 // تست رگرسیون: نسخه‌ی قبلی بعد از هر تلاش ناموفق تا ۸ ثانیه sleep می‌زد
        $username = $this->uniqueUsername();
        $ip = $this->uniqueIp();

        $startedAt = microtime(true);
        for ($i = 0; $i < 5; $i++) {
            $this->limiter->registerFailedAttempt($username, $ip);
        }
        $elapsedSeconds = microtime(true) - $startedAt;

        $this->assertLessThan(1.0, $elapsedSeconds, 'registerFailedAttempt() باید فوراً برگردد، نه با sleep() مسدودکننده.');
    }
}
