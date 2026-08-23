<?php
// PHP/tests/Unit/Services/PermissionServiceTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Services;

use App\Services\PermissionService;
use PHPUnit\Framework\TestCase;

// تست‌های integration-محور روی فایل واقعی config/permissions.json برای منطق fallback نقش به کاربر
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
 // اولویت‌بندی override کاربر روی مجوزهای نقش با داده‌ی موقت بررسی می‌شود.
        $reflection = new \ReflectionClass(PermissionService::class);
        $file = $reflection->getConstant('PERMISSIONS_FILE');
        $original = file_get_contents($file);

        try {
            $withUserOverride = $this->permissionsData;
            $withUserOverride['users']['custom_user'] = ['manage_users' => true];
            file_put_contents($file, json_encode($withUserOverride));

 // apcu در این محیط فعال نیست، پس نیازی به forget کردن کش نیست.
            $service = new PermissionService();
            $this->assertTrue($service->hasPermission('custom_user', 'operator', 'manage_users'));

 // کاربری که override اختصاصی ندارد باید همچنان مجوز نقش خودش را بگیرد.
            $this->assertFalse($service->hasPermission('other_operator', 'operator', 'manage_users'));
        } finally {
            file_put_contents($file, $original);
        }
    }
}
