<?php

declare(strict_types=1);

namespace AtkCargo\Core;

/**
 * Request Handler class to parse and sanitize HTTP input safely
 */
class Request
{
    private string $method;
    private array $params = [];
    private ?array $jsonBody = null;

    public function __construct()
    {
        $this->method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
        $this->parseInput();
    }

    /**
     * Parse incoming parameters from GET, POST, and raw JSON body
     */
    private function parseInput(): void
    {
        // 1. Load GET parameters
        foreach ($_GET as $key => $value) {
            $this->params[$key] = $value;
        }

        // 2. Load POST parameters
        foreach ($_POST as $key => $value) {
            $this->params[$key] = $value;
        }

        // 3. Load JSON Body if Content-Type is application/json or body is json
        $rawBody = file_get_contents('php://input');
        if (!empty($rawBody)) {
            try {
                $decoded = json_decode($rawBody, true, 512, JSON_THROW_ON_ERROR);
                if (is_array($decoded)) {
                    $this->jsonBody = $decoded;
                    $this->params = array_merge($this->params, $decoded);
                }
            } catch (\JsonException $e) {
                // Not JSON or invalid JSON - ignore and let validator handle if needed
            }
        }
    }

    /**
     * Get Request HTTP Method
     */
    public function getMethod(): string
    {
        return $this->method;
    }

    /**
     * Get parameter by key
     * 
     * @param string $key
     * @param mixed $default Default value if key is not set
     * @return mixed
     */
    public function get(string $key, $default = null)
    {
        return $this->params[$key] ?? $default;
    }

    /**
     * Get all parameters
     */
    public function all(): array
    {
        return $this->params;
    }

    /**
     * Sanitize input parameter for standard safe string rendering
     */
    public function getString(string $key, string $default = ''): string
    {
        $val = $this->get($key, $default);
        if ($val === null || is_array($val)) {
            return $default;
        }
        return trim((string)$val);
    }

    /**
     * Get input parameter as float
     */
    public function getFloat(string $key, float $default = 0.0): float
    {
        $val = $this->get($key);
        if ($val === null || $val === '' || is_array($val)) {
            return $default;
        }
        return (float)$val;
    }

    /**
     * Get input parameter as int
     */
    public function getInt(string $key, int $default = 0): int
    {
        $val = $this->get($key);
        if ($val === null || $val === '' || is_array($val)) {
            return $default;
        }
        return (int)$val;
    }

    /**
     * Get input parameter as bool
     */
    public function getBool(string $key, bool $default = false): bool
    {
        $val = $this->get($key);
        if ($val === null || is_array($val)) {
            return $default;
        }
        if ($val === 'false' || $val === '0' || $val === 0 || $val === false) {
            return false;
        }
        if ($val === 'true' || $val === '1' || $val === 1 || $val === true) {
            return true;
        }
        return (bool)$val;
    }
}
