<?php
// PHP/src/Exceptions/ConflictException.php

declare(strict_types=1);

namespace App\Exceptions;

use Exception;

// زمانی پرتاب می‌شود که دو درخواست هم‌زمان روی همان حواله تداخل کرده باشند؛ کد پیش‌فرض 409
class ConflictException extends ApiException {
    public function __construct(string $message, ?Exception $previous = null) {
        parent::__construct($message, 409, null, $previous);
    }
}
