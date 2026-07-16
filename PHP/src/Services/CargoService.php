<?php

declare(strict_types=1);

namespace AtkCargo\Services;

use AtkCargo\Repositories\CargoRepository;
use Exception;

/**
 * Business Logic Service Layer for Cargo Operations
 */
class CargoService
{
    private CargoRepository $cargoRepository;

    public function __construct()
    {
        $this->cargoRepository = new CargoRepository();
    }

    /**
     * Get active ships with stats
     */
    public function getShipsList(): array
    {
        $rawShips = $this->cargoRepository->getShipsList();
        
        $activeShips = [];
        $inactiveShips = [];
        $totalActiveTonnage = 0.0;
        $totalRemainingTonnage = 0.0;
        $totalLoadedTonnage = 0.0;
        
        foreach ($rawShips as $row) {
            $ship = [
                'name' => $row['shipName'],
                'cargoType' => $row['cargoType'],
                'warehouseCount' => (int)$row['warehouseCount'],
                'quotaCount' => (int)$row['quotaCount'],
                'shippingCompanyCount' => (int)$row['shippingCompanyCount'],
                'cargoTypeCount' => (int)$row['cargoTypeCount'],
                'totalTonnage' => (float)$row['totalTonnage'],
                'remainingTonnage' => (float)$row['remainingTonnage'],
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
    }

    /**
     * Get ship details
     */
    public function getShipDetails(string $shipName): array
    {
        if (empty($shipName)) {
            throw new Exception("نام کشتی مشخص نشده است", 400);
        }

        $details = $this->cargoRepository->getShipDetails($shipName);
        
        $warehouses = [];
        $totalQuotaCount = 0;
        $totalTonnage = 0.0;
        $totalRemainingTonnage = 0.0;
        $totalVoucherCount = 0;
        $isActive = false;

        foreach ($details as $row) {
            $warehouseTotalTonnage = (float)$row['totalTonnage'];
            $warehouseLoadedTonnage = (float)$row['loadedTonnage'];
            $warehouseRemainingTonnage = $warehouseTotalTonnage - $warehouseLoadedTonnage;
            $warehousePercentageLoaded = ($warehouseTotalTonnage > 0) ? ($warehouseLoadedTonnage / $warehouseTotalTonnage) * 100 : 0.0;

            $warehouses[] = [
                'name' => $row['loadingWarehouse'],
                'quotaCount' => (int)$row['quotaCount'],
                'uniqueQuotaCombinations' => (int)$row['uniqueQuotaCombinations'],
                'shippingCompanyCount' => (int)$row['shippingCompanyCount'],
                'cargoTypeCount' => (int)$row['cargoTypeCount'],
                'totalTonnage' => $warehouseTotalTonnage,
                'remainingTonnage' => $warehouseRemainingTonnage,
                'loadedTonnage' => $warehouseLoadedTonnage,
                'percentageLoaded' => number_format($warehousePercentageLoaded, 2, '.', '')
            ];

            $totalQuotaCount += (int)$row['quotaCount'];
            $totalTonnage += $warehouseTotalTonnage;
            $totalRemainingTonnage += $warehouseRemainingTonnage;
            $totalVoucherCount = (int)$row['totalVoucherCount'];
            $isActive = (bool)$row['isActive'];
        }

        if (empty($warehouses)) {
            throw new Exception("کشتی با نام '$shipName' یافت نشد.", 404);
        }

        $totalLoadedTonnage = $totalTonnage - $totalRemainingTonnage;
        $totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0.0;

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
    }

    /**
     * Get warehouse details
     */
    public function getWarehouseDetails(string $shipName, string $warehouseName): array
    {
        if (empty($shipName) || empty($warehouseName)) {
            throw new Exception("نام کشتی یا انبار مشخص نشده است", 400);
        }

        $details = $this->cargoRepository->getWarehouseDetails($shipName, $warehouseName);
        
        $quotas = [];
        $totalTonnage = 0.0;
        $totalRemainingTonnage = 0.0;
        $totalLoadedTonnage = 0.0;
        $totalVoucherCount = 0;
        $allExitDates = [];
        $uniqueCargoTypes = [];
        $uniqueShippingCompanies = [];
        $uniqueCargoOwners = [];
        $activeQuotasCount = 0;

        foreach ($details as $row) {
            $quotaNumber = $row['loadingQuotaNumber'];
            $cargoType = $row['cargoType'];
            $shippingCompany = $row['shippingCompany'];
            $cargoOwner = $row['cargoOwner'] ?? 'نامشخص';
            $isActive = (bool)$row['isActive'];

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

            $quotaTotalTonnage = (float)$row['totalTonnage'];
            $quotaLoadedTonnage = (float)$row['loadedTonnage'];
            $quotaRemainingTonnage = $quotaTotalTonnage - $quotaLoadedTonnage;
            $percentageLoaded = ($quotaTotalTonnage > 0) ? ($quotaLoadedTonnage / $quotaTotalTonnage) * 100 : 0.0;

            // Fetch exit dates for this quota
            $rawExitDates = $this->cargoRepository->getWarehouseExitDates((string)$quotaNumber, $shipName, $warehouseName);
            $exitDates = [];
            foreach ($rawExitDates as $exitRow) {
                $exitDates[] = [
                    'date' => $exitRow['exitDate'],
                    'time' => $exitRow['exitTime']
                ];
                $allExitDates[] = $exitRow['exitDate'];
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
                'voucherCount' => (int)$row['voucherCount'],
                'exitDates' => $exitDates
            ];

            $totalTonnage += $quotaTotalTonnage;
            $totalRemainingTonnage += $quotaRemainingTonnage;
            $totalLoadedTonnage += $quotaLoadedTonnage;
            $totalVoucherCount += (int)$row['voucherCount'];
        }

        // Sort quotas by active and then remaining tonnage descending
        usort($quotas, function($a, $b) {
            if ($a['isActive'] !== $b['isActive']) {
                return $b['isActive'] <=> $a['isActive'];
            }
            return $b['remainingTonnage'] <=> $a['remainingTonnage'];
        });

        $totalPercentageLoaded = ($totalTonnage > 0) ? ($totalLoadedTonnage / $totalTonnage) * 100 : 0.0;

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
            'availableExitDates' => array_values($allExitDates),
            'cargoTypes' => $uniqueCargoTypes,
            'shippingCompanies' => $uniqueShippingCompanies,
            'lastUpdated' => date('Y-m-d H:i:s')
        ];
    }

    /**
     * Get details of a single quota
     */
    public function getQuotaDetails(string $quotaNumber): ?array
    {
        if (empty($quotaNumber)) {
            throw new Exception("شماره کوتاژ مشخص نشده است", 400);
        }

        $row = $this->cargoRepository->getQuotaDetails($quotaNumber);
        if (!$row) {
            return null;
        }

        return [
            'id' => (int)$row['id'],
            'shipName' => $row['shipName'],
            'loadingWarehouse' => $row['loadingWarehouse'],
            'shippingCompany' => $row['shippingCompany'],
            'cargoType' => $row['cargoType'],
            'totalTonnage' => (float)$row['totalTonnage'],
            'loadedTonnage' => (float)$row['loadedTonnage'],
            'remainingTonnage' => (float)$row['remainingTonnage'],
            'percentageLoaded' => (float)$row['percentageLoaded'],
            'isActive' => (bool)$row['isActive'],
            'percentage' => $row['percentage'] !== null ? (float)$row['percentage'] : null,
            'isPercentageRestricted' => (bool)$row['isPercentageRestricted'],
            'tempTonnageStatus' => (int)$row['temp_tonnage_status'],
            'tempTonnageAmount' => (float)$row['temp_tonnage_amount']
        ];
    }

    /**
     * Get all quotas list in system (fully casted)
     */
    public function getAllQuotasList(): array
    {
        $rawList = $this->cargoRepository->getAllQuotasList();
        $formatted = [];
        foreach ($rawList as $row) {
            $formatted[] = [
                'id' => (int)$row['id'],
                'quotaNumber' => (int)$row['quotaNumber'],
                'shipName' => $row['shipName'],
                'warehouse' => $row['warehouse'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'totalTonnage' => (float)$row['totalTonnage'],
                'loadedTonnage' => (float)$row['loadedTonnage'],
                'remainingTonnage' => (float)$row['remainingTonnage'],
                'isActive' => (bool)$row['isActive'],
                'percentage' => $row['percentage'] !== null ? (float)$row['percentage'] : null,
                'isPercentageRestricted' => (bool)$row['isPercentageRestricted'],
                'tempTonnageStatus' => (int)$row['tempTonnageStatus'],
                'tempTonnageAmount' => (float)$row['tempTonnageAmount']
            ];
        }
        return $formatted;
    }

    /**
     * Get quotas list by ship name
     */
    public function getQuotasList(string $shipName): array
    {
        if (empty($shipName)) {
            throw new Exception("نام کشتی مشخص نشده است", 400);
        }

        $rawList = $this->cargoRepository->getQuotasList($shipName);
        $formatted = [];
        foreach ($rawList as $row) {
            $loadedTonnage = (float)$row['loadedTonnage'];
            $totalTonnage = (float)$row['totalTonnage'];
            $remainingTonnage = $totalTonnage - $loadedTonnage;
            $percentage = $row['percentage'] !== null ? (float)$row['percentage'] : null;
            $isPercentageRestricted = (bool)$row['isPercentageRestricted'];
            $percentageLoaded = ($totalTonnage > 0) ? ($loadedTonnage / $totalTonnage) * 100 : 0.0;
            
            // Calculate loadable tonnage
            $isTempTonnageActive = ((int)$row['tempTonnageStatus']) === 1;
            $tempTonnageAmount = (float)$row['tempTonnageAmount'];
            
            if ($isTempTonnageActive) {
                $loadableTonnage = $tempTonnageAmount;
            } else {
                if ($isPercentageRestricted && $percentage !== null && $percentage > 0) {
                    $allowedPercentageTonnage = $totalTonnage * ($percentage / 100);
                    $loadableTonnage = max(0.00, $allowedPercentageTonnage - $loadedTonnage);
                } else {
                    $loadableTonnage = $remainingTonnage;
                }
            }

            $exitVoucherCount = (int)$row['exitVoucherCount'];
            $avgVoucherWeight = ($exitVoucherCount > 0) ? ($loadedTonnage / $exitVoucherCount) : 0.0;

            $formatted[] = [
                'id' => (int)$row['id'],
                'number' => $row['number'],
                'shipName' => $row['shipName'] ?? '',
                'warehouse' => $row['warehouse'] ?? '',
                'cargoType' => $row['cargoType'] ?? '',
                'totalTonnage' => $totalTonnage,
                'remainingTonnage' => $remainingTonnage,
                'loadedTonnage' => $loadedTonnage,
                'percentageLoaded' => round($percentageLoaded, 2),
                'voucherCount' => (int)$row['voucherCount'],
                'entryVoucherCount' => 0,
                'exitVoucherCount' => $exitVoucherCount,
                'pendingVoucherCount' => 0,
                'isActive' => (bool)$row['isActive'],
                'shippingCompany' => $row['shippingCompany'] ?? '',
                'cargoOwner' => $row['cargoOwner'] ?? '',
                'percentage' => $percentage,
                'isPercentageRestricted' => $isPercentageRestricted,
                'loadableTonnage' => $loadableTonnage,
                'avgVoucherWeight' => round($avgVoucherWeight, 2),
                'lastExitDate' => $row['lastExitDate'],
                'quotaKey' => $row['number'] . '|' . ($row['shipName'] ?? '') . '|' . ($row['warehouse'] ?? '') . '|' . ($row['shippingCompany'] ?? '') . '|' . ($row['cargoType'] ?? '')
            ];
        }
        return $formatted;
    }

    /**
     * Get filtered quotas list
     */
    public function getFilteredQuotas(string $shipName, string $startDateTime, string $endDateTime): array
    {
        if (empty($shipName) || empty($startDateTime) || empty($endDateTime)) {
            throw new Exception("پارامترهای ورودی ناقص هستند", 400);
        }

        $rawList = $this->cargoRepository->getFilteredQuotas($shipName, $startDateTime, $endDateTime);
        $formatted = [];
        foreach ($rawList as $row) {
            $formatted[] = [
                'id' => (int)$row['id'],
                'trackingNumber' => $row['trackingNumber'],
                'loadingQuotaNumber' => $row['loadingQuotaNumber'],
                'loadingWarehouse' => $row['loadingWarehouse'],
                'shippingCompany' => $row['shippingCompany'],
                'cargoType' => $row['cargoType'],
                'driverName' => $row['driverName'],
                'truckLicensePlate' => $row['truckLicensePlate'],
                'netWeight' => (float)$row['netWeight'],
                'scaleReceiptNumber' => $row['scaleReceiptNumber'],
                'entryTime' => $row['entryTime'],
                'exitTime' => $row['exitTime'],
                'exitDate' => $row['exitDate'],
                'status' => $row['status'],
                'confirm' => $row['confirm'],
                'confirm_username' => $row['confirm_username'],
                'confirm_usertype' => $row['confirm_usertype'],
                'numberOfPeople' => (int)$row['numberOfPeople'],
                'duplicateConfirmation' => $row['duplicateConfirmation']
            ];
        }
        return $formatted;
    }

    /**
     * Get filtered summary statistics as formatted HTML string (legacy compatibility)
     */
    public function getFilteredSummaryHtml(string $shipName, string $warehouseName, string $selectedQuota, string $startDateTime, string $endDateTime): string
    {
        if (empty($selectedQuota) || $selectedQuota === "null") {
            throw new Exception("هیچ کوتاژی انتخاب نشده است", 400);
        }

        $data = $this->cargoRepository->getFilteredSummary($shipName, $warehouseName, $selectedQuota, $startDateTime, $endDateTime);
        $summary = $data['summary'];
        $details = $data['details'];

        $voucherDetails = [];
        $totalWeights = [];
        $uniqueUsers = [];

        foreach ($details as $row) {
            $cargoType = $row['cargoType'];
            if (!isset($totalWeights[$cargoType])) {
                $totalWeights[$cargoType] = 0.0;
            }
            $totalWeights[$cargoType] += (float)$row['netWeight'];

            if (!empty($row['username']) && !in_array($row['username'], $uniqueUsers)) {
                $uniqueUsers[] = $row['username'];
            }
            if (!empty($row['confirm_username']) && !in_array($row['confirm_username'], $uniqueUsers)) {
                $uniqueUsers[] = $row['confirm_username'];
            }

            $voucherDetails[] = [
                'trackingNumber' => $row['trackingNumber'],
                'entryTime' => $row['entryTime'],
                'netWeight' => (float)$row['netWeight'],
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
            'totalNetWeight' => (float)$summary['totalNetWeight'],
            'voucherCount' => (int)$summary['voucherCount'],
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

    /**
     * Check if a quota exists and returns status and messages
     */
    public function checkQuotaStatus(array $params): array
    {
        $quotaNumber = $params['quotaNumber'] ?? '';
        $shipName = $params['shipName'] ?? '';
        $cargoType = $params['cargoType'] ?? '';
        $shippingCompany = $params['shippingCompany'] ?? '';
        $warehouse = $params['warehouse'] ?? '';

        if (empty($quotaNumber)) {
            throw new Exception('شماره کوتاژ مشخص نشده است', 400);
        }

        // Fetch Quota Stats
        $row = $this->cargoRepository->checkQuotaExistenceCargo($quotaNumber, $shipName);
        
        if (empty($row)) {
            return [
                'isActive' => false,
                'status' => false,
                'message' => 'کوتاژ در سیستم ثبت نشده است',
                'details' => null
            ];
        }

        $isActive = (bool)$row['isActive'];
        $totalTonnage = (float)$row['totalTonnage'];
        $loadedTonnage = (float)$row['loadedTonnage'];
        $remainingCapacity = max(0.00, $totalTonnage - $loadedTonnage);
        
        $percentageLoaded = 0.0;
        if ($totalTonnage > 0) {
            $percentageLoaded = round(($loadedTonnage / $totalTonnage) * 100, 2);
        }

        // Apply temporary tonnage status
        $isTempTonnageActive = ((int)$row['temp_tonnage_status']) === 1;
        $tempTonnageAmount = (float)$row['temp_tonnage_amount'];

        if ($isTempTonnageActive) {
            $remainingCapacity = $tempTonnageAmount;
        }

        // Mismatch check logic
        $dbCargoType = $row['cargoType'] ?? '';
        $mismatch = false;
        
        if ($dbCargoType !== '' && $cargoType !== '' && strtolower($dbCargoType) !== strtolower($cargoType)) {
            $mismatch = true;
        }

        if ($mismatch) {
            return [
                'isActive' => false,
                'status' => false,
                'message' => "مغایرت اطلاعات: نوع کالا در سیستم '{$dbCargoType}' ثبت شده است",
                'details' => null
            ];
        }

        // Percentage restriction logic
        $isPercentageRestricted = (bool)$row['isPercentageRestricted'];
        $quotaPercentage = $row['percentage'] !== null ? (float)$row['percentage'] : null;

        if ($isPercentageRestricted && $quotaPercentage !== null && $quotaPercentage > 0) {
            $allowedWeight = $totalTonnage * ($quotaPercentage / 100);
            if ($loadedTonnage >= $allowedWeight) {
                return [
                    'isActive' => false,
                    'status' => false,
                    'message' => "محدودیت درصد بارگیری فعال است ({$quotaPercentage}%). سهمیه مجاز تکمیل شده است.",
                    'details' => null
                ];
            }
        }

        $statusMsg = $isActive ? 'کوتاژ فعال و معتبر است' : 'کوتاژ غیرفعال است';

        return [
            'isActive' => $isActive,
            'status' => $isActive,
            'message' => $statusMsg,
            'details' => [
                'quotaNumber' => $quotaNumber,
                'cargoType' => $dbCargoType,
                'totalTonnage' => $totalTonnage,
                'loadedTonnage' => $loadedTonnage,
                'remainingCapacity' => $remainingCapacity,
                'percentageLoaded' => $percentageLoaded,
                'isActive' => $isActive,
                'percentage' => $quotaPercentage,
                'is_enabled' => $isPercentageRestricted ? 1 : 0,
                'temp_tonnage_status' => $isTempTonnageActive ? 1 : 0,
                'temp_tonnage_amount' => $tempTonnageAmount
            ]
        ];
    }

    /**
     * Get loadable tonnage for a specific quota
     */
    public function getLoadableTonnage(string $quotaNumber, string $shippingCompany = '', string $warehouse = '', string $cargoType = ''): array
    {
        if (empty($quotaNumber)) {
            throw new Exception("شماره کوتاژ مشخص نشده است", 400);
        }

        $row = $this->cargoRepository->getLoadableTonnageInfo($quotaNumber, $shippingCompany, $warehouse, $cargoType);
        
        if ($row) {
            $loadedTonnage = (float)$row['loadedTonnage'];
            $totalTonnage = (float)$row['totalTonnage'];
            $remainingTonnage = max(0.0, $totalTonnage - $loadedTonnage);
            $percentage = $row['percentage'] !== null ? (float)$row['percentage'] : null;
            $isPercentageRestricted = (bool)$row['isPercentageRestricted'];

            $isTempTonnageActive = ((int)$row['temp_tonnage_status']) === 1;
            $tempTonnageAmount = (float)$row['temp_tonnage_amount'];

            if ($isTempTonnageActive) {
                // If temp tonnage is active, loadable is limited by temp tonnage amount
                $loadableTonnage = $tempTonnageAmount;
            } else {
                // Calculate standard loadable tonnage based on percentage restriction
                if ($isPercentageRestricted && $percentage !== null && $percentage > 0) {
                    $allowedPercentageTonnage = $totalTonnage * ($percentage / 100);
                    $loadableTonnage = max(0.00, $allowedPercentageTonnage - $loadedTonnage);
                } else {
                    $loadableTonnage = $remainingTonnage;
                }
            }

            $trucks18Wheeler = $loadableTonnage > 0 ? (int)floor($loadableTonnage / 25000) : 0;
            $trucks10Wheeler = $loadableTonnage > 0 ? (int)floor($loadableTonnage / 15000) : 0;

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

    /**
     * Edit Quota configuration
     */
    public function editQuota(array $params): bool
    {
        $id = (int)($params['id'] ?? 0);
        $oldQuotaNumber = $params['oldQuotaNumber'] ?? '';
        $newQuotaNumber = $params['newQuotaNumber'] ?? '';
        $shipName = $params['shipName'] ?? '';
        $shippingCompany = $params['shippingCompany'] ?? '';
        $warehouse = $params['warehouse'] ?? '';
        $cargoType = $params['cargoType'] ?? '';
        $totalTonnage = (float)($params['totalTonnage'] ?? 0.0);

        if ($id <= 0 || empty($oldQuotaNumber) || empty($newQuotaNumber) || empty($shipName)) {
            throw new Exception("پارامترهای ورودی ناقص هستند", 400);
        }

        try {
            $this->cargoRepository->beginTransaction();
            $res = $this->cargoRepository->editQuota($id, $oldQuotaNumber, $newQuotaNumber, $shipName, $shippingCompany, $warehouse, $cargoType, $totalTonnage);
            $this->cargoRepository->commit();
            return $res;
        } catch (Exception $e) {
            $this->cargoRepository->rollback();
            throw $e;
        }
    }

    /**
     * Update temporary tonnage Status and Amount
     */
    public function updateTemporaryTonnage(string $quotaNumber, int $enabled, ?float $tonnage): array
    {
        if (empty($quotaNumber)) {
            throw new Exception("شماره کوتاژ الزامی است", 400);
        }

        if ($enabled === 1 && $tonnage === null) {
            throw new Exception("مقدار تناژ موقت الزامی است", 400);
        }

        $quota = $this->cargoRepository->getQuotaDetails($quotaNumber);
        if (!$quota) {
            throw new Exception("کوتاژ مورد نظر یافت نشد", 404);
        }

        $res = $this->cargoRepository->updateTemporaryTonnage($quotaNumber, $enabled, $tonnage);

        if ($res) {
            return [
                'success' => true,
                'message' => 'تنظیمات تناژ موقت با موفقیت به‌روزرسانی شد'
            ];
        }

        throw new Exception("خطا در به‌روزرسانی تنظیمات تناژ موقت", 500);
    }

    /**
     * Update quota percentage
     */
    public function updateQuotaPercentage(string $quotaNumber, float $percentage): bool
    {
        if (empty($quotaNumber)) {
            throw new Exception("شماره کوتاژ الزامی است", 400);
        }

        return $this->cargoRepository->updateQuotaPercentage($quotaNumber, $percentage);
    }

    /**
     * Toggle quota active status
     */
    public function toggleQuotaStatus(string $quotaNumber, int $id = 0): bool
    {
        if (empty($quotaNumber) && $id <= 0) {
            throw new Exception("شماره کوتاژ یا شناسه الزامی است", 400);
        }

        return $this->cargoRepository->toggleQuotaStatus($quotaNumber, $id);
    }

    /**
     * Update quota percentage restriction
     */
    public function updateQuotaPercentageRestriction(string $quotaNumber, int $isEnabled): bool
    {
        if (empty($quotaNumber)) {
            throw new Exception("شماره کوتاژ الزامی است", 400);
        }

        return $this->cargoRepository->updateQuotaPercentageRestriction($quotaNumber, $isEnabled);
    }

    /**
     * Delete a Quota entry and associated cargoes
     */
    public function deleteQuota(array $params): bool
    {
        $quotaNumber = $params['quotaNumber'] ?? '';
        $shipName = $params['shipName'] ?? '';
        $warehouse = $params['warehouse'] ?? '';
        $shippingCompany = $params['shippingCompany'] ?? '';
        $cargoType = $params['cargoType'] ?? '';

        if (empty($quotaNumber) || empty($shipName)) {
            throw new Exception("پارامترهای ورودی ناقص هستند", 400);
        }

        try {
            $this->cargoRepository->beginTransaction();
            $res = $this->cargoRepository->deleteQuota($quotaNumber, $shipName, $warehouse, $shippingCompany, $cargoType);
            $this->cargoRepository->commit();
            return $res;
        } catch (Exception $e) {
            $this->cargoRepository->rollback();
            throw $e;
        }
    }

    /**
     * Delete Cargo by ID and restore temporary tonnage if applicable (from deleteCargoInfo.php)
     */
    public function deleteCargoInfo(int $cargoId): array
    {
        if ($cargoId <= 0) {
            throw new Exception("شناسه حواله نامعتبر است", 400);
        }

        try {
            $this->cargoRepository->beginTransaction();

            $cargoData = $this->cargoRepository->getCargoById($cargoId);
            if (!$cargoData) {
                $this->cargoRepository->rollback();
                throw new Exception("حواله یافت نشد یا قبلاً حذف شده است", 404);
            }

            // Execute delete
            $res = $this->cargoRepository->deleteCargoInfo($cargoId);
            
            if ($res) {
                // If it was already exit status and had weight, restore temporary tonnage if active
                if ($cargoData['status'] === 'خروج' && !empty($cargoData['netWeight'])) {
                    $netWeightValue = (float)$cargoData['netWeight'];
                    
                    $tempTonnage = $this->cargoRepository->getTempTonnageInfo(
                        $cargoData['shipName'],
                        $cargoData['loadingWarehouse'],
                        $cargoData['cargoType'],
                        $cargoData['shippingCompany'],
                        $cargoData['loadingQuotaNumber']
                    );

                    if ($tempTonnage && ((int)$tempTonnage['temp_tonnage_status']) === 1) {
                        $newTempTonnage = (float)$tempTonnage['temp_tonnage_amount'] + $netWeightValue;
                        
                        $this->cargoRepository->updateTempTonnageAmount(
                            $cargoData['shipName'],
                            $cargoData['loadingWarehouse'],
                            $cargoData['cargoType'],
                            $cargoData['shippingCompany'],
                            $cargoData['loadingQuotaNumber'],
                            $newTempTonnage
                        );
                    }
                }
                
                $this->cargoRepository->commit();
                return [
                    'status' => 'success',
                    'message' => 'حواله با موفقیت حذف شد'
                ];
            }

            $this->cargoRepository->rollback();
            throw new Exception("خطا در اجرای عملیات حذف", 500);

        } catch (Exception $e) {
            $this->cargoRepository->rollback();
            throw $e;
        }
    }

    /**
     * Confirm cargo entry (from confirm_cargo.php)
     */
    public function confirmCargoInfo(int $cargoId, string $username, string $userType): array
    {
        if ($cargoId <= 0 || empty($username)) {
            throw new Exception("پارامترهای ورودی ناقص هستند", 400);
        }

        try {
            $this->cargoRepository->beginTransaction();
            
            $cargoData = $this->cargoRepository->getCargoById($cargoId);
            if (!$cargoData) {
                $this->cargoRepository->rollback();
                throw new Exception("حواله یافت نشد", 404);
            }

            $confirmed = $this->cargoRepository->confirmCargoInfo($cargoId, $username, $userType);
            
            if ($confirmed) {
                $this->cargoRepository->commit();
                
                $confirmTime = date('H:i:s');
                $confirmDate = date('Y/m/d');

                return [
                    'status' => 'success',
                    'message' => "حواله شماره {$cargoData['trackingNumber']} با کوتاژ {$cargoData['loadingQuotaNumber']} در ساعت {$confirmTime} توسط {$username} با موفقیت تأیید شد",
                    'data' => [
                        'id' => $cargoId,
                        'trackingNumber' => $cargoData['trackingNumber'] ?? '',
                        'loadingQuotaNumber' => $cargoData['loadingQuotaNumber'] ?? '',
                        'confirmTime' => $confirmTime,
                        'confirmDate' => $confirmDate,
                        'confirmUsername' => $username,
                        'confirmUserType' => $userType
                    ]
                ];
            } else {
                $this->cargoRepository->commit();
                
                if ($cargoData['confirm'] === 'تائید شده') {
                    return [
                        'status' => 'error',
                        'message' => 'حواله قبلاً تأیید شده است یا تغییری اعمال نشد',
                        'data' => []
                    ];
                }

                throw new Exception("خطا در تایید حواله", 500);
            }

        } catch (Exception $e) {
            $this->cargoRepository->rollback();
            throw $e;
        }
    }

    /**
     * Process save or update cargo entry (from saveOrUpdateCargoInfo.php)
     */
    public function saveOrUpdateCargo(array $params): array
    {
        $id = isset($params['id']) && $params['id'] !== '' ? (int)$params['id'] : null;
        $shipName = $params['shipName'] ?? '';
        $trackingNumber = $params['trackingNumber'] ?? '';
        $loadingQuotaNumber = $params['loadingQuotaNumber'] ?? '';
        $loadingWarehouse = $params['loadingWarehouse'] ?? '';
        $cargoType = $params['cargoType'] ?? '';
        $shippingCompany = $params['shippingCompany'] ?? '';
        
        $netWeight = isset($params['netWeight']) && $params['netWeight'] !== '' ? (float)$params['netWeight'] : 0.0;
        $status = $params['status'] ?? 'ورود';

        // 1. Validate if exiting
        if ($status === 'خروج') {
            if ($netWeight <= 0) {
                throw new Exception("وزن خالص باید عددی مثبت و بزرگتر از صفر باشد.");
            }
            if (empty($params['scaleReceiptNumber'])) {
                throw new Exception("شماره قبض باسکول نمی‌تواند خالی باشد.");
            }
            if (!ctype_digit((string)$params['scaleReceiptNumber'])) {
                throw new Exception("شماره قبض باسکول باید فقط شامل اعداد باشد.");
            }
        }

        try {
            $this->cargoRepository->beginTransaction();

            $yesterdayStart = date('Y-m-d 00:00:00', strtotime('-1 day'));

            // Check if it's update or new
            $isUpdate = ($id !== null && $id > 0);
            $oldCargo = null;

            if ($isUpdate) {
                $oldCargo = $this->cargoRepository->getCargoById($id);
                if (!$oldCargo) {
                    throw new Exception("حواله مورد نظر جهت ویرایش یافت نشد", 404);
                }
            } else {
                // Check duplicate in past 24 hours
                $duplicate = $this->cargoRepository->checkDuplicateCargo24h($shipName, $trackingNumber, $yesterdayStart);
                if ($duplicate) {
                    // Same behavior as legacy script
                    if ($duplicate['status'] === 'ورود' && $status === 'ورود') {
                        throw new Exception("کامیون قبلا وارد شده و هنوز خارج نشده است.");
                    }
                    if ($duplicate['status'] === 'خروج' && $status === 'خروج') {
                        throw new Exception("کامیون قبلا خارج شده است.");
                    }
                    
                    // If exiting, update the existing entry instead of creating new
                    if ($duplicate['status'] === 'ورود' && $status === 'خروج') {
                        $isUpdate = true;
                        $id = (int)$duplicate['id'];
                        $oldCargo = $duplicate;
                    }
                }
            }

            // Load Temp Tonnage Config
            $tempTonnage = $this->cargoRepository->getTempTonnageInfo($shipName, $loadingWarehouse, $cargoType, $shippingCompany, $loadingQuotaNumber);
            $isTempTonnageActive = $tempTonnage && ((int)$tempTonnage['temp_tonnage_status']) === 1;
            
            // Manage temporary tonnage logic
            if ($isTempTonnageActive) {
                $currentTempAmount = (float)$tempTonnage['temp_tonnage_amount'];
                
                if ($status === 'خروج') {
                    if ($isUpdate && $oldCargo && $oldCargo['status'] === 'خروج') {
                        // Weight changed for already exited truck
                        $oldWeight = (float)$oldCargo['netWeight'];
                        $diff = $netWeight - $oldWeight;
                        $newTempAmount = $currentTempAmount - $diff;
                    } else {
                        // Truck now exiting (was inside or new entry)
                        $newTempAmount = $currentTempAmount - $netWeight;
                    }
                    
                    // Update temp tonnage in DB
                    $this->cargoRepository->updateTempTonnageAmount($shipName, $loadingWarehouse, $cargoType, $shippingCompany, $loadingQuotaNumber, $newTempAmount);
                }
            }

            // Save or Update DB
            if ($isUpdate && $id !== null) {
                // Keep values from old cargo if not provided
                $updateParams = array_merge([
                    'entryTime' => $oldCargo['entryTime'] ?? '',
                    'netWeight' => $netWeight,
                    'scaleReceiptNumber' => $params['scaleReceiptNumber'] ?? '',
                    'shortageWeight' => isset($params['shortageWeight']) ? (float)$params['shortageWeight'] : 0.0,
                    'excessWeight' => isset($params['excessWeight']) ? (float)$params['excessWeight'] : 0.0,
                    'exitTime' => $params['exitTime'] ?? date('H:i:s'),
                    'exitDate' => $params['exitDate'] ?? date('Y/m/d'),
                    'status' => $status,
                    'confirmation' => $params['confirmation'] ?? 'تائید نشده',
                    'numberOfPeople' => isset($params['numberOfPeople']) ? (int)$params['numberOfPeople'] : 1,
                    'duplicateConfirmation' => $params['duplicateConfirmation'] ?? ''
                ], $params);

                $this->cargoRepository->updateCargo($id, $updateParams);
                $finalId = $id;
                $message = "اطلاعات محموله با موفقیت به‌روزرسانی شد.";
            } else {
                $insertParams = array_merge([
                    'entryTime' => date('H:i:s'),
                    'netWeight' => 0.0,
                    'scaleReceiptNumber' => '',
                    'shortageWeight' => 0.0,
                    'excessWeight' => 0.0,
                    'exitTime' => '',
                    'exitDate' => '',
                    'status' => 'ورود',
                    'confirmation' => 'تائید نشده',
                    'numberOfPeople' => 1,
                    'duplicateConfirmation' => ''
                ], $params);

                $finalId = $this->cargoRepository->insertCargo($insertParams);
                $message = "اطلاعات محموله با موفقیت ثبت شد.";
            }

            $this->cargoRepository->commit();
            
            return [
                'error' => false,
                'message' => $message,
                'id' => $finalId
            ];

        } catch (Exception $e) {
            $this->cargoRepository->rollback();
            throw $e;
        }
    }
}
