<?php
// PHP/src/Services/LicenseAdminService.php

declare(strict_types=1);

namespace App\Services;

use App\Core\Logger;
use App\Exceptions\ApiException;
use App\Repositories\LicenseRepository;

// منطق تجاری پنل مدیریت لایسنس، جدا از لایه‌ی HTTP؛ خطاهای کاربر با ApiException به JSON تبدیل می‌شوند
final class LicenseAdminService {
    // پلن‌های مجاز؛ مقدار در دیتابیس ذخیره می‌شود و برچسب فارسی فقط برای نمایش است
    public const PLANS = [
        'standard' => 'استاندارد',
        'pro'      => 'حرفه‌ای',
        'trial'    => 'آزمایشی',
    ];

    private const MAX_COMPANY_NAME = 255;
    private const MAX_CONTACT_NAME = 150;
    private const MAX_CONTACT_PHONE = 32;
    private const MAX_CONTACT_EMAIL = 190;
    private const MAX_NOTES = 2000;

    // تعداد تلاش برای یافتن کلید یکتا؛ فقط محافظ در برابر خرابی منبع تصادف
    private const MAX_KEY_ATTEMPTS = 10;

    private LicenseRepository $repository;
    private Logger $logger;

    public function __construct(?LicenseRepository $repository = null) {
        $this->repository = $repository ?? new LicenseRepository();
        $this->logger = Logger::getInstance();
    }

    /** @return array<int,array<string,mixed>> ردیف‌های آماده برای نمایش */
    public function list(?string $search, ?string $status): array {
        $status = in_array($status, ['active', 'expired', 'inactive'], true) ? $status : null;
        $rows = $this->repository->listAll($search, $status);

        return array_map([$this, 'presentRow'], $rows);
    }

    /** @return array{total:int,active:int,expired:int,inactive:int,this_month:int,last_month:int} */
    public function stats(): array {
        return $this->repository->stats();
    }

    /** @param array<string,mixed> $input @return array<string,mixed> ردیف ساخته‌شده (شامل کلید تولیدشده) */
    public function create(array $input, string $actor): array {
        $data = $this->validate($input, null);

        $licenseKey = $this->generateUniqueKey();
        $id = $this->repository->create($licenseKey, $data);

        AuditLogger::log($actor, 'license.create', 'license', (string)$id, [
            'company_name' => $data['company_name'],
            'plan'         => $data['plan'],
            'expires_at'   => $data['expires_at'],
        ]);
        $this->logger->security("License created (id={$id}) by {$actor}");

        $row = $this->repository->findById($id);
        if ($row === null) {
            throw new ApiException('لایسنس ساخته شد اما بازخوانی آن ناموفق بود.', 500);
        }
        return $this->presentRow($row);
    }

    /** @param array<string,mixed> $input @return array<string,mixed> ردیف به‌روزشده */
    public function update(int $id, array $input, string $actor): array {
        $existing = $this->requireById($id);
        $data = $this->validate($input, $id);

        $this->repository->update($id, $data);

        AuditLogger::log($actor, 'license.update', 'license', (string)$id, [
            'before' => [
                'company_name' => $existing['company_name'],
                'plan'         => $existing['plan'],
                'expires_at'   => $existing['expires_at'],
            ],
            'after' => [
                'company_name' => $data['company_name'],
                'plan'         => $data['plan'],
                'expires_at'   => $data['expires_at'],
            ],
        ]);

        $row = $this->repository->findById($id);
        if ($row === null) {
            throw new ApiException('لایسنس پس از ویرایش یافت نشد.', 500);
        }
        return $this->presentRow($row);
    }

    /** @return array<string,mixed> ردیف با وضعیت جدید */
    public function toggle(int $id, string $actor): array {
        $existing = $this->requireById($id);
        $newState = !(bool)$existing['is_active'];

        $this->repository->setActive($id, $newState);

        AuditLogger::log($actor, $newState ? 'license.activate' : 'license.deactivate', 'license', (string)$id, [
            'company_name' => $existing['company_name'],
        ]);
        $this->logger->security(
            'License ' . ($newState ? 'activated' : 'deactivated') . " (id={$id}) by {$actor}"
        );

        $row = $this->repository->findById($id);
        if ($row === null) {
            throw new ApiException('لایسنس پس از تغییر وضعیت یافت نشد.', 500);
        }
        return $this->presentRow($row);
    }

    public function delete(int $id, string $actor): void {
        $existing = $this->requireById($id);

        if (!$this->repository->delete($id)) {
            throw new ApiException('لایسنس مورد نظر یافت نشد.', 404);
        }

        // کلید کامل عمداً در جزئیات ممیزی ثبت می‌شود تا پس از حذف رکورد قابل پیگیری بماند
        AuditLogger::log($actor, 'license.delete', 'license', (string)$id, [
            'company_name' => $existing['company_name'],
            'license_key'  => $existing['license_key'],
        ]);
        $this->logger->security("License deleted (id={$id}, company={$existing['company_name']}) by {$actor}");
    }

    // ردیف خام دیتابیس به شکل مصرفی پنل تبدیل می‌شود؛ کلیدها snake_case می‌مانند تا با دیتابیس هم‌نام باشند.
    /** @param array<string,mixed> $row @return array<string,mixed> */
    private function presentRow(array $row): array {
        $plan = (string)($row['plan'] ?? 'standard');

        return [
            'id'            => (int)$row['id'],
            'license_key'   => (string)$row['license_key'],
            'company_name'  => (string)$row['company_name'],
            'plan'          => $plan,
            'plan_label'    => self::PLANS[$plan] ?? $plan,
            'status'        => (string)($row['effective_status'] ?? 'active'),
            'is_active'     => (bool)$row['is_active'],
            'expires_at'    => $row['expires_at'],
            'contact_name'  => $row['contact_name'],
            'contact_phone' => $row['contact_phone'],
            'contact_email' => $row['contact_email'],
            'notes'         => $row['notes'],
            'created_at'    => $row['created_at'],
            'updated_at'    => $row['updated_at'] ?? null,
            'last_check'    => $row['last_check'],
        ];
    }

    /** @return array<string,mixed> */
    private function requireById(int $id): array {
        $row = $this->repository->findById($id);
        if ($row === null) {
            throw new ApiException('لایسنس مورد نظر یافت نشد.', 404);
        }
        return $row;
    }

    // اعتبارسنجی و نرمال‌سازی ورودی فرم؛ فقط کلیدهای شناخته‌شده برمی‌گردند.
    /** @param array<string,mixed> $input @return array{company_name:string,plan:string,expires_at:?string,contact_name:?string,contact_phone:?string,contact_email:?string,notes:?string} */
    private function validate(array $input, ?int $exceptId): array {
        $companyName = trim((string)($input['company_name'] ?? ''));
        if ($companyName === '') {
            throw new ApiException('نام شرکت الزامی است.', 422);
        }
        if (mb_strlen($companyName) > self::MAX_COMPANY_NAME) {
            throw new ApiException('نام شرکت بیش از حد طولانی است.', 422);
        }
        if ($this->repository->companyNameExists($companyName, $exceptId)) {
            throw new ApiException('این نام شرکت قبلاً ثبت شده است.', 409);
        }

        $plan = (string)($input['plan'] ?? 'standard');
        if (!array_key_exists($plan, self::PLANS)) {
            throw new ApiException('پلن انتخاب‌شده معتبر نیست.', 422);
        }

        return [
            'company_name'  => $companyName,
            'plan'          => $plan,
            'expires_at'    => $this->normalizeExpiry($input['expires_at'] ?? null),
            'contact_name'  => $this->optionalText($input['contact_name'] ?? null, self::MAX_CONTACT_NAME, 'نام رابط'),
            'contact_phone' => $this->optionalPhone($input['contact_phone'] ?? null),
            'contact_email' => $this->optionalEmail($input['contact_email'] ?? null),
            'notes'         => $this->optionalText($input['notes'] ?? null, self::MAX_NOTES, 'یادداشت'),
        ];
    }

    // ورودی از فرم به شکل datetime-local یا فقط تاریخ می‌آید؛ خالی یعنی نامحدود، و تاریخ گذشته عمداً پذیرفته می‌شود
    private function normalizeExpiry(mixed $value): ?string {
        if (!is_string($value)) {
            return null;
        }
        $value = trim($value);
        if ($value === '') {
            return null;
        }

        $value = str_replace('T', ' ', $value);
        if (preg_match('/^\d{4}-\d{2}-\d{2}$/', $value) === 1) {
            // فقط تاریخ داده شده — تا پایان همان روز معتبر بماند.
            $value .= ' 23:59:59';
        } elseif (preg_match('/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/', $value) === 1) {
            $value .= ':00';
        }

        $parsed = \DateTimeImmutable::createFromFormat('Y-m-d H:i:s', $value);
        if ($parsed === false || $parsed->format('Y-m-d H:i:s') !== $value) {
            throw new ApiException('تاریخ انقضا معتبر نیست.', 422);
        }

        return $value;
    }

    private function optionalText(mixed $value, int $maxLength, string $fieldLabel): ?string {
        if (!is_string($value)) {
            return null;
        }
        $value = trim($value);
        if ($value === '') {
            return null;
        }
        if (mb_strlen($value) > $maxLength) {
            throw new ApiException("{$fieldLabel} بیش از حد طولانی است.", 422);
        }
        return $value;
    }

    private function optionalPhone(mixed $value): ?string {
        $value = $this->optionalText($value, self::MAX_CONTACT_PHONE, 'شماره تماس');
        if ($value === null) {
            return null;
        }
        // ارقام فارسی/عربی به لاتین تبدیل می‌شوند تا شماره‌ی ذخیره‌شده یکدست و قابل جستجو بماند
        $value = strtr($value, [
            '۰' => '0', '۱' => '1', '۲' => '2', '۳' => '3', '۴' => '4',
            '۵' => '5', '۶' => '6', '۷' => '7', '۸' => '8', '۹' => '9',
            '٠' => '0', '١' => '1', '٢' => '2', '٣' => '3', '٤' => '4',
            '٥' => '5', '٦' => '6', '٧' => '7', '٨' => '8', '٩' => '9',
        ]);
        if (preg_match('/^[0-9+\-() ]{4,}$/', $value) !== 1) {
            throw new ApiException('شماره تماس معتبر نیست.', 422);
        }
        return $value;
    }

    private function optionalEmail(mixed $value): ?string {
        $value = $this->optionalText($value, self::MAX_CONTACT_EMAIL, 'ایمیل');
        if ($value === null) {
            return null;
        }
        if (filter_var($value, FILTER_VALIDATE_EMAIL) === false) {
            throw new ApiException('ایمیل معتبر نیست.', 422);
        }
        return $value;
    }

    // قرارداد کلید (۳۲ کاراکتر hex بزرگ) دست‌نخورده مانده؛ تغییر آن کلاینت اندروید را می‌شکند
    private function generateUniqueKey(): string {
        for ($attempt = 0; $attempt < self::MAX_KEY_ATTEMPTS; $attempt++) {
            $key = strtoupper(bin2hex(random_bytes(16)));
            if (!$this->repository->keyExists($key)) {
                return $key;
            }
        }
        throw new ApiException('تولید کلید یکتا ناموفق بود. لطفاً دوباره تلاش کنید.', 500);
    }
}
