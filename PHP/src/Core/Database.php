<?php
// PHP/src/Core/Database.php

declare(strict_types=1);

namespace App\Core;

use PDO;
use PDOException;
use mysqli;
use Exception;

class Database {
    private static ?self $instance = null;
    private ?PDO $pdo = null;
    private ?mysqli $mysqli = null;
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

    /**
     * دریافت کانکشن PDO بهینه‌شده
     */
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
            } catch (PDOException $e) {
                error_log("PDO Connection failed: " . $e->getMessage());
                throw new Exception("خطا در اتصال به پایگاه داده (PDO)");
            }
        }
        return $this->pdo;
    }

    /**
     * دریافت کانکشن mysqli بهینه‌شده جهت حفظ سازگاری با توابع قدیمی
     */
    public function getMysqliConnection(): mysqli {
        if ($this->mysqli === null) {
            $host = $this->config->get('db_host');
            $user = $this->config->get('db_user');
            $pass = $this->config->get('db_pass');
            $name = $this->config->get('db_name');

            // بررسی اتصال دائم مشابه برخی توابع قدیمی
            $this->mysqli = new mysqli($host, $user, $pass, $name);
            
            if ($this->mysqli->connect_error) {
                error_log("Mysqli Connection failed: " . $this->mysqli->connect_error);
                throw new Exception("خطا در اتصال به پایگاه داده (mysqli)");
            }
            
            $this->mysqli->set_charset("utf8mb4");
            
            // بهینه‌سازی‌های پایگاه داده برای سرعت بالاتر
            $this->mysqli->query("SET SESSION sql_mode = 'NO_ENGINE_SUBSTITUTION'");
            $this->mysqli->query("SET time_zone = '+03:30'");
        }
        return $this->mysqli;
    }

    /**
     * بستن اتصالات
     */
    public function closeConnections(): void {
        if ($this->mysqli !== null) {
            $this->mysqli->close();
            $this->mysqli = null;
        }
        $this->pdo = null;
    }
}
