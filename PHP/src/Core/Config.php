<?php

declare(strict_types=1);

namespace AtkCargo\Core;

/**
 * Configuration Manager class
 */
class Config
{
    private static bool $initialized = false;
    private static array $settings = [];

    /**
     * Initialize configurations by loading old config or environment variables
     */
    public static function init(): void
    {
        if (self::$initialized) {
            return;
        }

        // 1. Try to load the traditional config file if it exists to maintain compatibility
        $traditionalConfig = dirname(dirname(__DIR__)) . '/config/config.php';
        if (file_exists($traditionalConfig)) {
            require_once $traditionalConfig;
        }

        // 2. Define defaults or load from constants/env
        self::$settings = [
            'db' => [
                'host' => defined('DB_HOST') ? DB_HOST : (getenv('DB_HOST') ?: 'localhost'),
                'name' => defined('DB_NAME') ? DB_NAME : (getenv('DB_NAME') ?: 'myapp_db'),
                'user' => defined('DB_USER') ? DB_USER : (getenv('DB_USER') ?: 'myapp_user'),
                'password' => defined('DB_PASSWORD') ? DB_PASSWORD : (getenv('DB_PASSWORD') ?: ''),
            ],
            'timezone' => 'Asia/Tehran',
            'session_timeout' => 86400, // 24 hours
        ];

        self::$initialized = true;
    }

    /**
     * Get a configuration value by dot-notation key
     * 
     * @param string $key e.g., 'db.host'
     * @param mixed $default Default value if key is not found
     * @return mixed
     */
    public static function get(string $key, $default = null)
    {
        self::init();

        $parts = explode('.', $key);
        $current = self::$settings;

        foreach ($parts as $part) {
            if (!is_array($current) || !isset($current[$part])) {
                return $default;
            }
            $current = $current[$part];
        }

        return $current;
    }
}
