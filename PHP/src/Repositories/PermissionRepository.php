<?php
// PHP/src/Repositories/PermissionRepository.php

declare(strict_types=1);

namespace App\Repositories;

use App\Core\DatabaseManager;

/**
 * دسترسی به جداول role_permissions/user_permissions (DEEP_CODE_AUDIT.md
 * #Phase4.7 — جایگزین config/permissions.json). هر متد prepare() را از
 * DatabaseManager می‌گیرد که خودش در صورت نبود جدول (migration اجرا نشده)
 * یک Exception عادی می‌اندازد؛ فراخوان (PermissionService) این استثنا را
 * می‌گیرد و به فایل JSON قدیمی برمی‌گردد — این کلاس خودش هیچ fallback ای
 * ندارد، عمداً "فقط DB" است.
 */
final class PermissionRepository {
    private DatabaseManager $db;

    public function __construct(?DatabaseManager $db = null) {
        $this->db = $db ?? new DatabaseManager();
    }

    /** @return array<string, array<string, bool>> نگاشت role => feature => allowed */
    public function getAllRolePermissions(): array {
        $stmt = $this->db->prepare("SELECT role, feature, allowed FROM role_permissions");
        $stmt->execute();
        $result = $stmt->get_result();
        $out = [];
        while ($row = $result->fetch_assoc()) {
            $out[$row['role']][$row['feature']] = (bool)$row['allowed'];
        }
        $stmt->close();
        return $out;
    }

    /** @return array<string, array<string, bool>> نگاشت username => feature => allowed */
    public function getAllUserPermissions(): array {
        $stmt = $this->db->prepare("SELECT username, feature, allowed FROM user_permissions");
        $stmt->execute();
        $result = $stmt->get_result();
        $out = [];
        while ($row = $result->fetch_assoc()) {
            $out[$row['username']][$row['feature']] = (bool)$row['allowed'];
        }
        $stmt->close();
        return $out;
    }

    /** @param array<string, bool> $permissions */
    public function saveRolePermissions(string $role, array $permissions): void {
        $this->db->beginTransaction();
        try {
            $del = $this->db->prepare("DELETE FROM role_permissions WHERE role = ?");
            $del->bind_param("s", $role);
            $del->execute();
            $del->close();

            $ins = $this->db->prepare("INSERT INTO role_permissions (role, feature, allowed) VALUES (?, ?, ?)");
            foreach ($permissions as $feature => $allowed) {
                $allowedInt = $allowed ? 1 : 0;
                $ins->bind_param("ssi", $role, $feature, $allowedInt);
                $ins->execute();
            }
            $ins->close();
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
            $del->bind_param("s", $username);
            $del->execute();
            $del->close();

            $ins = $this->db->prepare("INSERT INTO user_permissions (username, feature, allowed) VALUES (?, ?, ?)");
            foreach ($permissions as $feature => $allowed) {
                $allowedInt = $allowed ? 1 : 0;
                $ins->bind_param("ssi", $username, $feature, $allowedInt);
                $ins->execute();
            }
            $ins->close();
            $this->db->commit();
        } catch (\Throwable $e) {
            $this->db->rollback();
            throw $e;
        }
    }

    /** حذف تنظیمات اختصاصی کاربر — بازگشت به وراثت از نقش */
    public function deleteUserPermissions(string $username): void {
        $stmt = $this->db->prepare("DELETE FROM user_permissions WHERE username = ?");
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $stmt->close();
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
        $row = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $row['last_updated'] ?? null;
    }
}
