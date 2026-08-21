<?php
// PHP/src/Services/PermissionService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\MicroCache;
use App\Repositories\PermissionRepository;

// منطق تشخیص سطح دسترسی کاربر؛ منبع اصلی دو جدول دیتابیس است و به‌صورت مشترک بین کنترلرها استفاده می‌شود
final class PermissionService {
    private const PERMISSIONS_FILE = __DIR__ . '/../../config/permissions.json';

    // کلید MicroCache؛ PermissionManager.php باید بعد از هر ذخیره همین کلید را forget کند
    public const CACHE_KEY = 'permissions_file_data';
    private const CACHE_TTL_SECONDS = 30;

    private ?PermissionRepository $repository;

    // پارامتر اختیاری فقط برای تست واحد (تزریق mock)؛ production بدون آرگومان صدا زده می‌شود
    public function __construct(?PermissionRepository $repository = null) {
        $this->repository = $repository;
    }

    // دریافت مجموعه دسترسی‌های مؤثر یک کاربر: تنظیمات اختصاصی او، وگرنه تنظیمات نقشش
    public function getUserPermissions(string $username, string $userType): array {
        $allData = MicroCache::remember(self::CACHE_KEY, self::CACHE_TTL_SECONDS, function () {
            return $this->loadFromDatabase() ?? $this->loadFromJsonFile();
        });

        if (isset($allData['roles'])) {
            return $allData['users'][$username] ?? $allData['roles'][$userType] ?? [];
        }

        return $allData[$userType] ?? [];
    }

    public function hasPermission(string $username, string $userType, string $feature): bool {
        $permissions = $this->getUserPermissions($username, $userType);
        return $permissions[$feature] ?? false;
    }

    // اگر جداول مجوز نبودند یا DB در دسترس نبود، null برمی‌گرداند تا به فایل قدیمی permissions.json برگردد
    private function loadFromDatabase(): ?array {
        try {
            $repo = $this->repository ?? new PermissionRepository();
            return [
                'roles' => $repo->getAllRolePermissions(),
                'users' => $repo->getAllUserPermissions(),
            ];
        } catch (\Throwable $e) {
            error_log('PermissionService: DB unavailable/not migrated, falling back to permissions.json: ' . $e->getMessage());
            return null;
        }
    }

    private function loadFromJsonFile(): array {
        if (!file_exists(self::PERMISSIONS_FILE)) {
            return [];
        }
        $decoded = json_decode((string)file_get_contents(self::PERMISSIONS_FILE), true);
        return is_array($decoded) ? $decoded : [];
    }
}
