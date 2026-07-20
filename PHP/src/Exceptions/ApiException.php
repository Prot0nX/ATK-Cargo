<?php
// PHP/src/Exceptions/ApiException.php

declare(strict_types=1);

namespace App\Exceptions;

use Exception;

class ApiException extends Exception {
    protected int $statusCode;
    protected ?array $details;

    public function __construct(string $message, int $statusCode = 400, ?array $details = null, ?Exception $previous = null) {
        parent::__construct($message, 0, $previous);
        $this->statusCode = $statusCode;
        $this->details = $details;
    }

    public function getStatusCode(): int {
        return $this->statusCode;
    }

    public function getDetails(): ?array {
        return $this->details;
    }
}
