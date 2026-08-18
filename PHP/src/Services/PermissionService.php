<?php
// PHP/src/Services/PermissionService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\MicroCache;

// منطق تشخیص سطح دسترسی کاربر از config/permissions.json که پیش‌تر فقط داخل
// AuthController (برای نمایش در پاسخ لاگین) تکرار شده بود؛ اینجا به‌صورت مشترک
// نگه داشته می‌شود تا هم AuthController و هم کنترلرهایی که باید *واقعاً* دسترسی
// نوشتن را گیت کنند (مثل AppApiController) از یک منبع واحد استفاده کنند.
final class PermissionService {
    private const PERMISSIONS_FILE = __DIR__ . '/../../config/permissions.json';

    // کلید MicroCache — PermissionManager.php بعد از هر ذخیره باید همین کلید
    // را forget کند، وگرنه تغییرات مجوز تا انقضای TTL (یا restart) اعمال
    // نمی‌شوند (DEEP_CODE_AUDIT.md #Phase2.7).
    public const CACHE_KEY = 'permissions_file_data';
    private const CACHE_TTL_SECONDS = 30;

    /**
     * دریافت مجموعه دسترسی‌های مؤثر یک کاربر: تنظیمات اختصاصی کاربر در صورت
     * وجود، در غیر این صورت تنظیمات نقش او. از هر دو ساختار فایل (قدیمی/مسطح
     * و جدید/roles+users) پشتیبانی می‌کند تا با فایل موجود سازگار بماند.
     */
    public function getUserPermissions(string $username, string $userType): array {
        $allData = MicroCache::remember(self::CACHE_KEY, self::CACHE_TTL_SECONDS, function () {
            if (!file_exists(self::PERMISSIONS_FILE)) {
                return [];
            }
            $decoded = json_decode((string)file_get_contents(self::PERMISSIONS_FILE), true);
            return is_array($decoded) ? $decoded : [];
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
}
