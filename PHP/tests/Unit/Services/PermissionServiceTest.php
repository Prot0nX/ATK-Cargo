<?php
// PHP/tests/Unit/Services/PermissionServiceTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Services\PermissionService;
use PHPUnit\Framework\TestCase;

/**
 * PermissionService::PERMISSIONS_FILE یک private const مبتنی بر __DIR__ است
 * (نه پارامتر قابل تزریق)، پس این تست‌ها مستقیماً روی config/permissions.json
 * واقعی اجرا می‌شوند — یک تست integration-محور برای منطق fallback نقش→کاربر،
 * نه mock کامل. APCu در محیط تست فعال نیست، پس MicroCache::remember هر بار
 * واقعاً فایل را می‌خواند (نتیجه بین تست‌ها cache نمی‌شود).
 */
final class PermissionServiceTest extends TestCase {
    private PermissionService $service;
    /** @var array<string, mixed> */
    private array $permissionsData;

    protected function setUp(): void {
        $this->service = new PermissionService();
        $file = dirname(__DIR__, 3) . '/config/permissions.json';
        $this->permissionsData = json_decode((string)file_get_contents($file), true);
    }

    public function testAdminRoleHasAllPermissionsGrantedInConfigFile(): void {
        $adminPermissions = $this->permissionsData['roles']['admin'];

        foreach ($adminPermissions as $feature => $expected) {
            $this->assertSame(
                $expected,
                $this->service->hasPermission('some_admin_user', 'admin', $feature),
                "Feature '$feature' for role 'admin' did not match config file"
            );
        }
    }

    public function testOperatorRoleHasRestrictedPermissionsAsPerConfigFile(): void {
        $operatorPermissions = $this->permissionsData['roles']['operator'];

        foreach ($operatorPermissions as $feature => $expected) {
            $this->assertSame(
                $expected,
                $this->service->hasPermission('some_operator_user', 'operator', $feature),
                "Feature '$feature' for role 'operator' did not match config file"
            );
        }
    }

    public function testUnknownFeatureNameReturnsFalse(): void {
        $this->assertFalse(
            $this->service->hasPermission('some_admin_user', 'admin', 'a_feature_that_does_not_exist')
        );
    }

    public function testUnknownUserTypeReturnsEmptyPermissionSet(): void {
        $permissions = $this->service->getUserPermissions('someone', 'a_role_that_does_not_exist');

        $this->assertSame([], $permissions);
    }

    public function testUnknownUserTypeMeansNoPermissionIsGranted(): void {
        $this->assertFalse(
            $this->service->hasPermission('someone', 'a_role_that_does_not_exist', 'manage_users')
        );
    }

    public function testPerUserOverrideTakesPrecedenceOverRolePermissions(): void {
        // config فعلی users را خالی نگه می‌دارد؛ این تست مستقیماً منطق
        // اولویت‌بندی getUserPermissions را با override موقت داده بررسی
        // می‌کند تا وابسته به محتوای فعلی فایل نباشد.
        $reflection = new \ReflectionClass(PermissionService::class);
        $file = $reflection->getConstant('PERMISSIONS_FILE');
        $original = file_get_contents($file);

        try {
            $withUserOverride = $this->permissionsData;
            $withUserOverride['users']['custom_user'] = ['manage_users' => true];
            file_put_contents($file, json_encode($withUserOverride));

            // MicroCache::remember فقط وقتی apcu فعال باشد کش می‌کند؛ این
            // محیط apcu ندارد، پس نیازی به forget کردن کش نیست.
            $service = new PermissionService();
            $this->assertTrue($service->hasPermission('custom_user', 'operator', 'manage_users'));

            // کاربری که override اختصاصی ندارد باید همچنان مجوز نقش خودش را بگیرد.
            $this->assertFalse($service->hasPermission('other_operator', 'operator', 'manage_users'));
        } finally {
            file_put_contents($file, $original);
        }
    }
}
