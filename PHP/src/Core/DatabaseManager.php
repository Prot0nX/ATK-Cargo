<?php
// PHP/src/Core/DatabaseManager.php

declare(strict_types=1);

namespace App\Core;

use Exception;
use mysqli;
use mysqli_stmt;

class DatabaseManager {
    private mysqli $conn;
    private array $preparedStatements = [];

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
    }

    public function prepare(string $query): mysqli_stmt {
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            // جزئیات خطای mysqli (که می‌تواند نام جدول/ستون را فاش کند) فقط در لاگ
            // سرور ثبت می‌شود؛ پیام بازگردانده‌شده به کلاینت عمومی و بی‌خطر است.
            error_log('DatabaseManager::prepare failed: ' . $this->conn->error);
            throw new Exception('خطا در پردازش درخواست. لطفاً بعداً تلاش کنید.');
        }
        return $stmt;
    }

    public function getPreparedStatement(string $key, string $query): mysqli_stmt {
        if (!isset($this->preparedStatements[$key])) {
            $this->preparedStatements[$key] = $this->prepare($query);
        }
        return $this->preparedStatements[$key];
    }

    public function close(): void {
        // اتصالات مرکزی توسط کلاس Database مدیریت می‌شوند
    }

    public function beginTransaction(): void {
        $this->conn->begin_transaction();
    }

    public function commit(): void {
        $this->conn->commit();
    }

    public function rollback(): void {
        $this->conn->rollback();
    }
}
