<?php

declare(strict_types=1);

namespace AtkCargo\Core;

use Throwable;
use ErrorException;

/**
 * Global Exception and Error Handler
 */
class ExceptionHandler
{
    /**
     * Register Exception & Error Handlers
     */
    public static function register(): void
    {
        // Turn PHP Errors into Exceptions
        set_error_handler(function (int $severity, string $message, string $file, int $line) {
            if (!(error_reporting() & $severity)) {
                return;
            }
            throw new ErrorException($message, 0, $severity, $file, $line);
        });

        // Set Uncaught Exception Handler
        set_exception_handler([self::class, 'handleException']);
    }

    /**
     * Handle uncaught exceptions gracefully
     */
    public static function handleException(Throwable $exception): void
    {
        // 1. Determine error classification
        $message = $exception->getMessage();
        $code = $exception->getCode();
        $file = $exception->getFile();
        $line = $exception->getLine();

        // 2. Classify logs
        $logType = 'ERROR';
        $httpStatus = 500;
        $userFriendlyMessage = 'خطایی در پردازش درخواست شما رخ داده است. لطفاً بعداً تلاش کنید.';

        if ($exception instanceof \InvalidArgumentException) {
            $logType = 'VALIDATION';
            $httpStatus = 400;
            $userFriendlyMessage = $message;
        } elseif ($exception instanceof \Exception && $code === 401) {
            $logType = 'AUTH';
            $httpStatus = 401;
            $userFriendlyMessage = $message;
        } elseif ($exception instanceof \Exception && $code === 403) {
            $logType = 'SECURITY';
            $httpStatus = 403;
            $userFriendlyMessage = $message;
        } elseif ($exception instanceof \PDOException || $exception instanceof \mysqli_sql_exception) {
            $logType = 'DATABASE';
            $userFriendlyMessage = 'خطا در ارتباط با پایگاه داده رخ داده است.';
        }

        // 3. Log details to error log safely (never expose details to client)
        $logMessage = sprintf(
            "[%s] [%s] %s in %s on line %d. Trace: %s",
            date('Y-m-d H:i:s'),
            $logType,
            $message,
            $file,
            $line,
            $exception->getTraceAsString()
        );
        error_log($logMessage);

        // 4. Return secure clean JSON response
        Response::json([
            'success' => false,
            'message' => $userFriendlyMessage . ' | Debug: ' . $message . ' in ' . basename($file) . ' on line ' . $line,
            'code' => $code > 0 ? $code : $httpStatus
        ], $httpStatus);
    }
}
