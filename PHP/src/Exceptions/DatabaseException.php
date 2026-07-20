<?php
// PHP/src/Exceptions/DatabaseException.php

declare(strict_types=1);

namespace App\Exceptions;

use Exception;

class DatabaseException extends ApiException {
    public function __construct(string $message = 'خطایی در اجرای عملیات پایگاه داده رخ داده است', ?Exception $previous = null) {
        parent::__construct($message, 500, null, $previous);
    }
}
