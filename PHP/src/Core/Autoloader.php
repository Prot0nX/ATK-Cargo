<?php

declare(strict_types=1);

namespace AtkCargo\Core;

/**
 * PSR-4 Autoloader implementation for AtkCargo namespace
 */
class Autoloader
{
    /**
     * Register autoloader with SPL autoloader stack.
     */
    public static function register(): void
    {
        spl_autoload_register(function (string $class) {
            $prefix = 'AtkCargo\\';
            $baseDir = dirname(__DIR__) . '/'; // This points to PHP/src/

            // Check if class uses the prefix
            $len = strlen($prefix);
            if (strncmp($prefix, $class, $len) !== 0) {
                return;
            }

            // Get relative class name
            $relativeClass = substr($class, $len);

            // Replace namespace separators with directory separators, append .php
            $file = $baseDir . str_replace('\\', '/', $relativeClass) . '.php';

            // If the file exists, require it
            if (file_exists($file)) {
                require $file;
            }
        });
    }
}
