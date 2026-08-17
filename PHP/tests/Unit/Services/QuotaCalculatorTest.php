<?php
// PHP/tests/Unit/Services/QuotaCalculatorTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Services\QuotaCalculator;
use PHPUnit\Framework\TestCase;

/**
 * تست‌های واحد منطق محاسبه‌ی تناژ/وضعیت کوتاژ (I-02) — این اعداد مستقیماً
 * روی تصمیم‌های عملیاتی واقعی (چند تن قابل بارگیری است، کوتاژ فعال است یا
 * نه) اثر می‌گذارند و قبل از این هیچ تست خودکاری نداشتند.
 */
final class QuotaCalculatorTest extends TestCase {
    private QuotaCalculator $calculator;

    protected function setUp(): void {
        $this->calculator = new QuotaCalculator();
    }

    // ===== calculateLoadableTonnage =====

    public function testUnrestrictedQuotaReturnsFullRemainingTonnage(): void {
        $result = $this->calculator->calculateLoadableTonnage(
            remainingTonnage: 500.0,
            totalTonnage: 1000.0,
            percentage: 20.0,
            isPercentageRestricted: false
        );

        // وقتی محدودیت درصدی غیرفعال است، percentage باید کاملاً نادیده گرفته شود.
        $this->assertSame(500.0, $result);
    }

    public function testRestrictedQuotaWithNullPercentageReturnsFullRemainingTonnage(): void {
        $result = $this->calculator->calculateLoadableTonnage(
            remainingTonnage: 500.0,
            totalTonnage: 1000.0,
            percentage: null,
            isPercentageRestricted: true
        );

        // isPercentageRestricted=true ولی percentage=null یعنی هیچ مقداری برای
        // کسر کردن مشخص نشده — نباید کرش کند یا مقدار غیرمنتظره برگرداند.
        $this->assertSame(500.0, $result);
    }

    public function testRestrictedQuotaSubtractsPercentageOfTotalTonnage(): void {
        $result = $this->calculator->calculateLoadableTonnage(
            remainingTonnage: 500.0,
            totalTonnage: 1000.0,
            percentage: 10.0,
            isPercentageRestricted: true
        );

        // ۱۰٪ از ۱۰۰۰ (کل تناژ، نه باقی‌مانده) = ۱۰۰ کسر می‌شود.
        $this->assertSame(400.0, $result);
    }

    public function testRestrictedQuotaWithZeroPercentBehavesLikeUnrestricted(): void {
        $result = $this->calculator->calculateLoadableTonnage(
            remainingTonnage: 500.0,
            totalTonnage: 1000.0,
            percentage: 0.0,
            isPercentageRestricted: true
        );

        $this->assertSame(500.0, $result);
    }

    public function testRestrictedQuotaWithHundredPercentSubtractsEntireTotalTonnage(): void {
        $result = $this->calculator->calculateLoadableTonnage(
            remainingTonnage: 500.0,
            totalTonnage: 1000.0,
            percentage: 100.0,
            isPercentageRestricted: true
        );

        // نتیجه می‌تواند منفی شود (باقی‌مانده کمتر از کل کسرشونده)؛ این
        // متد خودش این حالت را محدود نمی‌کند — مسئولیت لایه‌ی بالاتر است،
        // پس تست فقط رفتار فعلی (بدون clamp) را مستند می‌کند.
        $this->assertSame(-500.0, $result);
    }

    public function testZeroTotalTonnageWithRestrictionSubtractsNothing(): void {
        $result = $this->calculator->calculateLoadableTonnage(
            remainingTonnage: 0.0,
            totalTonnage: 0.0,
            percentage: 50.0,
            isPercentageRestricted: true
        );

        $this->assertSame(0.0, $result);
    }

    // ===== generateStatusMessage =====

    public function testInactiveQuotaMessageIgnoresPercentage(): void {
        $message = $this->calculator->generateStatusMessage(
            isActive: false,
            percentageLoaded: 10.0,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('غیرفعال است', $message);
        $this->assertStringContainsString('12345678', $message);
        $this->assertStringContainsString('برنج', $message);
    }

    public function testFullyLoadedActiveQuotaShowsCompletedMessage(): void {
        $message = $this->calculator->generateStatusMessage(
            isActive: true,
            percentageLoaded: 100.0,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('تکمیل شده است', $message);
    }

    public function testOverloadedQuotaAboveHundredPercentStillShowsCompletedMessage(): void {
        // اگر به هر دلیلی (مثلاً بارگیری همزمان) عدد از ۱۰۰٪ رد شود، پیام
        // باید همچنان «تکمیل شده» باشد، نه یک پیام غیرمنتظره یا خطا.
        $message = $this->calculator->generateStatusMessage(
            isActive: true,
            percentageLoaded: 104.5,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('تکمیل شده است', $message);
    }

    public function testQuotaAtWarningThresholdShowsWarningMessage(): void {
        // ۹۵ دقیقاً مرز است — باید داخل شاخه‌ی هشدار بیفتد (>=95)، نه شاخه‌ی عادی.
        $message = $this->calculator->generateStatusMessage(
            isActive: true,
            percentageLoaded: 95.0,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('هشدار', $message);
        $this->assertStringContainsString('95.00%', $message);
    }

    public function testQuotaJustBelowWarningThresholdShowsNormalActiveMessage(): void {
        // ۹۴.۹۹ باید هنوز پیام عادی «فعال است» را بدهد، نه هشدار.
        $message = $this->calculator->generateStatusMessage(
            isActive: true,
            percentageLoaded: 94.99,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('فعال است', $message);
        $this->assertStringNotContainsString('هشدار', $message);
    }

    public function testWarningMessageFormatsPercentageWithTwoDecimals(): void {
        $message = $this->calculator->generateStatusMessage(
            isActive: true,
            percentageLoaded: 97.5678,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('97.57%', $message);
    }

    public function testLowUsageActiveQuotaShowsNormalMessage(): void {
        $message = $this->calculator->generateStatusMessage(
            isActive: true,
            percentageLoaded: 0.0,
            quotaNumber: '12345678',
            cargoType: 'برنج'
        );

        $this->assertStringContainsString('فعال است', $message);
        $this->assertStringNotContainsString('هشدار', $message);
        $this->assertStringNotContainsString('تکمیل', $message);
    }
}
