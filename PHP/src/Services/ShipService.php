<?php
// PHP/src/Services/ShipService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\DatabaseManager;
use App\Core\MicroCache;
use App\Exceptions\ApiException;
use App\Validators\InputValidator;
use App\Enums\CargoStatus;
use PDO;

// منطق تجاری «کشتی/انبار» که از AppApiController استخراج شده تا آن کنترلر فقط dispatch/پارس درخواست باشد
final class ShipService {
    // بدون ->value: property-fetch در class const در PHP 8.1 مجاز نیست؛ ->value در محل مصرف اعمال می‌شود
    private const EXITED = CargoStatus::EXITED;

    private DatabaseManager $db;

    public function __construct() {
        $this->db = new DatabaseManager();
    }

    public function getShipsList(): array {
        // نتیجه‌ی این کوئری سنگین کوتاه‌مدت کش می‌شود تا روی هر poll دوباره اجرا نشود؛ نوشتن‌های مرتبط کش را صریحاً invalidate می‌کنند
        $shipsData = MicroCache::remember(MicroCache::SHIPS_LIST_KEY, 20, function () {
            // فیلدهای اضافی مثل shippingCompanyCount/cargoTypeCount که کلاینت اندروید map نمی‌کرد حذف شدند
            $query = "SELECT
                i.shipName, i.cargoType, COUNT(DISTINCT i.loadingWarehouse) as warehouseCount,
                COUNT(DISTINCT CONCAT(i.loadingQuotaNumber, '-', i.loadingWarehouse, '-', i.shippingCompany, '-', i.cargoType)) as quotaCount,
                SUM(i.cargoWeight) as totalTonnage, COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
                (SUM(i.cargoWeight) - COALESCE(SUM(loaded.loadedWeight), 0)) as remainingTonnage,
                MAX(i.isActive) as isActive
            FROM InitialInfo i
            LEFT JOIN (
                SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
                FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "'
                GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
            ) loaded ON
                loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
                AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
                AND loaded.cargoType = i.cargoType
            GROUP BY i.shipName, i.cargoType
            ORDER BY isActive DESC, shipName ASC";

            $stmt = $this->db->prepare($query);
            $stmt->execute();

            $activeShips = [];
            $inactiveShips = [];

            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $ship = [
                    'name' => $row['shipName'],
                    'cargoType' => $row['cargoType'],
                    'warehouseCount' => (int)$row['warehouseCount'],
                    'quotaCount' => (int)$row['quotaCount'],
                    'totalTonnage' => (float)$row['totalTonnage'],
                    'remainingTonnage' => (float)$row['remainingTonnage'],
                    'loadedTonnage' => (float)$row['loadedTonnage'],
                    'isActive' => (bool)$row['isActive']
                ];

                if ($ship['isActive']) {
                    $activeShips[] = $ship;
                } else {
                    $inactiveShips[] = $ship;
                }
            }

            return [
                'activeShips' => $activeShips,
                'inactiveShips' => $inactiveShips,
            ];
        });

        return [
            'data' => [
                'activeShips' => $shipsData['activeShips'],
                'inactiveShips' => $shipsData['inactiveShips'],
            ]
        ];
    }

    public function getShipDetails(string $shipName): array {
        $shipName = InputValidator::validateIdentifier($shipName);

        return MicroCache::remember(AppApiCacheKeys::shipDetails($shipName), 6, function () use ($shipName) {
            return $this->computeShipDetails($shipName);
        });
    }

    private function computeShipDetails(string $shipName): array {
        $query = "SELECT
            i.shipName, i.loadingWarehouse, COUNT(DISTINCT i.loadingQuotaNumber) as quotaCount,
            SUM(i.cargoWeight) as totalTonnage, COALESCE(SUM(loaded.loadedWeight), 0) as loadedTonnage,
            MAX(i.isActive) as isActive
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.shipName = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) loaded ON
            loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
            AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
            AND loaded.cargoType = i.cargoType
        WHERE i.shipName = ?
        GROUP BY i.shipName, i.loadingWarehouse";

        $stmt = $this->db->prepare($query);
        $stmt->execute([$shipName, $shipName]);

        // totalVoucherCount فقط به shipName وابسته است؛ به‌جای زیرکوئری تکراری برای هر انبار، یک بار جدا محاسبه می‌شود
        $voucherStmt = $this->db->prepare(
            "SELECT COUNT(DISTINCT trackingNumber) as totalVoucherCount FROM CargoInfo WHERE shipName = ? AND status = '" . self::EXITED->value . "'"
        );
        $voucherStmt->execute([$shipName]);
        $totalVoucherCount = (int)($voucherStmt->fetch(PDO::FETCH_ASSOC)['totalVoucherCount'] ?? 0);

        $warehouses = [];
        $totalQuotaCount = 0;
        $totalTonnage = 0;
        $totalRemainingTonnage = 0;
        $isActive = false;

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $warehouseTotalTonnage = floatval($row['totalTonnage']);
            $warehouseLoadedTonnage = floatval($row['loadedTonnage']);
            $warehouseRemainingTonnage = $warehouseTotalTonnage - $warehouseLoadedTonnage;

            $warehouses[] = [
                'name' => $row['loadingWarehouse'],
                'quotaCount' => intval($row['quotaCount']),
                'totalTonnage' => $warehouseTotalTonnage,
                'remainingTonnage' => $warehouseRemainingTonnage,
                'loadedTonnage' => $warehouseLoadedTonnage
            ];

            $totalQuotaCount += intval($row['quotaCount']);
            $totalTonnage += $warehouseTotalTonnage;
            $totalRemainingTonnage += $warehouseRemainingTonnage;
            // نتیجه با OR منطقی بین انبارها ترکیب می‌شود تا یک انبار غیرفعال، کشتی با انبارهای فعال دیگر را اشتباهاً غیرفعال نشان ندهد
            $isActive = $isActive || (bool)$row['isActive'];
        }

        if (empty($warehouses)) {
            throw new ApiException("کشتی با نام '$shipName' یافت نشد.", 404, ['code' => 'SHIP_NOT_FOUND']);
        }

        $totalLoadedTonnage = $totalTonnage - $totalRemainingTonnage;

        return [
            'name' => $shipName,
            'warehouseCount' => count($warehouses),
            'quotaCount' => $totalQuotaCount,
            'totalTonnage' => $totalTonnage,
            'remainingTonnage' => $totalRemainingTonnage,
            'loadedTonnage' => $totalLoadedTonnage,
            'totalVoucherCount' => $totalVoucherCount,
            'isActive' => $isActive,
            'warehouses' => $warehouses
        ];
    }

    public function getWarehouseDetails(string $shipName, string $warehouseName): array {
        $shipName = InputValidator::validateIdentifier($shipName);
        $warehouseName = InputValidator::validateIdentifier($warehouseName);

        $query = "SELECT
            i.loadingQuotaNumber, i.cargoType, i.shippingCompany, i.cargoOwner,
            i.cargoWeight as totalTonnage, i.isActive, COALESCE(loaded.loadedWeight, 0) as loadedTonnage,
            COALESCE(vouchers.voucherCount, 0) as voucherCount
        FROM InitialInfo i
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, SUM(c.netWeight) as loadedWeight
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.shipName = ? AND c.loadingWarehouse = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) loaded ON
            loaded.loadingQuotaNumber = i.loadingQuotaNumber AND loaded.shipName = i.shipName
            AND loaded.loadingWarehouse = i.loadingWarehouse AND loaded.shippingCompany = i.shippingCompany
            AND loaded.cargoType = i.cargoType
        LEFT JOIN (
            SELECT c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType, COUNT(DISTINCT c.trackingNumber) as voucherCount
            FROM CargoInfo c WHERE c.status = '" . self::EXITED->value . "' AND c.shipName = ? AND c.loadingWarehouse = ?
            GROUP BY c.loadingQuotaNumber, c.shipName, c.loadingWarehouse, c.shippingCompany, c.cargoType
        ) vouchers ON
            vouchers.loadingQuotaNumber = i.loadingQuotaNumber AND vouchers.shipName = i.shipName
            AND vouchers.loadingWarehouse = i.loadingWarehouse AND vouchers.shippingCompany = i.shippingCompany
            AND vouchers.cargoType = i.cargoType
        WHERE i.shipName = ? AND i.loadingWarehouse = ?";

        $stmt = $this->db->prepare($query);
        $stmt->execute([$shipName, $warehouseName, $shipName, $warehouseName, $shipName, $warehouseName]);

        $exitDatesByQuota = [];
        $exitQuery = "SELECT DISTINCT loadingQuotaNumber, exitDate, exitTime FROM CargoInfo c WHERE c.shipName = ?
            AND c.loadingWarehouse = ? AND c.status = '" . self::EXITED->value . "' ORDER BY loadingQuotaNumber, exitDate, exitTime";
        $exitStmt = $this->db->prepare($exitQuery);
        $exitStmt->execute([$shipName, $warehouseName]);
        while ($exitRow = $exitStmt->fetch(PDO::FETCH_ASSOC)) {
            $exitDatesByQuota[$exitRow['loadingQuotaNumber']][] = [
                'date' => $exitRow['exitDate'],
                'time' => $exitRow['exitTime']
            ];
        }

        $quotas = [];
        $totalTonnage = 0;
        $totalRemainingTonnage = 0;
        $totalLoadedTonnage = 0;
        $totalVoucherCount = 0;
        $allExitDates = [];
        $uniqueCargoTypes = [];
        $uniqueShippingCompanies = [];
        $uniqueCargoOwners = [];
        $seenCargoTypes = [];
        $seenShippingCompanies = [];
        $seenCargoOwners = [];
        $activeQuotasCount = 0;

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $quotaNumber = $row['loadingQuotaNumber'];
            $cargoType = $row['cargoType'];
            $shippingCompany = $row['shippingCompany'];
            $cargoOwner = $row['cargoOwner'] ?? 'نامشخص';
            $isActive = (bool)$row['isActive'];

            if (!isset($seenCargoTypes[$cargoType])) {
                $seenCargoTypes[$cargoType] = true;
                $uniqueCargoTypes[] = $cargoType;
            }
            if (!isset($seenShippingCompanies[$shippingCompany])) {
                $seenShippingCompanies[$shippingCompany] = true;
                $uniqueShippingCompanies[] = $shippingCompany;
            }
            if ($cargoOwner !== 'نامشخص' && !isset($seenCargoOwners[$cargoOwner])) {
                $seenCargoOwners[$cargoOwner] = true;
                $uniqueCargoOwners[] = $cargoOwner;
            }

            if ($isActive) {
                $activeQuotasCount++;
            }

            $quotaTotalTonnage = floatval($row['totalTonnage']);
            $quotaLoadedTonnage = floatval($row['loadedTonnage']);
            $quotaRemainingTonnage = $quotaTotalTonnage - $quotaLoadedTonnage;
            $percentageLoaded = ($quotaTotalTonnage > 0) ? ($quotaLoadedTonnage / $quotaTotalTonnage) * 100 : 0;

            $exitDates = $exitDatesByQuota[$quotaNumber] ?? [];
            foreach ($exitDates as $exitDateEntry) {
                $allExitDates[] = $exitDateEntry['date'];
            }

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

        usort($quotas, function($a, $b) {
            if ($a['isActive'] !== $b['isActive']) {
                return $b['isActive'] <=> $a['isActive'];
            }
            return $b['remainingTonnage'] <=> $a['remainingTonnage'];
        });

        $totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0;
        $allExitDates = array_unique($allExitDates);
        sort($allExitDates);

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
    }

    public function getFilteredSummary(string $shipName, string $warehouseName, string $selectedQuota, string $startDateTime, string $endDateTime): string {
        if (empty($selectedQuota) || $selectedQuota === "null") {
            throw new \Exception("هیچ کوتاژی انتخاب نشده است");
        }

        if (strlen($startDateTime) === 16) {
            $startDateTime .= ':00';
        }
        if (strlen($endDateTime) === 16) {
            $endDateTime .= ':00';
        }

        $startDate = substr($startDateTime, 0, 10);
        $startTime = substr($startDateTime, 11, 8);
        $endDate = substr($endDateTime, 0, 10);
        $endTime = substr($endDateTime, 11, 8);

        $dateRangeCondition = "(c.exitDate > ? OR (c.exitDate = ? AND c.exitTime >= ?))
            AND (c.exitDate < ? OR (c.exitDate = ? AND c.exitTime < ?))";

        $summaryQuery = "SELECT COALESCE(SUM(c.netWeight), 0) as totalNetWeight, COUNT(DISTINCT c.trackingNumber) as voucherCount,
            MIN(c.exitTime) as firstExitTime, MAX(c.exitTime) as lastExitTime, MIN(c.exitDate) as firstExitDate, MAX(c.exitDate) as lastExitDate
        FROM CargoInfo c
        WHERE c.loadingQuotaNumber = ? AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = '" . self::EXITED->value . "'
            AND $dateRangeCondition";

        $summaryParams = [$selectedQuota, $shipName, $warehouseName, $startDate, $startDate, $startTime, $endDate, $endDate, $endTime];
        $stmt = $this->db->prepare($summaryQuery);
        $stmt->execute($summaryParams);
        $summary = $stmt->fetch(PDO::FETCH_ASSOC);

 // JOIN روی کلید کامل پنج‌ستونی؛ کمتر از آن می‌توانست i.cargoOwner اشتباه یا ردیف تکراری بدهد
        $detailsQuery = "SELECT c.trackingNumber, c.entryTime, c.netWeight, c.exitTime, c.exitDate, c.scaleReceiptNumber,
            c.username, c.confirm_username, c.cargoType, c.shippingCompany, i.cargoOwner
        FROM CargoInfo c
        JOIN InitialInfo i ON c.loadingQuotaNumber = i.loadingQuotaNumber
            AND c.shipName = i.shipName
            AND c.loadingWarehouse = i.loadingWarehouse
            AND c.shippingCompany = i.shippingCompany
            AND c.cargoType = i.cargoType
        WHERE c.loadingQuotaNumber = ? AND c.shipName = ? AND c.loadingWarehouse = ? AND c.status = '" . self::EXITED->value . "'
            AND $dateRangeCondition
        ORDER BY c.exitDate, c.exitTime";

        $stmtDetails = $this->db->prepare($detailsQuery);
        $stmtDetails->execute($summaryParams);

        $voucherDetails = [];
        $totalWeights = [];
        $uniqueUsers = [];

        while ($row = $stmtDetails->fetch(PDO::FETCH_ASSOC)) {
            if (!isset($totalWeights[$row['cargoType']])) {
                $totalWeights[$row['cargoType']] = 0;
            }
            $totalWeights[$row['cargoType']] += floatval($row['netWeight']);

            if (!empty($row['username'])) {
                $uniqueUsers[$row['username']] = true;
            }
            if (!empty($row['confirm_username'])) {
                $uniqueUsers[$row['confirm_username']] = true;
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

        return json_encode($response, JSON_UNESCAPED_UNICODE);
    }
}
