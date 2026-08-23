<?php
// PHP/src/Core/Database.php

declare(strict_types=1);

namespace App\Core;

use PDO;
use PDOException;
use Exception;

class Database {
    private static ?self $instance = null;
    private ?PDO $pdo = null;
    private Config $config;

    private function __construct() {
        $this->config = Config::getInstance();
    }

    public static function getInstance(): self {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    // دریافت کانکشن PDO بهینه‌شده
    public function getPdoConnection(): PDO {
        if ($this->pdo === null) {
            try {
                $dsn = sprintf(
                    "mysql:host=%s;dbname=%s;charset=utf8mb4",
                    $this->config->get('db_host'),
                    $this->config->get('db_name')
                );
                
                $this->pdo = new PDO($dsn, $this->config->get('db_user'), $this->config->get('db_pass'), [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false,
                    PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci"
                ]);

 // هم‌راستا با نشست mysqli قبلی (SET NAMES نمی‌تواند در همان دستور با سایر assignmentها ترکیب شود، پس جدا اجرا می‌شود) تا مهاجرت مصرف‌کننده‌ها به PDO رفتار خاموش را تغییر ندهد
                $this->pdo->exec("SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION'");
                $this->pdo->exec("SET time_zone = '+03:30'");
            } catch (PDOException $e) {
                error_log("PDO Connection failed: " . $e->getMessage());
                throw new Exception("خطا در اتصال به پایگاه داده (PDO)");
            }
        }
        return $this->pdo;
    }

    // بستن اتصالات
    public function closeConnections(): void {
        $this->pdo = null;
    }
}
