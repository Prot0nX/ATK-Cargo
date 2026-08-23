<?php
// PHP/User/online_users_api.php — تنها endpoint JSON پنل مدیریت کاربران آنلاین.

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';
require_once __DIR__ . '/SessionManager.php';

use App\Core\Csrf;
use App\Core\Request;

ou_require_auth_json();

$request = new Request();
$action = (string)$request->get('action', '');

/** actionهایی که وضعیت را تغییر می‌دهند: POST + توکن CSRF الزامی. */
const WRITE_ACTIONS = ['force_logout', 'logout_all_users', 'cleanup_inactive'];

if (in_array($action, WRITE_ACTIONS, true)) {
 // توکن از هدر خوانده می‌شود (نه بدنه) تا بدنه‌ی JSON خالص بماند.
    Csrf::requireValid($request->getHeader('X-CSRF-Token'));
}

(new \App\Controllers\OnlineUsersController())->handleRequest();
