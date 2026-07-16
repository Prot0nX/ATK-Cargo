<?php

declare(strict_types=1);

namespace AtkCargo\Repositories;

use AtkCargo\Core\Database;
use mysqli;
use Exception;

/**
 * Repository layer for Cargo and Quota Database Operations using mysqli to maintain raw performance and compatibility
 */
class CargoRepository
{
    private mysqli $conn;

    public function __construct()
    {
        $this->conn = Database::getInstance()->getMysqli();
    }

    /**
     * Start a database transaction
     */
    public function beginTransaction(): void
    {
        $this->conn->begin_transaction();
    }

    /**
     * Commit a database transaction
     */
    public function commit(): void
    {
        $this->conn->commit();
    }

    /**
     * Rollback a database transaction
     */
    public function rollback(): void
    {
        $this->conn->rollback();
    }

    /**
     * Get list of ships and loading stats
     */
    public function getShipsList(): array
    {
        $query = "
        SELECT
            i.shipName,
            i.cargoType,
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
        GROUP BY i.shipName, i.cargoType
        ORDER BY isActive DESC, shipName ASC
        ";
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get details of a ship including quotas and loading statistics
     */
    public function getShipDetails(string $shipName): array
    {
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
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("s", $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get details of loading in a specific warehouse of a ship
     */
    public function getWarehouseDetails(string $shipName, string $warehouseName): array
    {
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
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("ssssss", $shipName, $warehouseName, $shipName, $warehouseName, $shipName, $warehouseName);
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get exit dates for a specific warehouse quota
     */
    public function getWarehouseExitDates($quotaNumber, string $shipName, string $warehouseName): array
    {
        $query = "
        SELECT DISTINCT exitDate, exitTime
        FROM CargoInfo c
        WHERE c.loadingQuotaNumber = ? 
          AND c.shipName = ?
          AND c.loadingWarehouse = ?
          AND c.status = 'خروج'
        ORDER BY exitDate, exitTime
        ";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL تاریخ‌های خروج");
        }
        $quotaStr = (string)$quotaNumber;
        $stmt->bind_param("sss", $quotaStr, $shipName, $warehouseName);
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get details of a quota number
     */
    public function getQuotaDetails(string $quotaNumber): ?array
    {
        $query = "
        SELECT
            i.id,
            i.shipName,
            i.loadingWarehouse,
            i.shippingCompany,
            i.cargoType,
            i.cargoWeight as totalTonnage,
            COALESCE(loaded.loadedWeight, 0) as loadedTonnage,
            (i.cargoWeight - COALESCE(loaded.loadedWeight, 0)) as remainingTonnage,
            CASE 
                WHEN i.cargoWeight > 0 THEN 
                    ROUND((COALESCE(loaded.loadedWeight, 0) / i.cargoWeight) * 100, 2)
                ELSE 0.00 
            END as percentageLoaded,
            i.isActive,
            i.percentage,
            i.is_enabled as isPercentageRestricted,
            i.temp_tonnage_status,
            i.temp_tonnage_amount
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
        WHERE i.loadingQuotaNumber = ?
        LIMIT 1
        ";
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("s", $quotaNumber);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        $stmt->close();
        return $row ?: null;
    }

    /**
     * Get all quotas list in system
     */
    public function getAllQuotasList(): array
    {
        $query = "
        SELECT 
            i.id,
            i.loadingQuotaNumber as quotaNumber, 
            i.shipName, 
            i.loadingWarehouse as warehouse, 
            i.shippingCompany, 
            i.cargoOwner,
            i.cargoType, 
            i.cargoWeight as totalTonnage,
            COALESCE(l.loadedWeight, 0) as loadedTonnage,
            (i.cargoWeight - COALESCE(l.loadedWeight, 0)) as remainingTonnage,
            i.isActive,
            i.percentage,
            i.is_enabled as isPercentageRestricted,
            i.temp_tonnage_status as tempTonnageStatus,
            i.temp_tonnage_amount as tempTonnageAmount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT 
                loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType,
                SUM(netWeight) as loadedWeight
            FROM CargoInfo
            WHERE status = 'خروج'
            GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType
        ) l ON 
            l.loadingQuotaNumber = i.loadingQuotaNumber 
            AND l.shipName = i.shipName 
            AND l.loadingWarehouse = i.loadingWarehouse
            AND l.shippingCompany = i.shippingCompany
            AND l.cargoType = i.cargoType
        ORDER BY i.shipName ASC, i.isActive DESC, i.loadingQuotaNumber ASC
        ";
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get quotas list by ship name
     */
    public function getQuotasList(string $shipName): array
    {
        $query = "
        SELECT 
            i.id,
            i.loadingQuotaNumber as number, 
            i.shipName, 
            i.loadingWarehouse as warehouse, 
            i.shippingCompany, 
            i.cargoOwner,
            i.cargoType, 
            i.cargoWeight as totalTonnage,
            i.isActive,
            i.percentage,
            i.is_enabled as isPercentageRestricted,
            i.temp_tonnage_status as tempTonnageStatus,
            i.temp_tonnage_amount as tempTonnageAmount,
            COALESCE(exit_data.loadedTonnage, 0) as loadedTonnage,
            COALESCE(all_vouchers.voucherCount, 0) as voucherCount,
            COALESCE(exit_data.exitVoucherCount, 0) as exitVoucherCount,
            COALESCE(exit_data.lastExitDate, '') as lastExitDate
        FROM InitialInfo i
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
            FROM CargoInfo c
            WHERE c.status = 'خروج' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
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
            FROM CargoInfo c
            WHERE c.status = 'خروج' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) all_vouchers ON 
            all_vouchers.loadingQuotaNumber = i.loadingQuotaNumber
            AND all_vouchers.shipName = i.shipName
            AND all_vouchers.loadingWarehouse = i.loadingWarehouse
            AND all_vouchers.shippingCompany = i.shippingCompany
            AND all_vouchers.cargoType = i.cargoType
        WHERE i.shipName = ?
        ORDER BY i.isActive DESC, i.loadingQuotaNumber ASC
        ";
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("sss", $shipName, $shipName, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get filtered quotas by ship and date range
     */
    public function getFilteredQuotas(string $shipName, string $startDateTime, string $endDateTime): array
    {
        $query = "
        SELECT 
            c.id,
            c.trackingNumber, 
            c.loadingQuotaNumber, 
            c.loadingWarehouse, 
            c.shippingCompany, 
            c.cargoType, 
            c.driverName, 
            c.truckLicensePlate, 
            c.netWeight, 
            c.scaleReceiptNumber, 
            c.entryTime, 
            c.exitTime, 
            c.exitDate, 
            c.status, 
            c.confirm,
            c.confirm_username,
            c.confirm_usertype,
            c.numberOfPeople,
            c.duplicateConfirmation,
            c.created_at,
            c.updated_at
        FROM CargoInfo c
        WHERE c.shipName = ? 
          AND c.created_at BETWEEN ? AND ?
        ORDER BY c.id DESC
        ";
        
        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("sss", $shipName, $startDateTime, $endDateTime);
        $stmt->execute();
        $result = $stmt->get_result();
        $rows = [];
        while ($row = $result->fetch_assoc()) {
            $rows[] = $row;
        }
        $stmt->close();
        return $rows;
    }

    /**
     * Get summary statistics for filtered parameters
     */
    public function getFilteredSummary(string $shipName, string $warehouseName, string $selectedQuota, string $startDateTime, string $endDateTime): array
    {
        if (strlen($startDateTime) === 16) {
            $startDateTime .= ':00';
        }
        if (strlen($endDateTime) === 16) {
            $endDateTime .= ':00';
        }

        // 1. Fetch Summary Statistics
        $summaryQuery = "
        SELECT 
            COALESCE(SUM(c.netWeight), 0) as totalNetWeight,
            COUNT(DISTINCT c.trackingNumber) as voucherCount,
            MIN(c.exitTime) as firstExitTime,
            MAX(c.exitTime) as lastExitTime,
            MIN(c.exitDate) as firstExitDate,
            MAX(c.exitDate) as lastExitDate
        FROM CargoInfo c
        WHERE c.loadingQuotaNumber = ?
          AND c.shipName = ?
          AND c.loadingWarehouse = ?
          AND c.status = 'خروج'
          AND CONCAT(c.exitDate, ' ', c.exitTime) >= ?
          AND CONCAT(c.exitDate, ' ', c.exitTime) < ?
        ";

        $stmt = $this->conn->prepare($summaryQuery);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL خلاصه");
        }
        $stmt->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
        $stmt->execute();
        $summary = $stmt->get_result()->fetch_assoc() ?: [];
        $stmt->close();

        // 2. Fetch Detailed Vouchers
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
        FROM CargoInfo c
        JOIN InitialInfo i ON 
            c.loadingQuotaNumber = i.loadingQuotaNumber
            AND c.shipName = i.shipName
        WHERE c.loadingQuotaNumber = ?
          AND c.shipName = ?
          AND c.loadingWarehouse = ?
          AND c.status = 'خروج'
          AND CONCAT(c.exitDate, ' ', c.exitTime) >= ?
          AND CONCAT(c.exitDate, ' ', c.exitTime) < ?
        ORDER BY c.exitDate ASC, c.exitTime ASC
        ";

        $stmtDetails = $this->conn->prepare($detailsQuery);
        if (!$stmtDetails) {
            throw new Exception("خطا در آماده‌سازی دستور SQL جزئیات حواله‌ها");
        }
        $stmtDetails->bind_param("sssss", $selectedQuota, $shipName, $warehouseName, $startDateTime, $endDateTime);
        $stmtDetails->execute();
        $detailsResult = $stmtDetails->get_result();
        
        $voucherDetails = [];
        while ($row = $detailsResult->fetch_assoc()) {
            $voucherDetails[] = $row;
        }
        $stmtDetails->close();

        return [
            'summary' => $summary,
            'details' => $voucherDetails
        ];
    }

    /**
     * Check if a quota exists and returns status
     */
    public function checkQuotaExistenceCargo(string $quotaNumber, string $shipName): array
    {
        $query = "
        SELECT 
            i.id,
            i.isActive, 
            i.cargoWeight as totalTonnage, 
            i.cargoType, 
            i.percentage, 
            i.is_enabled as isPercentageRestricted,
            i.temp_tonnage_status,
            i.temp_tonnage_amount,
            COALESCE(loaded.loadedWeight, 0) as loadedTonnage
        FROM InitialInfo i
        LEFT JOIN (
            SELECT 
                c.loadingQuotaNumber, 
                c.shipName, 
                SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c
            WHERE c.status = 'خروج'
            GROUP BY c.loadingQuotaNumber, c.shipName
        ) loaded ON loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
        WHERE i.loadingQuotaNumber = ? AND i.shipName = ?
        LIMIT 1
        ";

        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("ss", $quotaNumber, $shipName);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        $stmt->close();
        return $row ?: [];
    }

    /**
     * Get specific quota info for calculating loadable tonnage
     */
    public function getLoadableTonnageInfo(string $quotaNumber, string $shippingCompany, string $warehouse, string $cargoType): ?array
    {
        $query = "
        SELECT 
            i.cargoWeight as totalTonnage,
            i.percentage,
            i.is_enabled as isPercentageRestricted,
            i.temp_tonnage_status,
            i.temp_tonnage_amount,
            COALESCE(l.loadedWeight, 0) as loadedTonnage
        FROM InitialInfo i
        LEFT JOIN (
            SELECT 
                loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType,
                SUM(netWeight) as loadedWeight
            FROM CargoInfo
            WHERE status = 'خروج'
            GROUP BY loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType
        ) l ON 
            l.loadingQuotaNumber = i.loadingQuotaNumber 
            AND l.loadingWarehouse = i.loadingWarehouse
            AND l.shippingCompany = i.shippingCompany
            AND l.cargoType = i.cargoType
        WHERE i.loadingQuotaNumber = ? 
          AND i.shippingCompany = ? 
          AND i.loadingWarehouse = ? 
          AND i.cargoType = ?
        LIMIT 1
        ";

        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی دستور SQL");
        }
        $stmt->bind_param("ssss", $quotaNumber, $shippingCompany, $warehouse, $cargoType);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        $stmt->close();
        return $row ?: null;
    }

    /**
     * Update temporary tonnage Status and Amount
     */
    public function updateTemporaryTonnage(string $quotaNumber, int $enabled, ?float $tonnage): bool
    {
        if ($enabled === 1 && $tonnage !== null) {
            $query = "UPDATE InitialInfo SET temp_tonnage_status = 1, temp_tonnage_amount = ? WHERE loadingQuotaNumber = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("ds", $tonnage, $quotaNumber);
        } else {
            $query = "UPDATE InitialInfo SET temp_tonnage_status = 0, temp_tonnage_amount = 0.00 WHERE loadingQuotaNumber = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("s", $quotaNumber);
        }
        $result = $stmt->execute();
        $stmt->close();
        return $result;
    }

    /**
     * Edit Quota configuration
     */
    public function editQuota(
        int $id,
        string $oldQuotaNumber,
        string $newQuotaNumber,
        string $shipName,
        string $shippingCompany,
        string $warehouse,
        string $cargoType,
        float $totalTonnage
    ): bool {
        // Fetch old data
        $selectQuery = "SELECT loadingQuotaNumber, shipName, loadingWarehouse, shippingCompany, cargoType FROM InitialInfo WHERE id = ?";
        $selectStmt = $this->conn->prepare($selectQuery);
        $selectStmt->bind_param("i", $id);
        $selectStmt->execute();
        $oldData = $selectStmt->get_result()->fetch_assoc();
        $selectStmt->close();

        if (!$oldData) {
            throw new Exception("کوتاژ با شناسه مشخص شده یافت نشد");
        }

        // Update InitialInfo
        $queryInitialInfo = "UPDATE InitialInfo SET loadingQuotaNumber = ?, shipName = ?, shippingCompany = ?, loadingWarehouse = ?, cargoType = ?, cargoWeight = ? WHERE id = ?";
        $stmtInitialInfo = $this->conn->prepare($queryInitialInfo);
        $stmtInitialInfo->bind_param("sssssdi", $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage, $id);
        $stmtInitialInfo->execute();
        $stmtInitialInfo->close();

        // Update CargoInfo
        $queryCargoInfo = "UPDATE CargoInfo SET 
            loadingQuotaNumber = ?, 
            shipName = ?, 
            loadingWarehouse = ?,
            shippingCompany = ?,
            cargoType = ?
            WHERE loadingQuotaNumber = ? 
            AND shipName = ? 
            AND loadingWarehouse = ? 
            AND shippingCompany = ? 
            AND cargoType = ?";
        $stmtCargoInfo = $this->conn->prepare($queryCargoInfo);
        $stmtCargoInfo->bind_param("ssssssssss", 
            $newQuotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType,
            $oldData['loadingQuotaNumber'], $oldData['shipName'], $oldData['loadingWarehouse'], 
            $oldData['shippingCompany'], $oldData['cargoType']
        );
        $stmtCargoInfo->execute();
        $stmtCargoInfo->close();

        return true;
    }

    /**
     * Update quota percentage
     */
    public function updateQuotaPercentage(string $quotaNumber, float $percentage): bool
    {
        $isEnabled = ($percentage > 0.00) ? 1 : 0;
        $query = "UPDATE InitialInfo SET percentage = ?, is_enabled = ? WHERE loadingQuotaNumber = ?";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("dis", $percentage, $isEnabled, $quotaNumber);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    /**
     * Toggle Quota Active/Inactive state
     */
    public function toggleQuotaStatus(string $quotaNumber, int $id = 0): bool
    {
        if ($id > 0) {
            $query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE id = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("i", $id);
        } else {
            $query = "UPDATE InitialInfo SET isActive = NOT isActive WHERE loadingQuotaNumber = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bind_param("s", $quotaNumber);
        }
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    /**
     * Update percentage restriction status
     */
    public function updateQuotaPercentageRestriction(string $quotaNumber, int $isEnabled): bool
    {
        $query = "UPDATE InitialInfo SET is_enabled = ? WHERE loadingQuotaNumber = ?";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("is", $isEnabled, $quotaNumber);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    /**
     * Delete Quota from database and associated cargoes
     */
    public function deleteQuota(string $quotaNumber, string $shipName, string $warehouse, string $shippingCompany, string $cargoType): bool
    {
        // 1. Check existence
        $checkQuery = "SELECT COUNT(*) as count FROM InitialInfo 
                       WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
        $checkStmt = $this->conn->prepare($checkQuery);
        $checkStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
        $checkStmt->execute();
        $row = $checkStmt->get_result()->fetch_assoc();
        $checkStmt->close();

        if (($row['count'] ?? 0) == 0) {
            return false;
        }

        // 2. Delete from CargoInfo
        $cargoQuery = "DELETE FROM CargoInfo 
                       WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ?";
        $cargoStmt = $this->conn->prepare($cargoQuery);
        $cargoStmt->bind_param("ssss", $quotaNumber, $shipName, $warehouse, $shippingCompany);
        $cargoStmt->execute();
        $cargoStmt->close();

        // 3. Delete from InitialInfo
        $initialQuery = "DELETE FROM InitialInfo 
                         WHERE loadingQuotaNumber = ? AND shipName = ? AND loadingWarehouse = ? AND shippingCompany = ? AND cargoType = ?";
        $initialStmt = $this->conn->prepare($initialQuery);
        $initialStmt->bind_param("sssss", $quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
        $initialStmt->execute();
        $initialStmt->close();

        return true;
    }

    /**
     * Fetch Cargo details by ID
     */
    public function getCargoById(int $id): ?array
    {
        $stmt = $this->conn->prepare("
            SELECT netWeight, status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, trackingNumber, confirm 
            FROM CargoInfo 
            WHERE id = ? LIMIT 1
        ");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $row = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $row ?: null;
    }

    /**
     * Delete Cargo by ID (from deleteCargoInfo.php)
     */
    public function deleteCargoInfo(int $cargoId): bool
    {
        $stmt = $this->conn->prepare("DELETE FROM CargoInfo WHERE id = ?");
        $stmt->bind_param("i", $cargoId);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    /**
     * Confirm Cargo Info (from confirm_cargo.php)
     */
    public function confirmCargoInfo(int $cargoId, string $username, string $userType): bool
    {
        $query = "
            UPDATE CargoInfo 
            SET confirm = 'تائید شده', 
                confirm_username = ?, 
                confirm_usertype = ?, 
                updated_at = NOW() 
            WHERE id = ?
              AND (confirm IS NULL OR confirm != 'تائید شده')
        ";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("ssi", $username, $userType, $cargoId);
        $res = $stmt->execute();
        $affected = $stmt->affected_rows;
        $stmt->close();
        return $res && ($affected > 0);
    }

    /**
     * Fetch Quota temp tonnage Status and Amount by specific fields
     */
    public function getTempTonnageInfo(string $shipName, string $warehouse, string $cargoType, string $shippingCompany, string $quotaNumber): ?array
    {
        $query = "
            SELECT temp_tonnage_status, temp_tonnage_amount 
            FROM InitialInfo 
            WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ? AND loadingQuotaNumber = ? 
            LIMIT 1
        ";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("sssss", $shipName, $warehouse, $cargoType, $shippingCompany, $quotaNumber);
        $stmt->execute();
        $row = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $row ?: null;
    }

    /**
     * Update Temp Tonnage Amount
     */
    public function updateTempTonnageAmount(string $shipName, string $warehouse, string $cargoType, string $shippingCompany, string $quotaNumber, float $newAmount): bool
    {
        $query = "
            UPDATE InitialInfo 
            SET temp_tonnage_amount = ? 
            WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND shippingCompany = ? AND loadingQuotaNumber = ?
        ";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("dsssss", $newAmount, $shipName, $warehouse, $cargoType, $shippingCompany, $quotaNumber);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    /**
     * Check if a duplicate cargo exists in past 24 hours
     */
    public function checkDuplicateCargo24h(string $shipName, string $trackingNumber, string $yesterdayStart): ?array
    {
        $query = "
            SELECT id, loadingQuotaNumber, loadingWarehouse, shippingCompany, exitTime, exitDate, status, entryTime, confirm
            FROM CargoInfo 
            WHERE shipName = ? AND trackingNumber = ? AND updated_at >= ? 
            ORDER BY id DESC LIMIT 1
        ";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("sss", $shipName, $trackingNumber, $yesterdayStart);
        $stmt->execute();
        $row = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $row ?: null;
    }

    /**
     * Insert new Cargo record (from saveOrUpdateCargoInfo.php)
     */
    public function insertCargo(array $params): int
    {
        $query = "
            INSERT INTO CargoInfo 
            (shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, trackingNumber, username, userType, 
             entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status, confirmation, numberOfPeople, duplicateConfirmation, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        ";

        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی درج محموله: " . $this->conn->error);
        }

        $stmt->bind_param("sssssssssdsddssssis",
            $params['shipName'],
            $params['loadingWarehouse'],
            $params['cargoType'],
            $params['shippingCompany'],
            $params['loadingQuotaNumber'],
            $params['trackingNumber'],
            $params['username'],
            $params['userType'],
            $params['entryTime'],
            $params['netWeight'],
            $params['scaleReceiptNumber'],
            $params['shortageWeight'],
            $params['excessWeight'],
            $params['exitTime'],
            $params['exitDate'],
            $params['status'],
            $params['confirmation'],
            $params['numberOfPeople'],
            $params['duplicateConfirmation']
        );

        $stmt->execute();
        $insertId = $stmt->insert_id;
        $stmt->close();
        return $insertId;
    }

    /**
     * Update existing Cargo record
     */
    public function updateCargo(int $id, array $params): bool
    {
        $query = "
            UPDATE CargoInfo 
            SET entryTime = ?, 
                netWeight = ?, 
                scaleReceiptNumber = ?, 
                shortageWeight = ?, 
                excessWeight = ?, 
                exitTime = ?, 
                exitDate = ?, 
                status = ?, 
                confirmation = ?, 
                numberOfPeople = ?, 
                duplicateConfirmation = ?, 
                updated_at = NOW() 
            WHERE id = ?
        ";

        $stmt = $this->conn->prepare($query);
        if (!$stmt) {
            throw new Exception("خطا در آماده‌سازی بروزرسانی محموله: " . $this->conn->error);
        }

        $stmt->bind_param("sdsddssssisi",
            $params['entryTime'],
            $params['netWeight'],
            $params['scaleReceiptNumber'],
            $params['shortageWeight'],
            $params['excessWeight'],
            $params['exitTime'],
            $params['exitDate'],
            $params['status'],
            $params['confirmation'],
            $params['numberOfPeople'],
            $params['duplicateConfirmation'],
            $id
        );

        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }
}
