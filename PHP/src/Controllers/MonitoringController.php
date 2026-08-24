<?php
// PHP/src/Controllers/MonitoringController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Exceptions\ApiException;
use App\Repositories\MonitoringRepository;

// نمایش/تایید رویدادهای مانیتورینگ — فقط REST داخلی؛ داشبورد وب/بخش اندروید مصرف‌کننده‌ی فازهای بعدی‌اند، نه بخشی از این فاز.
class MonitoringController {
    private MonitoringRepository $repository;

    public function __construct(?MonitoringRepository $repository = null) {
        $this->repository = $repository ?? new MonitoringRepository();
    }

 /** @return array<int,array<string,mixed>> */
    public function listEvents(string $status, int $limit, ?int $beforeId): array {
        $status = in_array($status, ['open', 'acknowledged', 'all'], true) ? $status : 'open';
        return array_map([$this, 'presentEvent'], $this->repository->listEvents($status, $limit, $beforeId));
    }

    public function acknowledge(int $id, string $username): void {
        $event = $this->repository->findById($id);
        if ($event === null) {
            throw new ApiException('رویداد مورد نظر یافت نشد', 404);
        }
        if ($event['acknowledged_at'] !== null) {
            throw new ApiException('این رویداد قبلاً تایید شده است', 409);
        }
        $this->repository->acknowledge($id, $username);
    }

    public function delete(int $id): void {
        if (!$this->repository->delete($id)) {
            throw new ApiException('رویداد مورد نظر یافت نشد', 404);
        }
    }

 /** @return array<string,mixed> */
    public function summary(): array {
 // خودِ وضعیت سلامت زنده محاسبه می‌شود (همان evaluateHealth که health_monitor.php هم صدا می‌زند) نه از یک ردیف ذخیره‌شده، تا جدول monitoring_events با heartbeat هر ۵ دقیقه شلوغ نشود.
        $health = (new DiagnosticsController())->evaluateHealth();

        return [
            'openAlerts' => $this->repository->openCounts(),
            'health' => [
                'healthy' => $health['healthy'],
                'status' => $health['status'],
                'missingTables' => $health['missingTables'],
                'checkedAt' => date('Y-m-d H:i:s'),
            ],
        ];
    }

 /** @param array<string,mixed> $row @return array<string,mixed> */
    private function presentEvent(array $row): array {
        return [
            'id' => (int)$row['id'],
            'eventType' => (string)$row['event_type'],
            'severity' => (string)$row['severity'],
            'message' => (string)$row['message'],
            'source' => (string)$row['source'],
            'dedupeKey' => $row['dedupe_key'],
            'createdAt' => $row['created_at'],
            'acknowledgedAt' => $row['acknowledged_at'],
            'acknowledgedBy' => $row['acknowledged_by'],
        ];
    }
}