<?php

declare(strict_types=1);

namespace AtkCargo\Helpers;

/**
 * Jalali/Gregorian Date conversion helper wrapping traditional jdf
 */
class DateHelper
{
    /**
     * Convert Gregorian date to Jalali
     */
    public static function gregorianToJalali(int $gy, int $gm, int $gd): array
    {
        // Load traditional functions if not loaded
        self::ensureJdfLoaded();
        return gregorian_to_jalali($gy, $gm, $gd);
    }

    /**
     * Convert Jalali date to Gregorian
     */
    public static function jalaliToGregorian(int $jy, int $jm, int $jd): array
    {
        self::ensureJdfLoaded();
        return jalali_to_gregorian($jy, $jm, $jd);
    }

    /**
     * Format current or custom time as Jalali date string
     */
    public static function formatJalali(string $format, ?int $timestamp = null): string
    {
        self::ensureJdfLoaded();
        return jdate($format, $timestamp ?? time());
    }

    /**
     * Get current Jalali date in Y/m/d format
     */
    public static function getCurrentJalaliDate(): string
    {
        return self::formatJalali('Y/m/d');
    }

    /**
     * Helper to load legacy jdf script
     */
    private static function ensureJdfLoaded(): void
    {
        if (!function_exists('jdate')) {
            $legacyJdf = dirname(dirname(__DIR__)) . '/jdf.php';
            if (file_exists($legacyJdf)) {
                require_once $legacyJdf;
            }
        }
    }
}
