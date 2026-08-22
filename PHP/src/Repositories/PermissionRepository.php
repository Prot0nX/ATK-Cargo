<?php
// PHP/src/Repositories/PermissionRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\DatabaseManager;
use PDO;

// دسترسی به جداول role_permissions/user_permissions؛ عمداً بدون fallback، فقط DB
final class PermissionRepository {
    private DatabaseManager $db;

    public function __construct(?DatabaseManager $db = null) {
        $this->db = $db ?? new DatabaseManager();
    }

    /** @return array<string, array<string, bool>> نگاشت role => feature => allowed */
    public function getAllRolePermissions(): array {
        $stmt = $this->db->prepare("SELECT role, feature, allowed FROM role_permissions");
        $stmt->execute();
        $out = [];
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $out[$row['role']][$row['feature']] = (bool)$row['allowed'];
        }
        return $out;
    }

    /** @return array<string, array<string, bool>> نگاشت username => feature => allowed */
    public function getAllUserPermissions(): array {
        $stmt = $this->db->prepare("SELECT username, feature, allowed FROM user_permissions");
        $stmt->execute();
        $out = [];
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $out[$row['username']][$row['feature']] = (bool)$row['allowed'];
        }
        return $out;
    }

    /** @param array<string, bool> $permissions */
    public function saveRolePermissions(string $role, array $permissions): void {
        $this->db->beginTransaction();
        try {
            $del = $this->db->prepare("DELETE FROM role_permissions WHERE role = ?");
            $del->execute([$role]);

            $ins = $this->db->prepare("INSERT INTO role_permissions (role, feature, allowed) VALUES (?, ?, ?)");
            foreach ($permissions as $feature => $allowed) {
                $ins->execute([$role, $feature, $allowed ? 1 : 0]);
            }
            $this->db->commit();
        } catch (\Throwable $e) {
            $this->db->rollback();
            throw $e;
        }
    }

    /** @param array<string, bool> $permissions */
    public function saveUserPermissions(string $username, array $permissions): void {
        $this->db->beginTransaction();
        try {
            $del = $this->db->prepare("DELETE FROM user_permissions WHERE username = ?");
            $del->execute([$username]);

            $ins = $this->db->prepare("INSERT INTO user_permissions (username, feature, allowed) VALUES (?, ?, ?)");
            foreach ($permissions as $feature => $allowed) {
                $ins->execute([$username, $feature, $allowed ? 1 : 0]);
            }
            $this->db->commit();
        } catch (\Throwable $e) {
            $this->db->rollback();
            throw $e;
        }
    }

    // حذف تنظیمات اختصاصی کاربر، بازگشت به وراثت از نقش
    public function deleteUserPermissions(string $username): void {
        $stmt = $this->db->prepare("DELETE FROM user_permissions WHERE username = ?");
        $stmt->execute([$username]);
    }

    public function getLastUpdatedAt(): ?string {
        $stmt = $this->db->prepare(
            "SELECT MAX(t.updated_at) AS last_updated FROM (
                SELECT updated_at FROM role_permissions
                UNION ALL
                SELECT updated_at FROM user_permissions
            ) t"
        );
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['last_updated'] ?? null;
    }
}
