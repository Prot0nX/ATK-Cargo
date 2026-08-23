<?php
// PHP/Realtime Dashboard/api.php — تنها endpoint JSON این پنل؛ فقط auth‌گیت می‌کند و مستقیماً AnalyticsController موجود (همان که اپ موبایل استفاده می‌کند) را صدا می‌زند، بدون تکرار منطق شیفت/کوئری.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Request;
use App\Core\Response;
use App\Controllers\AnalyticsController;

rtd_require_auth_json();

$request = new Request();

if (!$request->isGet()) {
    Response::error('این پنل فقط عملیات خواندنی دارد؛ روش درخواست معتبر نیست.', 405);
}

// تنها اکشنی که این داشبورد نیاز دارد؛ AnalyticsController خودش کش (MicroCache) و پاسخ ETag/304 را مدیریت می‌کند.
$_GET['action'] = 'getRealTimeData';
(new AnalyticsController())->handleRealTimeLoadingData(RTD_ACTOR);
