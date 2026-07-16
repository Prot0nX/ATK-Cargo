<?php

declare(strict_types=1);

namespace AtkCargo\Core;

use PDO;
use mysqli;
use Exception;
use PDOException;

/**
 * Singleton Database Manager supporting both PDO and mysqli connections
 */
class Database
{
    private static ?Database $instance = null;
    private ?PDO $pdo = null;
    private ?mysqli $mysqli = null;

    private function __construct()
    {
        // Private constructor to prevent direct instantiation
    }

    /**
     * Get instance of Database Manager
     */
    public static function getInstance(): Database
    {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    /**
     * Get PDO database connection
     * 
     * @throws Exception
     */
    public function getPdo(): PDO
    {
        if ($this->pdo === null) {
            $host = Config::get('db.host');
            $dbName = Config::get('db.name');
            $user = Config::get('db.user');
            $pass = Config::get('db.password');

            try {
                $dsn = "mysql:host={$host};dbname={$dbName};charset=utf8mb4";
                $this->pdo = new PDO($dsn, $user, $pass, [
                    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                    PDO::ATTR_EMULATE_PREPARES => false,
                ]);
            } catch (PDOException $e) {
                error_log("PDO Connection Error: " . $e->getMessage());
                throw new Exception("خطا در اتصال به پایگاه داده (PDO)");
            }
        }
        return $this->pdo;
    }

    /**
     * Get mysqli database connection
     * 
     * @throws Exception
     */
    public function getMysqli(): mysqli
    {
        if ($this->mysqli === null) {
            $host = Config::get('db.host');
            $dbName = Config::get('db.name');
            $user = Config::get('db.user');
            $pass = Config::get('db.password');

            // Use persistent connections if defined as 'p:host' in config or fallback
            $connHost = (strpos($host, 'p:') === 0) ? $host : 'p:' . $host;
            
            $this->mysqli = new mysqli($connHost, $user, $pass, $dbName);

            if ($this->mysqli->connect_error) {
                error_log("mysqli Connection Error: " . $this->mysqli->connect_error);
                throw new Exception("خطا در اتصال به پایگاه داده (mysqli): " . $this->mysqli->connect_error);
            }

            $this->mysqli->set_charset("utf8mb4");
        }
        return $this->mysqli;
    }

    /**
     * Close active connections
     */
    public function closeConnections(): void
    {
        if ($this->mysqli !== null) {
            $this->mysqli->close();
            $this->mysqli = null;
        }
        $this->pdo = null;
    }

    public function __destruct()
    {
        $this->closeConnections();
    }
}
