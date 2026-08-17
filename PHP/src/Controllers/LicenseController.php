<?php
// PHP/src/Controllers/LicenseController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use mysqli;
use App\Core\Database;
use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;

class LicenseController {
    private mysqli $conn;
    private Logger $logger;
    private Request $request;

    public function __construct() {
        $this->conn = Database::getInstance()->getMysqliConnection();
        $this->logger = Logger::getInstance();
        $this->request = new Request();
    }

    /**
     * اعتبارسنجی لایسنس (validate_license.php)
     */
    public function validateLicense(): void {
        header('Content-Type: application/json');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('Content-Security-Policy: default-src \'self\'');
        date_default_timezone_set('Asia/Tehran');

        $rawData = file_get_contents('php://input');
        if (!$rawData) {
            Response::json([
                'success' => false,
                'message' => 'داده‌های ورودی نامعتبر است'
            ]);
        }

        $data = json_decode((string)$rawData, true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
            Response::json([
                'success' => false,
                'message' => 'فرمت داده‌های ورودی نامعتبر است'
            ]);
        }

        $licenseKey = trim((string)($data['licenseKey'] ?? ''));
        $updateLastCheck = $data['update_last_check'] ?? true;

        if (empty($licenseKey) || strlen($licenseKey) !== 32) {
            Response::json([
                'success' => false,
                'message' => 'کلید لایسنس نامعتبر است'
            ]);
        }

        try {
            $stmt = $this->conn->prepare("SELECT * FROM licenses WHERE license_key = ?");
            $stmt->bind_param("s", $licenseKey);
            $stmt->execute();
            $result = $stmt->get_result();

            if ($result->num_rows > 0) {
                $license = $result->fetch_assoc();
                $stmt->close();

                if ($updateLastCheck) {
                    $updateStmt = $this->conn->prepare("UPDATE licenses SET last_check = CURRENT_TIMESTAMP WHERE license_key = ?");
                    $updateStmt->bind_param("s", $licenseKey);
                    $updateStmt->execute();
                    $updateStmt->close();
                }

                if ((bool)$license['is_active']) {
                    Response::json([
                        'success' => true,
                        'message' => 'لایسنس معتبر است',
                        'license' => [
                            'licenseKey' => htmlspecialchars((string)$license['license_key']),
                            'companyName' => htmlspecialchars((string)$license['company_name']),
                            'activationDate' => $license['activation_date'],
                            'isActive' => (bool)$license['is_active'],
                            'lastCheck' => $updateLastCheck ? date('Y-m-d H:i:s') : $license['last_check']
                        ]
                    ]);
                } else {
                    Response::json([
                        'success' => false,
                        'message' => 'لایسنس غیرفعال شده است',
                        'license' => [
                            'licenseKey' => htmlspecialchars((string)$license['license_key']),
                            'lastCheck' => $updateLastCheck ? date('Y-m-d H:i:s') : $license['last_check']
                        ]
                    ]);
                }
            } else {
                $stmt->close();
                Response::json([
                    'success' => false,
                    'message' => 'لایسنس نامعتبر است'
                ]);
            }
        } catch (Exception $e) {
            $this->logger->error("License validation error: " . $e->getMessage());
            Response::json([
                'success' => false,
                'message' => 'خطای سیستمی رخ داده است'
            ]);
        }
    }

    /**
     * دریافت اطلاعات لایسنس (get_license_info.php)
     */
    public function getLicenseInfo(): void {
        header('Content-Type: application/json');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('Content-Security-Policy: default-src \'self\'');
        date_default_timezone_set('Asia/Tehran');

        if (!$this->request->isGet()) {
            http_response_code(405);
            echo json_encode(['success' => false, 'message' => 'روش درخواست معتبر نیست'], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $licenseKey = trim((string)$this->request->get('licenseKey', ''));

        if (empty($licenseKey) || strlen($licenseKey) !== 32) {
            Response::json([
                'success' => false,
                'message' => 'کلید لایسنس نامعتبر است'
            ]);
        }

        try {
            $stmt = $this->conn->prepare("SELECT * FROM licenses WHERE license_key = ?");
            $stmt->bind_param("s", $licenseKey);
            $stmt->execute();
            $result = $stmt->get_result();

            if ($result->num_rows > 0) {
                $license = $result->fetch_assoc();
                $stmt->close();

                Response::json([
                    'success' => true,
                    'message' => 'اطلاعات لایسنس با موفقیت دریافت شد',
                    'license' => [
                        'licenseKey' => htmlspecialchars((string)$license['license_key']),
                        'companyName' => htmlspecialchars((string)$license['company_name']),
                        'activationDate' => $license['activation_date'],
                        'isActive' => (bool)$license['is_active'],
                        'createdAt' => $license['created_at'],
                        'lastCheck' => $license['last_check']
                    ]
                ]);
            } else {
                $stmt->close();
                Response::json([
                    'success' => false,
                    'message' => 'لایسنس مورد نظر یافت نشد'
                ]);
            }
        } catch (Exception $e) {
            $this->logger->error("Get license info error: " . $e->getMessage());
            Response::json([
                'success' => false,
                'message' => 'خطای سیستمی رخ داده است'
            ]);
        }
    }

}
