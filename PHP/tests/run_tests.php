<?php
// PHP/tests/run_tests.php
// اسکریپت اختصاصی اجرای خودکار تست‌های واحد پروژه ATK-Cargo بدون نیاز به Composer

declare(strict_types=1);

require_once dirname(__DIR__) . '/src/bootstrap.php';

// Enable assertions
ini_set('zend.assertions', '1');
assert_options(ASSERT_ACTIVE, 1);
assert_options(ASSERT_EXCEPTION, 1);

echo "========================================================\n";
echo "   ATK-Cargo PHP Automated Test Suite Runner\n";
echo "========================================================\n\n";

$testFiles = [
    __DIR__ . '/SessionServiceTest.php',
    __DIR__ . '/CargoServiceTest.php'
];

$passedCount = 0;
$failedCount = 0;

foreach ($testFiles as $file) {
    if (!file_exists($file)) continue;
    require_once $file;

    $className = 'App\\Tests\\' . pathinfo($file, PATHINFO_FILENAME);
    if (!class_exists($className)) continue;

    echo "Running Test Suite: {$className}\n";
    echo "--------------------------------------------------------\n";

    $instance = new $className();
    $methods = get_class_methods($instance);

    foreach ($methods as $method) {
        if (strpos($method, 'test') === 0) {
            try {
                $instance->$method();
                echo "  [PASS] {$method}\n";
                $passedCount++;
            } catch (\Throwable $e) {
                echo "  [FAIL] {$method}: " . $e->getMessage() . "\n";
                $failedCount++;
            }
        }
    }
    echo "\n";
}

echo "========================================================\n";
echo "SUMMARY: Total Passed: {$passedCount} | Total Failed: {$failedCount}\n";
echo "========================================================\n";

exit($failedCount === 0 ? 0 : 1);
