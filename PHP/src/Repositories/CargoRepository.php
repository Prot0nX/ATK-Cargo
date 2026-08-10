<?php
// PHP/src/Repositories/CargoRepository.php

declare(strict_types=1);

namespace App\Repositories;

use mysqli;
use App\Core\Database;

class CargoRepository {
    private mysqli $conn;

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
    }

    public function getMysqliConnection(): mysqli {
        return $this->conn;
    }

    public function find24hCargo(string $shipName, string $trackingNumber, string $yesterdayStart): ?array {
        $query = "SELECT loadingQuotaNumber, loadingWarehouse, shippingCompany, exitTime, exitDate, status, entryTime 
                  FROM CargoInfo 
                  WHERE shipName = ? AND trackingNumber = ? AND updated_at >= ? 
                  ORDER BY id DESC LIMIT 1";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return null;
        $stmt->bind_param("sss", $shipName, $trackingNumber, $yesterdayStart);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $result ?: null;
    }

    public function findCargoByKeys(
        string $shipName, 
        string $warehouse, 
        string $cargoType, 
        string $company, 
        string $quotaNumber, 
        string $trackingNumber
    ): ?array {
        $query = "SELECT id, status, confirm, exitDate, exitTime, entryTime FROM CargoInfo WHERE 
                  shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                  shippingCompany = ? AND loadingQuotaNumber = ? AND trackingNumber = ? 
                  ORDER BY id DESC LIMIT 1";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return null;
        $stmt->bind_param("ssssss", $shipName, $warehouse, $cargoType, $company, $quotaNumber, $trackingNumber);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $result ?: null;
    }

    public function insertCargo(array $params, string $currentTime, int $numberOfPeople): bool {
        $query = "INSERT INTO CargoInfo (
            trackingNumber, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, 
            status, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber, 
            numberOfPeople, username, userType
        ) VALUES (?, ?, ?, ?, ?, ?, 'ورود', ?, ?, ?, ?, ?, ?, ?, ?)";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return false;
        // netWeight روی رکورد تازه‌ثبت‌شده (وضعیت «ورود») هنوز مقداری ندارد و
        // کلاینت رشته‌ی خالی می‌فرستد؛ باید NULL واقعی درج شود، نه '' که در
        // ستون عددی (INT) با sql_mode=STRICT_TRANS_TABLES با خطا رد می‌شود.
        $netWeight = ($params['netWeight'] === '' || $params['netWeight'] === null) ? null : $params['netWeight'];
        $stmt->bind_param("ssssssssssssss",
            $params['trackingNumber'],
            $currentTime,
            $netWeight,
            $params['scaleReceiptNumber'],
            $params['shortageWeight'],
            $params['excessWeight'],
            $params['shipName'],
            $params['loadingWarehouse'],
            $params['cargoType'],
            $params['shippingCompany'],
            $params['loadingQuotaNumber'],
            $numberOfPeople,
            $params['username'],
            $params['userType']
        );
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    public function findTempTonnage(string $shipName, string $warehouse, string $cargoType, string $company, string $quota): ?array {
        $query = "SELECT temp_tonnage_status, temp_tonnage_amount FROM InitialInfo 
                  WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                        shippingCompany = ? AND loadingQuotaNumber = ? LIMIT 1";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return null;
        $stmt->bind_param("sssss", $shipName, $warehouse, $cargoType, $company, $quota);
        $stmt->execute();
        $res = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $res ?: null;
    }

    public function updateTempTonnage(float $newTemp, string $shipName, string $warehouse, string $cargoType, string $company, string $quota): bool {
        $query = "UPDATE InitialInfo SET temp_tonnage_amount = ? 
                  WHERE shipName = ? AND loadingWarehouse = ? AND cargoType = ? AND 
                        shippingCompany = ? AND loadingQuotaNumber = ?";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return false;
        $stmt->bind_param("dsssss", $newTemp, $shipName, $warehouse, $cargoType, $company, $quota);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    public function updateCargoExit(int $cargoId, string $netWeight, string $scaleReceipt, string $currentTime, string $currentDate, string $username, string $userType): bool {
        $query = "UPDATE CargoInfo SET 
            netWeight = ?, scaleReceiptNumber = ?, exitTime = ?, exitDate = ?, 
            status = 'خروج', username = ?, userType = ? 
        WHERE id = ?";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return false;
        $stmt->bind_param("ssssssi", $netWeight, $scaleReceipt, $currentTime, $currentDate, $username, $userType, $cargoId);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    public function updateCargoShortageOrExcess(int $cargoId, string $shortageWeight, string $excessWeight, string $username, string $userType): bool {
        $query = "UPDATE CargoInfo SET 
            shortageWeight = ?, excessWeight = ?, username = ?, userType = ? 
        WHERE id = ?";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return false;
        $stmt->bind_param("ssssi", $shortageWeight, $excessWeight, $username, $userType, $cargoId);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    public function updateCargoExitExiting(int $cargoId, string $netWeight, string $scaleReceipt, string $currentTime, string $currentDate, string $username, string $userType): bool {
        $query = "UPDATE CargoInfo SET 
            netWeight = ?, scaleReceiptNumber = ?, exitTime = ?, exitDate = ?, 
            username = ?, userType = ? 
        WHERE id = ?";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return false;
        $stmt->bind_param("ssssssi", $netWeight, $scaleReceipt, $currentTime, $currentDate, $username, $userType, $cargoId);
        $res = $stmt->execute();
        $stmt->close();
        return $res;
    }

    public function findCargoById(int $id): ?array {
        $stmt = $this->conn->prepare("SELECT * FROM CargoInfo WHERE id = ? LIMIT 1");
        if (!$stmt) return null;
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $res = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $res ?: null;
    }

    public function isScaleReceiptDuplicate(string $receipt, int $excludeId): bool {
        $stmt = $this->conn->prepare("SELECT id FROM CargoInfo WHERE scaleReceiptNumber = ? AND id != ? LIMIT 1");
        if (!$stmt) return false;
        $stmt->bind_param("si", $receipt, $excludeId);
        $stmt->execute();
        $res = $stmt->get_result()->num_rows > 0;
        $stmt->close();
        return $res;
    }

    public function updateCargoFull(int $id, array $data): bool {
        $query = "UPDATE CargoInfo SET 
            trackingNumber = ?, numberOfPeople = ?, username = ?, userType = ?, 
            entryTime = ?, netWeight = ?, scaleReceiptNumber = ?, shortageWeight = ?, 
            excessWeight = ?, exitTime = ?, exitDate = ?, status = ?, confirm = ? 
            WHERE id = ?";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return false;
        $stmt->bind_param("sssssssssssssi", 
            $data['trackingNumber'], $data['numberOfPeople'], $data['username'], $data['userType'], 
            $data['entryTime'], $data['netWeight'], $data['scaleReceiptNumber'], $data['shortageWeight'], 
            $data['excessWeight'], $data['exitTime'], $data['exitDate'], $data['status'], $data['confirm'], $id
        );
        $res = $stmt->execute();
        $affected = $stmt->affected_rows;
        $stmt->close();
        return $res && ($affected > 0);
    }

    public function confirmCargo(int $cargoId, string $username, string $userType): array {
        $query = "UPDATE CargoInfo 
                  SET confirm = 'تائید شده', 
                      confirm_username = ?, 
                      confirm_usertype = ?, 
                      updated_at = NOW() 
                  WHERE id = ? AND (confirm IS NULL OR confirm != 'تائید شده')";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return ['success' => false, 'affected' => 0];
        $stmt->bind_param("ssi", $username, $userType, $cargoId);
        $stmt->execute();
        $affected = $stmt->affected_rows;
        $stmt->close();
        return ['success' => true, 'affected' => $affected];
    }

    public function deleteCargoById(int $id): bool {
        $stmt = $this->conn->prepare("DELETE FROM CargoInfo WHERE id = ?");
        if (!$stmt) return false;
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $affected = $stmt->affected_rows;
        $stmt->close();
        return $affected > 0;
    }

    public function searchByScaleReceipt(string $receipt): ?array {
        $query = "SELECT id, trackingNumber, numberOfPeople, username, userType, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status, confirm, confirmation, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM CargoInfo WHERE scaleReceiptNumber = ? LIMIT 1";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return null;
        $stmt->bind_param("s", $receipt);
        $stmt->execute();
        $res = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $res ?: null;
    }

    public function searchByTracking(string $tracking): array {
        $query = "SELECT id, trackingNumber, numberOfPeople, username, userType, entryTime, netWeight, scaleReceiptNumber, shortageWeight, excessWeight, exitTime, exitDate, status, confirm, confirmation, shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM CargoInfo WHERE trackingNumber = ? ORDER BY entryTime DESC";
        $stmt = $this->conn->prepare($query);
        if (!$stmt) return [];
        $stmt->bind_param("s", $tracking);
        $stmt->execute();
        $res = $stmt->get_result();
        $list = [];
        while ($row = $res->fetch_assoc()) {
            $list[] = $row;
        }
        $stmt->close();
        return $list;
    }

    public function getActiveShipsList(): array {
        $stmt = $this->conn->prepare("SELECT shipName, loadingWarehouse, cargoType, shippingCompany, loadingQuotaNumber FROM InitialInfo WHERE isActive = 1");
        if (!$stmt) return [];
        $stmt->execute();
        $res = $stmt->get_result();
        $list = [];
        while ($row = $res->fetch_assoc()) {
            $list[] = [
                'shipName' => $row['shipName'],
                'loadingWarehouse' => $row['loadingWarehouse'],
                'cargoType' => $row['cargoType'],
                'shippingCompany' => $row['shippingCompany'],
                'loadingQuotaNumber' => $row['loadingQuotaNumber']
            ];
        }
        $stmt->close();
        return $list;
    }

    public function findByScaleReceiptNumber(string $receipt): ?array {
        $stmt = $this->conn->prepare("SELECT trackingNumber, netWeight, loadingQuotaNumber FROM CargoInfo WHERE scaleReceiptNumber = ? LIMIT 1");
        if (!$stmt) return null;
        $stmt->bind_param("s", $receipt);
        $stmt->execute();
        $res = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return $res ?: null;
    }
}
