<?php
// PHP/Monitoring/logout.php

declare(strict_types=1);

require_once __DIR__ . '/_guard.php';

use App\Core\Csrf;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && Csrf::validate($_POST['csrf_token'] ?? null)) {
    mon_destroy_session();
}

header('Location: login.php');
exit;
