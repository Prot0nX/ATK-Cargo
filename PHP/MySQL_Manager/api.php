<?php
// PHP/MySQL_Manager/api.php — تنها endpoint JSON پنل مدیریت MySQL.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Controllers\DatabaseManagerController;
use App\Core\Csrf;
use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;
use App\Exceptions\ApiException;

mys_require_auth_json();

$request = new Request();
$action = (string)$request->get('action', '');

// actionهایی که وضعیت را تغییر می‌دهند: POST + توکن CSRF الزامی. توجه: downloadBackup اینجا نیست چون یک دانلود
// فایل GET با auth جداگانه (download.php) است، نه یک اکشن JSON.
const WRITE_ACTIONS = [
    'optimizeTable', 'analyzeTable', 'checkTable', 'repairTable',
    'saveBackupSettings', 'createBackup', 'verifyBackup', 'deleteBackup',
    'restoreBackup',
    'saveCleanupRule', 'toggleCleanupRule', 'deleteCleanupRule', 'runCleanup',
    'killQuery', 'runMaintenanceNow',
];

if (in_array($action, WRITE_ACTIONS, true)) {
    if (!$request->isPost()) {
        Response::error('این عملیات فقط با متد POST قابل انجام است.', 405);
    }
    Csrf::requireValid($request->getHeader('X-CSRF-Token'));
} elseif (!$request->isGet()) {
    Response::error('روش درخواست معتبر نیست.', 405);
}

function mys_required_table_name(Request $request): string {
    $name = trim((string)$request->get('table', ''));
    if ($name === '') {
        throw new ApiException('نام جدول مشخص نشده است.', 422);
    }
    return $name;
}

function mys_required_id(Request $request, string $paramName = 'id'): int {
    $id = (int)$request->get($paramName, 0);
    if ($id <= 0) {
        throw new ApiException('شناسه مشخص نشده است.', 422);
    }
    return $id;
}

try {
    $controller = new DatabaseManagerController();

    switch ($action) {
        case 'dashboard':
            Response::success('', ['dashboard' => $controller->dashboard()]);
            break;

        case 'health':
            Response::success('', ['health' => $controller->health()]);
            break;

        case 'tables':
            $page = max(1, (int)$request->get('page', 1));
            $perPage = (int)$request->get('perPage', 20);
            $search = trim((string)$request->get('search', ''));
            $sort = (string)$request->get('sort', 'size');
            $dir = (string)$request->get('dir', 'desc');
            Response::success('', ['tables' => $controller->tables($page, $perPage, $search, $sort, $dir)]);
            break;

        case 'tableDetail':
            $tableName = mys_required_table_name($request);
            $detail = $controller->tableDetail($tableName);
            if ($detail === null) {
                Response::error('جدول مورد نظر یافت نشد.', 404);
            }
            Response::success('', ['table' => $detail]);
            break;

        case 'indexes':
            $tableName = trim((string)$request->get('table', ''));
            Response::success('', $controller->indexes($tableName));
            break;

        case 'storage':
            Response::success('', ['storage' => $controller->storage()]);
            break;

        case 'optimizationRecommendations':
            Response::success('', ['recommendations' => $controller->optimizationRecommendations()]);
            break;

        case 'optimizationHistory':
            $limit = (int)$request->get('limit', 30);
            $historyTable = trim((string)$request->get('table', ''));
            Response::success('', ['history' => $controller->optimizationHistory($limit, $historyTable)]);
            break;

        case 'optimizationPreview':
            $tableName = mys_required_table_name($request);
            Response::success('', ['preview' => $controller->optimizationPreview($tableName)]);
            break;

        case 'optimizeTable':
            $tableName = mys_required_table_name($request);
            Response::success('عملیات OPTIMIZE با موفقیت انجام شد.', ['result' => $controller->runOptimize($tableName, MYS_ACTOR)]);
            break;

        case 'analyzeTable':
            $tableName = mys_required_table_name($request);
            Response::success('عملیات ANALYZE با موفقیت انجام شد.', ['result' => $controller->runAnalyze($tableName, MYS_ACTOR)]);
            break;

        case 'checkTable':
            $tableName = mys_required_table_name($request);
            Response::success('عملیات CHECK با موفقیت انجام شد.', ['result' => $controller->runCheck($tableName, MYS_ACTOR)]);
            break;

        case 'repairTable':
            $tableName = mys_required_table_name($request);
            Response::success('عملیات REPAIR با موفقیت انجام شد.', ['result' => $controller->runRepair($tableName, MYS_ACTOR)]);
            break;

        // ---------- Backup ----------

        case 'backupSettings':
            Response::success('', ['settings' => $controller->backupSettings()]);
            break;

        case 'saveBackupSettings':
            Response::success('تنظیمات پشتیبان‌گیری خودکار ذخیره شد.', ['settings' => $controller->saveBackupSettings($request->all(), MYS_ACTOR)]);
            break;

        case 'backups':
            $page = max(1, (int)$request->get('page', 1));
            $perPage = (int)$request->get('perPage', 20);
            Response::success('', ['backups' => $controller->backups($page, $perPage)]);
            break;

        case 'createBackup':
            $type = (string)$request->get('type', 'full');
            $tablesInput = $request->get('tables');
            $tables = is_array($tablesInput) && !empty($tablesInput) ? array_map('strval', $tablesInput) : null;
            $compress = (bool)$request->get('compress', true);
            Response::success('پشتیبان‌گیری با موفقیت انجام شد.', ['backup' => $controller->createBackup($type, $tables, $compress, MYS_ACTOR)], 201);
            break;

        case 'verifyBackup':
            $id = mys_required_id($request);
            Response::success('بررسی صحت پشتیبان انجام شد.', ['result' => $controller->verifyBackup($id, MYS_ACTOR)]);
            break;

        case 'deleteBackup':
            $id = mys_required_id($request);
            $controller->deleteBackup($id, MYS_ACTOR);
            Response::success('پشتیبان حذف شد.');
            break;

        // ---------- Restore ----------

        case 'restoreBackup':
            $id = mys_required_id($request);
            $confirmDbName = (string)$request->get('confirmDbName', '');
            Response::success('عملیات بازیابی به پایان رسید.', ['result' => $controller->restoreBackup($id, $confirmDbName, MYS_ACTOR)]);
            break;

        // ---------- Cleanup ----------

        case 'cleanupRules':
            Response::success('', ['rules' => $controller->cleanupRules()]);
            break;

        case 'saveCleanupRule':
            $ruleId = $request->get('id') !== null && $request->get('id') !== '' ? (int)$request->get('id') : null;
            $tableName = mys_required_table_name($request);
            $dateColumn = trim((string)$request->get('dateColumn', ''));
            if ($dateColumn === '') {
                throw new ApiException('نام ستون تاریخ مشخص نشده است.', 422);
            }
            $retentionDays = (int)$request->get('retentionDays', 0);
            $enabled = (bool)$request->get('enabled', true);
            Response::success('Rule پاکسازی ذخیره شد.', ['rule' => $controller->saveCleanupRule($ruleId, $tableName, $dateColumn, $retentionDays, $enabled, MYS_ACTOR)]);
            break;

        case 'toggleCleanupRule':
            $id = mys_required_id($request);
            $enabled = (bool)$request->get('enabled', true);
            Response::success('وضعیت Rule بروزرسانی شد.', ['rule' => $controller->toggleCleanupRule($id, $enabled, MYS_ACTOR)]);
            break;

        case 'deleteCleanupRule':
            $id = mys_required_id($request);
            $controller->deleteCleanupRule($id, MYS_ACTOR);
            Response::success('Rule حذف شد.');
            break;

        case 'cleanupPreview':
            $ruleId = mys_required_id($request, 'ruleId');
            Response::success('', ['preview' => $controller->cleanupPreview($ruleId)]);
            break;

        case 'runCleanup':
            $ruleId = mys_required_id($request, 'ruleId');
            $confirm = (bool)$request->get('confirm', false);
            Response::success('پاکسازی با موفقیت انجام شد.', ['result' => $controller->runCleanup($ruleId, $confirm, MYS_ACTOR)]);
            break;

        // ---------- Monitoring ----------

        case 'monitoringMetrics':
            Response::success('', ['metrics' => $controller->monitoringMetrics()]);
            break;

        case 'processList':
            Response::success('', ['processes' => $controller->processList()]);
            break;

        case 'killQuery':
            $processId = mys_required_id($request, 'processId');
            $controller->killQuery($processId, MYS_ACTOR);
            Response::success('Query متوقف شد.');
            break;

        case 'slowQueries':
            $limit = (int)$request->get('limit', 20);
            Response::success('', ['slowQueries' => $controller->slowQueries($limit)]);
            break;

        // ---------- Scheduler ----------

        case 'maintenanceSettings':
            Response::success('', ['settings' => $controller->maintenanceSettings()]);
            break;

        case 'runMaintenanceNow':
            Response::success('اجرای نگهداری خودکار به پایان رسید.', ['result' => $controller->runMaintenanceNow(MYS_ACTOR)]);
            break;

        // ---------- Logs / Audit ----------

        case 'auditLogs':
            $limit = (int)$request->get('limit', 50);
            $beforeIdRaw = $request->get('beforeId');
            $beforeId = ($beforeIdRaw !== null && $beforeIdRaw !== '') ? (int)$beforeIdRaw : null;
            $entityType = trim((string)$request->get('entityType', ''));
            Response::success('', ['logs' => $controller->auditLogs($limit, $beforeId, $entityType !== '' ? $entityType : null)]);
            break;

        default:
            Response::error('عملیات نامعتبر است.', 404);
    }
} catch (ApiException $e) {
    Response::error($e->getMessage(), $e->getStatusCode(), $e->getDetails());
} catch (\Throwable $e) {
    Logger::getInstance()->error('MySQL_Manager api.php: ' . $e->getMessage());
    Response::error('خطای داخلی سرور رخ داده است.', 500);
}
