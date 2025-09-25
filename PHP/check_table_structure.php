<?php
require_once __DIR__ . '/PHP/config/config.php';

try {
    $pdo = new PDO(
        "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4", 
        DB_USER, 
        DB_PASSWORD,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]
    );
    
    // نمایش ساختار جدول user_sessions
    echo "=== ساختار جدول user_sessions ===\n";
    $stmt = $pdo->prepare("SHOW CREATE TABLE user_sessions");
    $stmt->execute();
    $result = $stmt->fetch();
    echo $result['Create Table'] . "\n\n";
    
    // نمایش indexes و constraints
    echo "=== Indexes و Constraints ===\n";
    $stmt = $pdo->prepare("SHOW INDEX FROM user_sessions");
    $stmt->execute();
    $indexes = $stmt->fetchAll();
    
    foreach ($indexes as $index) {
        echo "Key: {$index['Key_name']}, Column: {$index['Column_name']}, Unique: {$index['Non_unique']}\n";
    }
    
} catch (Exception $e) {
    echo "خطا: " . $e->getMessage() . "\n";
}
?>