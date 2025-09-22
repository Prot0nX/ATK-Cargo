<?php
	//app_api_2.php
	
	declare(strict_types=1);
	
	ini_set('display_errors', 0);
	ini_set('display_startup_errors', 0);
	error_reporting(E_ALL);
	
	header('Content-Type: application/json; charset=UTF-8');
	
	require_once __DIR__ . '/config/config.php';
	
	date_default_timezone_set('Asia/Tehran');
	
	class DatabaseManager {
	private mysqli $conn;
	private array $preparedStatements = [];
	
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
	
	public function getPreparedStatement(string $key, string $query): mysqli_stmt {
		if (!isset($this->preparedStatements[$key])) {
			$this->preparedStatements[$key] = $this->prepare($query);
		}
		return $this->preparedStatements[$key];
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
		// $logDir = __DIR__ . '/logs';
		// if (!is_dir($logDir) && !mkdir($logDir, 0755, true) && !is_dir($logDir)) {
		// 	error_log("Failed to create log directory: " . error_get_last()['message']);
		// 	return;
		// }
		// $logFile = $logDir . '/custom.log';
		// $logMessage = date('[Y-m-d H:i:s] ') . $message . PHP_EOL;
		// file_put_contents($logFile, $logMessage, FILE_APPEND);
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
			// اعتبارسنجی سریع‌تر با استفاده از array_diff_key
			$requiredParams = ['quotaNumber', 'shipName', 'cargoType', 'shippingCompany'];
			$missingParams = array_diff($requiredParams, array_keys($params));
			
			if (!empty($missingParams)) {
				throw new Exception("پارامترهای الزامی مفقود: " . implode(', ', $missingParams));
			}
			
			// اعتبارسنجی خالی بودن در یک حلقه
			foreach ($requiredParams as $param) {
				if (empty(trim($params[$param]))) {
					throw new Exception("پارامتر $param نمی‌تواند خالی باشد");
				}
			}
			
			// پاکسازی داده‌های ورودی
			$quotaNumber = sanitizeInput($params['quotaNumber']);
			$shipName = sanitizeInput($params['shipName']);
			$cargoType = sanitizeInput($params['cargoType']);
			$shippingCompany = sanitizeInput($params['shippingCompany']);
			
			// جایگزینی LEFT JOIN با EXISTS برای بهتر شدن عملکرد
			$query = "SELECT 
				i.loadingQuotaNumber,
				i.shipName,
				i.cargoType,
				i.shippingCompany,
				i.cargoWeight as totalWeight,
				i.isActive,
				(
					SELECT COALESCE(SUM(c.netWeight), 0)
					FROM CargoInfo c
					WHERE c.status = 'خروج'
					AND c.loadingQuotaNumber = i.loadingQuotaNumber
					AND c.shipName = i.shipName
					AND c.cargoType = i.cargoType
					AND c.shippingCompany = i.shippingCompany
				) as loadedWeight,
				CASE 
					WHEN i.cargoWeight > 0 THEN 
						((
							SELECT COALESCE(SUM(c.netWeight), 0)
							FROM CargoInfo c
							WHERE c.status = 'خروج'
							AND c.loadingQuotaNumber = i.loadingQuotaNumber
							AND c.shipName = i.shipName
							AND c.cargoType = i.cargoType
							AND c.shippingCompany = i.shippingCompany
						) / i.cargoWeight) * 100
					ELSE 0 
				END as percentageLoaded,
				CASE 
					WHEN i.isActive = 0 THEN CONCAT('کوتاژ ', i.loadingQuotaNumber, ' با نوع کالای ', i.cargoType, ' غیرفعال است')
					WHEN i.cargoWeight > 0 AND ((
						SELECT COALESCE(SUM(c.netWeight), 0)
						FROM CargoInfo c
						WHERE c.status = 'خروج'
						AND c.loadingQuotaNumber = i.loadingQuotaNumber
						AND c.shipName = i.shipName
						AND c.cargoType = i.cargoType
						AND c.shippingCompany = i.shippingCompany
					) / i.cargoWeight) * 100 >= 100 THEN 
						CONCAT('ظرفیت بارگیری کوتاژ ', i.loadingQuotaNumber, ' با نوع کالای ', i.cargoType, ' تکمیل شده است')
					WHEN i.cargoWeight > 0 AND ((
						SELECT COALESCE(SUM(c.netWeight), 0)
						FROM CargoInfo c
						WHERE c.status = 'خروج'
						AND c.loadingQuotaNumber = i.loadingQuotaNumber
						AND c.shipName = i.shipName
						AND c.cargoType = i.cargoType
						AND c.shippingCompany = i.shippingCompany
					) / i.cargoWeight) * 100 >= 95 THEN 
						CONCAT('هشدار: ظرفیت بارگیری کوتاژ ', i.loadingQuotaNumber, ' با نوع کالای ', i.cargoType, ' به ', 
							   ROUND(((
									SELECT COALESCE(SUM(c.netWeight), 0)
									FROM CargoInfo c
									WHERE c.status = 'خروج'
									AND c.loadingQuotaNumber = i.loadingQuotaNumber
									AND c.shipName = i.shipName
									AND c.cargoType = i.cargoType
									AND c.shippingCompany = i.shippingCompany
								) / i.cargoWeight) * 100, 2), '% رسیده است')
					ELSE CONCAT('کوتاژ ', i.loadingQuotaNumber, ' با نوع کالای ', i.cargoType, ' فعال است')
				END as status_message
			FROM InitialInfo i 
			WHERE i.loadingQuotaNumber = ?
				AND i.shipName = ?
				AND i.cargoType = ?
				AND i.shippingCompany = ?
			LIMIT 1";
			
			$stmt = $db->prepare($query);
			$stmt->bind_param("ssss", $quotaNumber, $shipName, $cargoType, $shippingCompany);
			$stmt->execute();
			$result = $stmt->get_result();
			
			if ($row = $result->fetch_assoc()) {
			// محاسبه وضعیت فعال بودن بر اساس isActive و ظرفیت باقیمانده
			$isActiveStatus = (bool)$row['isActive'] && ((float)$row['loadedWeight'] < (float)$row['totalWeight']);
			
			return [
				'isActive' => $isActiveStatus,
				'status' => true,
				'message' => $row['status_message'],
				'details' => [
					'quotaNumber' => $row['loadingQuotaNumber'],
					'shipName' => $row['shipName'],
					'cargoType' => $row['cargoType'],
					'shippingCompany' => $row['shippingCompany'],
					'totalWeight' => (float)$row['totalWeight'],
					'loadedWeight' => (float)$row['loadedWeight'],
					'remainingCapacity' => max(0, (float)$row['totalWeight'] - (float)$row['loadedWeight']),
					'percentageLoaded' => number_format((float)$row['percentageLoaded'], 2, '.', '')
				]
			];
		}
			
			// هیچ کوتاژی با مشخصات دقیق یافت نشد
		return [
			'isActive' => false,
			'status' => false,
			'message' => "کوتاژ $quotaNumber با مشخصات درخواستی یافت نشد",
			'details' => null
		];
			
		} catch (Exception $e) {
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
		return "هشدار: ظرفیت بارگیری کوتاژ $quotaNumber با نوع کالای $cargoType به " . number_format($percentageLoaded, 2, '.', '') . "% رسیده است";
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
        'percentageLoaded' => number_format($percentageLoaded, 2, '.', '')
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
		AND shipName = ? 
		ORDER BY loadingQuotaNumber";
		
		try {
			$stmt = $db->prepare($query);
			$likeQuotaNumber = '%' . $quotaNumber;
			$stmt->bind_param("ss", $likeQuotaNumber, $shipName);
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
			COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, '-', i.loadingWarehouse, '-', i.shippingCompany, '-', i.cargoType)) as quotaCount,
			COUNT(DISTINCT i.shippingCompany) as shippingCompanyCount,
			COUNT(DISTINCT i.cargoType) as cargoTypeCount,
			SUM(i.cargoWeight) as totalTonnage,
			COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
			(SUM(i.cargoWeight) - COALESCE(SUM(loaded.loadedWeight), 0)) as remainingTonnage,
			CASE 
				WHEN SUM(i.cargoWeight) > 0 THEN 
					ROUND((COALESCE(SUM(loaded.loadedWeight), 0) / SUM(i.cargoWeight)) * 100, 2)
				ELSE 0.00 
			END as percentageLoaded,
			MAX(i.isActive) as isActive
		FROM InitialInfo i
		LEFT JOIN (
			SELECT 
				c.loadingQuotaNumber,
				c.shipName,
				c.loadingWarehouse,
				c.shippingCompany,
				c.cargoType,
				SUM(c.netWeight) as loadedWeight
			FROM CargoInfo c
			WHERE c.status = 'خروج'
			GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
		) loaded ON 
			loaded.loadingQuotaNumber = i.loadingQuotaNumber
			AND loaded.shipName = i.shipName
			AND loaded.loadingWarehouse = i.loadingWarehouse
			AND loaded.shippingCompany = i.shippingCompany
			AND loaded.cargoType = i.cargoType
		GROUP BY i.shipName
		ORDER BY isActive DESC, shipName ASC
		";
		
		try {
			$stmt = $db->prepare($query);
			$stmt->execute();
			$result = $stmt->get_result();
			
			$activeShips = [];
			$inactiveShips = [];
			$totalActiveTonnage = 0;
			$totalRemainingTonnage = 0;
			$totalLoadedTonnage = 0;
			
			// پردازش نتایج با حداقل محاسبات PHP
			while ($row = $result->fetch_assoc()) {
				$ship = [
					'name' => $row['shipName'],
					'warehouseCount' => (int)$row['warehouseCount'],
					'quotaCount' => (int)$row['quotaCount'],
					'shippingCompanyCount' => (int)$row['shippingCompanyCount'],
					'cargoTypeCount' => (int)$row['cargoTypeCount'],
					'totalTonnage' => (float)$row['totalTonnage'],
					'remainingTonnage' => max(0, (float)$row['remainingTonnage']),
					'loadedTonnage' => (float)$row['loadedTonnage'],
					'percentageLoaded' => (float)$row['percentageLoaded'],
					'isActive' => (bool)$row['isActive']
				];
				
				if ($ship['isActive']) {
					$activeShips[] = $ship;
					$totalActiveTonnage += $ship['totalTonnage'];
					$totalRemainingTonnage += $ship['remainingTonnage'];
					$totalLoadedTonnage += $ship['loadedTonnage'];
				} else {
					$inactiveShips[] = $ship;
				}
			}
			
			// ارسال پاسخ بهینه‌شده
			return [
				'data' => [
					'activeShips' => $activeShips,
					'inactiveShips' => $inactiveShips,
					'statistics' => [
						'totalShips' => count($activeShips) + count($inactiveShips),
						'activeShipsCount' => count($activeShips),
						'inactiveShipsCount' => count($inactiveShips),
						'totalActiveTonnage' => $totalActiveTonnage,
						'totalRemainingTonnage' => $totalRemainingTonnage,
						'totalLoadedTonnage' => $totalLoadedTonnage,
						'timestamp' => date('Y-m-d H:i:s')
					]
				]
			];
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
					'percentageLoaded' => number_format($warehousePercentageLoaded, 2, '.', '')
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
				'percentageLoaded' => number_format($totalPercentageLoaded, 2, '.', ''),
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
				'percentageLoaded' => number_format($totalPercentageLoaded, 2, '.', ''),
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

	function getFilteredQuotas(DatabaseManager $db, string $shipName, string $startDateTime, string $endDateTime): array {
		$shipName = sanitizeInput($shipName);
		$startDateTime = sanitizeInput($startDateTime);
		$endDateTime = sanitizeInput($endDateTime);
		
		// ثبت درخواست
		customLog("Fetching filtered quotas list for ship: $shipName, from: $startDateTime to: $endDateTime");
		
		// کوئری بهینه‌سازی شده با فیلتر زمانی بر اساس exitDate و exitTime
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
				AND (
					(c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?))
					AND
					(c.exitDate < ? OR (c.exitDate = ? AND c.exitTime <= ?))
				)
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
				COUNT(DISTINCT c.trackingNumber) as voucherCount
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
				AND c.shipName = ?
				AND (
					(c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?))
					AND
					(c.exitDate < ? OR (c.exitDate = ? AND c.exitTime <= ?))
				)
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
			// تجزیه تاریخ و زمان شروع و پایان
			$startDate = substr($startDateTime, 0, 10);
			$startTime = substr($startDateTime, 11, 8);
			$endDate = substr($endDateTime, 0, 10);
			$endTime = substr($endDateTime, 11, 8);
			
			$stmt = $db->prepare($query);
			$stmt->bind_param("sssssssssssssss", 
				$shipName, 
				$startDate, $startDate, $startTime, 
				$endDate, $endDate, $endTime,
				$shipName, 
				$startDate, $startDate, $startTime, 
				$endDate, $endDate, $endTime,
				$shipName
			);
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
				$exitVoucherCount = intval($row['exitVoucherCount']);
				$pendingVouchers = 0; // فقط بر اساس خروج محاسبه می‌شود
				
				// محاسبه میانگین وزن حواله‌ها - فقط برای حواله‌های خروج
				$avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;
				
				// ثبت اطلاعات مهم کوتاژ
				customLog("Filtered Quota: {$row['number']}, Warehouse: {$row['loadingWarehouse']}, Type: {$row['cargoType']}, Exit Vouchers: $exitVoucherCount, Loaded: $loadedTonnage");
				
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
					'entryVoucherCount' => 0,
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
			customLog("Filtered quotas list generated for ship: $shipName. Total: $totalQuotasCount, Active: $activeQuotasCount, Period: $startDateTime to $endDateTime");
			
			return $quotas;
		} catch (Exception $e) {
			customLog("Error in getFilteredQuotas: " . $e->getMessage());
			throw new Exception("خطا در دریافت لیست کوتاژهای فیلتر شده: " . $e->getMessage());
		}
	}

	function getAllQuotasList(DatabaseManager $db): array {
		// ثبت درخواست
		customLog("Fetching all quotas list from all ships - optimized for InitialInfo only");
		
		// کوئری بهینه‌سازی شده - فقط داده‌های InitialInfo
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
			i.temp_tonnage_status,
			i.temp_tonnage_amount
		FROM 
			InitialInfo i
		ORDER BY
			i.shipName ASC, i.isActive DESC, i.loadingQuotaNumber ASC
		";

		try {
			$stmt = $db->prepare($query);
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
				
				// فقط داده‌های اولیه InitialInfo - بدون پردازش CargoInfo
				$totalTonnage = floatval($row['totalTonnage']);
				$percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
				$isPercentageRestricted = (bool)$row['is_enabled'];
				
				// ثبت اطلاعات کوتاژ
				customLog("Quota: {$row['number']}, Ship: {$row['shipName']}, Warehouse: {$row['loadingWarehouse']}, Type: {$row['cargoType']}");
				
				$quotas[] = [
					'number' => $row['number'],
					'shipName' => $row['shipName'] ?? '',
					'warehouse' => $row['loadingWarehouse'] ?? '',
					'cargoType' => $row['cargoType'] ?? '',
					'totalTonnage' => $totalTonnage,
					'remainingTonnage' => $totalTonnage, // بدون محاسبه بار خروجی
					'loadedTonnage' => 0, // بدون پردازش CargoInfo
					'percentageLoaded' => 0,
					'voucherCount' => 0,
					'entryVoucherCount' => 0,
					'exitVoucherCount' => 0,
					'pendingVoucherCount' => 0,
					'isActive' => (bool)$row['isActive'],
					'shippingCompany' => $row['shippingCompany'] ?? '',
					'cargoOwner' => $row['cargoOwner'] ?? '',
					'percentage' => $percentage,
					'isPercentageRestricted' => $isPercentageRestricted,
					'loadableTonnage' => $totalTonnage, // کل تناژ قابل بارگیری
					'avgVoucherWeight' => 0,
					'lastExitDate' => '',
					'temporaryTonnageEnabled' => (bool)($row['temp_tonnage_status'] ?? false),
					'temporaryTonnageValue' => $row['temp_tonnage_amount'] ? floatval($row['temp_tonnage_amount']) : null,
					'quotaKey' => $row['number'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType']
				];
			}
			
			// ثبت اطلاعات آماری
			customLog("All quotas list generated. Total: $totalQuotasCount, Active: $activeQuotasCount");
			
			return $quotas;
		} catch (Exception $e) {
			customLog("Error in getAllQuotasList: " . $e->getMessage());
			throw new Exception("خطا در دریافت لیست تمام کوتاژها: " . $e->getMessage());
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
				COUNT(DISTINCT c.trackingNumber) as voucherCount
			FROM 
				CargoInfo c
			WHERE 
				c.status = 'خروج'
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
			$stmt->bind_param("sss", $shipName, $shipName, $shipName);
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
				$exitVoucherCount = intval($row['exitVoucherCount']);
				$pendingVouchers = 0; // فقط بر اساس خروج محاسبه می‌شود
				
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
					'entryVoucherCount' => 0,
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
			$isEnabled = ($percentage > 0.00) ? 1 : 0;
			
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

	// تابع دریافت کوتاژها با گروه‌بندی بر اساس شرکت حمل
	function getGroupedQuotas($db) {
		// دریافت تمام کوتاژها از تمام کشتی‌ها
		$quotas = getAllQuotasList($db);
		$grouped = [];
		
		foreach ($quotas as $quota) {
			$shipName = $quota['shipName'] ?: 'نامشخص';
			$cargoOwner = $quota['cargoOwner'] ?: 'نامشخص';
			
			if (!isset($grouped[$shipName])) {
				$grouped[$shipName] = [];
			}
			if (!isset($grouped[$shipName][$cargoOwner])) {
				$grouped[$shipName][$cargoOwner] = [];
			}
			$grouped[$shipName][$cargoOwner][] = $quota;
		}
		
		return $grouped;
	}

	// تابع به‌روزرسانی تناژ موقت
	function updateTemporaryTonnage($db, $quotaNumber, $enabled, $tonnage = null) {
		try {
			$quotaNumber = sanitizeInput($quotaNumber);
			$enabledInt = intval($enabled);
			
			customLog("updateTemporaryTonnage: Processing quota=$quotaNumber, enabled=$enabledInt, tonnage=$tonnage");
			
			// بررسی وجود کوتاژ قبل از به‌روزرسانی
			$checkQuery = "SELECT loadingQuotaNumber, temp_tonnage_status, temp_tonnage_amount FROM InitialInfo WHERE loadingQuotaNumber = ?";
			$checkStmt = $db->prepare($checkQuery);
			$checkStmt->bind_param("s", $quotaNumber);
			$checkStmt->execute();
			$result = $checkStmt->get_result();
			
			if ($result->num_rows === 0) {
				throw new Exception('کوتاژ مورد نظر یافت نشد');
			}
			
			$currentData = $result->fetch_assoc();
			customLog("updateTemporaryTonnage: Current data - enabled={$currentData['temp_tonnage_status']}, value={$currentData['temp_tonnage_amount']}");
			
			if ($enabledInt && $tonnage !== null) {
				// فعال کردن تناژ موقت با مقدار مشخص
				$query = "UPDATE InitialInfo SET temp_tonnage_status = 1, temp_tonnage_amount = ? WHERE loadingQuotaNumber = ?";
				$stmt = $db->prepare($query);
				$stmt->bind_param("ds", $tonnage, $quotaNumber);
				customLog("updateTemporaryTonnage: Enabling temporary tonnage with value: $tonnage");
			} else {
				// غیرفعال کردن تناژ موقت
				$query = "UPDATE InitialInfo SET temp_tonnage_status = 0, temp_tonnage_amount = NULL WHERE loadingQuotaNumber = ?";
				$stmt = $db->prepare($query);
				$stmt->bind_param("s", $quotaNumber);
				customLog("updateTemporaryTonnage: Disabling temporary tonnage");
			}
			
			if ($stmt->execute()) {
				if ($stmt->affected_rows > 0) {
					customLog("updateTemporaryTonnage: Successfully updated quota $quotaNumber");
					return ['success' => true, 'message' => 'تناژ موقت با موفقیت به‌روزرسانی شد'];
				} else {
					customLog("updateTemporaryTonnage: No rows affected - data might be the same");
					return ['success' => true, 'message' => 'تناژ موقت با موفقیت به‌روزرسانی شد (بدون تغییر)'];
				}
			} else {
				$error = $stmt->error;
				customLog("updateTemporaryTonnage: SQL execution failed - $error");
				throw new Exception('خطا در اجرای کوئری: ' . $error);
			}
		} catch (Exception $e) {
			customLog("updateTemporaryTonnage: Exception - " . $e->getMessage());
			throw new Exception('خطا در به‌روزرسانی تناژ موقت: ' . $e->getMessage());
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
            sendJsonResponse($ships);
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
	
	case 'getFilteredQuotas':
	if (!isset($_GET['shipName']) || !isset($_GET['startDateTime']) || !isset($_GET['endDateTime'])) {
		throw new Exception('پارامترهای ورودی ناقص هستند - نام کشتی، تاریخ شروع و پایان الزامی است');
	}
	
	$shipName = sanitizeInput($_GET['shipName']);
	$startDateTime = sanitizeInput($_GET['startDateTime']);
	$endDateTime = sanitizeInput($_GET['endDateTime']);
	
	$filteredQuotas = getFilteredQuotas($db, $shipName, $startDateTime, $endDateTime);
	sendJsonResponse($filteredQuotas);
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
$result = checkQuotaExistenceCargo($db, $_GET['quotaNumber'], $_GET['shipName']);
	sendJsonResponse($result);
	break;
	
	case 'checkQuotaExistenceCargo':
	if (!isset($_GET['quotaNumber']) || !isset($_GET['shipName'])) {
		throw new Exception('شماره کوتاژ یا نام کشتی مشخص نشده است');
	}
	$result = checkQuotaExistenceCargo($db, $_GET['quotaNumber'], $_GET['shipName']);
	sendJsonResponse($result);
	break;

	case 'getGroupedQuotas':
	$groupedQuotas = getGroupedQuotas($db);
	sendJsonResponse($groupedQuotas);
	break;

	case 'updateTemporaryTonnage':
if (!isset($_GET['quotaNumber']) || !isset($_GET['enabled'])) {
	throw new Exception('پارامترهای ورودی ناقص هستند');
}

// بررسی پارامتر tonnage فقط زمانی که enabled برابر 1 باشد
$enabled = intval($_GET['enabled']);
if ($enabled === 1 && !isset($_GET['tonnage'])) {
	throw new Exception('مقدار تناژ موقت الزامی است');
}

$tonnage = isset($_GET['tonnage']) ? floatval($_GET['tonnage']) : null;
customLog("updateTemporaryTonnage called with: quotaNumber={$_GET['quotaNumber']}, enabled=$enabled, tonnage=$tonnage");

$result = updateTemporaryTonnage($db, $_GET['quotaNumber'], $enabled, $tonnage);
sendJsonResponse($result);
break;
			
            $selectedQuota = $_GET['selectedQuota'];
            $startDate = sanitizeInput($_GET['startDate']);
            $endDate = sanitizeInput($_GET['endDate']);
            $filteredSummary = getFilteredSummary($db, $selectedQuota, $startDate, $endDate, $shippingCompany, $cargoType);
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
            // Get real-time data by combining ships list and active quotas
            $shipsList = getShipsList($db);
            $realTimeData = [
                'ships' => $shipsList['data'],
                'timestamp' => date('Y-m-d H:i:s'),
                'status' => 'success'
            ];
            sendJsonResponse($realTimeData);
            break;
    }
} catch (Exception $e) {
    customLog("API Error: " . $e->getMessage());
    sendJsonResponse(['error' => $e->getMessage()], 500);
}
	
?>