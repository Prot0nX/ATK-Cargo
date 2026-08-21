<?php
// PHP/src/Repositories/LicenseRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

// تنها منبع حقیقت کوئری‌های جدول licenses؛ برای جلوگیری از تعریف‌های ناهماهنگ «لایسنس معتبر» بین پنل و اپ
class LicenseRepository {
    // وضعیت مؤثر لایسنس؛ تعریف واحدی که هم پنل ادمین و هم LicenseController از آن استفاده می‌کنند
    private const STATUS_EXPR = "
        CASE
            WHEN is_active = 0 THEN 'inactive'
            WHEN expires_at IS NOT NULL AND expires_at <= NOW() THEN 'expired'
            ELSE 'active'
        END AS effective_status";

    // سقف سخت‌گیرانه به‌جای صفحه‌بندی کامل، مشابه UserRepository::getAll()
    private const MAX_ROWS = 5000;

    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

    public function findByKey(string $licenseKey): ?array {
        $stmt = $this->db->prepare(
            "SELECT *, " . self::STATUS_EXPR . " FROM licenses WHERE license_key = :key LIMIT 1"
        );
        $stmt->execute([':key' => $licenseKey]);
        $row = $stmt->fetch();
        return $row ?: null;
    }

    public function findById(int $id): ?array {
        $stmt = $this->db->prepare(
            "SELECT *, " . self::STATUS_EXPR . " FROM licenses WHERE id = :id LIMIT 1"
        );
        $stmt->execute([':id' => $id]);
        $row = $stmt->fetch();
        return $row ?: null;
    }

    // لیست لایسنس‌ها با جستجو و فیلتر وضعیت، هر دو سمت سرور
    /**
     * @param string|null $status یکی از active|expired|inactive یا null برای همه
     */
    public function listAll(?string $search = null, ?string $status = null): array {
        $sql = "SELECT *, " . self::STATUS_EXPR . " FROM licenses";
        $params = [];
        $where = [];

        if ($search !== null && $search !== '') {
            // سه placeholder مجزا لازم است چون EMULATE_PREPARES=false تکرار یک نام را با خطا رد می‌کند
            $where[] = '(company_name LIKE :search_company'
                . ' OR license_key LIKE :search_key'
                . ' OR contact_name LIKE :search_contact)';
            $needle = '%' . $this->escapeLike($search) . '%';
            $params[':search_company'] = $needle;
            $params[':search_key'] = $needle;
            $params[':search_contact'] = $needle;
        }

        if ($status === 'inactive') {
            $where[] = 'is_active = 0';
        } elseif ($status === 'expired') {
            $where[] = 'is_active = 1 AND expires_at IS NOT NULL AND expires_at <= NOW()';
        } elseif ($status === 'active') {
            $where[] = 'is_active = 1 AND (expires_at IS NULL OR expires_at > NOW())';
        }

        if ($where !== []) {
            $sql .= ' WHERE ' . implode(' AND ', $where);
        }

        $sql .= ' ORDER BY created_at DESC LIMIT ' . self::MAX_ROWS;

        $stmt = $this->db->prepare($sql);
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

    // شمارش تفکیکی وضعیت‌ها به همراه فعالیت دو ماه اخیر، در یک رفت‌وبرگشت
    /**
     * @return array{total:int,active:int,expired:int,inactive:int,this_month:int,last_month:int}
     */
    public function stats(): array {
        $sql = "SELECT
                COUNT(*) AS total,
                SUM(is_active = 1 AND (expires_at IS NULL OR expires_at > NOW())) AS active,
                SUM(is_active = 1 AND expires_at IS NOT NULL AND expires_at <= NOW()) AS expired,
                SUM(is_active = 0) AS inactive,
                SUM(created_at >= DATE_FORMAT(NOW(), '%Y-%m-01')) AS this_month,
                SUM(created_at >= DATE_FORMAT(NOW() - INTERVAL 1 MONTH, '%Y-%m-01')
                    AND created_at <  DATE_FORMAT(NOW(), '%Y-%m-01')) AS last_month
            FROM licenses";
        $row = $this->db->query($sql)->fetch() ?: [];

        // SUM() روی جدول خالی NULL برمی‌گرداند نه صفر
        return [
            'total'      => (int)($row['total'] ?? 0),
            'active'     => (int)($row['active'] ?? 0),
            'expired'    => (int)($row['expired'] ?? 0),
            'inactive'   => (int)($row['inactive'] ?? 0),
            'this_month' => (int)($row['this_month'] ?? 0),
            'last_month' => (int)($row['last_month'] ?? 0),
        ];
    }

    public function keyExists(string $licenseKey): bool {
        $stmt = $this->db->prepare("SELECT 1 FROM licenses WHERE license_key = :key LIMIT 1");
        $stmt->execute([':key' => $licenseKey]);
        return $stmt->fetchColumn() !== false;
    }

    // یکتایی نام شرکت در لایه‌ی اپلیکیشن اعمال می‌شود؛ exceptId برای حالت ویرایش رکورد خودش
    public function companyNameExists(string $companyName, ?int $exceptId = null): bool {
        $sql = "SELECT 1 FROM licenses WHERE company_name = :name";
        $params = [':name' => $companyName];
        if ($exceptId !== null) {
            $sql .= " AND id <> :id";
            $params[':id'] = $exceptId;
        }
        $stmt = $this->db->prepare($sql . " LIMIT 1");
        $stmt->execute($params);
        return $stmt->fetchColumn() !== false;
    }

    /**
     * @param array<string,mixed> $data کلیدهایش قبلاً در LicenseAdminService غربال و اعتبارسنجی شده‌اند
     * @return int شناسه‌ی رکورد ساخته‌شده
     */
    public function create(string $licenseKey, array $data): int {
        $stmt = $this->db->prepare(
            "INSERT INTO licenses
                (license_key, company_name, plan, expires_at, contact_name, contact_phone, contact_email, notes)
             VALUES
                (:key, :company, :plan, :expires, :cname, :cphone, :cemail, :notes)"
        );
        $stmt->execute([
            ':key'     => $licenseKey,
            ':company' => $data['company_name'],
            ':plan'    => $data['plan'],
            ':expires' => $data['expires_at'],
            ':cname'   => $data['contact_name'],
            ':cphone'  => $data['contact_phone'],
            ':cemail'  => $data['contact_email'],
            ':notes'   => $data['notes'],
        ]);
        return (int)$this->db->lastInsertId();
    }

    /**
     * @param array<string,mixed> $data
     */
    public function update(int $id, array $data): void {
        $stmt = $this->db->prepare(
            "UPDATE licenses SET
                company_name  = :company,
                plan          = :plan,
                expires_at    = :expires,
                contact_name  = :cname,
                contact_phone = :cphone,
                contact_email = :cemail,
                notes         = :notes
             WHERE id = :id"
        );
        $stmt->execute([
            ':company' => $data['company_name'],
            ':plan'    => $data['plan'],
            ':expires' => $data['expires_at'],
            ':cname'   => $data['contact_name'],
            ':cphone'  => $data['contact_phone'],
            ':cemail'  => $data['contact_email'],
            ':notes'   => $data['notes'],
            ':id'      => $id,
        ]);
    }

    public function setActive(int $id, bool $isActive): void {
        $stmt = $this->db->prepare("UPDATE licenses SET is_active = :active WHERE id = :id");
        $stmt->execute([':active' => $isActive ? 1 : 0, ':id' => $id]);
    }

    public function delete(int $id): bool {
        $stmt = $this->db->prepare("DELETE FROM licenses WHERE id = :id");
        $stmt->execute([':id' => $id]);
        return $stmt->rowCount() > 0;
    }

    public function touchLastCheck(string $licenseKey): void {
        $stmt = $this->db->prepare("UPDATE licenses SET last_check = CURRENT_TIMESTAMP WHERE license_key = :key");
        $stmt->execute([':key' => $licenseKey]);
    }

    // خنثی‌سازی wildcardهای LIKE در ورودی کاربر تا به‌عنوان الگو تفسیر نشود
    private function escapeLike(string $value): string {
        return str_replace(['\\', '%', '_'], ['\\\\', '\\%', '\\_'], $value);
    }
}
