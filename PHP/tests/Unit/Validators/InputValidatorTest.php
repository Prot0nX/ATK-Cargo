<?php
// PHP/tests/Unit/Validators/InputValidatorTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Validators;

use App\Exceptions\ApiException;
use App\Validators\InputValidator;
use PHPUnit\Framework\TestCase;

/**
 * تست‌های واحد InputValidator — از جمله validatePassword که سدّ سمت سرور
 * برای حداقل طول رمز عبور است (Phase 1.2: قبلاً رمز ۴ رقمی عددی مجاز بود).
 */
final class InputValidatorTest extends TestCase {

    // ===== sanitize =====

    public function testSanitizeStripsHtmlTags(): void {
        $this->assertSame('alert(1)', InputValidator::sanitize('<script>alert(1)</script>'));
    }

    public function testSanitizeEscapesSpecialCharacters(): void {
        $this->assertSame('M&amp;V', InputValidator::sanitize('M&V'));
    }

    public function testSanitizeTrimsWhitespace(): void {
        $this->assertSame('hello', InputValidator::sanitize('  hello  '));
    }

    // ===== validateRequired =====

    public function testValidateRequiredPassesWhenAllFieldsPresent(): void {
        $this->expectNotToPerformAssertions();
        InputValidator::validateRequired(['a' => '1', 'b' => '2'], ['a', 'b']);
    }

    public function testValidateRequiredThrowsWhenFieldMissing(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateRequired(['a' => '1'], ['a', 'b']);
    }

    public function testValidateRequiredThrowsWhenFieldIsEmptyString(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateRequired(['a' => '   '], ['a']);
    }

    public function testValidateRequiredListsAllMissingFieldsInMessage(): void {
        try {
            InputValidator::validateRequired([], ['username', 'password']);
            $this->fail('Expected ApiException was not thrown');
        } catch (ApiException $e) {
            $this->assertStringContainsString('username', $e->getMessage());
            $this->assertStringContainsString('password', $e->getMessage());
        }
    }

    // ===== validateFloat =====

    public function testValidateFloatAcceptsPositiveNumber(): void {
        $this->assertSame(12.5, InputValidator::validateFloat('12.5', 'weight'));
    }

    public function testValidateFloatRejectsZero(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateFloat(0, 'weight');
    }

    public function testValidateFloatRejectsNegative(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateFloat(-5, 'weight');
    }

    public function testValidateFloatRejectsNonNumeric(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateFloat('not-a-number', 'weight');
    }

    // ===== validateDigits =====

    public function testValidateDigitsAcceptsDigitsOnly(): void {
        $this->expectNotToPerformAssertions();
        InputValidator::validateDigits('12345678', 'receipt');
    }

    public function testValidateDigitsRejectsNonDigits(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateDigits('123abc', 'receipt');
    }

    public function testValidateDigitsRejectsEmptyString(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateDigits('', 'receipt');
    }

    // ===== validateUsername =====

    public function testValidateUsernameAcceptsThreeOrMoreCharacters(): void {
        $this->assertSame('abc', InputValidator::validateUsername('abc'));
    }

    public function testValidateUsernameRejectsFewerThanThreeCharacters(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateUsername('ab');
    }

    // ===== validatePassword (Phase 1.2) =====

    public function testValidatePasswordAcceptsEightCharacters(): void {
        $this->assertSame('12345678', InputValidator::validatePassword('12345678'));
    }

    public function testValidatePasswordRejectsFewerThanEightCharacters(): void {
        $this->expectException(ApiException::class);
        InputValidator::validatePassword('1234567');
    }

    public function testValidatePasswordRejectsEmptyString(): void {
        $this->expectException(ApiException::class);
        InputValidator::validatePassword('');
    }

    public function testValidatePasswordCountsMultiByteCharactersCorrectly(): void {
        // ۸ کاراکتر فارسی (چندبایتی) باید هم مثل ۸ کاراکتر لاتین معتبر باشد
        $eightPersianChars = 'رمزعبوریک';
        $this->assertGreaterThanOrEqual(8, mb_strlen($eightPersianChars));
        $this->assertSame($eightPersianChars, InputValidator::validatePassword($eightPersianChars));
    }

    // ===== validateIdentifier =====

    public function testValidateIdentifierPreservesAmpersandWithoutEscaping(): void {
        // برخلاف sanitize، اینجا نباید htmlspecialchars اعمال شود چون مقدار
        // مستقیم در prepared statement مقایسه می‌شود (نگاه کنید به کامنت متد).
        $this->assertSame('M&V', InputValidator::validateIdentifier('M&V'));
    }

    public function testValidateIdentifierRejectsEmptyValue(): void {
        $this->expectException(\Exception::class);
        InputValidator::validateIdentifier('   ');
    }

    public function testValidateIdentifierRejectsValueLongerThanMaxLength(): void {
        $this->expectException(\Exception::class);
        InputValidator::validateIdentifier(str_repeat('a', 151));
    }

    public function testValidateIdentifierAcceptsValueAtMaxLength(): void {
        $value = str_repeat('a', 150);
        $this->assertSame($value, InputValidator::validateIdentifier($value));
    }
}
