<?php

declare(strict_types=1);

namespace AtkCargo\Repositories;

use AtkCargo\Core\Database;
use PDO;
use Exception;

/**
 * Repository layer for Users Database Queries using PDO
 */
class UserRepository
{
    private PDO $db;

    public function __construct()
    {
        $this->db = Database::getInstance()->getPdo();
    }

    /**
     * Get all users sorted by creation date descending
     */
    public function getAllUsers(): array
    {
        $stmt = $this->db->prepare("SELECT id, username, fullName, userType, created_at, updated_at FROM Users ORDER BY created_at DESC");
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    /**
     * Get user by ID
     */
    public function getUserById(int $id): ?array
    {
        $stmt = $this->db->prepare("SELECT id, username, fullName, userType FROM Users WHERE id = ? LIMIT 1");
        $stmt->execute([$id]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    /**
     * Get user by username
     */
    public function getUserByUsername(string $username): ?array
    {
        $stmt = $this->db->prepare("SELECT id, username, fullName, userType FROM Users WHERE username = ? LIMIT 1");
        $stmt->execute([$username]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    /**
     * Check if a username is taken by another user
     */
    public function isUsernameTaken(string $username, ?int $excludeId = null): bool
    {
        $query = "SELECT COUNT(*) FROM Users WHERE username = ?";
        $params = [$username];

        if ($excludeId !== null) {
            $query .= " AND id != ?";
            $params[] = $excludeId;
        }

        $stmt = $this->db->prepare($query);
        $stmt->execute($params);
        return ((int)$stmt->fetchColumn()) > 0;
    }

    /**
     * Count total system administrators
     */
    public function countAdmins(): int
    {
        $stmt = $this->db->prepare("SELECT COUNT(*) FROM Users WHERE userType = 'admin'");
        $stmt->execute();
        return (int)$stmt->fetchColumn();
    }

    /**
     * Create a new user
     */
    public function createUser(string $username, string $fullName, string $password, string $userType): int
    {
        $stmt = $this->db->prepare("
            INSERT INTO Users (username, fullName, password, userType, created_at, updated_at) 
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ");
        $stmt->execute([$username, $fullName, $password, $userType]);
        return (int)$this->db->lastInsertId();
    }

    /**
     * Update user details dynamically
     */
    public function updateUser(int $id, array $fields): bool
    {
        if (empty($fields)) {
            return false;
        }

        $sets = [];
        $params = [];

        foreach ($fields as $key => $val) {
            $sets[] = "{$key} = ?";
            $params[] = $val;
        }

        $params[] = $id;
        $setsStr = implode(", ", $sets);

        $stmt = $this->db->prepare("UPDATE Users SET {$setsStr}, updated_at = CURRENT_TIMESTAMP WHERE id = ?");
        return $stmt->execute($params);
    }

    /**
     * Delete user by ID
     */
    public function deleteUser(int $id): bool
    {
        $stmt = $this->db->prepare("DELETE FROM Users WHERE id = ?");
        return $stmt->execute([$id]);
    }
}
