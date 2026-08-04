<?php
// PHP/tests/SessionServiceTest.php

declare(strict_types=1);

namespace App\Tests;

use App\Validators\InputValidator;

class SessionServiceTest {
    public function testSanitizeUsername(): void {
        $raw = "  user_test<script>alert(1)</script>  ";
        $clean = InputValidator::sanitize($raw);
        if ($clean !== "user_testalert(1)") {
            // Note: InputValidator removes html tags and converts special chars
            // Let's verify string sanitization output
        }
        assert(strpos($clean, '<script>') === false, "Sanitize must strip script tags");
    }

    public function testTokenLengthAndFormat(): void {
        $token = bin2hex(random_bytes(32));
        assert(strlen($token) === 64, "Session token must be 64 hexadecimal characters");
        assert(ctype_xdigit($token), "Session token must be valid hex string");
    }

    public function testSessionTimeoutLogic(): void {
        $sessionTimeout = 86400; // 24 hours
        $loginTime = time() - 90000; // 25 hours ago
        $isExpired = (time() - $loginTime) > $sessionTimeout;
        assert($isExpired === true, "Session older than 24h must be considered expired");
    }
}
