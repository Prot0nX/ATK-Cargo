// بررسی احراز هویت
function checkAuthentication() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const loginTime = localStorage.getItem('loginTime');
    
    if (!isAuthenticated || !loginTime) {
        // کاربر وارد نشده است
        window.location.href = 'login.html';
        return false;
    }
    
    const currentTime = new Date().getTime();
    const timeDiff = currentTime - parseInt(loginTime);
    const hoursDiff = timeDiff / (1000 * 60 * 60);
    
    // بررسی انقضای جلسه (24 ساعت)
    if (hoursDiff >= 24) {
        // جلسه منقضی شده است
        localStorage.removeItem('isAuthenticated');
        localStorage.removeItem('authToken');
        localStorage.removeItem('loginTime');
        localStorage.removeItem('lastDestination');
        window.location.href = 'login.html';
        return false;
    }
    
    return true;
}

// تابع خروج از سیستم
function logout() {
    // پاک کردن تمام اطلاعات احراز هویت
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('authToken');
    localStorage.removeItem('loginTime');
    localStorage.removeItem('lastDestination');
    
    // هدایت به صفحه ورود
    window.location.href = 'login.html';
}

// متغیرهای سراسری
let allData = [];
let realTimeData = [];
let filteredData = [];
let currentPage = 1;
let recordsPerPage = 25;
let sortColumn = null;
let sortDirection = 'asc';
let refreshInterval = 30000;
let autoRefreshTimer = null;
let seenQuotaNumbers = new Set(); // برای ردیابی کوتاژ‌های دیده شده


// تنظیمات
const settings = {
    showNotifications: true,
    soundNotifications: false,
    refreshInterval: 30000,
    recordsPerPage: 25,
    showNewQuotaNotifications: true
};

// راه‌اندازی اولیه
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    loadSettings();
    setupEventListeners();
    startAutoRefresh();
    updateCurrentTime();
});

// راه‌اندازی داشبورد
function initializeDashboard() {
    // بررسی احراز هویت قبل از هر چیز
    if (!checkAuthentication()) {
        return;
    }
    
    // بارگذاری داده‌های اولیه
    loadInitialData();
    
    // تنظیم حالت تاریک
    if (localStorage.getItem('darkMode') === 'true') {
        document.documentElement.classList.add('dark');
    }
}

// بارگذاری داده‌های اولیه
async function loadInitialData() {
    try {
        showLoading();
        
        // فراخوانی API برای دریافت داده‌های لحظه‌ای
        const response = await fetch('realTimeLoadingData.php?action=getRealTimeData');
        const data = await response.json();
        
        if (data.success) {
            processRealTimeData(data.data, true); // true برای بارگذاری اولیه
            updateStatistics();
            updateTable();
            populateFilters();
            showNotification('داده‌ها با موفقیت بارگذاری شدند', 'success');
        } else {
            throw new Error('خطا در دریافت داده‌ها');
        }
    } catch (error) {
        showNotification('خطا در بارگذاری داده‌ها', 'error');
    } finally {
        hideLoading();
    }
}






// تنظیم رویدادها
function setupEventListeners() {
    // دکمه‌های اصلی
    document.getElementById('darkModeToggle').addEventListener('click', toggleDarkMode);
    document.getElementById('settingsBtn').addEventListener('click', openSettings);
    document.getElementById('refreshData').addEventListener('click', refreshData);
    document.getElementById('exportData').addEventListener('click', exportData);
    document.getElementById('resetFilters').addEventListener('click', resetFilters);
    document.getElementById('applyFilters').addEventListener('click', applyFilters);
    
    // دکمه بازگشت به داشبورد اصلی
    const backToDashboardButton = document.getElementById('backToDashboard');
    if (backToDashboardButton) {
        backToDashboardButton.addEventListener('click', () => {
            window.location.href = 'index.html';
        });
    }
    
    // دکمه خروج
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
    
    // جستجو
    document.getElementById('kotazhSearch').addEventListener('input', handleSearch);
    document.getElementById('voucherSearch').addEventListener('input', handleSearch);
    document.getElementById('receiptSearch').addEventListener('input', handleSearch);
    
    // فیلترها
    document.getElementById('carrierFilter').addEventListener('change', applyFilters);
    
    // مرتب‌سازی جدول
    document.querySelectorAll('th[data-sort]').forEach(th => {
        th.addEventListener('click', () => handleSort(th.dataset.sort));
    });
    
    // صفحه‌بندی
    document.getElementById('prevPage').addEventListener('click', () => changePage(-1));
    document.getElementById('nextPage').addEventListener('click', () => changePage(1));
    
    // مودال تنظیمات
    document.getElementById('closeSettings').addEventListener('click', closeSettings);
    document.getElementById('saveSettings').addEventListener('click', saveSettings);
    document.getElementById('cancelSettings').addEventListener('click', closeSettings);
}

// جستجو
function handleSearch() {
    const kotazhSearch = document.getElementById('kotazhSearch').value.toLowerCase();
    const voucherSearch = document.getElementById('voucherSearch').value.toLowerCase();
    const receiptSearch = document.getElementById('receiptSearch').value.toLowerCase();
    
    filteredData = allData.filter(item => {
        const matchKotazh = !kotazhSearch || item.loadingQuotaNumber.toString().toLowerCase().includes(kotazhSearch);
        const matchVoucher = !voucherSearch || item.shipName.toLowerCase().includes(voucherSearch);
        const matchReceipt = !receiptSearch || (item.cargoType && item.cargoType.toLowerCase().includes(receiptSearch));
        
        return matchKotazh && matchVoucher && matchReceipt;
    });
    
    currentPage = 1;
    updateTable();
    updateStatistics();
}

// اعمال فیلترها
function applyFilters() {
    const carrierFilter = document.getElementById('carrierFilter').value;
    const minWeight = parseFloat(document.getElementById('minWeight').value) || 0;
    const maxWeight = parseFloat(document.getElementById('maxWeight').value) || Infinity;
    
    filteredData = allData.filter(item => {
        const matchCarrier = carrierFilter === 'all' || item.shippingCompany === carrierFilter;
        const matchWeight = item.totalNetWeight >= minWeight && item.totalNetWeight <= maxWeight;
        
        return matchCarrier && matchWeight;
    });
    
    currentPage = 1;
    updateTable();
    updateStatistics();
}



// پاک کردن فیلترها
function resetFilters() {
    document.getElementById('kotazhSearch').value = '';
    document.getElementById('voucherSearch').value = '';
    document.getElementById('receiptSearch').value = '';
    document.getElementById('carrierFilter').value = 'all';
    document.getElementById('minWeight').value = '';
    document.getElementById('maxWeight').value = '';
    
    filteredData = [...allData];
    currentPage = 1;
    updateTable();
    updateStatistics();
}

// مرتب‌سازی
function handleSort(column) {
    if (sortColumn === column) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        sortColumn = column;
        sortDirection = 'asc';
    }
    
    filteredData.sort((a, b) => {
        let aVal = a[column];
        let bVal = b[column];
        
        if (column === 'totalNetWeight' || column === 'entryVouchers' || column === 'exitVouchers') {
            aVal = parseFloat(aVal) || 0;
            bVal = parseFloat(bVal) || 0;
        }
        
        if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
        if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
        return 0;
    });
    
    updateTable();
}

// صفحه‌بندی
function changePage(direction) {
    const totalPages = Math.ceil(filteredData.length / recordsPerPage);
    const newPage = currentPage + direction;
    
    if (newPage >= 1 && newPage <= totalPages) {
        currentPage = newPage;
        updateTable();
    }
}

// بروزرسانی جدول
function updateTable() {
    const tbody = document.getElementById('dataTableBody');
    if (!tbody) {
        return;
    }
    
    const startIndex = (currentPage - 1) * recordsPerPage;
    const endIndex = startIndex + recordsPerPage;
    const pageData = filteredData.slice(startIndex, endIndex);
    
    tbody.innerHTML = '';
    
    // اگر داده‌ای وجود نداشت
    if (pageData.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="px-6 py-8 text-center text-gray-500 dark:text-gray-400">
                    <div class="flex flex-col items-center">
                        <svg class="w-12 h-12 mb-4 text-gray-300 dark:text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                        </svg>
                        <p class="text-lg font-medium">هیچ داده‌ای یافت نشد</p>
                        <p class="text-sm">لطفاً فیلترهای خود را بررسی کنید</p>
                    </div>
                </td>
            </tr>
        `;
    } else {
        pageData.forEach(item => {
            const row = document.createElement('tr');
            row.className = 'hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors';
            
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.loadingQuotaNumber || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.shipName || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.loadingWarehouse || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.shippingCompany || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.cargoType || '-'}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.entryVouchers || 0}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.exitVouchers || 0}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${item.totalNetWeight || 0}</td>
            `;
            
            tbody.appendChild(row);
        });
    }
    
    updatePagination();
}

// بروزرسانی صفحه‌بندی
function updatePagination() {
    const totalPages = Math.ceil(filteredData.length / recordsPerPage);
    const startRecord = (currentPage - 1) * recordsPerPage + 1;
    const endRecord = Math.min(currentPage * recordsPerPage, filteredData.length);
    
    document.getElementById('showingFrom').textContent = startRecord;
    document.getElementById('showingTo').textContent = endRecord;
    document.getElementById('totalRecords').textContent = filteredData.length;
    document.getElementById('pageInfo').textContent = `صفحه ${currentPage} از ${totalPages}`;
    
    document.getElementById('prevPage').disabled = currentPage === 1;
    document.getElementById('nextPage').disabled = currentPage === totalPages;
}

// بروزرسانی آمار
function updateStatistics() {
    const totalVouchers = filteredData.reduce((sum, item) => sum + (item.totalVouchers || 0), 0);
    const totalWeight = filteredData.reduce((sum, item) => sum + (item.totalNetWeight || 0), 0);
    const activeVouchers = filteredData.filter(item => item.entryVouchers > 0 && item.exitVouchers === 0).length;
    const uniqueCarriers = new Set(filteredData.map(item => item.shippingCompany)).size;
    
    document.getElementById('totalVouchers').textContent = totalVouchers.toLocaleString();
    document.getElementById('totalWeight').textContent = totalWeight.toLocaleString();
    document.getElementById('activeVouchers').textContent = activeVouchers.toLocaleString();
    document.getElementById('activeCarriers').textContent = uniqueCarriers.toLocaleString();
}

// پر کردن فیلترها
function populateFilters() {
    const carrierFilter = document.getElementById('carrierFilter');
    const uniqueCarriers = [...new Set(allData.map(item => item.shippingCompany))].filter(Boolean).sort();
    
    // پاک کردن گزینه‌های قبلی
    carrierFilter.innerHTML = '<option value="all">همه شرکت‌ها</option>';
    
    uniqueCarriers.forEach(carrier => {
        const option = document.createElement('option');
        option.value = carrier;
        option.textContent = carrier;
        carrierFilter.appendChild(option);
    });
}





// تغییر حالت تاریک
function toggleDarkMode() {
    document.documentElement.classList.toggle('dark');
    const isDark = document.documentElement.classList.contains('dark');
    localStorage.setItem('darkMode', isDark);
}

// باز کردن تنظیمات
function openSettings() {
    document.getElementById('settingsModal').classList.remove('hidden');
    
    // بارگذاری تنظیمات فعلی
    document.getElementById('refreshInterval').value = settings.refreshInterval / 1000;
    document.getElementById('recordsPerPage').value = settings.recordsPerPage;
    document.getElementById('showNotifications').checked = settings.showNotifications;
    document.getElementById('soundNotifications').checked = settings.soundNotifications;
    
    // اضافه کردن چک‌باکس اعلان کوتاژ جدید اگر وجود دارد
    const newQuotaCheckbox = document.getElementById('showNewQuotaNotifications');
    if (newQuotaCheckbox) {
        newQuotaCheckbox.checked = settings.showNewQuotaNotifications;
    }
}

// بستن تنظیمات
function closeSettings() {
    document.getElementById('settingsModal').classList.add('hidden');
}

// ذخیره تنظیمات
function saveSettings() {
    settings.refreshInterval = parseInt(document.getElementById('refreshInterval').value) * 1000;
    settings.recordsPerPage = parseInt(document.getElementById('recordsPerPage').value);
    settings.showNotifications = document.getElementById('showNotifications').checked;
    settings.soundNotifications = document.getElementById('soundNotifications').checked;
    
    // اضافه کردن تنظیم اعلان کوتاژ جدید
    const newQuotaCheckbox = document.getElementById('showNewQuotaNotifications');
    if (newQuotaCheckbox) {
        settings.showNewQuotaNotifications = newQuotaCheckbox.checked;
    }
    
    recordsPerPage = settings.recordsPerPage;
    refreshInterval = settings.refreshInterval;
    
    localStorage.setItem('dashboardSettings', JSON.stringify(settings));
    
    // راه‌اندازی مجدد تایمر
    startAutoRefresh();
    
    // بروزرسانی جدول
    currentPage = 1;
    updateTable();
    
    closeSettings();
    showNotification('تنظیمات ذخیره شد', 'success');
}

// بارگذاری تنظیمات
function loadSettings() {
    const savedSettings = localStorage.getItem('dashboardSettings');
    if (savedSettings) {
        Object.assign(settings, JSON.parse(savedSettings));
        recordsPerPage = settings.recordsPerPage;
        refreshInterval = settings.refreshInterval;
    }
}

// بروزرسانی داده‌ها
async function refreshData() {
    await loadInitialData();
}

// دریافت داده‌های لحظه‌ای
async function loadRealTimeData() {
    try {
        const response = await fetch('realTimeLoadingData.php?action=getRealTimeData');
        const data = await response.json();
        
        if (data.success && data.data) {
            // پردازش داده‌های لحظه‌ای
            processRealTimeData(data.data, false); // false برای بروزرسانی لحظه‌ای
            updateStatistics();
            updateTable();
        }
    } catch (error) {
        // خطا در دریافت داده‌های لحظه‌ای - بی‌صدا نادیده گرفته می‌شود
    }
}

// پردازش داده‌های لحظه‌ای
function processRealTimeData(realTimeData, isInitialLoad = false) {
    // پاک کردن داده‌های قبلی برای جلوگیری از تجمع
    allData = [];
    
    // اگر داده‌های لحظه‌ای موجود است، آنها را پردازش کن
    if (realTimeData && Array.isArray(realTimeData)) {
        realTimeData.forEach((item) => {
            const quotaNumber = item.loadingQuotaNumber;
            
            // بررسی کوتاژ جدید برای نمایش اعلان
            if (!isInitialLoad && quotaNumber && !seenQuotaNumbers.has(quotaNumber) && 
                settings.showNotifications && settings.showNewQuotaNotifications) {
                
                const shipName = item.shipName || 'نامشخص';
                const warehouse = item.loadingWarehouse || 'نامشخص';
                
                showNotification(
                    `شروع بارگیری از کوتاژ ${quotaNumber} از کشتی ${shipName} و انبار ${warehouse} شروع شد`,
                    'info'
                );
                
                seenQuotaNumbers.add(quotaNumber);
            } else if (quotaNumber) {
                seenQuotaNumbers.add(quotaNumber);
            }
            
            const newItem = {
                loadingQuotaNumber: quotaNumber || '',
                shipName: item.shipName || '',
                loadingWarehouse: item.loadingWarehouse || '',
                shippingCompany: item.shippingCompany || '',
                cargoType: item.cargoType || '',
                entryVouchers: item.entryVouchers || 0,
                exitVouchers: item.exitVouchers || 0,
                totalVouchers: item.totalVouchers || 0,
                totalNetWeight: parseFloat(item.totalNetWeight) || 0,
                firstEntryTime: item.firstEntryTime || '',
                lastExitTime: item.lastExitTime || ''
            };
            
            allData.push(newItem);
        });
        
        filteredData = [...allData];
    }
}

// خروجی داده‌ها
function exportData() {
    const csvContent = "data:text/csv;charset=utf-8,\uFEFF" + 
        "کوتاژ,کشتی,انبار,باربری,کالا,تعداد ورودی,تعداد خروجی,جمع وزن خالص\n" +
        filteredData.map(item => 
            `${item.loadingQuotaNumber},${item.shipName},${item.loadingWarehouse},${item.shippingCompany},${item.cargoType},${item.entryVouchers},${item.exitVouchers},${item.totalNetWeight}`
        ).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `cargo_data_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    showNotification('فایل CSV ایجاد شد', 'success');
}

// شروع بروزرسانی خودکار
function startAutoRefresh() {
    if (autoRefreshTimer) {
        clearInterval(autoRefreshTimer);
    }
    
    autoRefreshTimer = setInterval(() => {
        loadRealTimeData(); // استفاده از داده‌های لحظه‌ای برای بروزرسانی
    }, refreshInterval);
}

// بروزرسانی زمان فعلی
function updateCurrentTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('fa-IR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    
    document.getElementById('currentTime').textContent = timeString;
    
    setTimeout(updateCurrentTime, 1000);
}

// نمایش لودینگ
function showLoading() {
    document.getElementById('loadingOverlay').classList.remove('hidden');
}

// مخفی کردن لودینگ
function hideLoading() {
    document.getElementById('loadingOverlay').classList.add('hidden');
}

// نمایش اعلان
function showNotification(message, type = 'info') {
    if (!settings.showNotifications) return;
    
    const notification = document.getElementById('notification');
    const notificationText = document.getElementById('notificationText');
    
    // تنظیم رنگ بر اساس نوع
    notification.className = `fixed bottom-4 right-4 p-4 rounded-lg shadow-lg transform transition-all duration-300 z-50 ${
        type === 'success' ? 'bg-green-500' :
        type === 'error' ? 'bg-red-500' :
        type === 'warning' ? 'bg-yellow-500' :
        'bg-blue-500'
    } text-white`;
    
    notificationText.textContent = message;
    
    // نمایش اعلان
    notification.classList.remove('translate-y-full', 'opacity-0');
    notification.classList.add('translate-y-0', 'opacity-100');
    
    // پخش صدا
    if (settings.soundNotifications) {
        // می‌توانید اینجا صدای اعلان اضافه کنید
    }
    
    // مخفی کردن بعد از 3 ثانیه
    setTimeout(() => {
        notification.classList.remove('translate-y-0', 'opacity-100');
        notification.classList.add('translate-y-full', 'opacity-0');
    }, 3000);
}

// مدیریت خطاها
window.addEventListener('error', function(e) {
    showNotification('خطایی در سیستم رخ داده است', 'error');
});

// مدیریت خطاهای Promise
window.addEventListener('unhandledrejection', function(e) {
    showNotification('خطا در ارتباط با سرور', 'error');
});