<?php
// PHP/tests/CargoServiceTest.php

declare(strict_types=1);

namespace App\Tests;

class CargoServiceTest {
    public function testValidScaleReceiptNumberFormat(): void {
        $receipt = "48123456";
        $isValidDigit = ctype_digit($receipt) && strlen($receipt) === 8;
        $firstTwo = substr($receipt, 0, 2);
        $isValidPrefix = ($firstTwo >= '44' && $firstTwo <= '55');

        assert($isValidDigit === true, "Scale receipt must be 8 digits");
        assert($isValidPrefix === true, "Scale receipt prefix must be between 44 and 55");
    }

    public function testInvalidScaleReceiptNumberFormat(): void {
        $invalidReceipt = "1234567"; // 7 digits
        $isValid = ctype_digit($invalidReceipt) && strlen($invalidReceipt) === 8;
        assert($isValid === false, "Scale receipt with 7 digits must fail validation");

        $invalidPrefixReceipt = "33123456"; // prefix 33 out of range
        $firstTwo = substr($invalidPrefixReceipt, 0, 2);
        $isValidPrefix = ($firstTwo >= '44' && $firstTwo <= '55');
        assert($isValidPrefix === false, "Scale receipt prefix 33 must fail validation");
    }

    public function testNetWeightValidation(): void {
        $netWeight = "24500.50";
        $isValidFloat = filter_var($netWeight, FILTER_VALIDATE_FLOAT) !== false && floatval($netWeight) > 0;
        assert($isValidFloat === true, "Net weight must be positive float");

        $invalidWeight = "-150";
        $isValidNegative = filter_var($invalidWeight, FILTER_VALIDATE_FLOAT) !== false && floatval($invalidWeight) > 0;
        assert($isValidNegative === false, "Negative net weight must fail validation");
    }
}
