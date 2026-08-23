<?php
// PHP/src/Controllers/LicenseController.php

declare(strict_types=1);

namespace App\Controllers;

use Exception;
use App\Core\Logger;
use App\Core\Request;
use App\Core\Response;
use App\Repositories\LicenseRepository;

class LicenseController {

 // ===== CONFIGURATION =====

 // سقف درخواست‌های مجاز در هر پنجره‌ی زمانی (هر بار اجرای اپ ۲ درخواست می‌فرستد)
    private const RATE_LIMIT_MAX    = 100;
 // مدت پنجره‌ی زمانی نرخ‌سنجی به ثانیه
    private const RATE_LIMIT_WINDOW = 60;
 // پیشوند کلید کش برای جداسازی از شمارنده‌های دیگر
    private const RATE_CACHE_PREFIX = 'lic_rate_';

 // ===== DEPENDENCIES =====

    private LicenseRepository $licenses;
    private Logger $logger;
    private Request $request;

    public function __construct() {
        $this->licenses = new LicenseRepository();
        $this->logger   = Logger::getInstance();
        $this->request  = new Request();
    }

 // ===== PUBLIC ENDPOINTS =====

 // POST license/validate — اعتبارسنجی کلید لایسنس توسط اپ اندروید
    public function validateLicense(): void {
        $this->sendJsonHeaders();
        $this->enforceRateLimit();

 // خواندن و پارس بدنه‌ی JSON
        $rawData = file_get_contents('php://input');
        if (!$rawData) {
            Response::json(['success' => false, 'message' => 'داده‌های ورودی نامعتبر است']);
        }

        $data = json_decode((string)$rawData, true);
        if (json_last_error() !== JSON_ERROR_NONE || !is_array($data)) {
            Response::json(['success' => false, 'message' => 'فرمت داده‌های ورودی نامعتبر است']);
        }

 // استخراج و اعتبارسنجی اولیه‌ی کلید (همیشه ۳۲ کاراکتر hex است)
        $licenseKey      = trim((string)($data['licenseKey'] ?? ''));
        $updateLastCheck = $data['update_last_check'] ?? true;

        if ($licenseKey === '' || strlen($licenseKey) !== 32) {
            Response::json(['success' => false, 'message' => 'کلید لایسنس نامعتبر است']);
        }

        try {
            $license = $this->licenses->findByKey($licenseKey);

            if ($license === null) {
                Response::json(['success' => false, 'message' => 'لایسنس نامعتبر است']);
            }

 // ثبت زمان آخرین بررسی فقط در صورت درخواست صریح
            if ($updateLastCheck) {
                $this->licenses->touchLastCheck($licenseKey);
            }

            $lastCheck = $updateLastCheck ? date('Y-m-d H:i:s') : $license['last_check'];

 // وضعیت مؤثر از Repository خوانده می‌شود تا با پنل ادمین یکسان باشد
            $status = (string)($license['effective_status'] ?? 'active');

            if ($status === 'active') {
                Response::json([
                    'success' => true,
                    'message' => 'لایسنس معتبر است',
                    'license' => $this->presentLicense($license, $lastCheck),
                ]);
            }

 // تفکیک «منقضی» از «غیرفعال» برای راحتی تیم پشتیبانی
            if ($status === 'expired') {
                Response::json([
                    'success' => false,
                    'code'    => 'license_expired',
                    'message' => 'اعتبار لایسنس به پایان رسیده است',
                    'license' => [
                        'licenseKey' => (string)$license['license_key'],
                        'expiresAt'  => $license['expires_at'],
                        'lastCheck'  => $lastCheck,
                    ],
                ]);
            }

 // هر وضعیت دیگری = غیرفعال‌شده توسط ادمین
            Response::json([
                'success' => false,
                'code'    => 'license_inactive',
                'message' => 'لایسنس غیرفعال شده است',
                'license' => [
                    'licenseKey' => (string)$license['license_key'],
                    'lastCheck'  => $lastCheck,
                ],
            ]);

        } catch (Exception $e) {
            $this->logger->error('License validation error: ' . $e->getMessage());
            Response::json(['success' => false, 'message' => 'خطای سیستمی رخ داده است']);
        }
    }

 // GET license/info — دریافت اطلاعات کامل لایسنس (کلید در هدر، نه query string)
    public function getLicenseInfo(): void {
        $this->sendJsonHeaders();

        if (!$this->request->isGet()) {
            Response::error('روش درخواست معتبر نیست.', 405);
        }

        $this->enforceRateLimit();

 // کلید از هدر خوانده می‌شود تا در لاگ وب‌سرور ثبت نشود
        $licenseKey = trim((string)($this->request->getHeader('X-License-Key') ?? ''));

        if ($licenseKey === '' || strlen($licenseKey) !== 32) {
            Response::json(['success' => false, 'message' => 'کلید لایسنس نامعتبر است']);
        }

        try {
            $license = $this->licenses->findByKey($licenseKey);

            if ($license === null) {
                Response::json(['success' => false, 'message' => 'لایسنس مورد نظر یافت نشد']);
            }

            Response::json([
                'success' => true,
                'message' => 'اطلاعات لایسنس با موفقیت دریافت شد',
 // createdAt فقط در این endpoint اضافه می‌شود (در validate لازم نیست)
                'license' => $this->presentLicense($license, $license['last_check']) + [
                    'createdAt' => $license['created_at'],
                ],
            ]);

        } catch (Exception $e) {
            $this->logger->error('Get license info error: ' . $e->getMessage());
            Response::json(['success' => false, 'message' => 'خطای سیستمی رخ داده است']);
        }
    }

 // ===== CORE LOGIC =====

 // شکل یکسان فیلدهای license در تمام پاسخ‌های موفق
    private function presentLicense(array $license, ?string $lastCheck): array {
        return [
            'licenseKey'     => (string)$license['license_key'],
            'companyName'    => (string)$license['company_name'],
            'plan'           => (string)($license['plan'] ?? 'standard'),
            'activationDate' => $license['activation_date'],
            'expiresAt'      => $license['expires_at'],
            'isActive'       => (bool)$license['is_active'],
            'lastCheck'      => $lastCheck,
        ];
    }

 // ===== HELPERS =====

 // هدرهای امنیتی و Content-Type مشترک هر دو endpoint
    private function sendJsonHeaders(): void {
        header('Content-Type: application/json');
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header("Content-Security-Policy: default-src 'self'");
        date_default_timezone_set('Asia/Tehran');
    }

 // بررسی و افزایش شمارنده‌ی نرخ — اگر از سقف گذشت، 429 برمی‌گرداند
    private function enforceRateLimit(): void {
        $key   = self::RATE_CACHE_PREFIX . $this->request->getClientIp();
        $count = $this->incrementRateCounter($key);

        if ($count > self::RATE_LIMIT_MAX) {
            Response::json(['success' => false, 'message' => 'تعداد درخواست‌ها بیش از حد مجاز است.'], 429);
        }
    }

 // افزایش اتمیک شمارنده با APCu (ترجیحی) یا فایل (جایگزین)
    private function incrementRateCounter(string $key): int {
        if (function_exists('apcu_add') && function_exists('apcu_inc')) {
            apcu_add($key, 0, self::RATE_LIMIT_WINDOW);
            return (int)apcu_inc($key);
        }

 // فایل جایگزین برای محیط‌هایی که APCu ندارند
        $safeKey = preg_replace('/[^a-zA-Z0-9_]/', '_', $key) ?? 'unknown';
        $dir     = APP_ROOT . '/log';
        if (!is_dir($dir)) {
            mkdir($dir, 0755, true);
        }

        $file = $dir . "/rate_{$safeKey}.json";
        $now  = time();
        $data = ['count' => 0, 'expires' => $now + self::RATE_LIMIT_WINDOW];

        if (file_exists($file)) {
            $raw = json_decode((string)file_get_contents($file), true);
 // اگر پنجره هنوز باز است داده‌ی قبلی را نگه می‌داریم، وگرنه ریست می‌شود
            if (is_array($raw) && ($raw['expires'] ?? 0) > $now) {
                $data = $raw;
            }
        }

        $data['count']++;
        file_put_contents($file, json_encode($data), LOCK_EX);
        return $data['count'];
    }
}
