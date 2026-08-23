<?php
// PHP/src/Core/DatabaseManager.php

declare(strict_types=1);

namespace App\Core;

use Exception;
use PDO;
use PDOException;
use PDOStatement;

class DatabaseManager {
    private PDO $conn;
    private array $preparedStatements = [];

    public function __construct() {
        $this->conn = Database::getInstance()->getPdoConnection();
    }

    public function prepare(string $query): PDOStatement {
        try {
            return $this->conn->prepare($query);
        } catch (PDOException $e) {
 // جزئیات خطای PDO فقط در لاگ سرور ثبت می‌شود، پاسخ کلاینت عمومی است
            error_log('DatabaseManager::prepare failed: ' . $e->getMessage());
            throw new Exception('خطا در پردازش درخواست. لطفاً بعداً تلاش کنید.');
        }
    }

    public function getPreparedStatement(string $key, string $query): PDOStatement {
        if (!isset($this->preparedStatements[$key])) {
            $this->preparedStatements[$key] = $this->prepare($query);
        }
        return $this->preparedStatements[$key];
    }

    public function close(): void {
 // اتصالات مرکزی توسط کلاس Database مدیریت می‌شوند
    }

    public function beginTransaction(): void {
        $this->conn->beginTransaction();
    }

    public function commit(): void {
        $this->conn->commit();
    }

    public function rollback(): void {
        $this->conn->rollBack();
    }
}
