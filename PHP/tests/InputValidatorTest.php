<?php
// PHP/tests/InputValidatorTest.php

declare(strict_types=1);

namespace App\Tests;

use PHPUnit\Framework\TestCase;
use App\Validators\InputValidator;
use App\Exceptions\ApiException;

class InputValidatorTest extends TestCase {
    public function testSanitizeRemovesTagsAndTrims(): void {
        $raw = "  <script>alert('xss')</script> Hello World  ";
        $cleaned = InputValidator::sanitize($raw);
        $this->assertEquals("alert(&#039;xss&#039;) Hello World", $cleaned);
    }

    public function testValidateRequiredPassesWhenAllFieldsPresent(): void {
        $data = ['username' => 'admin', 'password' => '123456'];
        $required = ['username', 'password'];
        
        $this->expectNotToPerformAssertions();
        InputValidator::validateRequired($data, $required);
    }

    public function testValidateRequiredThrowsExceptionWhenMissingField(): void {
        $this->expectException(ApiException::class);
        $this->expectExceptionCode(400);

        $data = ['username' => 'admin'];
        $required = ['username', 'password'];
        InputValidator::validateRequired($data, $required);
    }

    public function testValidateFloatValidatesPositiveFloat(): void {
        $val = InputValidator::validateFloat("123.45", "netWeight");
        $this->assertEquals(123.45, $val);
    }

    public function testValidateFloatThrowsExceptionForNegativeOrZero(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateFloat("-10.5", "netWeight");
    }

    public function testValidateUsernameThrowsExceptionIfTooShort(): void {
        $this->expectException(ApiException::class);
        InputValidator::validateUsername("ab");
    }
}
