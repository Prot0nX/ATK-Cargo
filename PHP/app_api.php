<?php
	//app_api.php
	
	declare(strict_types=1);
	
	ini_set('display_errors', 0);
	ini_set('display_startup_errors', 0);
	error_reporting(E_ALL);
	
	header('Content-Type: application/json; charset=UTF-8');
	
	require_once __DIR__ . '/config/config.php';
	
	date_default_timezone_set('Asia/Tehran');
	
	class DatabaseManager {
		private mysqli $conn;
		
		public function __construct() {
			$this->conn = $this->getDbConnection();
		}
		
		private function getDbConnection(): mysqli {
			$conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
			if ($conn->connect_error) {
				throw new Exception("خطا در اتصال به پایگاه داده: " . $conn->connect_error);
			}
			$conn->set_charset("utf8mb4");
			return $conn;
		}
		
		public function prepare(string $query): mysqli_stmt {
			$stmt = $this->conn->prepare($query);
			if (!$stmt) {
				throw new Exception("خطا در آماده‌سازی دستور SQL: " . $this->conn->error);
			}
			return $stmt;
		}
		
		public function close(): void {
			$this->conn->close();
		}
		
		public function beginTransaction(): void {
			$this->conn->begin_transaction();
		}
		
		public function commit(): void {
			$this->conn->commit();
		}
		
		public function rollback(): void {
			$this->conn->rollback();
		}
	}
	
	function customLog(string $message): void {
		$logDir = __DIR__ . '/logs';
		if (!is_dir($logDir) && !mkdir($logDir, 0755, true) && !is_dir($logDir)) {
			error_log("Failed to create log directory: " . error_get_last()['message']);
			return;
		}
		$logFile = $logDir . '/custom.log';
		$logMessage = date('[Y-m-d H:i:s] ') . $message . PHP_EOL;
		file_put_contents($logFile, $logMessage, FILE_APPEND);
	}
	
	function sendJsonResponse($data, int $statusCode = 200): void {
		http_response_code($statusCode);
		echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_THROW_ON_ERROR);
		exit;
	}
	
	function sanitizeInput(string $input): string {
		return htmlspecialchars(strip_tags(trim($input)), ENT_QUOTES, 'UTF-8');
	}
	
	function checkQuotaStatus(DatabaseManager $db, array $params): array {
		try {
			// Log incoming request
			customLog("Starting checkQuotaStatus with params: " . json_encode($params));
			
			// Validate required parameters
			$requiredParams = ['quotaNumber', 'shipName', 'cargoType', 'shippingCompany'];
			foreach ($requiredParams as $param) {
				if (!isset($params[$param]) || empty($params[$param])) {
					customLog("Missing required parameter: $param");
					throw new Exception("پارامتر $param الزامی است");
				}
			}
			
			// Sanitize inputs
			$quotaNumber = sanitizeInput($params['quotaNumber']);
			$shipName = sanitizeInput($params['shipName']);
			$cargoType = sanitizeInput($params['cargoType']);
			$shippingCompany = sanitizeInput($params['shippingCompany']);
			
			// Log sanitized inputs
			customLog("Sanitized inputs - QuotaNumber: $quotaNumber, Ship: $shipName, Cargo: $cargoType, Company: $shippingCompany");
			
			// Get quota details with comprehensive information
			$query = "SELECT 
			i.loadingQuotaNumber,
			i.shipName,
			i.cargoType,
			i.shippingCompany,
			i.cargoWeight as totalWeight,
			i.isActive,
			COALESCE((
			SELECT SUM(CAST(netWeight AS DECIMAL(10,2)))
			FROM CargoInfo c
			WHERE c.loadingQuotaNumber = i.loadingQuotaNumber
			AND c.shipName = i.shipName
			AND c.cargoType = i.cargoType
			AND c.shippingCompany = i.shippingCompany
			AND c.status = 'خروج'
			), 0) as loadedWeight
			FROM InitialInfo i 
			WHERE i.loadingQuotaNumber = ?
			AND i.shipName = ?
			AND i.cargoType = ?
			AND i.shippingCompany = ?";
			
			// Log query
			customLog("Executing query: $query");
			customLog("With parameters: [$quotaNumber, $shipName, $cargoType, $shippingCompany]");
			
			$stmt = $db->prepare($query);
			$stmt->bind_param("ssss", $quotaNumber, $shipName, $cargoType, $shippingCompany);
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
				customLog("Found quota record: " . json_encode($row));
				
				$loadedWeight = floatval($row['loadedWeight']);
				$totalWeight = floatval($row['totalWeight']);
				$percentageLoaded = ($totalWeight > 0) ? ($loadedWeight / $totalWeight) * 100 : 0;
				$remainingCapacity = $totalWeight - $loadedWeight;
				$isActive = (bool)$row['isActive'];
				
				$response = [
                'isActive' => $isActive && ($loadedWeight < $totalWeight),
                'status' => true,
                'message' => generateStatusMessage($isActive, $percentageLoaded, $quotaNumber, $cargoType),
                'details' => [
				'quotaNumber' => $quotaNumber,
				'shipName' => $row['shipName'],
				'cargoType' => $row['cargoType'],
				'shippingCompany' => $row['shippingCompany'],
				'totalWeight' => $totalWeight,
				'loadedWeight' => $loadedWeight,
				'remainingCapacity' => $remainingCapacity,
				'percentageLoaded' => round($percentageLoaded, 2)
                ]
				];
				
				customLog("Returning response: " . json_encode($response));
				return $response;
			}
			
			// If no exact match found, check for similar quotas
			customLog("No exact match found, checking for similar quotas...");
			
			$queryCheck = "SELECT 
			loadingQuotaNumber, 
			shipName, 
			cargoType, 
			shippingCompany,
			isActive
			FROM InitialInfo 
			WHERE loadingQuotaNumber = ?";
			
			$stmtCheck = $db->prepare($queryCheck);
			$stmtCheck->bind_param("s", $quotaNumber);
			$stmtCheck->execute();
			$resultCheck = $stmtCheck->get_result();
			
			if ($resultCheck->num_rows > 0) {
				$existingQuotas = [];
				while ($row = $resultCheck->fetch_assoc()) {
					$existingQuotas[] = $row;
				}
				
				customLog("Found similar quotas: " . json_encode($existingQuotas));
				
				return [
                'isActive' => false,
                'status' => false,
                'message' => "کوتاژ $quotaNumber با مشخصات متفاوتی ثبت شده است",
                'details' => [
				'existingQuotas' => $existingQuotas,
				'requestedQuota' => [
				'quotaNumber' => $quotaNumber,
				'shipName' => $shipName,
				'cargoType' => $cargoType,
				'shippingCompany' => $shippingCompany
				]
                ]
				];
			}
			
			// No quota found
			customLog("No quota found with number: $quotaNumber");
			return [
            'isActive' => false,
            'status' => false,
            'message' => "کوتاژ $quotaNumber یافت نشد",
            'details' => null
			];
			
			} catch (Exception $e) {
			customLog("Error in checkQuotaStatus: " . $e->getMessage());
			throw new Exception("خطا در بررسی وضعیت کوتاژ: " . $e->getMessage());
		}
	}
	
	function generateStatusMessage(bool $isActive, float $percentageLoaded, string $quotaNumber, string $cargoType): string {
		if (!$isActive) {
			return "کوتاژ $quotaNumber با نوع کالای $cargoType غیرفعال است";
		}
		
		if ($percentageLoaded >= 100) {
			return "ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType تکمیل شده است";
		}
		
		if ($percentageLoaded >= 95) {
			return "هشدار: ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType به " . round($percentageLoaded, 2) . "% رسیده است";
		}
		
		return "کوتاژ $quotaNumber با نوع کالای $cargoType فعال است";
	}
	
	function createStatusDetails(array $row, float $loadedWeight, float $remainingCapacity, float $percentageLoaded): array {
		return [
        'quotaNumber' => $row['loadingQuotaNumber'],
        'shipName' => $row['shipName'],
        'cargoType' => $row['cargoType'],
        'shippingCompany' => $row['shippingCompany'],
        'totalWeight' => floatval($row['totalWeight']),
        'loadedWeight' => $loadedWeight,
        'remainingCapacity' => $remainingCapacity,
        'percentageLoaded' => round($percentageLoaded, 2)
		];
	}
	
	function generateMismatchMessage(string $quotaNumber, array $existingQuotas): string {
		$message = "کوتاژ $quotaNumber با مشخصات متفاوتی ثبت شده است:\n";
		foreach ($existingQuotas as $quota) {
			$message .= sprintf(
            "- کشتی: %s، نوع کالا: %s، شرکت باربری: %s\n",
            $quota['shipName'],
            $quota['cargoType'],
            $quota['shippingCompany']
			);
		}
		return $message;
	}
	
	function checkQuotaExistence(DatabaseManager $db, string $quotaNumber, string $shipName): array {
		$quotaNumber = sanitizeInput($quotaNumber);
		$shipName = sanitizeInput($shipName);
		
		$query = "SELECT shipName FROM InitialInfo WHERE loadingQuotaNumber = ?";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("s", $quotaNumber);
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
				$existingShipName = $row['shipName'];
				$exists = true;
				$isForSelectedShip = ($existingShipName === $shipName);
				$message = $isForSelectedShip ? "کوتاژ برای کشتی انتخاب شده معتبر است." : "کوتاژ برای کشتی دیگری ثبت شده است.";
				} else {
				$exists = false;
				$isForSelectedShip = false;
				$message = "کوتاژ مورد نظر در سیستم وجود ندارد.";
			}
			
			return [
            'exists' => $exists,
            'isForSelectedShip' => $isForSelectedShip,
            'message' => $message
			];
			} catch (Exception $e) {
			throw new Exception("خطا در بررسی وجود کوتاژ: " . $e->getMessage());
		}
	}
	
	function checkQuotaExistenceCargo(DatabaseManager $db, string $quotaNumber, string $shipName): array {
		$quotaNumber = sanitizeInput($quotaNumber);
		$shipName = sanitizeInput($shipName);
		
		customLog("Checking quota existence: $quotaNumber for ship: $shipName");
		
		$query = "SELECT loadingQuotaNumber, shipName, shippingCompany, cargoType, loadingWarehouse FROM InitialInfo WHERE loadingQuotaNumber LIKE ?";
		
		try {
			$stmt = $db->prepare($query);
			$likeQuotaNumber = '%' . $quotaNumber;
			$stmt->bind_param("s", $likeQuotaNumber);
			$stmt->execute();
			$result = $stmt->get_result();
			
			$matchingQuotas = [];
			while ($row = $result->fetch_assoc()) {
				$matchingQuotas[] = [
                'quotaNumber' => $row['loadingQuotaNumber'],
                'shipName' => $row['shipName'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'warehouse' => $row['loadingWarehouse']
				];
			}
			
			$exists = !empty($matchingQuotas);
			$message = $exists ? "کوتاژ(های) مطابق یافت شد." : "کوتاژ مورد نظر در سیستم وجود ندارد.";
			
			customLog("Matching quotas: " . json_encode($matchingQuotas));
			customLog("Exists: " . ($exists ? 'true' : 'false'));
			
			return [
            'exists' => $exists,
            'matchingQuotas' => $matchingQuotas,
            'message' => $message
			];
			} catch (Exception $e) {
			customLog("Error in checkQuotaExistenceCargo: " . $e->getMessage());
			throw new Exception("خطا در بررسی وجود کوتاژ: " . $e->getMessage());
		}
	}
	
	function getShipsList(DatabaseManager $db): array {
		$query = "
		SELECT
        i.shipName,
        COUNT(DISTINCT i.loadingWarehouse) as warehouseCount,
        COUNT(DISTINCT i.loadingQuotaNumber) as quotaCount,
        SUM(i.cargoWeight) as totalTonnage,
        SUM(i.cargoWeight) - COALESCE(
		SUM(
		(SELECT SUM(netWeight)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
		AND CargoInfo.status = 'خروج')
		),
		0
        ) as remainingTonnage,
        MAX(i.isActive) as isActive
		FROM
        InitialInfo i
		GROUP BY
        i.shipName
		ORDER BY
        isActive DESC, shipName ASC
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->execute();
			$result = $stmt->get_result();
			
			$activeShips = [];
			$inactiveShips = [];
			while ($row = $result->fetch_assoc()) {
				$ship = [
                'name' => $row['shipName'],
                'warehouseCount' => intval($row['warehouseCount']),
                'quotaCount' => intval($row['quotaCount']),
                'totalTonnage' => intval($row['totalTonnage']),
                'remainingTonnage' => max(0, intval($row['remainingTonnage'])),
                'isActive' => (bool)$row['isActive']
				];
				
				if ($ship['isActive']) {
					$activeShips[] = $ship;
					customLog("Added to active ships");
					} else {
					$inactiveShips[] = $ship;
					customLog("Added to inactive ships");
				}
			}
			
			sendJsonResponse(['data' => [
			'activeShips' => $activeShips,
			'inactiveShips' => $inactiveShips
			]]);
			} catch (Exception $e) {
			customLog("Error in getShipsList: " . $e->getMessage());
			throw new Exception("خطا در دریافت لیست کشتی‌ها: " . $e->getMessage());
		}
	}
	
	function getShipDetails(DatabaseManager $db, string $shipName): array {
		$shipName = sanitizeInput($shipName);
		$query = "
        SELECT 
		i.shipName,
		i.loadingWarehouse,
		COUNT(DISTINCT i.loadingQuotaNumber) as quotaCount,
		SUM(i.cargoWeight) as totalTonnage,
		SUM(i.cargoWeight) - COALESCE(
		SUM(
		(SELECT SUM(netWeight)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
		AND CargoInfo.status = 'خروج')
		),
		0
		) as remainingTonnage,
		(SELECT COUNT(DISTINCT trackingNumber)
		FROM CargoInfo
		WHERE CargoInfo.shipName = i.shipName) as totalVoucherCount,
		MAX(i.isActive) as isActive
        FROM 
		InitialInfo i
        WHERE 
		i.shipName = ?
        GROUP BY 
		i.shipName, i.loadingWarehouse
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("s", $shipName);
			$stmt->execute();
			$result = $stmt->get_result();
			
			$warehouses = [];
			$totalQuotaCount = 0;
			$totalTonnage = 0;
			$totalRemainingTonnage = 0;
			$totalVoucherCount = 0;
			$isActive = false;
			
			while ($row = $result->fetch_assoc()) {
				$warehouses[] = [
                'name' => $row['loadingWarehouse'],
                'quotaCount' => intval($row['quotaCount']),
                'totalTonnage' => floatval($row['totalTonnage']),
                'remainingTonnage' => max(0, floatval($row['remainingTonnage']))
				];
				$totalQuotaCount += intval($row['quotaCount']);
				$totalTonnage += floatval($row['totalTonnage']);
				$totalRemainingTonnage += max(0, floatval($row['remainingTonnage']));
				$totalVoucherCount = intval($row['totalVoucherCount']);
				$isActive = (bool)$row['isActive']; 
			}
			
			if (empty($warehouses)) {
				throw new Exception("کشتی با نام '$shipName' یافت نشد.");
			}
			
			return [
            'name' => $shipName,
            'warehouseCount' => count($warehouses),
            'quotaCount' => $totalQuotaCount,
            'totalTonnage' => $totalTonnage,
            'remainingTonnage' => $totalRemainingTonnage,
            'totalVoucherCount' => $totalVoucherCount,
            'isActive' => $isActive,
            'warehouses' => $warehouses
			];
			} catch (Exception $e) {
			throw new Exception("خطا در دریافت جزئیات کشتی: " . $e->getMessage());
		}
	}
	
	function getWarehouseDetails(DatabaseManager $db, string $shipName, string $warehouseName): array {
		$shipName = sanitizeInput($shipName);
		$warehouseName = sanitizeInput($warehouseName);
		$query = "
        SELECT 
		i.loadingQuotaNumber,
		i.cargoWeight as totalTonnage,
		i.cargoWeight - COALESCE(
		(SELECT SUM(netWeight)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
		AND CargoInfo.status = 'خروج'),
		0
		) as remainingTonnage,
		COALESCE(
		(SELECT SUM(netWeight)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
		AND CargoInfo.status = 'خروج'),
		0
		) as loadedTonnage,
		(SELECT COUNT(DISTINCT trackingNumber)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber) as voucherCount
        FROM 
		InitialInfo i
        WHERE 
		i.shipName = ? AND i.loadingWarehouse = ?
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("ss", $shipName, $warehouseName);
			$stmt->execute();
			$result = $stmt->get_result();
			
			$quotas = [];
			$totalTonnage = 0;
			$totalRemainingTonnage = 0;
			$totalLoadedTonnage = 0;
			$totalVoucherCount = 0;
			$allExitDates = [];
			
			while ($row = $result->fetch_assoc()) {
				$quotaNumber = $row['loadingQuotaNumber'];
				
				$exitQuery = "
                SELECT DISTINCT exitDate, exitTime
                FROM CargoInfo
                WHERE loadingQuotaNumber = ? AND status = 'خروج'
                ORDER BY exitDate, exitTime
				";
				$exitStmt = $db->prepare($exitQuery);
				$exitStmt->bind_param("s", $quotaNumber);
				$exitStmt->execute();
				$exitResult = $exitStmt->get_result();
				
				$exitDates = [];
				while ($exitRow = $exitResult->fetch_assoc()) {
					$exitDates[] = [
                    'date' => $exitRow['exitDate'],
                    'time' => $exitRow['exitTime']
					];
					$allExitDates[] = $exitRow['exitDate'];
				}
				
				$quotas[] = [
                'number' => $quotaNumber,
                'totalTonnage' => floatval($row['totalTonnage']),
                'remainingTonnage' => max(0, floatval($row['remainingTonnage'])),
                'loadedTonnage' => floatval($row['loadedTonnage']),
                'voucherCount' => intval($row['voucherCount']),
                'exitDates' => $exitDates
				];
				
				$totalTonnage += floatval($row['totalTonnage']);
				$totalRemainingTonnage += max(0, floatval($row['remainingTonnage']));
				$totalLoadedTonnage += floatval($row['loadedTonnage']);
				$totalVoucherCount += intval($row['voucherCount']);
			}
			
			$allExitDates = array_unique($allExitDates);
			sort($allExitDates);
			
			return [
            'name' => $warehouseName,
            'quotaCount' => count($quotas),
            'totalTonnage' => $totalTonnage,
            'remainingTonnage' => $totalRemainingTonnage,
            'loadedTonnage' => $totalLoadedTonnage,
            'quotas' => $quotas,
            'availableExitDates' => $allExitDates
			];
			} catch (Exception $e) {
			throw new Exception("خطا در دریافت جزئیات انبار: " . $e->getMessage());
		}
	}
	
	function getFilteredSummary(DatabaseManager $db, string $shipName, string $warehouseName, string $selectedQuota, string $startDateTime, string $endDateTime): string {
		try {
			if (empty($selectedQuota) || $selectedQuota === "null") {
				throw new Exception("هیچ کوتاژی انتخاب نشده است");
			}
			
			$summaryQuery = "
            SELECT 
			COALESCE(SUM(CAST(netWeight AS DECIMAL(10,2))), 0) as totalNetWeight,
			COUNT(DISTINCT trackingNumber) as voucherCount
            FROM 
			CargoInfo
            WHERE
			loadingQuotaNumber = ?
			AND shipName = ?
			AND loadingWarehouse = ?
			AND status = 'خروج'
			AND CONCAT(exitDate, ' ', exitTime) >= ?
			AND CONCAT(exitDate, ' ', exitTime) < ?
			";
			
			$stmt = $db->prepare($summaryQuery);
			$stmt->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
			$stmt->execute();
			$summaryResult = $stmt->get_result();
			$summary = $summaryResult->fetch_assoc();
			
			$detailsQuery = "
            SELECT 
			trackingNumber,
			entryTime,
			netWeight,
			exitTime,
			exitDate,
			scaleReceiptNumber,
			username,
			confirm_username
            FROM 
			CargoInfo
            WHERE 
			loadingQuotaNumber = ?
			AND shipName = ?
			AND loadingWarehouse = ?
			AND status = 'خروج'
			AND CONCAT(exitDate, ' ', exitTime) >= ?
			AND CONCAT(exitDate, ' ', exitTime) < ?
            ORDER BY 
			exitDate, exitTime
			";
			
			$stmtDetails = $db->prepare($detailsQuery);
			$stmtDetails->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
			$stmtDetails->execute();
			$detailsResult = $stmtDetails->get_result();
			$voucherDetails = [];
			while ($row = $detailsResult->fetch_assoc()) {
				$voucherDetails[] = [
                'trackingNumber' => $row['trackingNumber'],
                'entryTime' => $row['entryTime'],
                'netWeight' => floatval($row['netWeight']),
                'exitTime' => $row['exitTime'],
                'exitDate' => $row['exitDate'],
                'scaleReceiptNumber' => $row['scaleReceiptNumber'],
                'username' => $row['username'],
                'confirmUsername' => $row['confirm_username']
				];
			}
			
			$response = [
            'totalNetWeight' => floatval($summary['totalNetWeight']),
            'voucherCount' => intval($summary['voucherCount']),
            'voucherDetails' => $voucherDetails
			];
			
			return json_encode($response, JSON_UNESCAPED_UNICODE);
			} catch (Exception $e) {
			// لاگ خطا
			throw new Exception("خطا در دریافت خلاصه فیلتر شده: " . $e->getMessage());
		}
	}
	
	function getQuotaDetails(DatabaseManager $db, string $quotaNumber): ?array {
		$quotaNumber = sanitizeInput($quotaNumber);
		$query = "
        SELECT 
		i.loadingQuotaNumber, 
		i.cargoWeight as totalTonnage,
		i.cargoWeight - COALESCE(
		(SELECT SUM(netWeight)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
		AND CargoInfo.status = 'خروج'),
		0
		) as remainingTonnage,
		COALESCE(
		(SELECT SUM(netWeight)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
		AND CargoInfo.status = 'خروج'),
		0
		) as loadedTonnage,
		(SELECT COUNT(DISTINCT trackingNumber)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber) as voucherCount,
		(SELECT MIN(exitDate)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber) as startDate,
		(SELECT MAX(exitDate)
		FROM CargoInfo
		WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber) as endDate,
		i.shipName,
		i.loadingWarehouse
        FROM InitialInfo i
        WHERE i.loadingQuotaNumber = ?
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("s", $quotaNumber);
			$stmt->execute();
			$result = $stmt->get_result();
			if ($row = $result->fetch_assoc()) {
				return [
                'number' => $row['loadingQuotaNumber'],
                'totalTonnage' => floatval($row['totalTonnage']),
                'remainingTonnage' => max(0, floatval($row['remainingTonnage'])),
                'loadedTonnage' => floatval($row['loadedTonnage']),
                'voucherCount' => intval($row['voucherCount']),
                'startDate' => $row['startDate'],
                'endDate' => $row['endDate'],
                'shipName' => $row['shipName'],
                'warehouseName' => $row['loadingWarehouse']
				];
			}
			return null;
			} catch (Exception $e) {
			throw new Exception("خطا در دریافت جزئیات کوتاژ: " . $e->getMessage());
		}
	}

	function getQuotasList(DatabaseManager $db, string $shipName): array {
		$shipName = sanitizeInput($shipName);
		
		$query = "
		SELECT 
			i.loadingQuotaNumber as number,
			i.shipName,
			i.loadingWarehouse,
			i.cargoType,
			i.cargoWeight as totalTonnage,
			i.isActive,
			i.shippingCompany,
			i.cargoOwner,
			i.percentage,
			i.is_enabled,
			(
				SELECT COALESCE(SUM(netWeight), 0)
				FROM CargoInfo 
				WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
				AND CargoInfo.status = 'خروج'
			) as loadedTonnage,
			(
				SELECT COUNT(DISTINCT trackingNumber)
				FROM CargoInfo
				WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
			) as voucherCount
		FROM 
			InitialInfo i
		WHERE 
			i.shipName = ?";

		try {			
			$stmt = $db->prepare($query);
			$stmt->bind_param("s", $shipName);
			$stmt->execute();
			$result = $stmt->get_result();
			$quotas = [];
			
			while ($row = $result->fetch_assoc()) {
				$loadedTonnage = floatval($row['loadedTonnage']);
				$totalTonnage = floatval($row['totalTonnage']);
				$remainingTonnage = max(0, $totalTonnage - $loadedTonnage);
				$percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
				$isPercentageRestricted = (bool)$row['is_enabled'];
				
				// محاسبه تناژ قابل بارگیری
				$loadableTonnage = calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
				
				$quotas[] = [
					'number' => $row['number'],
					'shipName' => $row['shipName'] ?? '',
					'warehouse' => $row['loadingWarehouse'] ?? '',
					'cargoType' => $row['cargoType'] ?? '',
					'totalTonnage' => $totalTonnage,
					'remainingTonnage' => $remainingTonnage,
					'loadedTonnage' => $loadedTonnage,
					'voucherCount' => intval($row['voucherCount']),
					'isActive' => (bool)$row['isActive'],
					'shippingCompany' => $row['shippingCompany'] ?? '',
					'cargoOwner' => $row['cargoOwner'] ?? '',
					'percentage' => $percentage,
					'isPercentageRestricted' => $isPercentageRestricted,
					'loadableTonnage' => $loadableTonnage
				];
			}
			
			return $quotas;
		} catch (Exception $e) {
			customLog("Error in getQuotasList: " . $e->getMessage());
			throw new Exception("خطا در دریافت لیست کوتاژها: " . $e->getMessage());
		}
	}

	/**
	 * محاسبه تناژ قابل بارگیری با در نظر گرفتن محدودیت درصدی
	 * 
	 * @param float $remainingTonnage تناژ باقیمانده
	 * @param float $totalTonnage کل تناژ
	 * @param float|null $percentage درصد محدودیت (اگر وجود دارد)
	 * @param bool $isPercentageRestricted آیا محدودیت درصدی فعال است
	 * @return float تناژ قابل بارگیری
	 */
	function calculateLoadableTonnage(float $remainingTonnage, float $totalTonnage, ?float $percentage, bool $isPercentageRestricted): float {
		if ($isPercentageRestricted && $percentage !== null) {
			$percentageAmount = $totalTonnage * ($percentage / 100);
			return $remainingTonnage - $percentageAmount;
		}
		return $remainingTonnage;
	}

	/**
	 * API برای دریافت تناژ قابل بارگیری برای یک کوتاژ خاص
	 * این API برای استفاده مستقیم از سمت کلاینت برای دریافت سریع مقدار تناژ قابل بارگیری طراحی شده است
	 */
	function getLoadableTonnage(DatabaseManager $db, string $quotaNumber, string $shippingCompany = '', string $warehouse = '', string $cargoType = ''): array {
		$quotaNumber = sanitizeInput($quotaNumber);
		$shippingCompany = sanitizeInput($shippingCompany);
		$warehouse = sanitizeInput($warehouse);
		$cargoType = sanitizeInput($cargoType);
		
		$query = "
		SELECT 
			i.loadingQuotaNumber as number,
			i.cargoWeight as totalTonnage,
			i.percentage,
			i.is_enabled,
			(
				SELECT COALESCE(SUM(netWeight), 0)
				FROM CargoInfo 
				WHERE CargoInfo.loadingQuotaNumber = i.loadingQuotaNumber
				AND CargoInfo.shippingCompany = i.shippingCompany
				AND CargoInfo.loadingWarehouse = i.loadingWarehouse
				AND CargoInfo.cargoType = i.cargoType
				AND CargoInfo.status = 'خروج'
			) as loadedTonnage
		FROM 
			InitialInfo i
		WHERE 
			i.loadingQuotaNumber = ?";
		
		$params = [$quotaNumber];
		$types = "s";
		
		// اگر پارامترهای اضافی ارسال شده باشند، آنها را به کوئری اضافه می‌کنیم
		if (!empty($shippingCompany)) {
			$query .= " AND i.shippingCompany = ?";
			$params[] = $shippingCompany;
			$types .= "s";
		}
		
		if (!empty($warehouse)) {
			$query .= " AND i.loadingWarehouse = ?";
			$params[] = $warehouse;
			$types .= "s";
		}
		
		if (!empty($cargoType)) {
			$query .= " AND i.cargoType = ?";
			$params[] = $cargoType;
			$types .= "s";
		}
		
		try {
			$stmt = $db->prepare($query);
			
			// باند کردن پارامترها به صورت داینامیک
			if (count($params) > 0) {
				$stmt->bind_param($types, ...$params);
			}
			
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
				$loadedTonnage = floatval($row['loadedTonnage']);
				$totalTonnage = floatval($row['totalTonnage']);
				$remainingTonnage = $totalTonnage - $loadedTonnage;
				$percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
				$isPercentageRestricted = (bool)$row['is_enabled'];
				
				// محاسبه تناژ قابل بارگیری
				$loadableTonnage = calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
				
				// محاسبه تعداد کامیون‌های 18 چرخ و 10 چرخ - با در نظر گرفتن مقادیر منفی
				$trucks18Wheeler = $loadableTonnage > 0 ? floor($loadableTonnage / 25000) : 0;
				$trucks10Wheeler = $loadableTonnage > 0 ? floor($loadableTonnage / 15000) : 0;
				
				return [
					'success' => true,
					'loadableTonnage' => $loadableTonnage,
					'remainingTonnage' => $remainingTonnage,
					'totalTonnage' => $totalTonnage,
					'loadedTonnage' => $loadedTonnage,
					'percentage' => $percentage,
					'isPercentageRestricted' => $isPercentageRestricted,
					'trucks18Wheeler' => $trucks18Wheeler,
					'trucks10Wheeler' => $trucks10Wheeler
				];
			}
			
			return [
				'success' => false,
				'message' => 'کوتاژ مورد نظر با مشخصات وارد شده یافت نشد'
			];
		} catch (Exception $e) {
			customLog("Error in getLoadableTonnage: " . $e->getMessage());
			return [
				'success' => false,
				'message' => "خطا در محاسبه تناژ قابل بارگیری: " . $e->getMessage()
			];
		}
	}

	function getRealTimeData(DatabaseManager $db): array {
		$currentJalaliDate = gregorian_to_jalali(date('Y'), date('m'), date('d'));
		$currentJalaliDateStr = $currentJalaliDate[0] . '/' . sprintf("%02d", $currentJalaliDate[1]) . '/' . sprintf("%02d", $currentJalaliDate[2]);
		
		$query = "
        SELECT 
		i.loadingQuotaNumber,
		i.shipName,
		i.loadingWarehouse,
		COUNT(CASE WHEN c.status = 'ورود' THEN 1 END) AS entryVouchers,
		COUNT(CASE WHEN c.status = 'خروج' AND c.exitDate = ? AND c.exitTime >= '07:30:00' THEN 1 END) AS exitVouchers,
		SUM(CASE WHEN c.status = 'خروج' AND c.exitDate = ? AND c.exitTime >= '07:30:00' THEN c.netWeight ELSE 0 END) AS totalNetWeight
        FROM 
		InitialInfo i
        LEFT JOIN 
		CargoInfo c ON i.loadingQuotaNumber = c.loadingQuotaNumber
        WHERE 
		c.status = 'ورود' OR
		(c.status = 'خروج' AND c.exitDate = ? AND c.exitTime >= '07:30:00')
        GROUP BY 
		i.loadingQuotaNumber, i.shipName, i.loadingWarehouse
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("sss", $currentJalaliDateStr, $currentJalaliDateStr, $currentJalaliDateStr);
			$stmt->execute();
			$result = $stmt->get_result();
			return $result->fetch_all(MYSQLI_ASSOC);
			} catch (Exception $e) {
			throw new Exception("خطا در دریافت داده‌های لحظه‌ای: " . $e->getMessage());
		}
	}
	
	function editQuota(DatabaseManager $db, string $oldQuotaNumber, string $newQuotaNumber, string $shipName, string $shippingCompany, string $warehouse, string $cargoType, float $totalTonnage): bool {
		try {
			$db->beginTransaction();
			
			// Update InitialInfo table
			$queryInitialInfo = "UPDATE InitialInfo SET loadingQuotaNumber = ?, shipName = ?, shippingCompany = ?, loadingWarehouse = ?, cargoType = ?, cargoWeight = ? WHERE loadingQuotaNumber = ?";
			$stmtInitialInfo = $db->prepare($queryInitialInfo);
			$stmtInitialInfo->bind_param("sssssds", $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage, $oldQuotaNumber);
			$stmtInitialInfo->execute();
			
			// Update CargoInfo table - بروزرسانی تمام حواله های مرتبط با همه فیلدها
			$queryCargoInfo = "UPDATE CargoInfo SET 
				loadingQuotaNumber = ?, 
				shipName = ?, 
				loadingWarehouse = ?,
				shippingCompany = ?,
				cargoType = ?
				WHERE loadingQuotaNumber = ?";
			$stmtCargoInfo = $db->prepare($queryCargoInfo);
			$stmtCargoInfo->bind_param("ssssss", $newQuotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType, $oldQuotaNumber);
			$stmtCargoInfo->execute();
			
			// لاگ کردن عملیات برای بررسی
			customLog("Quota edit successful - Old: $oldQuotaNumber, New: $newQuotaNumber, ShippingCompany: $shippingCompany, CargoType: $cargoType");
			
			$db->commit();
			return true;
			} catch (Exception $e) {
			$db->rollback();
			customLog("Error in editQuota: " . $e->getMessage());
			throw new Exception("خطا در ویرایش کوتاژ: " . $e->getMessage());
		}
	}
	
	function updateQuotaPercentage(DatabaseManager $db, string $quotaNumber, float $percentage): bool {
		try {
			// محاسبه مقدار is_enabled بر اساس درصد
			$isEnabled = ($percentage > 0.0) ? 1 : 0;
			
			// بروزرسانی همزمان درصد و وضعیت فعال بودن
			$query = "UPDATE InitialInfo SET percentage = ?, is_enabled = ? WHERE loadingQuotaNumber = ?";
			$stmt = $db->prepare($query);
			$stmt->bind_param("dis", $percentage, $isEnabled, $quotaNumber);
			return $stmt->execute();
			} catch (Exception $e) {
			throw new Exception("خطا در بروزرسانی درصد کوتاژ: " . $e->getMessage());
		}
	}
	
	function toggleQuotaStatus(DatabaseManager $db, string $quotaNumber): bool {
		try {
			$query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE loadingQuotaNumber = ?";
			$stmt = $db->prepare($query);
			$stmt->bind_param("s", $quotaNumber);
			$stmt->execute();
			return true;
			} catch (Exception $e) {
			throw new Exception("خطا در تغییر وضعیت کوتاژ: " . $e->getMessage());
		}
	}
	
	function updateQuotaPercentageRestriction(DatabaseManager $db, string $quotaNumber, int $isEnabled): bool {
		try {
			$query = "UPDATE InitialInfo SET is_enabled = ? WHERE loadingQuotaNumber = ?";
			$stmt = $db->prepare($query);
			$stmt->bind_param("is", $isEnabled, $quotaNumber);
			return $stmt->execute();
			} catch (Exception $e) {
			throw new Exception("خطا در بروزرسانی وضعیت محدودیت درصد: " . $e->getMessage());
		}
	}
	
	function deleteQuota(DatabaseManager $db, string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType): bool {
		try {
			$db->beginTransaction();
			
			// حذف از جدول CargoInfo
			$queryCargoInfo = "DELETE FROM CargoInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ?";
			$stmtCargoInfo = $db->prepare($queryCargoInfo);
			$stmtCargoInfo->bind_param("ssss", $quotaNumber, $shipName, $warehouse, $shippingCompany);
			$stmtCargoInfo->execute();
			
			// حذف از جدول InitialInfo
			$queryInitialInfo = "DELETE FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
			$stmtInitialInfo = $db->prepare($queryInitialInfo);
			$stmtInitialInfo->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
			$stmtInitialInfo->execute();
			
			$db->commit();
			return true;
			} catch (Exception $e) {
			$db->rollback();
			throw new Exception("خطا در حذف کوتاژ: " . $e->getMessage());
		}
	}
	
	function gregorian_to_jalali($gy, $gm, $gd): array {
		$g_d_m = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
		$gy2 = ($gm > 2) ? ($gy + 1) : $gy;
		$days = 355666 + (365 * $gy) + floor(($gy2 + 3) / 4) - floor(($gy2 + 99) / 100) + floor(($gy2 + 399) / 400) + $gd + $g_d_m[$gm - 1];
		$jy = -1595 + (33 * floor($days / 12053));
		$days %= 12053;
		$jy += 4 * floor($days / 1461);
		$days %= 1461;
		if ($days > 365) {
			$jy += floor(($days - 1) / 365);
			$days = ($days - 1) % 365;
		}
		if ($days < 186) {
			$jm = 1 + floor($days / 31);
			$jd = 1 + ($days % 31);
			} else {
			$jm = 7 + floor(($days - 186) / 30);
			$jd = 1 + (($days - 186) % 30);
		}
		return [$jy, $jm, $jd];
	}
	
	try {
		if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
			throw new Exception('روش درخواست نامعتبر است');
		}
		
		$db = new DatabaseManager();
		
		if (!isset($_GET['action'])) {
			throw new Exception('عملیات مشخص نشده است');
		}
		
		$action = sanitizeInput($_GET['action']);
		
		switch ($action) {
			case 'getShipsList':
            $ships = getShipsList($db);
            sendJsonResponse(['data' => $ships]);
            break;
			
			case 'getShipDetails':
            if (!isset($_GET['shipName'])) {
                throw new Exception('نام کشتی مشخص نشده است');
			}
            $shipDetails = getShipDetails($db, $_GET['shipName']);
            sendJsonResponse($shipDetails);
            break;
			
			case 'getWarehouseDetails':
            if (!isset($_GET['shipName']) || !isset($_GET['warehouseName'])) {
                throw new Exception('نام کشتی یا انبار مشخص نشده است');
			}
            $warehouseDetails = getWarehouseDetails($db, $_GET['shipName'], $_GET['warehouseName']);
            sendJsonResponse($warehouseDetails);
            break;
			
			case 'getQuotaDetails':
            if (!isset($_GET['quotaNumber'])) {
                throw new Exception('شماره کوتاژ مشخص نشده است');
			}
            $quotaDetails = getQuotaDetails($db, $_GET['quotaNumber']);
            if ($quotaDetails === null) {
                sendJsonResponse(['error' => 'کوتاژ مورد نظر یافت نشد'], 404);
				} else {
                sendJsonResponse($quotaDetails);
			}
            break;
			
	case 'getQuotasList':
	if (!isset($_GET['shipName'])) {
		throw new Exception('نام کشتی مشخص نشده است');
	}
	$quotasList = getQuotasList($db, $_GET['shipName']);
	sendJsonResponse($quotasList);
	break;
	
	case 'getFilteredSummary':
	if (!isset($_GET['shipName']) || !isset($_GET['warehouseName']) || !isset($_GET['selectedQuota']) || !isset($_GET['startDateTime']) || !isset($_GET['endDateTime'])) {
		throw new Exception('پارامترهای ورودی ناقص هستند');
	}
	
	$shipName = sanitizeInput($_GET['shipName']);
	$warehouseName = sanitizeInput($_GET['warehouseName']);
	$selectedQuota = sanitizeInput($_GET['selectedQuota']);
	$startDateTime = sanitizeInput($_GET['startDateTime']);
	$endDateTime = sanitizeInput($_GET['endDateTime']);
	
	$filteredSummary = getFilteredSummary($db, $shipName, $warehouseName, $selectedQuota, $startDateTime, $endDateTime);
	echo $filteredSummary; 
	exit;
	
	case 'checkQuotaExistence':
	if (!isset($_GET['quotaNumber']) || !isset($_GET['shipName'])) {
		throw new Exception('شماره کوتاژ یا نام کشتی مشخص نشده است');
	}
	$result = checkQuotaExistence($db, $_GET['quotaNumber'], $_GET['shipName']);
	sendJsonResponse($result);
	break;
	
	case 'checkQuotaExistenceCargo':
	if (!isset($_GET['quotaNumber']) || !isset($_GET['shipName'])) {
		throw new Exception('شماره کوتاژ یا نام کشتی مشخص نشده است');
	}
	$result = checkQuotaExistenceCargo($db, $_GET['quotaNumber'], $_GET['shipName']);
	sendJsonResponse($result);
	break;
			
            $selectedQuota = $_GET['selectedQuota'];
            $startDate = sanitizeInput($_GET['startDate']);
            $endDate = sanitizeInput($_GET['endDate']);
            $filteredSummary = getFilteredSummary($db, $selectedQuota, $startDate, $endDate);
            echo $filteredSummary; 
            exit;
			
	case 'editQuota':
	if (!isset($_GET['oldQuotaNumber']) || !isset($_GET['newQuotaNumber']) || !isset($_GET['shipName']) || !isset($_GET['shippingCompany']) || !isset($_GET['warehouse']) || !isset($_GET['cargoType']) || !isset($_GET['totalTonnage'])) {
		throw new Exception('پارامترهای ورودی ناقص هستند');
	}
	$result = editQuota(
	$db,
	$_GET['oldQuotaNumber'],
	$_GET['newQuotaNumber'],
	$_GET['shipName'],
	$_GET['shippingCompany'],
	$_GET['warehouse'],
	$_GET['cargoType'],
	floatval($_GET['totalTonnage'])
	);
	sendJsonResponse(['success' => $result]);
	break;
	
	case 'updateQuotaPercentage':
		if (!isset($_GET['quotaNumber']) || !isset($_GET['percentage']) || !isset($_GET['isEnabled'])) {
			throw new Exception('پارامترهای ورودی ناقص هستند');
		}
		$result = updateQuotaPercentage(
			$db,
			$_GET['quotaNumber'],
			floatval($_GET['percentage']),
			intval($_GET['isEnabled'])
		);
		sendJsonResponse(['success' => $result]);
		break;
			
	case 'toggleQuotaStatus':
				if (!isset($_GET['quotaNumber'])) {
					throw new Exception('شماره کوتاژ مشخص نشده است');
				}
				$result = toggleQuotaStatus($db, $_GET['quotaNumber']);
				sendJsonResponse(['success' => $result]);
				break;
				
	case 'updateQuotaPercentageRestriction':
            if (!isset($_GET['quotaNumber']) || !isset($_GET['isEnabled'])) {
                throw new Exception('پارامترهای ورودی ناقص هستند');
			}
            $result = updateQuotaPercentageRestriction(
			$db,
			$_GET['quotaNumber'],
			intval($_GET['isEnabled'])
            );
            sendJsonResponse(['success' => $result]);
            break;
			
			case 'deleteQuota':
			if (!isset($_GET['quotaNumber']) || !isset($_GET['shipName']) || !isset($_GET['warehouse']) || !isset($_GET['shippingCompany']) || !isset($_GET['cargoType'])) {
				throw new Exception('پارامترهای ورودی ناقص هستند');
			}
			$result = deleteQuota(
			$db,
			$_GET['quotaNumber'],
			$_GET['shipName'],
			$_GET['warehouse'],
			$_GET['shippingCompany'],
			$_GET['cargoType']
			);
			sendJsonResponse(['success' => $result]);
			break;
			
			case 'getLoadableTonnage':
				if (!isset($_GET['quotaNumber'])) {
					throw new Exception('شماره کوتاژ مشخص نشده است');
				}
				$quotaNumber = sanitizeInput($_GET['quotaNumber']);
				$shippingCompany = isset($_GET['shippingCompany']) ? sanitizeInput($_GET['shippingCompany']) : '';
				$warehouse = isset($_GET['warehouse']) ? sanitizeInput($_GET['warehouse']) : '';
				$cargoType = isset($_GET['cargoType']) ? sanitizeInput($_GET['cargoType']) : '';
				
				$result = getLoadableTonnage($db, $quotaNumber, $shippingCompany, $warehouse, $cargoType);
				sendJsonResponse($result);
				break;
			
			case 'checkQuotaStatus':
			try {
				if (!isset($_GET['quotaNumber'])) {
					throw new Exception('شماره کوتاژ مشخص نشده است');
				}
				
				$params = [
				'quotaNumber' => $_GET['quotaNumber'],
				'shipName' => $_GET['shipName'] ?? '',
				'cargoType' => $_GET['cargoType'] ?? '',
				'shippingCompany' => $_GET['shippingCompany'] ?? ''
				];
				
				customLog("API received checkQuotaStatus request with params: " . json_encode($params));
				
				$status = checkQuotaStatus($db, $params);
				customLog("API sending response: " . json_encode($status));
				
				sendJsonResponse($status);
				} catch (Exception $e) {
				customLog("API error in checkQuotaStatus: " . $e->getMessage());
				sendJsonResponse([
				'isActive' => false,
				'status' => false,
				'message' => $e->getMessage(),
				'details' => null
				], 500);
			}
			break;
			
			case 'getRealTimeData':
            $realTimeData = getRealTimeData($db);
            sendJsonResponse($realTimeData);
            break;
			
			default:
            throw new Exception('عملیات نامعتبر است');
		}
		} catch (Exception $e) {
		sendJsonResponse(['error' => $e->getMessage()], 500);
		} finally {
		if (isset($db)) {
			$db->close();
		}
	}
	
?>