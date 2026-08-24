<?php
// PHP/src/Repositories/MySQLMetaRepository.php — تنها منبع کوئری‌های read-only متادیتای MySQL برای پنل MySQL_Manager
// (information_schema، SHOW STATUS/VARIABLES، PROCESSLIST). این پنل عمداً فقط روی دیتابیس پیش‌فرض اتصال
// (atk_cargo) کار می‌کند، نه هر دیتابیسی روی سرور — دامنه‌ی کوچک‌تر یعنی سطح حمله‌ی کوچک‌تر.

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Config;
use App\Core\DatabaseManager;
use PDO;
use Throwable;

class MySQLMetaRepository {
    private DatabaseManager $db;
    private string $dbName;

    // آستانه‌های درصد فضای هدررفته برای توصیه‌ی بهینه‌سازی — بند ۱۰/۱۱ spec
    public const FRAGMENTATION_HIGH = 15.0;
    public const FRAGMENTATION_MEDIUM = 5.0;

    public function __construct() {
        $this->db = new DatabaseManager();
        $this->dbName = (string)Config::getInstance()->get('db_name', 'atk_cargo');
    }

    public function databaseName(): string {
        return $this->dbName;
    }

    // اطلاعات کلی دیتابیس فعلی (charset/collation) از information_schema.SCHEMATA
    public function databaseInfo(): ?array {
        $stmt = $this->db->prepare(
            'SELECT SCHEMA_NAME AS name, DEFAULT_CHARACTER_SET_NAME AS charset, DEFAULT_COLLATION_NAME AS collation
             FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = :db'
        );
        $stmt->execute([':db' => $this->dbName]);
        $row = $stmt->fetch();
        return $row === false ? null : $row;
    }

    // لیست کامل جدول‌ها با اندازه/موتور/fragmentation — بدون Pagination چون فقط برای شمارش/جمع در Dashboard استفاده می‌شود
    public function allTablesRaw(): array {
        $stmt = $this->db->prepare(
            "SELECT
                TABLE_NAME AS name,
                ENGINE AS engine,
                TABLE_ROWS AS `rows`,
                DATA_LENGTH AS data_length,
                INDEX_LENGTH AS index_length,
                DATA_FREE AS data_free,
                TABLE_COLLATION AS `collation`,
                CREATE_TIME AS created_at,
                UPDATE_TIME AS updated_at,
                TABLE_COMMENT AS table_comment
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = :db AND TABLE_TYPE = 'BASE TABLE'
            ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC"
        );
        $stmt->execute([':db' => $this->dbName]);
        return $stmt->fetchAll();
    }

    // Pagination سمت سرور برای صفحه‌ی Tables — طبق بند ۳۶ spec، جدول‌های بزرگ یکجا به Browser فرستاده نشوند
    public function paginatedTables(int $page, int $perPage, string $search, string $sort, string $dir): array {
        $perPage = max(1, min($perPage, 200));
        $page = max(1, $page);
        $offset = ($page - 1) * $perPage;

        $sortColumns = [
            'name' => 'TABLE_NAME',
            'rows' => 'TABLE_ROWS',
            'size' => '(DATA_LENGTH + INDEX_LENGTH)',
            'updated_at' => 'UPDATE_TIME',
        ];
        $sortColumn = $sortColumns[$sort] ?? $sortColumns['size'];
        $direction = strtoupper($dir) === 'ASC' ? 'ASC' : 'DESC';

        $where = 'TABLE_SCHEMA = :db AND TABLE_TYPE = \'BASE TABLE\'';
        $params = [':db' => $this->dbName];
        if ($search !== '') {
            $where .= ' AND TABLE_NAME LIKE :search ESCAPE \'\\\\\'';
            $params[':search'] = '%' . $this->escapeLike($search) . '%';
        }

        $countStmt = $this->db->prepare("SELECT COUNT(*) AS total FROM information_schema.TABLES WHERE {$where}");
        $countStmt->execute($params);
        $total = (int)($countStmt->fetch()['total'] ?? 0);

        $stmt = $this->db->prepare(
            "SELECT
                TABLE_NAME AS name, ENGINE AS engine, TABLE_ROWS AS `rows`,
                DATA_LENGTH AS data_length, INDEX_LENGTH AS index_length, DATA_FREE AS data_free,
                TABLE_COLLATION AS `collation`, CREATE_TIME AS created_at, UPDATE_TIME AS updated_at
            FROM information_schema.TABLES
            WHERE {$where}
            ORDER BY {$sortColumn} {$direction}
            LIMIT :limit OFFSET :offset"
        );
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        $stmt->bindValue(':limit', $perPage, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        return [
            'rows' => $stmt->fetchAll(),
            'total' => $total,
            'page' => $page,
            'perPage' => $perPage,
        ];
    }

    // بررسی وجود جدول در دیتابیس هدف — پیش از هر عملیات روی نام جدول، برای جلوگیری از تزریق شناسه (identifier) که PDO prepare نمی‌کند
    public function tableExists(string $tableName): bool {
        $stmt = $this->db->prepare(
            'SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA = :db AND TABLE_NAME = :name AND TABLE_TYPE = \'BASE TABLE\''
        );
        $stmt->execute([':db' => $this->dbName, ':name' => $tableName]);
        return $stmt->fetch() !== false;
    }

    public function tableDetail(string $tableName): ?array {
        $stmt = $this->db->prepare(
            "SELECT
                TABLE_NAME AS name, ENGINE AS engine, TABLE_ROWS AS `rows`,
                DATA_LENGTH AS data_length, INDEX_LENGTH AS index_length, DATA_FREE AS data_free,
                TABLE_COLLATION AS `collation`, CREATE_TIME AS created_at, UPDATE_TIME AS updated_at,
                AUTO_INCREMENT AS auto_increment, TABLE_COMMENT AS table_comment
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = :db AND TABLE_NAME = :name AND TABLE_TYPE = 'BASE TABLE'"
        );
        $stmt->execute([':db' => $this->dbName, ':name' => $tableName]);
        $row = $stmt->fetch();
        return $row === false ? null : $row;
    }

    // ستون‌های یک جدول — برای نمایش جزئیات
    public function tableColumns(string $tableName): array {
        $stmt = $this->db->prepare(
            'SELECT COLUMN_NAME AS name, COLUMN_TYPE AS type, IS_NULLABLE AS is_nullable,
                    COLUMN_KEY AS `key`, COLUMN_DEFAULT AS default_value, EXTRA AS extra
             FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = :db AND TABLE_NAME = :name
             ORDER BY ORDINAL_POSITION ASC'
        );
        $stmt->execute([':db' => $this->dbName, ':name' => $tableName]);
        return $stmt->fetchAll();
    }

    // ایندکس‌های یک جدول یا کل دیتابیس (وقتی $tableName خالی باشد) — بند ۱۹ spec
    public function indexes(string $tableName = ''): array {
        $where = 'TABLE_SCHEMA = :db';
        $params = [':db' => $this->dbName];
        if ($tableName !== '') {
            $where .= ' AND TABLE_NAME = :name';
            $params[':name'] = $tableName;
        }
        $stmt = $this->db->prepare(
            "SELECT
                TABLE_NAME AS table_name, INDEX_NAME AS index_name, NON_UNIQUE AS non_unique,
                SEQ_IN_INDEX AS seq_in_index, COLUMN_NAME AS column_name, CARDINALITY AS cardinality,
                INDEX_TYPE AS index_type
            FROM information_schema.STATISTICS
            WHERE {$where}
            ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX"
        );
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

    // متغیرهای وضعیت سراسری (Connections/Threads/Uptime و…) — در صورت نبود GRANT کافی، آرایه‌ی خالی برمی‌گرداند نه کرش
    public function globalStatus(array $keys): array {
        if (empty($keys)) {
            return [];
        }
        try {
            $placeholders = implode(',', array_fill(0, count($keys), '?'));
            $stmt = $this->db->prepare("SHOW GLOBAL STATUS WHERE Variable_name IN ({$placeholders})");
            $stmt->execute(array_values($keys));
            $result = [];
            foreach ($stmt->fetchAll() as $row) {
                $result[$row['Variable_name']] = $row['Value'];
            }
            return $result;
        } catch (Throwable $e) {
            error_log('MySQLMetaRepository::globalStatus failed: ' . $e->getMessage());
            return [];
        }
    }

    public function globalVariables(array $keys): array {
        if (empty($keys)) {
            return [];
        }
        try {
            $placeholders = implode(',', array_fill(0, count($keys), '?'));
            $stmt = $this->db->prepare("SHOW GLOBAL VARIABLES WHERE Variable_name IN ({$placeholders})");
            $stmt->execute(array_values($keys));
            $result = [];
            foreach ($stmt->fetchAll() as $row) {
                $result[$row['Variable_name']] = $row['Value'];
            }
            return $result;
        } catch (Throwable $e) {
            error_log('MySQLMetaRepository::globalVariables failed: ' . $e->getMessage());
            return [];
        }
    }

    // PROCESSLIST — نیاز به GRANT PROCESS دارد؛ نبود دسترسی نباید کل صفحه را بترکاند
    public function processList(): array {
        try {
            $stmt = $this->db->prepare('SHOW FULL PROCESSLIST');
            $stmt->execute();
            return $stmt->fetchAll();
        } catch (Throwable $e) {
            error_log('MySQLMetaRepository::processList failed: ' . $e->getMessage());
            return [];
        }
    }

    public function mysqlVersion(): string {
        try {
            $stmt = $this->db->prepare('SELECT VERSION() AS v');
            $stmt->execute();
            return (string)($stmt->fetch()['v'] ?? 'نامشخص');
        } catch (Throwable $e) {
            return 'نامشخص';
        }
    }

    // نگهبان امنیتی: مقادیر ورودی جستجو در LIKE باید کاراکترهای ویژه‌شان neutralize شود (الگوی LicenseRepository::escapeLike)
    public function escapeLike(string $value): string {
        return str_replace(['\\', '%', '_'], ['\\\\', '\\%', '\\_'], $value);
    }
}
