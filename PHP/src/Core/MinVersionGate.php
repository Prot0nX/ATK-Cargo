<?php
// PHP/src/Core/MinVersionGate.php

declare(strict_types=1);

namespace App\Core;

/**
 * قفل نسخه‌ی منقضی (min_allowed_version) — نسخه‌ی مخصوص router جدید (v2،
 * ApiAuthGate). عمداً منطق AuthenticatesRequests::enforceMinAppVersion
 * (مسیر v1/target=) را دوباره‌نویسی می‌کند به‌جای reuse مستقیم، چون آن یکی
 * پاسخ خطا را از طریق sendAuthErrorResponse قابل‌override هر کنترلر می‌فرستد
 * (برای حفظ قرارداد کلاینت فعلی)، در حالی که v2 از همان ابتدا با یک شکل پاسخ
 * یکدست (Response::error) طراحی شده و نباید به آن قرارداد قدیمی مقید باشد.
 * وقتی هدر X-App-Version ارسال نشود (نصب‌های قدیمی‌تر) عبور مجاز است.
 */
final class MinVersionGate {
    public static function enforce(Request $request): void {
        $appVersion = $request->getHeader('X-App-Version');
        if (!$appVersion) {
            return;
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
