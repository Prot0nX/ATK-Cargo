<?php
// PHP/tests/Unit/Services/LoginAttemptLimiterTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Services\LoginAttemptLimiter;
use PHPUnit\Framework\TestCase;

/**
 * تست‌های واحد محدودکننده‌ی brute-force (S-06). APCu در محیط اجرای این تست‌ها
 * فعال نیست، پس LoginAttemptLimiter خودش روی fallback فایلی (APP_ROOT/log)
 * می‌افتد؛ به همین دلیل هر تست کلید یکتای خودش را با uniqid() می‌سازد تا
 * فایل‌های باقی‌مانده از یک تست روی تست بعدی اثر نگذارند. sleep() واقعی با یک
 * no-op جایگزین شده (Phase 3.3 refactor) تا تست‌ها فوری اجرا شوند و فقط منطق
 * شمارش/قفل را بسنجند، نه گذر زمان واقعی backoff را.
 */
final class LoginAttemptLimiterTest extends TestCase {
    private LoginAttemptLimiter $limiter;
    /** @var array<int, int> */
    private array $sleptSeconds;

    protected function setUp(): void {
        $this->sleptSeconds = [];
        $this->limiter = new LoginAttemptLimiter(function (int $seconds): void {
            $this->sleptSeconds[] = $seconds;
        });
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

        // شمارنده‌ی username پاک شد، پس این username دیگر قفل نیست...
        $this->assertFalse($this->limiter->isLocked($username, $ip));

        // ...اما شمارنده‌ی IP دست‌نخورده ماند: یک username دیگر از همان IP
        // باید همچنان بخشی از سقف MAX_IP_ATTEMPTS را از قبل مصرف‌شده ببیند.
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

        // MAX_IP_ATTEMPTS = 50؛ ۳۰ تلاش با username A و ۲۰ با username B روی
        // همان IP باید مجموعاً به سقف IP برسد، حتی با اینکه هیچ‌کدام به‌تنهایی
        // به سقف MAX_USER_ATTEMPTS نرسیده‌اند (که اینجا امکان‌پذیر نیست چون
        // MAX_USER_ATTEMPTS=5 < 30، پس در عمل usernameA زودتر خودش قفل می‌شود؛
        // نکته‌ی این تست خواندن شمارنده‌ی IP مستقل از شمارنده‌ی username است).
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

    public function testBackoffDelayIsExponentialAndCappedAtMaxBackoffSeconds(): void {
        $username = $this->uniqueUsername();
        $ip = $this->uniqueIp();

        // تلاش ۱ تا ۵: تأخیر باید 2, 4, 8, 8, 8 باشد (سقف MAX_BACKOFF_SECONDS=8).
        for ($i = 0; $i < 5; $i++) {
            $this->limiter->registerFailedAttempt($username, $ip);
        }

        $this->assertSame([2, 4, 8, 8, 8], $this->sleptSeconds);
    }
}
