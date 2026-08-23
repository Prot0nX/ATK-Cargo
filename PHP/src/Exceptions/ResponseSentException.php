<?php
// PHP/src/Exceptions/ResponseSentException.php

declare(strict_types=1);

namespace App\Exceptions;

use Exception;

// جایگزین قابل‌catch برای exit در Response::json هنگام اجرای PHPUnit؛ فقط زیر TESTING_MODE فعال می‌شود
class ResponseSentException extends Exception {
    private array $payload;
    private int $statusCode;

    public function __construct(array $payload, int $statusCode) {
        parent::__construct('Response::json called with TESTING_MODE active');
        $this->payload = $payload;
        $this->statusCode = $statusCode;
    }

    public function getPayload(): array {
        return $this->payload;
    }

    public function getStatusCode(): int {
        return $this->statusCode;
    }
}
