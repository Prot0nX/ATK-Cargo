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
			// ثبت درخواست ورودی
			customLog("Starting checkQuotaStatus with params: " . json_encode($params));
			
			// اعتبارسنجی پارامترهای الزامی
			$requiredParams = ['quotaNumber', 'shipName', 'cargoType', 'shippingCompany'];
			foreach ($requiredParams as $param) {
				if (!isset($params[$param]) || empty($params[$param])) {
					customLog("Missing required parameter: $param");
					throw new Exception("پارامتر $param الزامی است");
				}
			}
			
			// پاکسازی داده‌های ورودی
			$quotaNumber = sanitizeInput($params['quotaNumber']);
			$shipName = sanitizeInput($params['shipName']);
			$cargoType = sanitizeInput($params['cargoType']);
			$shippingCompany = sanitizeInput($params['shippingCompany']);
			
			// ثبت داده‌های پاکسازی شده
			customLog("Sanitized inputs - QuotaNumber: $quotaNumber, Ship: $shipName, Cargo: $cargoType, Company: $shippingCompany");
			
			// کوئری بهینه‌شده برای دریافت اطلاعات کوتاژ
			$query = "SELECT 
				i.loadingQuotaNumber,
				i.shipName,
				i.cargoType,
				i.shippingCompany,
				i.cargoWeight as totalWeight,
				i.isActive,
				COALESCE((
					SELECT SUM(netWeight)
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
			AND i.shippingCompany = ?
			LIMIT 1";
			
			// ثبت کوئری
			customLog("Executing query: $query");
			customLog("With parameters: [$quotaNumber, $shipName, $cargoType, $shippingCompany]");
			
			$stmt = $db->prepare($query);
			$stmt->bind_param("ssss", $quotaNumber, $shipName, $cargoType, $shippingCompany);
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
				customLog("Found quota record: " . json_encode($row));
				
				// محاسبات مربوط به وزن و ظرفیت
				$loadedWeight = floatval($row['loadedWeight']);
				$totalWeight = floatval($row['totalWeight']);
				$percentageLoaded = ($totalWeight > 0) ? ($loadedWeight / $totalWeight) * 100 : 0;
				$remainingCapacity = $totalWeight - $loadedWeight;
				$isActive = (bool)$row['isActive'];
				
				// ساخت پاسخ
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
			
			// اگر کوتاژ دقیق یافت نشد، بررسی کوتاژهای مشابه
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
			
			// هیچ کوتاژی یافت نشد
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
	
	function checkQuotaExistenceCargo(DatabaseManager $db, string $quotaNumber, string $shipName): array {
		// پاکسازی ورودی‌ها
		$quotaNumber = sanitizeInput($quotaNumber);
		$shipName = sanitizeInput($shipName);
		
		customLog("Checking quota existence: $quotaNumber for ship: $shipName");
		
		// بهینه‌سازی کوئری با اضافه کردن ایندکس و محدود کردن تعداد نتایج
		$query = "SELECT 
			loadingQuotaNumber, 
			shipName, 
			shippingCompany, 
			cargoType, 
			loadingWarehouse 
		FROM InitialInfo 
		WHERE loadingQuotaNumber LIKE ? 
		ORDER BY loadingQuotaNumber";
		
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
		// بهینه‌سازی کوئری برای محاسبه تناژهای کشتی‌ها با کاهش محاسبات تکراری
		$query = "
		SELECT
		i.shipName,
		COUNT(DISTINCT i.loadingWarehouse) as warehouseCount,
		COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, '-', i.loadingWarehouse, '-', i.shippingCompany, '-', i.cargoType)) as quotaCount,
		COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType)) as uniqueQuotaCombinations,
		SUM(i.cargoWeight) as totalTonnage,
		COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
		MAX(i.isActive) as isActive,
		COUNT(DISTINCT i.shippingCompany) as shippingCompanyCount,
		COUNT(DISTINCT i.cargoType) as cargoTypeCount
		FROM
			InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedWeight
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) loaded ON 
			loaded.loadingQuotaNumber = i.loadingQuotaNumber
			AND loaded.shipName = i.shipName
			AND loaded.loadingWarehouse = i.loadingWarehouse
			AND loaded.shippingCompany = i.shippingCompany
			AND loaded.cargoType = i.cargoType
		GROUP BY
			i.shipName
		ORDER BY
			isActive DESC, shipName ASC
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->execute();
			$result = $stmt->get_result();
			
			customLog("Query executed successfully. Processing results...");
			
			$activeShips = [];
			$inactiveShips = [];
			$totalShips = 0;
			$totalActiveShips = 0;
			$totalInactiveShips = 0;
			
			while ($row = $result->fetch_assoc()) {
				$totalShips++;
				
				// محاسبه تناژ بارگیری شده و باقیمانده
				$totalTonnage = floatval($row['totalTonnage']);
				$loadedTonnage = floatval($row['loadedTonnage']);
				$remainingTonnage = max(0, $totalTonnage - $loadedTonnage);
				$percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;
				
				$ship = [
					'name' => $row['shipName'],
					'warehouseCount' => intval($row['warehouseCount']),
					'quotaCount' => intval($row['quotaCount']),
					'uniqueQuotaCombinations' => intval($row['uniqueQuotaCombinations']),
					'shippingCompanyCount' => intval($row['shippingCompanyCount']),
					'cargoTypeCount' => intval($row['cargoTypeCount']),
					'totalTonnage' => $totalTonnage,
					'remainingTonnage' => $remainingTonnage,
					'loadedTonnage' => $loadedTonnage,
					'percentageLoaded' => round($percentageLoaded, 2),
					'isActive' => (bool)$row['isActive']
				];
				
				// ثبت جزئیات برای عیب‌یابی
				customLog("Ship: {$ship['name']}, Total: {$ship['totalTonnage']}, Remaining: {$ship['remainingTonnage']}, Loaded: {$loadedTonnage}");
				
				if ($ship['isActive']) {
					$activeShips[] = $ship;
					$totalActiveShips++;
				} else {
					$inactiveShips[] = $ship;
					$totalInactiveShips++;
				}
			}
			
			sendJsonResponse(['data' => [
				'activeShips' => $activeShips,
				'inactiveShips' => $inactiveShips,
				'statistics' => [
					'totalShips' => $totalShips,
					'activeShipsCount' => $totalActiveShips,
					'inactiveShipsCount' => $totalInactiveShips,
					'totalActiveTonnage' => array_sum(array_column($activeShips, 'totalTonnage')),
					'totalRemainingTonnage' => array_sum(array_column($activeShips, 'remainingTonnage')),
					'totalLoadedTonnage' => array_sum(array_column($activeShips, 'loadedTonnage')),
					'timestamp' => date('Y-m-d H:i:s')
				]
			]]);
		} catch (Exception $e) {
			customLog("Error in getShipsList: " . $e->getMessage());
			throw new Exception("خطا در دریافت لیست کشتی‌ها: " . $e->getMessage());
		}
	}
	
	function getShipDetails(DatabaseManager $db, string $shipName): array {
		$shipName = sanitizeInput($shipName);
		
		// بهینه‌سازی کوئری با استفاده از JOIN به جای subquery‌های متعدد
		$query = "
		SELECT 
			i.shipName,
			i.loadingWarehouse,
			COUNT(DISTINCT i.loadingQuotaNumber) as quotaCount,
			COUNT(DISTINCT i.shippingCompany) as shippingCompanyCount,
			COUNT(DISTINCT i.cargoType) as cargoTypeCount,
			COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType)) as uniqueQuotaCombinations,
			SUM(i.cargoWeight) as totalTonnage,
			COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
			(
				SELECT COUNT(DISTINCT c.trackingNumber)
				FROM CargoInfo c
				WHERE c.shipName = i.shipName
				AND c.status = 'خروج'
			) as totalVoucherCount,
			MAX(i.isActive) as isActive
		FROM 
			InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedWeight
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) loaded ON 
			loaded.loadingQuotaNumber = i.loadingQuotaNumber
			AND loaded.shipName = i.shipName
			AND loaded.loadingWarehouse = i.loadingWarehouse
			AND loaded.shippingCompany = i.shippingCompany
			AND loaded.cargoType = i.cargoType
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
				// محاسبه تناژ بارگیری شده و باقیمانده برای هر انبار
				$warehouseTotalTonnage = floatval($row['totalTonnage']);
				$warehouseLoadedTonnage = floatval($row['loadedTonnage']);
				$warehouseRemainingTonnage = max(0, $warehouseTotalTonnage - $warehouseLoadedTonnage);
				$warehousePercentageLoaded = ($warehouseTotalTonnage > 0) ? ($warehouseLoadedTonnage / $warehouseTotalTonnage) * 100 : 0;
				
				$warehouses[] = [
					'name' => $row['loadingWarehouse'],
					'quotaCount' => intval($row['quotaCount']),
					'uniqueQuotaCombinations' => intval($row['uniqueQuotaCombinations']),
					'shippingCompanyCount' => intval($row['shippingCompanyCount']),
					'cargoTypeCount' => intval($row['cargoTypeCount']),
					'totalTonnage' => $warehouseTotalTonnage,
					'remainingTonnage' => $warehouseRemainingTonnage,
					'loadedTonnage' => $warehouseLoadedTonnage,
					'percentageLoaded' => round($warehousePercentageLoaded, 2)
				];
				
				// محاسبه مجموع برای کل کشتی
				$totalQuotaCount += intval($row['quotaCount']);
				$totalTonnage += $warehouseTotalTonnage;
				$totalRemainingTonnage += $warehouseRemainingTonnage;
				$totalVoucherCount = intval($row['totalVoucherCount']);
				$isActive = (bool)$row['isActive']; 
				
				// ثبت اطلاعات برای عیب‌یابی
				customLog("Warehouse: {$row['loadingWarehouse']}, Quotas: {$row['quotaCount']}, Total: $warehouseTotalTonnage, Remaining: $warehouseRemainingTonnage");
			}
			
			if (empty($warehouses)) {
				throw new Exception("کشتی با نام '$shipName' یافت نشد.");
			}
			
			// محاسبه آمار کلی کشتی
			$totalLoadedTonnage = $totalTonnage - $totalRemainingTonnage;
			$totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0;
			
			customLog("Ship details calculation complete for $shipName. Total tonnage: $totalTonnage, Remaining: $totalRemainingTonnage, Loaded: $totalLoadedTonnage");
			
			return [
				'name' => $shipName,
				'warehouseCount' => count($warehouses),
				'quotaCount' => $totalQuotaCount,
				'totalTonnage' => $totalTonnage,
				'remainingTonnage' => $totalRemainingTonnage,
				'loadedTonnage' => $totalLoadedTonnage,
				'percentageLoaded' => round($totalPercentageLoaded, 2),
				'totalVoucherCount' => $totalVoucherCount,
				'isActive' => $isActive,
				'warehouses' => $warehouses,
				'lastUpdated' => date('Y-m-d H:i:s')
			];
		} catch (Exception $e) {
			throw new Exception("خطا در دریافت جزئیات کشتی: " . $e->getMessage());
		}
	}
	
	function getWarehouseDetails(DatabaseManager $db, string $shipName, string $warehouseName): array {
		$shipName = sanitizeInput($shipName);
		$warehouseName = sanitizeInput($warehouseName);
		
		// بهینه‌سازی کوئری با استفاده از JOIN و گروه‌بندی نتایج
		$query = "
		SELECT 
			i.loadingQuotaNumber,
			i.cargoType,
			i.shippingCompany,
			i.cargoOwner,
			i.cargoWeight as totalTonnage,
			i.isActive,
			COALESCE(loaded.loadedWeight, 0) as loadedTonnage,
			COALESCE(vouchers.voucherCount, 0) as voucherCount
		FROM 
			InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedWeight
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
				AND c.shipName = ?
				AND c.loadingWarehouse = ?
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) loaded ON 
			loaded.loadingQuotaNumber = i.loadingQuotaNumber
			AND loaded.shipName = i.shipName
			AND loaded.loadingWarehouse = i.loadingWarehouse
			AND loaded.shippingCompany = i.shippingCompany
			AND loaded.cargoType = i.cargoType
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				COUNT(DISTINCT c.trackingNumber) as voucherCount
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
				AND c.shipName = ?
				AND c.loadingWarehouse = ?
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) vouchers ON 
			vouchers.loadingQuotaNumber = i.loadingQuotaNumber
			AND vouchers.shipName = i.shipName
			AND vouchers.loadingWarehouse = i.loadingWarehouse
			AND vouchers.shippingCompany = i.shippingCompany
			AND vouchers.cargoType = i.cargoType
		WHERE 
			i.shipName = ? 
			AND i.loadingWarehouse = ?
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("ssssss", $shipName, $warehouseName, $shipName, $warehouseName, $shipName, $warehouseName);
			$stmt->execute();
			$result = $stmt->get_result();
			
			$quotas = [];
			$totalTonnage = 0;
			$totalRemainingTonnage = 0;
			$totalLoadedTonnage = 0;
			$totalVoucherCount = 0;
			$allExitDates = [];
			$uniqueCargoTypes = [];
			$uniqueShippingCompanies = [];
			$uniqueCargoOwners = [];
			$activeQuotasCount = 0;
			
			customLog("Processing warehouse details for $shipName - $warehouseName");
			
			while ($row = $result->fetch_assoc()) {
				$quotaNumber = $row['loadingQuotaNumber'];
				$cargoType = $row['cargoType'];
				$shippingCompany = $row['shippingCompany'];
				$cargoOwner = $row['cargoOwner'] ?? 'نامشخص';
				$isActive = (bool)$row['isActive'];
				
				// ثبت انواع کالا، شرکت‌های باربری و صاحبان کالا برای آمار
				if (!in_array($cargoType, $uniqueCargoTypes)) {
					$uniqueCargoTypes[] = $cargoType;
				}
				if (!in_array($shippingCompany, $uniqueShippingCompanies)) {
					$uniqueShippingCompanies[] = $shippingCompany;
				}
				if (!in_array($cargoOwner, $uniqueCargoOwners) && $cargoOwner !== 'نامشخص') {
					$uniqueCargoOwners[] = $cargoOwner;
				}
				
				if ($isActive) {
					$activeQuotasCount++;
				}
				
				// محاسبه درصد پیشرفت
				$quotaTotalTonnage = floatval($row['totalTonnage']);
				$quotaLoadedTonnage = floatval($row['loadedTonnage']);
				$quotaRemainingTonnage = max(0, $quotaTotalTonnage - $quotaLoadedTonnage);
				$percentageLoaded = ($quotaTotalTonnage > 0) ? ($quotaLoadedTonnage / $quotaTotalTonnage) * 100 : 0;
				
				// دریافت تاریخ‌های خروج برای این کوتاژ - استفاده از کوئری بهینه شده
				$exitQuery = "
				SELECT DISTINCT exitDate, exitTime
				FROM CargoInfo c
				WHERE c.loadingQuotaNumber = ? 
				AND c.shipName = ?
				AND c.loadingWarehouse = ?
				AND c.status = 'خروج'
				ORDER BY exitDate, exitTime
				";
				$exitStmt = $db->prepare($exitQuery);
				$exitStmt->bind_param("sss", $quotaNumber, $shipName, $warehouseName);
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
				
				// ثبت اطلاعات کوتاژ
				customLog("Quota: $quotaNumber, Type: $cargoType, Company: $shippingCompany, Total: $quotaTotalTonnage, Loaded: $quotaLoadedTonnage");
				
				$quotas[] = [
					'number' => $quotaNumber,
					'cargoType' => $cargoType,
					'shippingCompany' => $shippingCompany,
					'cargoOwner' => $cargoOwner,
					'isActive' => $isActive,
					'totalTonnage' => $quotaTotalTonnage,
					'remainingTonnage' => $quotaRemainingTonnage,
					'loadedTonnage' => $quotaLoadedTonnage,
					'percentageLoaded' => round($percentageLoaded, 2),
					'voucherCount' => intval($row['voucherCount']),
					'exitDates' => $exitDates
				];
				
				$totalTonnage += $quotaTotalTonnage;
				$totalRemainingTonnage += $quotaRemainingTonnage;
				$totalLoadedTonnage += $quotaLoadedTonnage;
				$totalVoucherCount += intval($row['voucherCount']);
			}
			
			// مرتب‌سازی کوتاژها براساس فعال بودن و سپس بیشترین تناژ باقیمانده
			usort($quotas, function($a, $b) {
				if ($a['isActive'] !== $b['isActive']) {
					return $b['isActive'] <=> $a['isActive']; // کوتاژهای فعال در ابتدا
				}
				return $b['remainingTonnage'] <=> $a['remainingTonnage']; // سپس براساس بیشترین تناژ باقیمانده
			});
			
			// محاسبه درصد تکمیل کل انبار
			$totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0;
			
			$allExitDates = array_unique($allExitDates);
			sort($allExitDates);
			
			customLog("Warehouse summary - Quotas: " . count($quotas) . ", Active: $activeQuotasCount, Total: $totalTonnage, Loaded: $totalLoadedTonnage");
			
			return [
				'name' => $warehouseName,
				'shipName' => $shipName,
				'quotaCount' => count($quotas),
				'activeQuotasCount' => $activeQuotasCount,
				'cargoTypesCount' => count($uniqueCargoTypes),
				'shippingCompaniesCount' => count($uniqueShippingCompanies),
				'cargoOwnersCount' => count($uniqueCargoOwners),
				'totalTonnage' => $totalTonnage,
				'remainingTonnage' => $totalRemainingTonnage,
				'loadedTonnage' => $totalLoadedTonnage,
				'percentageLoaded' => round($totalPercentageLoaded, 2),
				'totalVoucherCount' => $totalVoucherCount,
				'quotas' => $quotas,
				'availableExitDates' => $allExitDates,
				'cargoTypes' => $uniqueCargoTypes,
				'shippingCompanies' => $uniqueShippingCompanies,
				'lastUpdated' => date('Y-m-d H:i:s')
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
			
			// ثبت درخواست برای عیب‌یابی
			customLog("getFilteredSummary request - Ship: $shipName, Warehouse: $warehouseName, Quota: $selectedQuota, Start: $startDateTime, End: $endDateTime");
			
			// بهینه‌سازی کوئری خلاصه با تمرکز بر کاهش محاسبات تکراری
			$summaryQuery = "
			SELECT 
				COALESCE(SUM(c.netWeight), 0) as totalNetWeight,
				COUNT(DISTINCT c.trackingNumber) as voucherCount,
				MIN(c.exitTime) as firstExitTime,
				MAX(c.exitTime) as lastExitTime,
				MIN(c.exitDate) as firstExitDate,
				MAX(c.exitDate) as lastExitDate
			FROM 
				CargoInfo c
			WHERE
				c.loadingQuotaNumber = ?
				AND c.shipName = ?
				AND c.loadingWarehouse = ?
				AND c.status = 'خروج'
				AND CONCAT(c.exitDate, ' ', c.exitTime) >= ?
				AND CONCAT(c.exitDate, ' ', c.exitTime) < ?
			";
			
			$stmt = $db->prepare($summaryQuery);
			$stmt->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
			$stmt->execute();
			$summaryResult = $stmt->get_result();
			$summary = $summaryResult->fetch_assoc();
			
			// بهینه‌سازی کوئری جزئیات با join مناسب
			$detailsQuery = "
			SELECT 
				c.trackingNumber,
				c.entryTime,
				c.netWeight,
				c.exitTime,
				c.exitDate,
				c.scaleReceiptNumber,
				c.username,
				c.confirm_username,
				c.cargoType,
				c.shippingCompany,
				i.cargoOwner
			FROM 
				CargoInfo c
			JOIN InitialInfo i ON 
				c.loadingQuotaNumber = i.loadingQuotaNumber
				AND c.shipName = i.shipName
			WHERE 
				c.loadingQuotaNumber = ?
				AND c.shipName = ?
				AND c.loadingWarehouse = ?
				AND c.status = 'خروج'
				AND CONCAT(c.exitDate, ' ', c.exitTime) >= ?
				AND CONCAT(c.exitDate, ' ', c.exitTime) < ?
			ORDER BY 
				c.exitDate, c.exitTime
			";
			
			$stmtDetails = $db->prepare($detailsQuery);
			$stmtDetails->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
			$stmtDetails->execute();
			$detailsResult = $stmtDetails->get_result();
			
			$voucherDetails = [];
			$totalWeights = [];
			$uniqueUsers = [];
			
			while ($row = $detailsResult->fetch_assoc()) {
				// محاسبه و جمع‌آوری اطلاعات آماری
				if (!isset($totalWeights[$row['cargoType']])) {
					$totalWeights[$row['cargoType']] = 0;
				}
				$totalWeights[$row['cargoType']] += floatval($row['netWeight']);
				
				if (!empty($row['username']) && !in_array($row['username'], $uniqueUsers)) {
					$uniqueUsers[] = $row['username'];
				}
				if (!empty($row['confirm_username']) && !in_array($row['confirm_username'], $uniqueUsers)) {
					$uniqueUsers[] = $row['confirm_username'];
				}
				
				$voucherDetails[] = [
					'trackingNumber' => $row['trackingNumber'],
					'entryTime' => $row['entryTime'],
					'netWeight' => floatval($row['netWeight']),
					'exitTime' => $row['exitTime'],
					'exitDate' => $row['exitDate'],
					'scaleReceiptNumber' => $row['scaleReceiptNumber'],
					'username' => $row['username'],
					'confirmUsername' => $row['confirm_username'],
					'cargoType' => $row['cargoType'],
					'shippingCompany' => $row['shippingCompany'],
					'cargoOwner' => $row['cargoOwner'] ?? 'نامشخص'
				];
			}
			
			// آماده‌سازی پاسخ کامل
			$response = [
				'totalNetWeight' => floatval($summary['totalNetWeight']),
				'voucherCount' => intval($summary['voucherCount']),
				'voucherDetails' => $voucherDetails,
				'statistics' => [
					'cargoTypeWeights' => $totalWeights,
					'operatorCount' => count($uniqueUsers),
					'firstOperation' => [
						'date' => $summary['firstExitDate'] ?? '',
						'time' => $summary['firstExitTime'] ?? ''
					],
					'lastOperation' => [
						'date' => $summary['lastExitDate'] ?? '',
						'time' => $summary['lastExitTime'] ?? ''
					]
				],
				'quotaNumber' => $selectedQuota,
				'shipName' => $shipName,
				'warehouseName' => $warehouseName,
				'startDateTime' => $startDateTime,
				'endDateTime' => $endDateTime,
				'generatedAt' => date('Y-m-d H:i:s')
			];
			
			customLog("getFilteredSummary response - Total weight: {$summary['totalNetWeight']}, Vouchers: {$summary['voucherCount']}");
			
			return json_encode($response, JSON_UNESCAPED_UNICODE);
		} catch (Exception $e) {
			customLog("Error in getFilteredSummary: " . $e->getMessage());
			throw new Exception("خطا در دریافت خلاصه فیلتر شده: " . $e->getMessage());
		}
	}
	
	function getQuotaDetails(DatabaseManager $db, string $quotaNumber): ?array {
		$quotaNumber = sanitizeInput($quotaNumber);
		
		// ثبت درخواست
		customLog("Fetching quota details for: $quotaNumber");
		
		// کوئری بهینه‌شده با استفاده از JOIN به جای subquery‌های متعدد
		$query = "
		SELECT 
			i.loadingQuotaNumber, 
			i.shipName,
			i.loadingWarehouse,
			i.shippingCompany,
			i.cargoType,
			i.cargoOwner,
			i.cargoWeight as totalTonnage,
			i.isActive,
			i.percentage,
			i.is_enabled,
			COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage,
			COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount,
			COALESCE(exit_data.startDate, '') as startDate,
			COALESCE(exit_data.endDate, '') as endDate
		FROM 
			InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedTonnage,
				COUNT(DISTINCT c.trackingNumber) as exitVoucherCount,
				MIN(c.exitDate) as startDate,
				MAX(c.exitDate) as endDate
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) exit_data ON 
			exit_data.loadingQuotaNumber = i.loadingQuotaNumber
			AND exit_data.shipName = i.shipName
			AND exit_data.loadingWarehouse = i.loadingWarehouse
			AND exit_data.shippingCompany = i.shippingCompany
			AND exit_data.cargoType = i.cargoType
		WHERE 
			i.loadingQuotaNumber = ?
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("s", $quotaNumber);
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
				// محاسبه تناژ و درصد پیشرفت
				$totalTonnage = floatval($row['totalTonnage']);
				$loadedTonnage = floatval($row['loadedTonnage']);
				$remainingTonnage = max(0, $totalTonnage - $loadedTonnage);
				$percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;
				$isActive = (bool)$row['isActive'];
				$isPercentageRestricted = (bool)$row['is_enabled'];
				$percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
				
				// محاسبه تناژ قابل بارگیری با در نظر گرفتن محدودیت درصدی
				$loadableTonnage = calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
				
				// محاسبه تعداد حواله‌های خروج
				$exitVoucherCount = intval($row['exitVoucherCount']);
				
				// محاسبه میانگین وزن حواله‌ها - فقط برای حواله‌های خروج
				$avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;
				
				// ثبت اطلاعات برای عیب‌یابی
				customLog("Quota details - Number: {$row['loadingQuotaNumber']}, Total: $totalTonnage, Loaded: $loadedTonnage, Remaining: $remainingTonnage");
				customLog("Voucher counts - Exit: $exitVoucherCount");
				
				return [
					'number' => $row['loadingQuotaNumber'],
					'shipName' => $row['shipName'],
					'warehouseName' => $row['loadingWarehouse'],
					'shippingCompany' => $row['shippingCompany'],
					'cargoType' => $row['cargoType'],
					'cargoOwner' => $row['cargoOwner'] ?? 'نامشخص',
					'totalTonnage' => $totalTonnage,
					'remainingTonnage' => $remainingTonnage,
					'loadedTonnage' => $loadedTonnage,
					'percentageLoaded' => round($percentageLoaded, 2),
					'loadableTonnage' => $loadableTonnage,
					'isActive' => $isActive,
					'voucherCount' => $exitVoucherCount,
					'exitVoucherCount' => $exitVoucherCount,
					'startDate' => $row['startDate'],
					'endDate' => $row['endDate'],
					'avgVoucherWeight' => round($avgVoucherWeight, 2),
					'isPercentageRestricted' => $isPercentageRestricted,
					'percentage' => $percentage,
					'lastUpdated' => date('Y-m-d H:i:s')
				];
			}
			
			customLog("No quota found with number: $quotaNumber");
			return null;
		} catch (Exception $e) {
			customLog("Error in getQuotaDetails: " . $e->getMessage());
			throw new Exception("خطا در دریافت جزئیات کوتاژ: " . $e->getMessage());
		}
	}

	function getQuotasList(DatabaseManager $db, string $shipName): array {
		$shipName = sanitizeInput($shipName);
		
		// ثبت درخواست
		customLog("Fetching quotas list for ship: $shipName");
		
		// بهینه‌سازی کوئری با استفاده از JOIN به جای subquery‌های متعدد
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
			COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage,
			COALESCE(all_vouchers.voucherCount, 0) as voucherCount,
			COALESCE(entry_data.entryVoucherCount, 0) as entryVoucherCount,
			COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount,
			COALESCE(exit_data.lastExitDate, '') as lastExitDate
		FROM 
			InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedTonnage,
				COUNT(DISTINCT c.trackingNumber) as exitVoucherCount,
				MAX(c.exitDate) as lastExitDate
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
				AND c.shipName = ?
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) exit_data ON 
			exit_data.loadingQuotaNumber = i.loadingQuotaNumber
			AND exit_data.shipName = i.shipName
			AND exit_data.loadingWarehouse = i.loadingWarehouse
			AND exit_data.shippingCompany = i.shippingCompany
			AND exit_data.cargoType = i.cargoType
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany, 
			c.cargoType,
			COUNT(DISTINCT c.trackingNumber) as entryVoucherCount
		FROM 
			CargoInfo c
		WHERE 
			c.status = 'ورود'
			AND c.shipName = ?
		GROUP BY 
			c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) entry_data ON 
			entry_data.loadingQuotaNumber = i.loadingQuotaNumber
			AND entry_data.shipName = i.shipName
			AND entry_data.loadingWarehouse = i.loadingWarehouse
			AND entry_data.shippingCompany = i.shippingCompany
			AND entry_data.cargoType = i.cargoType
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				COUNT(DISTINCT c.trackingNumber) as voucherCount
			FROM 
				CargoInfo c
			WHERE 
				c.status IN ('ورود', 'خروج')
				AND c.shipName = ?
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) all_vouchers ON 
			all_vouchers.loadingQuotaNumber = i.loadingQuotaNumber
			AND all_vouchers.shipName = i.shipName
			AND all_vouchers.loadingWarehouse = i.loadingWarehouse
			AND all_vouchers.shippingCompany = i.shippingCompany
			AND all_vouchers.cargoType = i.cargoType
		WHERE 
			i.shipName = ?
		ORDER BY
			i.isActive DESC, i.loadingQuotaNumber ASC
		";

		try {
			$stmt = $db->prepare($query);
			$stmt->bind_param("ssss", $shipName, $shipName, $shipName, $shipName);
			$stmt->execute();
			$result = $stmt->get_result();
			$quotas = [];
			$totalQuotasCount = 0;
			$activeQuotasCount = 0;
			
			while ($row = $result->fetch_assoc()) {
				$totalQuotasCount++;
				if ((bool)$row['isActive']) {
					$activeQuotasCount++;
				}
				
				// محاسبه تناژ و درصد پیشرفت
				$loadedTonnage = floatval($row['loadedTonnage']);
				$totalTonnage = floatval($row['totalTonnage']);
				$remainingTonnage = max(0, $totalTonnage - $loadedTonnage);
				$percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
				$isPercentageRestricted = (bool)$row['is_enabled'];
				$percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;
				
				// محاسبه تناژ قابل بارگیری با در نظر گرفتن محدودیت درصدی
				$loadableTonnage = calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
				
				// محاسبه حواله‌های در انتظار
				$entryVoucherCount = intval($row['entryVoucherCount']);
				$exitVoucherCount = intval($row['exitVoucherCount']);
				$pendingVouchers = max(0, $entryVoucherCount - $exitVoucherCount);
				
				// محاسبه میانگین وزن حواله‌ها - فقط برای حواله‌های خروج
				$avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;
				
				// ثبت اطلاعات مهم کوتاژ
				customLog("Quota: {$row['number']}, Warehouse: {$row['loadingWarehouse']}, Type: {$row['cargoType']}, Exit Vouchers: $exitVoucherCount, Loaded: $loadedTonnage");
				
				$quotas[] = [
					'number' => $row['number'],
					'shipName' => $row['shipName'] ?? '',
					'warehouse' => $row['loadingWarehouse'] ?? '',
					'cargoType' => $row['cargoType'] ?? '',
					'totalTonnage' => $totalTonnage,
					'remainingTonnage' => $remainingTonnage,
					'loadedTonnage' => $loadedTonnage,
					'percentageLoaded' => round($percentageLoaded, 2),
					'voucherCount' => intval($row['voucherCount']),
					'entryVoucherCount' => $entryVoucherCount,
					'exitVoucherCount' => $exitVoucherCount,
					'pendingVoucherCount' => $pendingVouchers,
					'isActive' => (bool)$row['isActive'],
					'shippingCompany' => $row['shippingCompany'] ?? '',
					'cargoOwner' => $row['cargoOwner'] ?? '',
					'percentage' => $percentage,
					'isPercentageRestricted' => $isPercentageRestricted,
					'loadableTonnage' => $loadableTonnage,
					'avgVoucherWeight' => round($avgVoucherWeight, 2),
					'lastExitDate' => $row['lastExitDate'],
					'quotaKey' => $row['number'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType']
				];
			}
			
			// ثبت اطلاعات آماری
			customLog("Quotas list generated for ship: $shipName. Total: $totalQuotasCount, Active: $activeQuotasCount");
			
			return $quotas;
		} catch (Exception $e) {
			customLog("Error in getQuotasList: " . $e->getMessage());
			throw new Exception("خطا در دریافت لیست کوتاژها: " . $e->getMessage());
		}
	}

	function calculateLoadableTonnage(float $remainingTonnage, float $totalTonnage, ?float $percentage, bool $isPercentageRestricted): float {
		if ($isPercentageRestricted && $percentage !== null) {
			$percentageAmount = $totalTonnage * ($percentage / 100);
			return $remainingTonnage - $percentageAmount;
		}
		return $remainingTonnage;
	}

	function getLoadableTonnage(DatabaseManager $db, string $quotaNumber, string $shippingCompany = '', string $warehouse = '', string $cargoType = ''): array {
		// پاکسازی ورودی‌ها
		$quotaNumber = sanitizeInput($quotaNumber);
		$shippingCompany = sanitizeInput($shippingCompany);
		$warehouse = sanitizeInput($warehouse);
		$cargoType = sanitizeInput($cargoType);
		
		// ساخت پارامترهای کوئری
		$whereConditions = ["i.loadingQuotaNumber = ?"];
		$params = [$quotaNumber];
		$types = "s";
		
		// اضافه کردن پارامترهای اختیاری به شرط کوئری
		if (!empty($shippingCompany)) {
			$whereConditions[] = "i.shippingCompany = ?";
			$params[] = $shippingCompany;
			$types .= "s";
		}
		
		if (!empty($warehouse)) {
			$whereConditions[] = "i.loadingWarehouse = ?";
			$params[] = $warehouse;
			$types .= "s";
		}
		
		if (!empty($cargoType)) {
			$whereConditions[] = "i.cargoType = ?";
			$params[] = $cargoType;
			$types .= "s";
		}
		
		// بهینه‌سازی کوئری با استفاده از JOIN
		$query = "
		SELECT 
			i.loadingQuotaNumber as number,
			i.cargoWeight as totalTonnage,
			i.percentage,
			i.is_enabled,
			COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage
		FROM 
			InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedTonnage
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
			GROUP BY 
				c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) exit_data ON 
			exit_data.loadingQuotaNumber = i.loadingQuotaNumber
			AND exit_data.shipName = i.shipName
			AND exit_data.loadingWarehouse = i.loadingWarehouse
			AND exit_data.shippingCompany = i.shippingCompany
			AND exit_data.cargoType = i.cargoType
		WHERE " . implode(" AND ", $whereConditions) . "
		LIMIT 1";
		
		try {
			$stmt = $db->prepare($query);
			
			// باند کردن پارامترها به صورت داینامیک
			if (count($params) > 0) {
				$stmt->bind_param($types, ...$params);
			}
			
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
				// محاسبه تناژ‌ها
				$loadedTonnage = floatval($row['loadedTonnage']);
				$totalTonnage = floatval($row['totalTonnage']);
				$remainingTonnage = $totalTonnage - $loadedTonnage;
				$percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
				$isPercentageRestricted = (bool)$row['is_enabled'];
				
				// محاسبه تناژ قابل بارگیری
				$loadableTonnage = calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
				
				// محاسبه تعداد کامیون‌ها
				$trucks18Wheeler = $loadableTonnage > 0 ? floor($loadableTonnage / 25000) : 0;
				$trucks10Wheeler = $loadableTonnage > 0 ? floor($loadableTonnage / 15000) : 0;
				
				// ثبت نتیجه
				customLog("Loadable tonnage calculation - Total: $totalTonnage, Loaded: $loadedTonnage, Remaining: $remainingTonnage, Loadable: $loadableTonnage");
				
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
			// پاکسازی ورودی‌ها
			$quotaNumber = sanitizeInput($quotaNumber);
			$shipName = sanitizeInput($shipName);
			$warehouse = sanitizeInput($warehouse);
			$shippingCompany = sanitizeInput($shippingCompany);
			$cargoType = sanitizeInput($cargoType);
			
			// ثبت درخواست حذف
			customLog("Deleting quota - Number: $quotaNumber, Ship: $shipName, Warehouse: $warehouse, Company: $shippingCompany, Type: $cargoType");
			
			// آغاز تراکنش برای حفظ یکپارچگی داده‌ها
			$db->beginTransaction();
			
			// بررسی وجود کوتاژ قبل از حذف
			$checkQuery = "SELECT COUNT(*) as count FROM InitialInfo 
			WHERE loadingQuotaNumber = ? 
			AND shipName = ? 
			AND loadingWarehouse = ? 
			AND shippingCompany = ? 
			AND cargoType = ?";
			
			$checkStmt = $db->prepare($checkQuery);
			$checkStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
			$checkStmt->execute();
			$checkResult = $checkStmt->get_result();
			$row = $checkResult->fetch_assoc();
			
			if ($row['count'] == 0) {
				customLog("Quota not found for deletion - Number: $quotaNumber");
				$db->rollback();
				return false;
			}
			
			// حذف از جدول CargoInfo با یک کوئری بهینه‌شده
			$cargoQuery = "DELETE FROM CargoInfo 
			WHERE loadingQuotaNumber = ? 
			AND shipName = ? 
			AND loadingWarehouse = ? 
			AND shippingCompany = ?";
			
			$cargoStmt = $db->prepare($cargoQuery);
			$cargoStmt->bind_param("ssss", $quotaNumber, $shipName, $warehouse, $shippingCompany);
			$cargoStmt->execute();
			$cargoRowsDeleted = $cargoStmt->affected_rows;
			
			// حذف از جدول InitialInfo
			$initialQuery = "DELETE FROM InitialInfo 
			WHERE loadingQuotaNumber = ? 
			AND shipName = ? 
			AND loadingWarehouse = ? 
			AND shippingCompany = ? 
			AND cargoType = ?";
			
			$initialStmt = $db->prepare($initialQuery);
			$initialStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
			$initialStmt->execute();
			$initialRowsDeleted = $initialStmt->affected_rows;
			
			// ثبت نتیجه حذف
			customLog("Deletion completed - InitialInfo rows: $initialRowsDeleted, CargoInfo rows: $cargoRowsDeleted");
			
			$db->commit();
			return true;
		} catch (Exception $e) {
			// بازگردانی تراکنش در صورت بروز خطا
			if (isset($db)) {
				$db->rollback();
			}
			customLog("Error in deleteQuota: " . $e->getMessage());
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