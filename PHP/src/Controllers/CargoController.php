<?php

declare(strict_types=1);

namespace AtkCargo\Controllers;

use AtkCargo\Core\Request;
use AtkCargo\Core\Response;
use AtkCargo\Services\CargoService;
use Exception;

/**
 * Controller class for routing all Cargo and Quota requests
 */
class CargoController
{
    private CargoService $cargoService;

    public function __construct()
    {
        $this->cargoService = new CargoService();
    }

    /**
     * Get list of active and inactive ships with statistics
     */
    public function getShipsList(Request $request): void
    {
        $result = $this->cargoService->getShipsList();
        Response::json($result);
    }

    /**
     * Get quotas and status for a specific ship
     */
    public function getShipDetails(Request $request): void
    {
        $shipName = $request->getString('shipName');
        try {
            $result = $this->cargoService->getShipDetails($shipName);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Get loading details in a specific ship warehouse
     */
    public function getWarehouseDetails(Request $request): void
    {
        $shipName = $request->getString('shipName');
        $warehouseName = $request->getString('warehouseName');
        try {
            $result = $this->cargoService->getWarehouseDetails($shipName, $warehouseName);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Get details of a single quota
     */
    public function getQuotaDetails(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        try {
            $result = $this->cargoService->getQuotaDetails($quotaNumber);
            if ($result === null) {
                Response::json(['error' => 'کوتاژ مورد نظر یافت نشد'], 404);
            } else {
                Response::json($result);
            }
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Get list of all quotas for a ship
     */
    public function getQuotasList(Request $request): void
    {
        $shipName = $request->getString('shipName');
        try {
            $result = $this->cargoService->getQuotasList($shipName);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Get filtered quotas by date range
     */
    public function getFilteredQuotas(Request $request): void
    {
        $shipName = $request->getString('shipName');
        $startDateTime = $request->getString('startDateTime');
        $endDateTime = $request->getString('endDateTime');

        try {
            $result = $this->cargoService->getFilteredQuotas($shipName, $startDateTime, $endDateTime);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Get filtered summary statistics as HTML format
     */
    public function getFilteredSummary(Request $request): void
    {
        $shipName = $request->getString('shipName');
        $warehouseName = $request->getString('warehouseName');
        $selectedQuota = $request->getString('selectedQuota');
        $startDateTime = $request->getString('startDateTime');
        $endDateTime = $request->getString('endDateTime');

        // Note: For backwards compatibility, this endpoint returns raw HTML output
        try {
            $htmlResult = $this->cargoService->getFilteredSummaryHtml($shipName, $warehouseName, $selectedQuota, $startDateTime, $endDateTime);
            echo $htmlResult;
            exit;
        } catch (Exception $e) {
            http_response_code(400);
            echo "خطا در دریافت خلاصه آمار: " . $e->getMessage();
            exit;
        }
    }

    /**
     * Check if a quota exists and returns status
     */
    public function checkQuotaExistence(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        $shipName = $request->getString('shipName');

        if (empty($quotaNumber) || empty($shipName)) {
            Response::json(['error' => 'شماره کوتاژ یا نام کشتی مشخص نشده است'], 400);
        }

        try {
            $result = $this->cargoService->checkQuotaStatus([
                'quotaNumber' => $quotaNumber,
                'shipName' => $shipName
            ]);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Get all active quotas grouped by ship name and cargo owner
     */
    public function getGroupedQuotas(Request $request): void
    {
        try {
            $allQuotas = $this->cargoService->getAllQuotasList();
            $grouped = [];
            
            foreach ($allQuotas as $quota) {
                // Determine owner from shipping company to mimic old behavior
                $shipName = $quota['shipName'] ?: 'نامشخص';
                $cargoOwner = $quota['shippingCompany'] ?: 'نامشخص';
                
                if (!isset($grouped[$shipName])) {
                    $grouped[$shipName] = [];
                }
                if (!isset($grouped[$shipName][$cargoOwner])) {
                    $grouped[$shipName][$cargoOwner] = [];
                }
                $grouped[$shipName][$cargoOwner][] = $quota;
            }
            
            Response::json($grouped);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 500);
        }
    }

    /**
     * Update temporary tonnage settings for a quota
     */
    public function updateTemporaryTonnage(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        $enabled = $request->getInt('enabled');
        
        $tonnage = null;
        if ($request->get('tonnage') !== null) {
            $tonnage = $request->getFloat('tonnage');
        }

        try {
            $result = $this->cargoService->updateTemporaryTonnage($quotaNumber, $enabled, $tonnage);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Edit Quota configuration
     */
    public function editQuota(Request $request): void
    {
        $params = $request->all();
        try {
            $res = $this->cargoService->editQuota($params);
            Response::json(['success' => $res]);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Update quota percentage
     */
    public function updateQuotaPercentage(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        $percentage = $request->getFloat('percentage');

        try {
            $res = $this->cargoService->updateQuotaPercentage($quotaNumber, $percentage);
            Response::json(['success' => $res]);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Toggle active/inactive status of a quota
     */
    public function toggleQuotaStatus(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        $id = $request->getInt('id');

        try {
            $res = $this->cargoService->toggleQuotaStatus($quotaNumber, $id);
            Response::json(['success' => $res]);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Update percentage restriction mode
     */
    public function updateQuotaPercentageRestriction(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        $isEnabled = $request->getInt('isEnabled');

        try {
            $res = $this->cargoService->updateQuotaPercentageRestriction($quotaNumber, $isEnabled);
            Response::json(['success' => $res]);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Delete Quota entry
     */
    public function deleteQuota(Request $request): void
    {
        $params = $request->all();
        try {
            $res = $this->cargoService->deleteQuota($params);
            Response::json(['success' => $res]);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Calculate and return loadable tonnage
     */
    public function getLoadableTonnage(Request $request): void
    {
        $quotaNumber = $request->getString('quotaNumber');
        $shippingCompany = $request->getString('shippingCompany');
        $warehouse = $request->getString('warehouse');
        $cargoType = $request->getString('cargoType');

        try {
            $result = $this->cargoService->getLoadableTonnage($quotaNumber, $shippingCompany, $warehouse, $cargoType);
            Response::json($result);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 400);
        }
    }

    /**
     * Check live quota validation status for driver entry
     */
    public function checkQuotaStatus(Request $request): void
    {
        $params = [
            'quotaNumber' => $request->getString('quotaNumber'),
            'shipName' => $request->getString('shipName'),
            'cargoType' => $request->getString('cargoType'),
            'shippingCompany' => $request->getString('shippingCompany'),
            'warehouse' => $request->getString('warehouse')
        ];

        try {
            $status = $this->cargoService->checkQuotaStatus($params);
            Response::json($status);
        } catch (Exception $e) {
            Response::json([
                'isActive' => false,
                'status' => false,
                'message' => $e->getMessage(),
                'details' => null
            ], 500);
        }
    }

    /**
     * Combine active ships and time info for real-time dashboard
     */
    public function getRealTimeData(Request $request): void
    {
        try {
            $shipsList = $this->cargoService->getShipsList();
            Response::json([
                'ships' => $shipsList['data']['activeShips'] ?? [],
                'timestamp' => date('Y-m-d H:i:s'),
                'status' => 'success'
            ]);
        } catch (Exception $e) {
            Response::json(['error' => $e->getMessage()], 500);
        }
    }

    /**
     * Handle save or update cargo request (from saveOrUpdateCargoInfo.php)
     */
    public function saveOrUpdateCargo(Request $request): void
    {
        $data = $request->all();
        try {
            $result = $this->cargoService->saveOrUpdateCargo($data);
            Response::json($result);
        } catch (Exception $e) {
            Response::json([
                'error' => true,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    /**
     * Handle cargo delete request (from deleteCargoInfo.php)
     */
    public function deleteCargo(Request $request): void
    {
        $cargoId = $request->getInt('id');
        try {
            $result = $this->cargoService->deleteCargoInfo($cargoId);
            Response::json($result);
        } catch (Exception $e) {
            Response::json([
                'status' => 'error',
                'message' => 'خطا در حذف حواله: ' . $e->getMessage()
            ], 400);
        }
    }

    /**
     * Handle cargo confirm request (from confirm_cargo.php)
     */
    public function confirmCargo(Request $request): void
    {
        $cargoId = $request->getInt('id');
        $username = $request->getString('username');
        $userType = $request->getString('userType');

        try {
            $result = $this->cargoService->confirmCargoInfo($cargoId, $username, $userType);
            Response::json($result);
        } catch (Exception $e) {
            Response::json([
                'status' => 'error',
                'message' => $e->getMessage()
            ], 400);
        }
    }
}
