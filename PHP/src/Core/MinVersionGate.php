<?php
// PHP/src/Core/MinVersionGate.php

declare(strict_types=1);

namespace App\Core;

// قفل نسخه‌ی منقضی مخصوص router جدید (v2)؛ با پاسخ یکدست Response::error، مستقل از AuthenticatesRequests
final class MinVersionGate {
    public static function enforce(Request $request): void {
        // نبود هدر دیگر عبور آزاد نمی‌دهد؛ قبلاً همین باعث دور زدن کامل گیت نسخه می‌شد (DEEP_CODE_AUDIT.md #۳)
        $appVersion = $request->getHeader('X-App-Version');
        if ($appVersion === null || $appVersion === '') {
            Response::error('هدر X-App-Version الزامی است.', 426);
        }

        $configFile = APP_ROOT . '/update_config.php';
        if (!file_exists($configFile)) {
            return;
        }

        $config = include $configFile;
        $minAllowed = $config['min_allowed_version'] ?? $config['min_required_version'] ?? null;
        if (!$minAllowed) {
            return;
        }

        if (version_compare((string)$appVersion, (string)$minAllowed, '<')) {
            Response::error('نسخه‌ی برنامه‌ی شما منسوخ شده است. لطفاً به‌روزرسانی کنید.', 426);
        }
    }
}
