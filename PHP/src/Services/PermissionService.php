<?php
// PHP/src/Services/PermissionService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\MicroCache;
use App\Repositories\PermissionRepository;

// منطق تشخیص سطح دسترسی کاربر. منبع اصلی از Phase 4.7 دو جدول دیتابیس
// (role_permissions/user_permissions) است، نه دیگر config/permissions.json —
// اینجا به‌صورت مشترک نگه داشته می‌شود تا هم AuthController و هم کنترلرهایی
// که باید *واقعاً* دسترسی نوشتن را گیت کنند (مثل AppApiController) از یک
// منبع واحد استفاده کنند.
final class PermissionService {
    private const PERMISSIONS_FILE = __DIR__ . '/../../config/permissions.json';

    // کلید MicroCache — PermissionManager.php بعد از هر ذخیره باید همین کلید
    // را forget کند، وگرنه تغییرات مجوز تا انقضای TTL (یا restart) اعمال
    // نمی‌شوند (DEEP_CODE_AUDIT.md #Phase2.7).
    public const CACHE_KEY = 'permissions_file_data';
    private const CACHE_TTL_SECONDS = 30;

    private ?PermissionRepository $repository;

    // پارامتر اختیاری فقط برای تست واحد (تزریق mock بدون DB واقعی)؛ کد
    // production با new PermissionService() بدون آرگومان کار می‌کند.
    public function __construct(?PermissionRepository $repository = null) {
        $this->repository = $repository;
    }

    /**
     * دریافت مجموعه دسترسی‌های مؤثر یک کاربر: تنظیمات اختصاصی کاربر در صورت
     * وجود، در غیر این صورت تنظیمات نقش او.
     */
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

    /**
     * جداول role_permissions/user_permissions هنوز ساخته نشده باشند
     * (migrations/2026_08_19_permissions_to_database.sql اجرا نشده) یا DB در
     * دسترس نباشد → null، یعنی «به فایل قدیمی برگرد»، نه کرش کل مسیر احراز
     * هویت/مجوز. این fallback عمداً موقت نیست؛ تا وقتی migration روی سرور
     * تولید اجرا نشود، رفتار قبلی دقیقاً حفظ می‌شود.
     */
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
