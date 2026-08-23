<?php
// PHP/src/Controllers/AuditLogController.php

declare(strict_types=1);

namespace App\Controllers;

use App\Exceptions\ApiException;
use App\Repositories\AuditLogRepository;

// نمایش لاگ تغییرات (audit_log) — فقط REST خواندن؛ نوشتن از App\Services\AuditLogger::log() انجام می‌شود
class AuditLogController {
    private AuditLogRepository $repository;

    public function __construct(?AuditLogRepository $repository = null) {
        $this->repository = $repository ?? new AuditLogRepository();
    }

    /** @return array<int,array<string,mixed>> */
    public function listLogs(int $limit, ?int $beforeId, ?string $username, ?string $entityType): array {
        return array_map([$this, 'presentLog'], $this->repository->listLogs($limit, $beforeId, $username, $entityType));
    }

    public function delete(int $id): void {
        if (!$this->repository->delete($id)) {
            throw new ApiException('لاگ مورد نظر یافت نشد', 404);
        }
    }

    /**
     * @param array<string,mixed> $row
     * @return array<string,mixed>
     */
    private function presentLog(array $row): array {
        $detailsRaw = $row['details'] ?? null;
        $details = $detailsRaw !== null ? json_decode((string)$detailsRaw, true) : null;

        return [
            'id' => (int)$row['id'],
            'username' => (string)$row['username'],
            'action' => (string)$row['action'],
            'entityType' => (string)$row['entity_type'],
            'entityId' => (string)$row['entity_id'],
            'details' => $details,
            'createdAt' => $row['created_at'],
        ];
    }
}
