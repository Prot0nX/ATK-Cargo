<?php
// PHP/src/Services/QuotaService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\DatabaseManager;
use App\Core\MicroCache;
use App\Validators\InputValidator;
use App\Enums\CargoStatus;

// منطق تجاری «کوتاژ» که قبلاً داخل AppApiController بود؛ اکنون آن کنترلر فقط delegate می‌کند
final class QuotaService {
    // بدون ->value: PHP 8.1 (تولید) اجازه‌ی property-fetch در class const را نمی‌دهد.
    private const EXITED = CargoStatus::EXITED;

    private DatabaseManager $db;
    private QuotaCalculator $calculator;

    public function __construct() {
        $this->db = new DatabaseManager();
        $this->calculator = new QuotaCalculator();
    }

    public function checkQuotaStatus(array $params): array {
        // پیاده‌سازی منطق checkQuotaStatus
        $requiredParams = ['quotaNumber', 'shipName', 'cargoType', 'shippingCompany', 'warehouse'];
        foreach ($requiredParams as $param) {
            if (empty(trim($params[$param] ?? ''))) {
                throw new \Exception("پارامتر $param نمی‌تواند خالی باشد");
            }
        }

        $quotaNumber = InputValidator::sanitize($params['quotaNumber']);
        $shipName = InputValidator::sanitize($params['shipName']);
        $cargoType = InputValidator::sanitize($params['cargoType']);
        $shippingCompany = InputValidator::sanitize($params['shippingCompany']);
        $warehouse = InputValidator::sanitize($params['warehouse']);

        $query = "SELECT
            i.loadingQuotaNumber, i.shipName, i.cargoType, i.shippingCompany, i.loadingWarehouse,
            i.cargoWeight as totalWeight, i.isActive, COALESCE(SUM(c.netWeight), 0) as loadedWeight
        FROM InitialInfo i
        LEFT JOIN CargoInfo c ON
            c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
            AND c.cargoType = i.cargoType AND c.shippingCompany = i.shippingCompany
            AND c.loadingWarehouse = i.loadingWarehouse AND c.status = '" . self::EXITED->value . "'
        WHERE i.loadingQuotaNumber = ? AND i.shipName = ? AND i.cargoType = ?
            AND i.shippingCompany = ? AND i.loadingWarehouse = ?
        GROUP BY i.loadingQuotaNumber, i.shipName, i.cargoType, i.shippingCompany, i.loadingWarehouse, i.cargoWeight, i.isActive
        LIMIT 1";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("sssss", $quotaNumber, $shipName, $cargoType, $shippingCompany, $warehouse);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $totalWeight = (float)$row['totalWeight'];
            $loadedWeight = (float)$row['loadedWeight'];
            $isActiveDb = (bool)$row['isActive'];

            $percentageLoaded = $totalWeight > 0 ? ($loadedWeight / $totalWeight) * 100 : 0.0;
            $remainingCapacity = $totalWeight - $loadedWeight;
            $isActiveStatus = $isActiveDb && ($loadedWeight < $totalWeight);

            $statusMessage = $this->calculator->generateStatusMessage($isActiveDb, $percentageLoaded, $quotaNumber, $cargoType);

            return [
                'isActive' => $isActiveStatus,
                'status' => true,
                'message' => $statusMessage,
                'details' => [
                    'quotaNumber' => $row['loadingQuotaNumber'],
                    'shipName' => $row['shipName'],
                    'cargoType' => $row['cargoType'],
                    'shippingCompany' => $row['shippingCompany'],
                    'totalWeight' => $totalWeight,
                    'loadedWeight' => $loadedWeight,
                    'remainingCapacity' => $remainingCapacity,
                    'percentageLoaded' => number_format($percentageLoaded, 2, '.', '')
                ]
            ];
        }

        return [
            'isActive' => false,
            'status' => false,
            'message' => "کوتاژ $quotaNumber با مشخصات درخواستی یافت نشد",
            'details' => null
        ];
    }

    public function checkQuotaExistenceCargo(string $quotaNumber, string $shipName): array {
        $quotaNumber = InputValidator::sanitize($quotaNumber);
        $shipName = InputValidator::sanitize($shipName);

        // REVERSE() تطبیق معکوس رقم‌ها را بدون ستون واقعی انجام می‌دهد؛ isActive هم مستقیماً اینجا محاسبه می‌شود تا از N+1 روی checkQuotaStatus جلوگیری شود
        $query = "SELECT i.loadingQuotaNumber, i.shipName, i.shippingCompany, i.cargoType, i.loadingWarehouse,
                i.isActive, i.cargoWeight as totalWeight, COALESCE(SUM(c.netWeight), 0) as loadedWeight
            FROM InitialInfo i
            LEFT JOIN CargoInfo c ON
                c.loadingQuotaNumber = i.loadingQuotaNumber AND c.shipName = i.shipName
                AND c.cargoType = i.cargoType AND c.shippingCompany = i.shippingCompany
                AND c.loadingWarehouse = i.loadingWarehouse AND c.status = '" . self::EXITED->value . "'
            WHERE REVERSE(i.loadingQuotaNumber) LIKE ? AND i.shipName = ?
            GROUP BY i.loadingQuotaNumber, i.shipName, i.shippingCompany, i.cargoType, i.loadingWarehouse, i.isActive, i.cargoWeight
            ORDER BY i.loadingQuotaNumber";

        $stmt = $this->db->prepare($query);
        $reversedLikeQuotaNumber = strrev($quotaNumber) . '%';
        $stmt->bind_param("ss", $reversedLikeQuotaNumber, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();

        $matchingQuotas = [];
        while ($row = $result->fetch_assoc()) {
            $totalWeight = (float)$row['totalWeight'];
            $loadedWeight = (float)$row['loadedWeight'];
            $matchingQuotas[] = [
                'quotaNumber' => $row['loadingQuotaNumber'],
                'shipName' => $row['shipName'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'warehouse' => $row['loadingWarehouse'],
                'isActive' => (bool)$row['isActive'] && ($loadedWeight < $totalWeight)
            ];
        }

        $exists = !empty($matchingQuotas);
        $message = $exists ? "کوتاژ(های) مطابق یافت شد." : "کوتاژ مورد نظر در سیستم وجود ندارد.";

        return [
            'exists' => $exists,
            'matchingQuotas' => $matchingQuotas,
            'message' => $message
        ];
    }

    public function getQuotaDetails(string $quotaNumber): ?array {
        $quotaNumber = InputValidator::sanitize($quotaNumber);

        $query = "SELECT
            i.loadingQuotaNumber, i.shipName, i.loadingWarehouse, i.shippingCompany, i.cargoType, i.cargoOwner,
            i.cargoWeight as totalTonnage, i.isActive, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage, COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount,
            COALESCE(exit_data.startDate, '') as startDate, COALESCE(exit_data.endDate, '') as endDate
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType,
                SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount,
                MIN(c.exitDate) as startDate, MAX(c.exitDate) as endDate
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.loadingQuotaNumber = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.loadingQuotaNumber = ?";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ss", $quotaNumber, $quotaNumber);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $totalTonnage = floatval($row['totalTonnage']);
            $loadedTonnage = floatval($row['loadedTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0;
            $isActive = (bool)$row['isActive'];
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;

            $loadableTonnage = $this->calculator->calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
            $exitVoucherCount = intval($row['exitVoucherCount']);
            $avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0;

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
        return null;
    }

    // LIMIT 2000 یک سقف سخت‌گیرانه است، نه صفحه‌بندی واقعی؛ فقط محافظ در برابر رشد غیرمنتظره
    public function getFilteredQuotas(string $shipName, string $startDateTime, string $endDateTime): array {
        $shipName = InputValidator::validateIdentifier($shipName);
        $startDateTime = InputValidator::validateIdentifier($startDateTime);
        $endDateTime = InputValidator::validateIdentifier($endDateTime);

        if (strlen($startDateTime) === 16) {
            $startDateTime .= ':00';
        }
        if (strlen($endDateTime) === 16) {
            $endDateTime .= ':00';
        }

        // all_vouchers قبلاً یک LEFT JOIN مستقل و تکراری بود که همان عدد را دوباره محاسبه می‌کرد؛ حذف شد
        $query = "SELECT i.id, i.loadingQuotaNumber as number, i.shipName, i.loadingWarehouse, i.cargoType,
            i.cargoWeight as totalTonnage, i.isActive, i.shippingCompany, i.cargoOwner, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage,
            COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType,
                SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.shipName = ?
                AND ((c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?)) AND (c.exitDate < ? OR (c.exitDate = ? AND c.exitTime <= ?)))
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC
        LIMIT 2000";

        $startDate = substr($startDateTime, 0, 10);
        $startTime = substr($startDateTime, 11, 8);
        $endDate = substr($endDateTime, 0, 10);
        $endTime = substr($endDateTime, 11, 8);

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ssssssss",
            $shipName, $startDate, $startDate, $startTime, $endDate, $endDate, $endTime,
            $shipName
        );
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $exitVoucherCount = intval($row['exitVoucherCount']);

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'voucherCount' => $exitVoucherCount,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted
            ];
        }
        return $quotas;
    }

    public function getAllQuotasList(?string $shipName = null): array {
        $query = "SELECT id, loadingQuotaNumber as number, shipName, loadingWarehouse, cargoType, cargoWeight as totalTonnage,
            isActive, shippingCompany, cargoOwner, percentage, is_enabled, temp_tonnage_status, temp_tonnage_amount
        FROM InitialInfo";
        if ($shipName !== null && $shipName !== '') {
            $query .= " WHERE shipName = ?";
        }
        $query .= " ORDER BY shipName ASC, isActive DESC, loadingQuotaNumber ASC";

        $stmt = $this->db->prepare($query);
        if ($shipName !== null && $shipName !== '') {
            $stmt->bind_param("s", $shipName);
        }
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $totalTonnage = floatval($row['totalTonnage']);
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $totalTonnage,
                'loadedTonnage' => 0,
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
                'loadableTonnage' => $totalTonnage,
                'avgVoucherWeight' => 0,
                'lastExitDate' => '',
                'temporaryTonnageEnabled' => (bool)($row['temp_tonnage_status'] ?? false),
                'temporaryTonnageValue' => $row['temp_tonnage_amount'] ? floatval($row['temp_tonnage_amount']) : null,
                'quotaKey' => $row['number'] . '|' . $row['shipName'] . '|' . $row['loadingWarehouse'] . '|' . $row['shippingCompany'] . '|' . $row['cargoType']
            ];
        }
        return $quotas;
    }

    public function getQuotasList(string $shipName): array {
        $shipName = InputValidator::validateIdentifier($shipName);

        return MicroCache::remember(AppApiCacheKeys::quotasList($shipName), 6, function () use ($shipName) {
            return $this->computeQuotasList($shipName);
        });
    }

    private function computeQuotasList(string $shipName): array {
        // LIMIT 2000 فقط یک سقف محافظتی است؛ all_vouchers تکراری حذف شد و exitVoucherCount برای هر دو فیلد استفاده می‌شود
        $query = "SELECT i.id, i.loadingQuotaNumber as number, i.shipName, i.loadingWarehouse, i.cargoType, i.cargoWeight as totalTonnage, i.isActive, i.shippingCompany, i.cargoOwner, i.percentage, i.is_enabled,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage, COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedTonnage, COUNT(DISTINCT c.trackingNumber) as exitVoucherCount
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC
        LIMIT 2000";

        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ss", $shipName, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        $quotas = [];

        while ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];
            $exitVoucherCount = intval($row['exitVoucherCount']);

            $quotas[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['loadingWarehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'voucherCount' => $exitVoucherCount,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted
            ];
        }
        return $quotas;
    }

    public function getLoadableTonnage(string $quotaNumber, string $shippingCompany = '', string $warehouse = '', string $cargoType = ''): array {
        $quotaNumber = InputValidator::sanitize($quotaNumber);
        $shippingCompany = InputValidator::sanitize($shippingCompany);
        $warehouse = InputValidator::sanitize($warehouse);
        $cargoType = InputValidator::sanitize($cargoType);

        $whereConditions = ["i.loadingQuotaNumber = ?"];
        $params = [$quotaNumber];
        $types = "s";

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

        $query = "SELECT i.loadingQuotaNumber as number, i.cargoWeight as totalTonnage, i.percentage, i.is_enabled, COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedTonnage
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.loadingQuotaNumber = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) exit_data ON
            exit_data.loadingQuotaNumber = i.loadingQuotaNumber AND exit_data.shipName = i.shipName
            AND exit_data.loadingWarehouse = i.loadingWarehouse AND exit_data.shippingCompany = i.shippingCompany
            AND exit_data.cargoType = i.cargoType
        WHERE " . implode(" AND ", $whereConditions) . " LIMIT 1";

        $derivedParams = array_merge([$quotaNumber], $params);
        $derivedTypes = "s" . $types;

        $stmt = $this->db->prepare($query);
        $stmt->bind_param($derivedTypes, ...$derivedParams);
        $stmt->execute();
        $result = $stmt->get_result();

        if ($row = $result->fetch_assoc()) {
            $loadedTonnage = floatval($row['loadedTonnage']);
            $totalTonnage = floatval($row['totalTonnage']);
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? floatval($row['percentage']) : null;
            $isPercentageRestricted = (bool)$row['is_enabled'];

            $loadableTonnage = $this->calculator->calculateLoadableTonnage($remainingTonnage, $totalTonnage, $percentage, $isPercentageRestricted);
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
    }

    public function editQuota(int $id, string $oldQuotaNumber, string $newQuotaNumber, string $shipName, string $shippingCompany, string $warehouse, string $cargoType, float $totalTonnage, ?string $actorUsername = null): bool {
        try {
            $this->db->beginTransaction();

            $selectQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType FROM InitialInfo WHERE id = ?";
            $selectStmt = $this->db->prepare($selectQuery);
            $selectStmt->bind_param("i", $id);
            $selectStmt->execute();
            $result = $selectStmt->get_result();
            $oldData = $result->fetch_assoc();

            if (!$oldData) {
                throw new \Exception("کوتاژ با شناسه مشخص شده یافت نشد");
            }

            $queryInitialInfo = "UPDATE InitialInfo SET loadingQuotaNumber = ?, shipName = ?, shippingCompany = ?, loadingWarehouse = ?, cargoType = ?, cargoWeight = ? WHERE id = ?";
            $stmtInitialInfo = $this->db->prepare($queryInitialInfo);
            $stmtInitialInfo->bind_param("sssssdi", $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage, $id);
            $stmtInitialInfo->execute();

            $queryCargoInfo = "UPDATE CargoInfo SET loadingQuotaNumber = ?, shipName = ?, loadingWarehouse = ?, shippingCompany = ?, cargoType = ?
                WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $stmtCargoInfo = $this->db->prepare($queryCargoInfo);
            $stmtCargoInfo->bind_param("ssssssssss",
                $newQuotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType,
                $oldData['loadingQuotaNumber'], $oldData['shipName'], $oldData['loadingWarehouse'], $oldData['shippingCompany'], $oldData['cargoType']
            );
            $stmtCargoInfo->execute();

            $this->db->commit();
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
            MicroCache::forget(AppApiCacheKeys::shipDetails($oldData['shipName']));
            MicroCache::forget(AppApiCacheKeys::quotasList($oldData['shipName']));
            if ($shipName !== $oldData['shipName']) {
                MicroCache::forget(AppApiCacheKeys::shipDetails($shipName));
                MicroCache::forget(AppApiCacheKeys::quotasList($shipName));
            }
            if ($actorUsername !== null) {
                AuditLogger::log($actorUsername, 'editQuota', 'quota', (string)$id, [
                    'oldQuotaNumber' => $oldData['loadingQuotaNumber'],
                    'newQuotaNumber' => $newQuotaNumber,
                    'shipName' => $shipName,
                    'shippingCompany' => $shippingCompany,
                    'warehouse' => $warehouse,
                    'cargoType' => $cargoType,
                    'totalTonnage' => $totalTonnage,
                ]);
            }
            return true;
        } catch (\Exception $e) {
            $this->db->rollback();
            throw new \Exception("خطا در ویرایش کوتاژ: " . $e->getMessage());
        }
    }

    // هر سه تابع زیر عمداً فقط با id کار می‌کنند، نه loadingQuotaNumber که یکتا نیست
    // نام کشتی مرتبط با یک ردیف InitialInfo، برای invalidate کردن کش per-ship بعد از نوشتن با فقط id
    private function getShipNameById(int $id): ?string {
        $stmt = $this->db->prepare("SELECT shipName FROM InitialInfo WHERE id = ?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $row = $stmt->get_result()->fetch_assoc();
        return $row['shipName'] ?? null;
    }

    private function forgetShipCaches(?string $shipName): void {
        if ($shipName === null) {
            return;
        }
        MicroCache::forget(AppApiCacheKeys::shipDetails($shipName));
        MicroCache::forget(AppApiCacheKeys::quotasList($shipName));
    }

    public function updateQuotaPercentage(int $id, float $percentage, ?string $actorUsername = null): bool {
        $shipName = $this->getShipNameById($id);
        $isEnabled = ($percentage > 0.00) ? 1 : 0;
        $query = "UPDATE InitialInfo SET percentage = ?, is_enabled = ? WHERE id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("dii", $percentage, $isEnabled, $id);
        $success = $stmt->execute();
        if ($success) {
            $this->forgetShipCaches($shipName);
            if ($actorUsername !== null) {
                AuditLogger::log($actorUsername, 'updateQuotaPercentage', 'quota', (string)$id, ['percentage' => $percentage]);
            }
        }
        return $success;
    }

    public function toggleQuotaStatus(int $id, ?string $actorUsername = null): bool {
        // یک SELECT اضافه تا جزئیات معنادار (وضعیت قبل/بعد و شماره کوتاژ) برای audit_log ثبت شود
        $beforeStmt = $this->db->prepare("SELECT shipName, loadingQuotaNumber, isActive FROM InitialInfo WHERE id = ?");
        $beforeStmt->bind_param("i", $id);
        $beforeStmt->execute();
        $before = $beforeStmt->get_result()->fetch_assoc();
        $shipName = $before['shipName'] ?? null;

        $query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("i", $id);
        $success = $stmt->execute();
        if ($success) {
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
            $this->forgetShipCaches($shipName);
            if ($actorUsername !== null && $before !== null) {
                $previousIsActive = (bool)$before['isActive'];
                AuditLogger::log($actorUsername, 'toggleQuotaStatus', 'quota', (string)$id, [
                    'quotaNumber' => $before['loadingQuotaNumber'],
                    'shipName' => $shipName,
                    'previousIsActive' => $previousIsActive,
                    'newIsActive' => !$previousIsActive,
                ]);
            }
        }
        return $success;
    }

    public function updateQuotaPercentageRestriction(int $id, int $isEnabled, ?string $actorUsername = null): bool {
        $shipName = $this->getShipNameById($id);
        $query = "UPDATE InitialInfo SET is_enabled = ? WHERE id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->bind_param("ii", $isEnabled, $id);
        $success = $stmt->execute();
        if ($success) {
            $this->forgetShipCaches($shipName);
            if ($actorUsername !== null) {
                AuditLogger::log($actorUsername, 'updateQuotaPercentageRestriction', 'quota', (string)$id, ['isEnabled' => $isEnabled]);
            }
        }
        return $success;
    }

    public function deleteQuota(string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType, ?string $actorUsername = null): bool {
        try {
            $quotaNumber = InputValidator::validateIdentifier($quotaNumber);
            $shipName = InputValidator::validateIdentifier($shipName);
            $warehouse = InputValidator::validateIdentifier($warehouse);
            $shippingCompany = InputValidator::validateIdentifier($shippingCompany);
            $cargoType = InputValidator::validateIdentifier($cargoType);

            $this->db->beginTransaction();

            $checkQuery = "SELECT COUNT(*) as count FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $checkStmt = $this->db->prepare($checkQuery);
            $checkStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $checkStmt->execute();
            $checkResult = $checkStmt->get_result();
            $row = $checkResult->fetch_assoc();

            if ($row['count'] == 0) {
                $this->db->rollback();
                return false;
            }

            $cargoQuery = "DELETE FROM CargoInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $cargoStmt = $this->db->prepare($cargoQuery);
            $cargoStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $cargoStmt->execute();

            $initialQuery = "DELETE FROM InitialInfo WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
            $initialStmt = $this->db->prepare($initialQuery);
            $initialStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $initialStmt->execute();

            $this->db->commit();
            MicroCache::forget(MicroCache::SHIPS_LIST_KEY);
            $this->forgetShipCaches($shipName);
            if ($actorUsername !== null) {
                AuditLogger::log($actorUsername, 'deleteQuota', 'quota', $quotaNumber, [
                    'shipName' => $shipName,
                    'warehouse' => $warehouse,
                    'shippingCompany' => $shippingCompany,
                    'cargoType' => $cargoType,
                ]);
            }
            return true;
        } catch (\Exception $e) {
            $this->db->rollback();
            throw new \Exception("خطا در حذف کوتاژ: " . $e->getMessage());
        }
    }

    public function getGroupedQuotas(?string $shipName = null): array {
        $quotas = $this->getAllQuotasList($shipName);
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

    public function updateTemporaryTonnage(string $quotaNumber, int $enabledVal, ?float $tonnageVal, ?string $actorUsername = null): array {
        $quotaNumber = InputValidator::validateIdentifier($quotaNumber);

        $checkQuery = "SELECT loadingQuotaNumber FROM InitialInfo WHERE loadingQuotaNumber = ?";
        $checkStmt = $this->db->prepare($checkQuery);
        $checkStmt->bind_param("s", $quotaNumber);
        $checkStmt->execute();
        if ($checkStmt->get_result()->num_rows === 0) {
            throw new \Exception('کوتاژ مورد نظر یافت نشد');
        }

        if ($enabledVal && $tonnageVal !== null) {
            $query = "UPDATE InitialInfo SET temp_tonnage_status = 1, temp_tonnage_amount = ? WHERE loadingQuotaNumber = ?";
            $stmt = $this->db->prepare($query);
            $stmt->bind_param("ds", $tonnageVal, $quotaNumber);
        } else {
            $query = "UPDATE InitialInfo SET temp_tonnage_status = 0, temp_tonnage_amount = NULL WHERE loadingQuotaNumber = ?";
            $stmt = $this->db->prepare($query);
            $stmt->bind_param("s", $quotaNumber);
        }

        if ($stmt->execute()) {
            if ($actorUsername !== null) {
                AuditLogger::log($actorUsername, 'updateTemporaryTonnage', 'quota', $quotaNumber, [
                    'enabled' => $enabledVal,
                    'tonnage' => $tonnageVal,
                ]);
            }
            return ['success' => true, 'message' => 'تناژ موقت با موفقیت به‌روزرسانی شد'];
        }
        throw new \Exception('خطا در اجرای کوئری');
    }
}
