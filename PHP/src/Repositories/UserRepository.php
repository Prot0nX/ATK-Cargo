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

    /**
     * دریافت کاربر بر اساس نام کاربری
     */
    public function getByUsername(string $username): ?array {
        $stmt = $this->db->prepare("SELECT * FROM Users WHERE username = :username LIMIT 1");
        $stmt->execute([':username' => $username]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    /**
     * دریافت اطلاعات یک کاربر بر اساس شناسه
     */
    public function getById(int $id): ?array {
        $stmt = $this->db->prepare("SELECT * FROM Users WHERE id = :id LIMIT 1");
        $stmt->execute([':id' => $id]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    /**
     * دریافت لیست تمامی کاربران
     */
    public function getAll(): array {
        $stmt = $this->db->query("SELECT id, username, fullName, userType, created_at, updated_at FROM Users ORDER BY created_at DESC");
        return $stmt->fetchAll();
    }

    /**
     * ایجاد کاربر جدید
     */
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

    /**
     * به‌روزرسانی اطلاعات کاربر
     */
    public function update(int $id, array $updates): bool {
        if (empty($updates)) {
            return false;
        }

        $fields = [];
        $params = [':id' => $id];
        
        foreach ($updates as $key => $value) {
            $fields[] = "{$key} = :{$key}";
            $params[":{$key}"] = $value;
        }
        
        $fieldsStr = implode(', ', $fields);
        $stmt = $this->db->prepare("UPDATE Users SET {$fieldsStr}, updated_at = CURRENT_TIMESTAMP WHERE id = :id");
        return $stmt->execute($params);
    }

    /**
     * حذف کاربر بر اساس شناسه
     */
    public function delete(int $id): bool {
        $stmt = $this->db->prepare("DELETE FROM Users WHERE id = :id");
        return $stmt->execute([':id' => $id]);
    }

    /**
     * دریافت تعداد مدیران سیستم
     */
    public function getAdminCount(): int {
        $stmt = $this->db->query("SELECT COUNT(*) as count FROM Users WHERE userType = 'admin'");
        $result = $stmt->fetch();
        return (int)($result['count'] ?? 0);
    }
}
