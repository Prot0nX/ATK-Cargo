<?php
// PHP/src/Repositories/UserRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\Database;
use PDO;

class UserRepository {
    private PDO $db;

    public function __construct() {
        $this->db = Database::getInstance()->getPdoConnection();
    }

    // دریافت کاربر بر اساس نام کاربری
    public function getByUsername(string $username): ?array {
        $stmt = $this->db->prepare("SELECT * FROM Users WHERE username = :username LIMIT 1");
        $stmt->execute([':username' => $username]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    // دریافت اطلاعات یک کاربر بر اساس شناسه
    public function getById(int $id): ?array {
        $stmt = $this->db->prepare("SELECT * FROM Users WHERE id = :id LIMIT 1");
        $stmt->execute([':id' => $id]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    // دریافت لیست تمامی کاربران
    public function getAll(): array {
        // سقف سخت‌گیرانه به‌جای صفحه‌بندی کامل، فقط محافظ در برابر رشد غیرمنتظره
        $stmt = $this->db->query("SELECT id, username, fullName, userType, created_at, updated_at FROM Users ORDER BY created_at DESC LIMIT 5000");
        return $stmt->fetchAll();
    }

    // دریافت لیست کاربران بر اساس نوع، برای فهرست مقصدهای چت بدون افشای کل جدول
    public function getByUserType(string $userType): array {
        $stmt = $this->db->prepare("SELECT id, username, fullName, userType FROM Users WHERE userType = :userType ORDER BY fullName ASC");
        $stmt->execute([':userType' => $userType]);
        return $stmt->fetchAll();
    }

    // ایجاد کاربر جدید
    public function create(array $data): int {
        $stmt = $this->db->prepare("
            INSERT INTO Users (username, fullName, password, userType, created_at, updated_at) 
            VALUES (:username, :fullName, :password, :userType, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ");
        
        $stmt->execute([
            ':username' => $data['username'],
            ':fullName' => $data['fullName'],
            ':password' => $data['password'],
            ':userType' => $data['userType']
        ]);
        
        return (int)$this->db->lastInsertId();
    }

    // به‌روزرسانی پسورد کاربر، برای Silent Migration از SHA-256 به bcrypt
    public function updatePassword(int $id, string $hashedPassword): bool {
        $stmt = $this->db->prepare(
            "UPDATE Users SET password = :password, updated_at = CURRENT_TIMESTAMP WHERE id = :id"
        );
        return $stmt->execute([
            ':password' => $hashedPassword,
            ':id'       => $id,
        ]);
    }

    // ستون‌های مجاز برای update()؛ allow-list جلوی تزریق نام ستون دلخواه در آینده را می‌گیرد
    private const UPDATABLE_COLUMNS = ['username', 'fullName', 'password', 'userType'];

    // به‌روزرسانی اطلاعات کاربر
    public function update(int $id, array $updates): bool {
        if (empty($updates)) {
            return false;
        }

        $fields = [];
        $params = [':id' => $id];

        foreach ($updates as $key => $value) {
            if (!in_array($key, self::UPDATABLE_COLUMNS, true)) {
                throw new \InvalidArgumentException("ستون غیرمجاز برای به‌روزرسانی: {$key}");
            }
            $fields[] = "{$key} = :{$key}";
            $params[":{$key}"] = $value;
        }

        $fieldsStr = implode(', ', $fields);
        $stmt = $this->db->prepare("UPDATE Users SET {$fieldsStr}, updated_at = CURRENT_TIMESTAMP WHERE id = :id");
        return $stmt->execute($params);
    }

    // حذف کاربر بر اساس شناسه
    public function delete(int $id): bool {
        $stmt = $this->db->prepare("DELETE FROM Users WHERE id = :id");
        return $stmt->execute([':id' => $id]);
    }

    // دریافت تعداد مدیران سیستم
    public function getAdminCount(): int {
        $stmt = $this->db->query("SELECT COUNT(*) as count FROM Users WHERE userType = 'admin'");
        $result = $stmt->fetch();
        return (int)($result['count'] ?? 0);
    }
}

