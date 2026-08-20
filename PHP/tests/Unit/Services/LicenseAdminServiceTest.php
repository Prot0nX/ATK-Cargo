<?php
// PHP/tests/Unit/Services/LicenseAdminServiceTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Exceptions\ApiException;
use App\Repositories\LicenseRepository;
use App\Services\LicenseAdminService;
use PHPUnit\Framework\MockObject\MockObject;
use PHPUnit\Framework\TestCase;

/**
 * تست‌های واحد منطق تجاری پنل مدیریت لایسنس.
 *
 * ریپازیتوری با mock جایگزین می‌شود (createMock سازنده را صدا نمی‌زند، پس
 * هیچ اتصال دیتابیسی برقرار نمی‌شود). AuditLogger عمداً stub نشده: خودش هر
 * Throwable را می‌بلعد، پس در نبود دیتابیس بی‌صدا رد می‌شود — دقیقاً همان
 * رفتاری که روی نصب‌های بدون جدول audit_log دارد.
 */
final class LicenseAdminServiceTest extends TestCase {
    /** @var LicenseRepository&MockObject */
    private $repository;
    private LicenseAdminService $service;

    protected function setUp(): void {
        $this->repository = $this->createMock(LicenseRepository::class);
        $this->service = new LicenseAdminService($this->repository);
    }

    /**
     * @param array<string,mixed> $overrides
     * @return array<string,mixed>
     */
    private function row(array $overrides = []): array {
        return $overrides + [
            'id' => 1,
            'license_key' => '0123456789ABCDEF0123456789ABCDEF',
            'company_name' => 'شرکت نمونه',
            'plan' => 'standard',
            'effective_status' => 'active',
            'is_active' => 1,
            'activation_date' => '2026-01-01 00:00:00',
            'expires_at' => null,
            'contact_name' => null,
            'contact_phone' => null,
            'contact_email' => null,
            'notes' => null,
            'created_at' => '2026-01-01 00:00:00',
            'updated_at' => '2026-01-01 00:00:00',
            'last_check' => null,
        ];
    }

    /**
     * mock را برای یک create موفق آماده می‌کند و ورودی رسیده به
     * repository::create را برای بازرسی برمی‌گرداند.
     *
     * @param array<string,mixed> $input
     * @return array{key:string,data:array<string,mixed>}
     */
    private function captureCreate(array $input): array {
        $captured = ['key' => '', 'data' => []];

        $this->repository->method('companyNameExists')->willReturn(false);
        $this->repository->method('keyExists')->willReturn(false);
        $this->repository->method('findById')->willReturn($this->row());
        $this->repository->expects($this->once())
            ->method('create')
            ->willReturnCallback(function (string $key, array $data) use (&$captured): int {
                $captured['key'] = $key;
                $captured['data'] = $data;
                return 1;
            });

        $this->service->create($input, 'tester');

        return $captured;
    }

    // --- قالب کلید ---------------------------------------------------------

    /**
     * قرارداد کلید نباید تغییر کند: LicenseController صریحاً strlen === 32 را
     * چک می‌کند و کلاینت اندروید کلید را کامپایل‌شده دارد.
     */
    public function testGeneratedKeyIs32UppercaseHexCharacters(): void {
        $captured = $this->captureCreate(['company_name' => 'شرکت الف']);

        $this->assertSame(32, strlen($captured['key']));
        $this->assertMatchesRegularExpression('/^[0-9A-F]{32}$/', $captured['key']);
    }

    public function testKeyGenerationRetriesOnCollision(): void {
        $this->repository->method('companyNameExists')->willReturn(false);
        $this->repository->method('findById')->willReturn($this->row());
        $this->repository->method('create')->willReturn(1);

        // اولین کلید تصادفی «تکراری» است، دومی آزاد.
        $this->repository->expects($this->exactly(2))
            ->method('keyExists')
            ->willReturnOnConsecutiveCalls(true, false);

        $this->service->create(['company_name' => 'شرکت ب'], 'tester');
    }

    // --- اعتبارسنجی نام شرکت ----------------------------------------------

    public function testEmptyCompanyNameIsRejected(): void {
        $this->expectException(ApiException::class);
        $this->service->create(['company_name' => '   '], 'tester');
    }

    public function testDuplicateCompanyNameIsRejectedWithConflictStatus(): void {
        $this->repository->method('companyNameExists')->willReturn(true);

        try {
            $this->service->create(['company_name' => 'شرکت تکراری'], 'tester');
            $this->fail('انتظار ApiException می‌رفت.');
        } catch (ApiException $e) {
            $this->assertSame(409, $e->getStatusCode());
        }
    }

    public function testCompanyNameIsTrimmed(): void {
        $captured = $this->captureCreate(['company_name' => '  شرکت ج  ']);
        $this->assertSame('شرکت ج', $captured['data']['company_name']);
    }

    // --- پلن ---------------------------------------------------------------

    public function testUnknownPlanIsRejected(): void {
        $this->repository->method('companyNameExists')->willReturn(false);

        try {
            $this->service->create(['company_name' => 'شرکت د', 'plan' => 'enterprise'], 'tester');
            $this->fail('انتظار ApiException می‌رفت.');
        } catch (ApiException $e) {
            $this->assertSame(422, $e->getStatusCode());
        }
    }

    public function testPlanDefaultsToStandardWhenAbsent(): void {
        $captured = $this->captureCreate(['company_name' => 'شرکت ه']);
        $this->assertSame('standard', $captured['data']['plan']);
    }

    // --- تاریخ انقضا -------------------------------------------------------

    public function testEmptyExpiryMeansUnlimited(): void {
        $captured = $this->captureCreate(['company_name' => 'شرکت و', 'expires_at' => '']);
        $this->assertNull($captured['data']['expires_at']);
    }

    public function testDateOnlyExpiryExtendsToEndOfDay(): void {
        $captured = $this->captureCreate(['company_name' => 'شرکت ز', 'expires_at' => '2027-03-20']);
        $this->assertSame('2027-03-20 23:59:59', $captured['data']['expires_at']);
    }

    /** ورودی <input type="datetime-local"> قالب `Y-m-d\TH:i` دارد. */
    public function testDatetimeLocalExpiryIsNormalized(): void {
        $captured = $this->captureCreate(['company_name' => 'شرکت ح', 'expires_at' => '2027-03-20T14:30']);
        $this->assertSame('2027-03-20 14:30:00', $captured['data']['expires_at']);
    }

    public function testMalformedExpiryIsRejected(): void {
        $this->repository->method('companyNameExists')->willReturn(false);

        $this->expectException(ApiException::class);
        $this->service->create(['company_name' => 'شرکت ط', 'expires_at' => 'فردا'], 'tester');
    }

    public function testImpossibleCalendarDateIsRejected(): void {
        $this->repository->method('companyNameExists')->willReturn(false);

        // createFromFormat خودش 2027-02-31 را به 3 مارس «سرریز» می‌کند؛ مقایسه‌ی
        // رفت‌وبرگشتی داخل normalizeExpiry همین را می‌گیرد.
        $this->expectException(ApiException::class);
        $this->service->create(['company_name' => 'شرکت ی', 'expires_at' => '2027-02-31'], 'tester');
    }

    /**
     * تاریخ گذشته عمداً مجاز است — راهی برای منقضی‌کردن فوری یک لایسنس یا
     * ثبت انقضای یک قرارداد تمام‌شده.
     */
    public function testPastExpiryIsAccepted(): void {
        $captured = $this->captureCreate(['company_name' => 'شرکت ک', 'expires_at' => '2020-01-01']);
        $this->assertSame('2020-01-01 23:59:59', $captured['data']['expires_at']);
    }

    // --- اطلاعات تماس -----------------------------------------------------

    public function testPersianDigitsInPhoneAreConvertedToLatin(): void {
        $captured = $this->captureCreate([
            'company_name' => 'شرکت ل',
            'contact_phone' => '۰۹۱۲۳۴۵۶۷۸۹',
        ]);
        $this->assertSame('09123456789', $captured['data']['contact_phone']);
    }

    public function testInvalidPhoneIsRejected(): void {
        $this->repository->method('companyNameExists')->willReturn(false);

        $this->expectException(ApiException::class);
        $this->service->create([
            'company_name' => 'شرکت م',
            'contact_phone' => 'تماس بگیرید',
        ], 'tester');
    }

    public function testInvalidEmailIsRejected(): void {
        $this->repository->method('companyNameExists')->willReturn(false);

        $this->expectException(ApiException::class);
        $this->service->create([
            'company_name' => 'شرکت ن',
            'contact_email' => 'not-an-email',
        ], 'tester');
    }

    public function testBlankOptionalFieldsBecomeNull(): void {
        $captured = $this->captureCreate([
            'company_name' => 'شرکت س',
            'contact_name' => '  ',
            'contact_phone' => '',
            'contact_email' => '',
            'notes' => '   ',
        ]);

        $this->assertNull($captured['data']['contact_name']);
        $this->assertNull($captured['data']['contact_phone']);
        $this->assertNull($captured['data']['contact_email']);
        $this->assertNull($captured['data']['notes']);
    }

    /** کلیدهای ناشناخته‌ی ورودی نباید به لایه‌ی دیتابیس برسند. */
    public function testUnknownInputKeysAreDropped(): void {
        $captured = $this->captureCreate([
            'company_name' => 'شرکت ع',
            'is_active' => 0,
            'id' => 999,
            'license_key' => 'ATTACKER_SUPPLIED_KEY',
        ]);

        $this->assertArrayNotHasKey('is_active', $captured['data']);
        $this->assertArrayNotHasKey('id', $captured['data']);
        $this->assertArrayNotHasKey('license_key', $captured['data']);
    }

    // --- نمایش ردیف --------------------------------------------------------

    public function testPresentedRowCarriesEffectiveStatusAndPlanLabel(): void {
        $this->repository->method('listAll')->willReturn([
            $this->row(['effective_status' => 'expired', 'plan' => 'pro', 'expires_at' => '2020-01-01 00:00:00']),
        ]);

        $rows = $this->service->list(null, null);

        $this->assertCount(1, $rows);
        $this->assertSame('expired', $rows[0]['status']);
        $this->assertSame('حرفه‌ای', $rows[0]['plan_label']);
        $this->assertTrue($rows[0]['is_active']);
    }

    /** فیلتر وضعیت ناشناخته باید نادیده گرفته شود، نه اینکه به کوئری برسد. */
    public function testUnknownStatusFilterIsIgnored(): void {
        $this->repository->expects($this->once())
            ->method('listAll')
            ->with('متن', null)
            ->willReturn([]);

        $this->service->list('متن', 'bogus');
    }

    public function testKnownStatusFilterIsForwarded(): void {
        $this->repository->expects($this->once())
            ->method('listAll')
            ->with(null, 'expired')
            ->willReturn([]);

        $this->service->list(null, 'expired');
    }

    // --- عملیات روی رکورد موجود -------------------------------------------

    public function testToggleFlipsActiveFlag(): void {
        $this->repository->method('findById')->willReturn($this->row(['is_active' => 1]));
        $this->repository->expects($this->once())->method('setActive')->with(1, false);

        $this->service->toggle(1, 'tester');
    }

    public function testMissingLicenseYields404(): void {
        $this->repository->method('findById')->willReturn(null);

        try {
            $this->service->toggle(42, 'tester');
            $this->fail('انتظار ApiException می‌رفت.');
        } catch (ApiException $e) {
            $this->assertSame(404, $e->getStatusCode());
        }
    }

    public function testDeleteOfMissingRowYields404(): void {
        $this->repository->method('findById')->willReturn($this->row());
        $this->repository->method('delete')->willReturn(false);

        try {
            $this->service->delete(1, 'tester');
            $this->fail('انتظار ApiException می‌رفت.');
        } catch (ApiException $e) {
            $this->assertSame(404, $e->getStatusCode());
        }
    }

    /** هنگام ویرایش، خودِ رکورد نباید «نام تکراری» شمرده شود. */
    public function testUpdateExcludesOwnIdFromDuplicateCheck(): void {
        $this->repository->method('findById')->willReturn($this->row());
        $this->repository->expects($this->once())
            ->method('companyNameExists')
            ->with('شرکت نمونه', 7)
            ->willReturn(false);

        $this->service->update(7, ['company_name' => 'شرکت نمونه'], 'tester');
    }
}
