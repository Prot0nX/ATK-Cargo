<?php
/**
 * PHP/export_schema.php
 *
 * اسکریپت استخراج ساختار کامل پایگاه داده (Tables, Indexes, Keys, Constraints, Views)
 * از دیتابیس سرور cargo_test و ذخیره در فایل schema.sql.
 */

declare(strict_types=1);

require_once __DIR__ . '/config/config.php';

class DatabaseSchemaExporter
{
    private PDO $pdo;
    private string $dbName;
    private string $outputFile;

    public function __construct(string $dbName = 'cargo_test', ?string $outputFile = null)
    {
        $this->dbName = $dbName;
        $this->outputFile = $outputFile ?? __DIR__ . '/schema.sql';

        $host = defined('DB_HOST') ? DB_HOST : 'localhost';
        $user = defined('DB_USER') ? DB_USER : 'root';
        $pass = defined('DB_PASSWORD') ? DB_PASSWORD : '';

        $dsn = "mysql:host={$host};charset=utf8mb4";
        
        try {
            $this->pdo = new PDO($dsn, $user, $pass, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4"
            ]);
        } catch (PDOException $e) {
            throw new RuntimeException("خطا در اتصال به سرور پایگاه داده: " . $e->getMessage(), (int)$e->getCode(), $e);
        }
    }

    /**
     * اجرای فرایند استخراج ساختار و ذخیره در فایل
     */
    public function export(): bool
    {
        // بررسی وجود دیتابیس
        $stmt = $this->pdo->prepare("SHOW DATABASES LIKE :dbname");
        $stmt->execute([':dbname' => $this->dbName]);
        if (!$stmt->fetch()) {
            throw new RuntimeException("پایگاه داده '{$this->dbName}' روی سرور یافت نشد.");
        }

        // انتخاب دیتابیس
        $this->pdo->exec("USE `{$this->dbName}`");

        $sqlContent = [];
        $sqlContent[] = "-- ============================================================";
        $sqlContent[] = "-- ATK-Cargo Database Schema Exporter";
        $sqlContent[] = "-- Database Target: {$this->dbName}";
        $sqlContent[] = "-- Exported Date  : " . date('Y-m-d H:i:s');
        $sqlContent[] = "-- ============================================================";
        $sqlContent[] = "";
        $sqlContent[] = "SET FOREIGN_KEY_CHECKS = 0;";
        $sqlContent[] = "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";";
        $sqlContent[] = "SET NAMES utf8mb4;";
        $sqlContent[] = "SET time_zone = \"+00:00\";";
        $sqlContent[] = "";

        // دریافت لیست جداول
        $tables = $this->getTables();
        $sqlContent[] = "-- ------------------------------------------------------------";
        $sqlContent[] = "-- Tables Structure (" . count($tables) . " tables)";
        $sqlContent[] = "-- ------------------------------------------------------------";
        $sqlContent[] = "";

        foreach ($tables as $table) {
            $sqlContent[] = "--";
            $sqlContent[] = "-- Table structure for table `{$table}`";
            $sqlContent[] = "--";
            $sqlContent[] = "DROP TABLE IF EXISTS `{$table}`;";

            $createStmt = $this->getCreateTableStatement($table);
            // نرمال‌سازی AUTO_INCREMENT به 1 برای داشتن فایل شکیلی از Schema اصلی
            $cleanCreateStmt = preg_replace('/AUTO_INCREMENT=\d+/i', 'AUTO_INCREMENT=1', $createStmt);
            
            $sqlContent[] = $cleanCreateStmt . ";";
            $sqlContent[] = "";
        }

        // استخراج Views در صورت وجود
        $views = $this->getViews();
        if (!empty($views)) {
            $sqlContent[] = "-- ------------------------------------------------------------";
            $sqlContent[] = "-- Views Structure (" . count($views) . " views)";
            $sqlContent[] = "-- ------------------------------------------------------------";
            $sqlContent[] = "";

            foreach ($views as $view) {
                $sqlContent[] = "--";
                $sqlContent[] = "-- View structure for `{$view}`";
                $sqlContent[] = "--";
                $sqlContent[] = "DROP VIEW IF EXISTS `{$view}`;";
                $createViewStmt = $this->getCreateViewStatement($view);
                $sqlContent[] = $createViewStmt . ";";
                $sqlContent[] = "";
            }
        }

        // استخراج Triggers در صورت وجود
        $triggers = $this->getTriggers();
        if (!empty($triggers)) {
            $sqlContent[] = "-- ------------------------------------------------------------";
            $sqlContent[] = "-- Triggers (" . count($triggers) . " triggers)";
            $sqlContent[] = "-- ------------------------------------------------------------";
            $sqlContent[] = "";

            foreach ($triggers as $trigger) {
                $sqlContent[] = "--";
                $sqlContent[] = "-- Trigger `{$trigger['Trigger']}`";
                $sqlContent[] = "--";
                $sqlContent[] = "DROP TRIGGER IF EXISTS `{$trigger['Trigger']}`;";
                $sqlContent[] = "DELIMITER $$";
                $sqlContent[] = "CREATE TRIGGER `{$trigger['Trigger']}` {$trigger['Timing']} {$trigger['Event']} ON `{$trigger['Table']}` FOR EACH ROW {$trigger['Statement']}$$";
                $sqlContent[] = "DELIMITER ;";
                $sqlContent[] = "";
            }
        }

        $sqlContent[] = "SET FOREIGN_KEY_CHECKS = 1;";
        $sqlContent[] = "";

        $finalOutput = implode("\n", $sqlContent);

        $dir = dirname($this->outputFile);
        if (!is_dir($dir)) {
            mkdir($dir, 0755, true);
        }

        $result = file_put_contents($this->outputFile, $finalOutput);
        if ($result === false) {
            throw new RuntimeException("خطا در نوشتن فایل خروجی در مسیر: {$this->outputFile}");
        }

        return true;
    }

    /**
     * دریافت اسامی تمام جداول (بدون Views)
     */
    private function getTables(): array
    {
        $stmt = $this->pdo->query("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'");
        $tables = [];
        while ($row = $stmt->fetch(PDO::FETCH_NUM)) {
            $tables[] = $row[0];
        }
        return $tables;
    }

    /**
     * دریافت اسامی تمام Views
     */
    private function getViews(): array
    {
        $stmt = $this->pdo->query("SHOW FULL TABLES WHERE Table_type = 'VIEW'");
        $views = [];
        while ($row = $stmt->fetch(PDO::FETCH_NUM)) {
            $views[] = $row[0];
        }
        return $views;
    }

    /**
     * دریافت دستور CREATE TABLE
     */
    private function getCreateTableStatement(string $table): string
    {
        $stmt = $this->pdo->query("SHOW CREATE TABLE `{$table}`");
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['Create Table'] ?? '';
    }

    /**
     * دریافت دستور CREATE VIEW
     */
    private function getCreateViewStatement(string $view): string
    {
        $stmt = $this->pdo->query("SHOW CREATE VIEW `{$view}`");
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['Create View'] ?? '';
    }

    /**
     * دریافت لیست تریگرها
     */
    private function getTriggers(): array
    {
        $stmt = $this->pdo->query("SHOW TRIGGERS");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}

// ------------------------------------------------------------------
// نقطه ورود برای اجرا در CLI
// ------------------------------------------------------------------
if (php_sapi_name() === 'cli' || isset($_SERVER['HTTP_HOST'])) {
    $options = getopt('', ['db:', 'output:']);
    $dbName = $options['db'] ?? 'cargo_test';
    $outputFile = $options['output'] ?? __DIR__ . '/schema.sql';

    try {
        echo "در حال استخراج ساختار پایگاه داده '{$dbName}'...\n";
        $exporter = new DatabaseSchemaExporter($dbName, $outputFile);
        $success = $exporter->export();
        if ($success) {
            echo "✅ فایل schema.sql با موفقیت در مسیر زیر ساخته شد:\n{$outputFile}\n";
        }
    } catch (Exception $e) {
        echo "❌ خطا در استخراج ساختار پایگاه داده: " . $e->getMessage() . "\n";
        exit(1);
    }
}
