<?php
define('IS_CLI', php_sapi_name() === 'cli');
$_GET['action'] = 'generateDailyStats';

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/realTimeLoadingData.php';

$dbConnection = getDbConnection();
$cargoAPI = new CargoAPI($dbConnection);
$cargoAPI->handleRequest();