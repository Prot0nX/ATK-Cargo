<?php
declare(strict_types=1);

session_start();

header('Content-Type: application/json; charset=UTF-8');

$is_authenticated = isset($_SESSION['perm_manager_auth']) && $_SESSION['perm_manager_auth'] === true;
if (!$is_authenticated) {
    http_response_code(401);
    echo json_encode([
        'success' => false,
        'error' => 'دسترسی غیرمجاز: برای دیدن مدیریت فایل‌ها نیاز به ورود به سیستم است.'
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function scanPHPFiles($directory = '.') {
    $phpFiles = [];
    
    try {
        $files = scandir($directory);
        
        foreach ($files as $file) {
            if ($file === '.' || $file === '..') {
                continue;
            }
            
            $fullPath = $directory . DIRECTORY_SEPARATOR . $file;
            
            // Check if it's a PHP file
            if (is_file($fullPath) && pathinfo($file, PATHINFO_EXTENSION) === 'php') {
                $phpFiles[] = [
                    'name' => $file,
                    'size' => filesize($fullPath),
                    'modified' => filemtime($fullPath),
                    'path' => $fullPath
                ];
            }
        }
        
        // Sort files by name
        usort($phpFiles, function($a, $b) {
            return strcmp($a['name'], $b['name']);
        });
        
        return $phpFiles;
        
    } catch (Exception $e) {
        throw new Exception("Error scanning directory: " . $e->getMessage());
    }
}

function logAccess($action, $file = null, $clientIP = null) {
    // Ensure log directory exists
    if (!is_dir('log')) {
        mkdir('log', 0755, true);
    }
    
    $logFile = 'log/access.log';
    $timestamp = date('Y-m-d H:i:s');
    $ip = $clientIP ?: ($_SERVER['REMOTE_ADDR'] ?? 'unknown');
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
    
    $logEntry = "[$timestamp] IP: $ip | Action: $action";
    if ($file) {
        $logEntry .= " | File: $file";
    }
    $logEntry .= " | User-Agent: $userAgent\n";
    
    file_put_contents($logFile, $logEntry, FILE_APPEND | LOCK_EX);
}

try {
    // Log the file listing access
    logAccess('LIST_FILES');
    
    $phpFiles = scanPHPFiles();
    
    echo json_encode([
        'success' => true,
        'files' => $phpFiles,
        'count' => count($phpFiles),
        'timestamp' => time()
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage(),
        'timestamp' => time()
    ], JSON_PRETTY_PRINT);
}
?>