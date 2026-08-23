<?php
// PHP/Monitoring/api.php — تنها endpoint JSON داشبورد مانیتورینگ سیستم.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Controllers\MonitoringController;
use App\Core\Csrf;
use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;

mon_require_auth_json();

$request = new Request();
$action = (string)$request->get('action', '');

/** actionهایی که وضعیت را تغییر می‌دهند: POST + توکن CSRF الزامی. */
const WRITE_ACTIONS = ['acknowledge'];

if (in_array($action, WRITE_ACTIONS, true)) {
    if (!$request->isPost()) {
        Response::error('این عملیات فقط با متد POST قابل انجام است.', 405);
    }
    Csrf::requireValid($request->getHeader('X-CSRF-Token'));
} elseif (!$request->isGet()) {
    Response::error('روش درخواست معتبر نیست.', 405);
}

function mon_required_id(Request $request): int {
    $id = (int)$request->get('id', 0);
    if ($id <= 0) {
        throw new ApiException('شناسه‌ی رویداد مشخص نشده است.', 422);
    }
    return $id;
}

try {
    $controller = new MonitoringController();

    switch ($action) {
        case 'summary':
            Response::success('', $controller->summary());
            break;

        case 'events':
            $status = (string)$request->get('status', 'open');
            $limit = (int)$request->get('limit', 50);
            $beforeIdRaw = $request->get('beforeId');
            $beforeId = ($beforeIdRaw !== null && $beforeIdRaw !== '') ? (int)$beforeIdRaw : null;
            Response::success('', ['events' => $controller->listEvents($status, $limit, $beforeId)]);
            break;

        case 'acknowledge':
            $controller->acknowledge(mon_required_id($request), MON_ACTOR);
            Response::success('رویداد تأیید شد.');
            break;

        default:
            Response::error('عملیات نامعتبر است.', 404);
    }
} catch (ApiException $e) {
    Response::error($e->getMessage(), $e->getStatusCode(), $e->getDetails());
} catch (\Throwable $e) {
    Logger::getInstance()->error('Monitoring api.php: ' . $e->getMessage());
    Response::error('خطای داخلی سرور رخ داده است.', 500);
}
