<?php
declare(strict_types=1);

require_once __DIR__ . '/../src/bootstrap.php';
require_once __DIR__ . '/SessionManager.php';

(new \App\Controllers\OnlineUsersController())->handleRequest();