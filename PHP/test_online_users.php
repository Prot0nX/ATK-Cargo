<?php
// تنظیم منطقه زمانی تهران
date_default_timezone_set('Asia/Tehran');

require_once __DIR__ . '/SessionManager.php';
require_once __DIR__ . '/jdf.php';

// تنظیم هدرهای HTTP
header('Content-Type: text/html; charset=utf-8');

// تابع ایجاد داده‌های نمونه برای تست
function createSampleData() {
    try {
        $sessionManager = new SessionManager();
        
        // داده‌های نمونه کاربران
        $sampleUsers = [
            [
                'username' => 'admin_test',
                'device_id' => 'DEVICE_001_ADMIN',
                'device_model' => 'Samsung Galaxy S21',
                'android_version' => '12.0',
                'ip_address' => '192.168.1.100',
                'userType' => 'admin'
            ],
            [
                'username' => 'operator_1',
                'device_id' => 'DEVICE_002_OP',
                'device_model' => 'Xiaomi Redmi Note 10',
                'android_version' => '11.0',
                'ip_address' => '192.168.1.101',
                'userType' => 'operator'
            ],
            [
                'username' => 'verifier_1',
                'device_id' => 'DEVICE_003_VER',
                'device_model' => 'Huawei P30',
                'android_version' => '10.0',
                'ip_address' => '192.168.1.102',
                'userType' => 'verifier'
            ],
            [
                'username' => 'operator_2',
                'device_id' => 'DEVICE_004_OP2',
                'device_model' => 'iPhone 13',
                'android_version' => '15.0',
                'ip_address' => '192.168.1.103',
                'userType' => 'operator'
            ]
        ];
        
        $results = [];
        
        foreach ($sampleUsers as $user) {
            try {
                $result = $sessionManager->createSession(
                    $user['username'],
                    $user['device_id'],
                    $user['device_model'],
                    $user['android_version'],
                    $user['ip_address'],
                    $user['userType']
                );
                
                $results[] = [
                    'user' => $user['username'],
                    'success' => $result['success'],
                    'message' => $result['message']
                ];
                
                // شبیه‌سازی فعالیت‌های مختلف
                if ($result['success']) {
                    // برخی کاربران را بیکار کن
                    if (in_array($user['username'], ['operator_2'])) {
                        // شبیه‌سازی عدم فعالیت (5 دقیقه پیش)
                        $sessionManager->updateLastActivity($user['username'], $user['device_id']);
                    }
                }
                
            } catch (Exception $e) {
                $results[] = [
                    'user' => $user['username'],
                    'success' => false,
                    'message' => $e->getMessage()
                ];
            }
        }
        
        return $results;
        
    } catch (Exception $e) {
        return ['error' => $e->getMessage()];
    }
}

// بررسی درخواست
$action = $_GET['action'] ?? 'show_page';

if ($action === 'create_sample_data') {
    $results = createSampleData();
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($results, JSON_UNESCAPED_UNICODE);
    exit;
}

// نمایش صفحه تست
?>
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تست سیستم مدیریت کاربران آنلاین</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body { font-family: 'Tahoma', 'Arial', sans-serif; }
    </style>
</head>
<body class="bg-gray-100 font-sans">
    <div class="container mx-auto px-4 py-8">
        <div class="max-w-4xl mx-auto">
            <!-- Header -->
            <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
                <h1 class="text-3xl font-bold text-gray-800 mb-4 flex items-center">
                    <i class="fas fa-cogs text-blue-500 ml-3"></i>
                    تست سیستم مدیریت کاربران آنلاین
                </h1>
                <p class="text-gray-600">این صفحه برای تست و بررسی عملکرد سیستم مدیریت کاربران آنلاین طراحی شده است.</p>
            </div>

            <!-- Actions -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                <div class="bg-white rounded-lg shadow-lg p-6">
                    <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
                        <i class="fas fa-database text-green-500 ml-2"></i>
                        ایجاد داده‌های نمونه
                    </h2>
                    <p class="text-gray-600 mb-4">برای تست سیستم، داده‌های نمونه کاربران ایجاد کنید.</p>
                    <button id="createSampleBtn" class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center">
                        <i class="fas fa-plus ml-2"></i>
                        ایجاد داده‌های نمونه
                    </button>
                    <div id="sampleResult" class="mt-4 hidden"></div>
                </div>

                <div class="bg-white rounded-lg shadow-lg p-6">
                    <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
                        <i class="fas fa-users text-blue-500 ml-2"></i>
                        مشاهده سیستم مدیریت
                    </h2>
                    <p class="text-gray-600 mb-4">سیستم مدیریت کاربران آنلاین را مشاهده کنید.</p>
                    <a href="online_users_manager.html" target="_blank" class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors inline-flex items-center">
                        <i class="fas fa-external-link-alt ml-2"></i>
                        باز کردن سیستم مدیریت
                    </a>
                </div>
            </div>

            <!-- Current Status -->
            <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
                <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
                    <i class="fas fa-chart-bar text-purple-500 ml-2"></i>
                    وضعیت فعلی سیستم
                </h2>
                <div id="currentStatus" class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="bg-blue-50 p-4 rounded-lg">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-blue-600 text-sm font-medium">کاربران آنلاین</p>
                                <p class="text-2xl font-bold text-blue-800" id="onlineUsersCount">-</p>
                            </div>
                            <i class="fas fa-user-check text-blue-500 text-2xl"></i>
                        </div>
                    </div>
                    
                    <div class="bg-green-50 p-4 rounded-lg">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-green-600 text-sm font-medium">جلسات فعال</p>
                                <p class="text-2xl font-bold text-green-800" id="activeSessionsCount">-</p>
                            </div>
                            <i class="fas fa-desktop text-green-500 text-2xl"></i>
                        </div>
                    </div>
                    
                    <div class="bg-purple-50 p-4 rounded-lg">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-purple-600 text-sm font-medium">ورودی‌های امروز</p>
                                <p class="text-2xl font-bold text-purple-800" id="todayLoginsCount">-</p>
                            </div>
                            <i class="fas fa-sign-in-alt text-purple-500 text-2xl"></i>
                        </div>
                    </div>
                </div>
                
                <button id="refreshStatusBtn" class="mt-4 bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center">
                    <i class="fas fa-sync-alt ml-2"></i>
                    بروزرسانی وضعیت
                </button>
            </div>

            <!-- API Test -->
            <div class="bg-white rounded-lg shadow-lg p-6">
                <h2 class="text-xl font-semibold text-gray-800 mb-4 flex items-center">
                    <i class="fas fa-code text-orange-500 ml-2"></i>
                    تست API
                </h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <button id="testApiBtn" class="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center justify-center">
                        <i class="fas fa-play ml-2"></i>
                        تست API کاربران آنلاین
                    </button>
                    
                    <button id="testStatsBtn" class="bg-indigo-500 hover:bg-indigo-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center justify-center">
                        <i class="fas fa-chart-line ml-2"></i>
                        تست API آمار
                    </button>
                </div>
                
                <div id="apiResult" class="mt-4 hidden">
                    <h3 class="font-semibold text-gray-800 mb-2">نتیجه API:</h3>
                    <pre id="apiOutput" class="bg-gray-100 p-4 rounded-lg text-sm overflow-x-auto"></pre>
                </div>
            </div>
        </div>
    </div>

    <script>
        // ایجاد داده‌های نمونه
        document.getElementById('createSampleBtn').addEventListener('click', async function() {
            const btn = this;
            const result = document.getElementById('sampleResult');
            
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin ml-2"></i>در حال ایجاد...';
            
            try {
                const response = await fetch('?action=create_sample_data');
                const data = await response.json();
                
                result.classList.remove('hidden');
                result.innerHTML = `
                    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
                        <h4 class="font-semibold mb-2">نتیجه ایجاد داده‌های نمونه:</h4>
                        <ul class="list-disc list-inside space-y-1">
                            ${data.map(item => `
                                <li class="${item.success ? 'text-green-600' : 'text-red-600'}">
                                    ${item.user}: ${item.message}
                                </li>
                            `).join('')}
                        </ul>
                    </div>
                `;
                
                // بروزرسانی وضعیت
                setTimeout(loadStatus, 1000);
                
            } catch (error) {
                result.classList.remove('hidden');
                result.innerHTML = `
                    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        خطا در ایجاد داده‌های نمونه: ${error.message}
                    </div>
                `;
            } finally {
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-plus ml-2"></i>ایجاد داده‌های نمونه';
            }
        });
        
        // بارگذاری وضعیت
        async function loadStatus() {
            try {
                const response = await fetch('online_users_api.php?action=get_online_users');
                const data = await response.json();
                
                if (data.success) {
                    document.getElementById('onlineUsersCount').textContent = data.total_count;
                    document.getElementById('activeSessionsCount').textContent = data.total_count;
                }
                
                // بارگذاری آمار
                const statsResponse = await fetch('online_users_api.php?action=get_session_stats');
                const statsData = await statsResponse.json();
                
                if (statsData.success && statsData.stats.today_logins !== undefined) {
                    document.getElementById('todayLoginsCount').textContent = statsData.stats.today_logins;
                }
                
            } catch (error) {
                console.error('Error loading status:', error);
            }
        }
        
        // بروزرسانی وضعیت
        document.getElementById('refreshStatusBtn').addEventListener('click', loadStatus);
        
        // تست API
        document.getElementById('testApiBtn').addEventListener('click', async function() {
            const result = document.getElementById('apiResult');
            const output = document.getElementById('apiOutput');
            
            try {
                const response = await fetch('online_users_api.php?action=get_online_users');
                const data = await response.json();
                
                result.classList.remove('hidden');
                output.textContent = JSON.stringify(data, null, 2);
                
            } catch (error) {
                result.classList.remove('hidden');
                output.textContent = 'خطا: ' + error.message;
            }
        });
        
        document.getElementById('testStatsBtn').addEventListener('click', async function() {
            const result = document.getElementById('apiResult');
            const output = document.getElementById('apiOutput');
            
            try {
                const response = await fetch('online_users_api.php?action=get_session_stats');
                const data = await response.json();
                
                result.classList.remove('hidden');
                output.textContent = JSON.stringify(data, null, 2);
                
            } catch (error) {
                result.classList.remove('hidden');
                output.textContent = 'خطا: ' + error.message;
            }
        });
        
        // بارگذاری اولیه
        loadStatus();
    </script>
</body>
</html>