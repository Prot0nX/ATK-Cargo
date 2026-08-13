<?php
// PHP/src/Exceptions/ConflictException.php

declare(strict_types=1);

namespace App\Exceptions;

use Exception;

/**
 * زمانی پرتاب می‌شود که دو درخواست هم‌زمان روی همان حواله تداخل کرده باشند
 * (مثلاً هر دو تلاش کرده‌اند رکورد «ورود» را به «خروج» ببرند، یا هر دو یک
 * حوالهٔ تازه با کلید یکسان درج کرده‌اند). کد HTTP پیش‌فرض 409 است.
 */
class ConflictException extends ApiException {
    public function __construct(string $message, ?Exception $previous = null) {
        parent::__construct($message, 409, null, $previous);
    }
}
