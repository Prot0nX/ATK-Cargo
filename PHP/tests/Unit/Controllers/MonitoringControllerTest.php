<?php
// PHP/tests/Unit/Controllers/MonitoringControllerTest.php

declare(strict_types=1);

namespace App\Tests\Unit\Controllers;

use App\Controllers\MonitoringController;
use App\Exceptions\ApiException;
use App\Repositories\MonitoringRepository;
use PHPUnit\Framework\MockObject\MockObject;
use PHPUnit\Framework\TestCase;

// تست‌های واحد منطق MonitoringController با ریپازیتوری mock شده
final class MonitoringControllerTest extends TestCase {
    /** @var MonitoringRepository&MockObject */
    private $repository;
    private MonitoringController $controller;

    protected function setUp(): void {
        $this->repository = $this->createMock(MonitoringRepository::class);
        $this->controller = new MonitoringController($this->repository);
    }

    /** @param array<string,mixed> $overrides @return array<string,mixed> */
    private function row(array $overrides = []): array {
        return $overrides + [
            'id' => 1,
            'event_type' => 'HEALTH_CHECK_FAILED',
            'severity' => 'critical',
            'message' => 'اتصال دیتابیس برقرار نشد.',
            'source' => 'health_check',
            'dedupe_key' => 'HEALTH_CHECK_FAILED',
            'acknowledged_at' => null,
            'acknowledged_by' => null,
            'created_at' => '2026-08-22 10:00:00',
        ];
    }

    // --- acknowledge ---------------------------------------------------------

    public function testAcknowledgeThrows404WhenEventNotFound(): void {
        $this->repository->method('findById')->willReturn(null);

        try {
            $this->controller->acknowledge(999, 'admin1');
            $this->fail('انتظار ApiException می‌رفت.');
        } catch (ApiException $e) {
            $this->assertSame(404, $e->getStatusCode());
        }
    }

    public function testAcknowledgeThrows409WhenAlreadyAcknowledged(): void {
        $this->repository->method('findById')->willReturn(
            $this->row(['acknowledged_at' => '2026-08-22 11:00:00', 'acknowledged_by' => 'admin0'])
        );

        try {
            $this->controller->acknowledge(1, 'admin1');
            $this->fail('انتظار ApiException می‌رفت.');
        } catch (ApiException $e) {
            $this->assertSame(409, $e->getStatusCode());
        }
    }

    public function testAcknowledgeCallsRepositoryOnHappyPath(): void {
        $this->repository->method('findById')->willReturn($this->row());
        $this->repository->expects($this->once())
            ->method('acknowledge')
            ->with(1, 'admin1')
            ->willReturn(true);

        $this->controller->acknowledge(1, 'admin1');
    }

    // --- listEvents ------------------------------------------------------------

    public function testUnknownStatusFilterDefaultsToOpen(): void {
        $this->repository->expects($this->once())
            ->method('listEvents')
            ->with('open', 50, null)
            ->willReturn([]);

        $this->controller->listEvents('not-a-real-status', 50, null);
    }

    public function testValidStatusFilterIsPassedThrough(): void {
        $this->repository->expects($this->once())
            ->method('listEvents')
            ->with('acknowledged', 10, 5)
            ->willReturn([]);

        $this->controller->listEvents('acknowledged', 10, 5);
    }

    public function testListEventsMapsRowToCamelCaseResponse(): void {
        $this->repository->method('listEvents')->willReturn([$this->row()]);

        $events = $this->controller->listEvents('open', 50, null);

        $this->assertSame([
            'id' => 1,
            'eventType' => 'HEALTH_CHECK_FAILED',
            'severity' => 'critical',
            'message' => 'اتصال دیتابیس برقرار نشد.',
            'source' => 'health_check',
            'dedupeKey' => 'HEALTH_CHECK_FAILED',
            'createdAt' => '2026-08-22 10:00:00',
            'acknowledgedAt' => null,
            'acknowledgedBy' => null,
        ], $events[0]);
    }

    // --- summary -----------------------------------------------------------

    public function testSummaryIncludesOpenCountsFromRepository(): void {
        $this->repository->method('openCounts')->willReturn([
            'open_total' => 3,
            'open_critical' => 1,
            'open_warning' => 2,
            'open_info' => 0,
        ]);

        $summary = $this->controller->summary();

        $this->assertSame([
            'open_total' => 3,
            'open_critical' => 1,
            'open_warning' => 2,
            'open_info' => 0,
        ], $summary['openAlerts']);
        $this->assertArrayHasKey('health', $summary);
        $this->assertArrayHasKey('healthy', $summary['health']);
        $this->assertArrayHasKey('checkedAt', $summary['health']);
    }
}
