// بررسی احراز هویت
function checkAuthentication() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const loginTime = localStorage.getItem('loginTime');
    
    if (!isAuthenticated || !loginTime) {
        // کاربر وارد نشده است
        window.location.href = '../login/login.html';
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
        window.location.href = '../login/login.html';
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
    window.location.href = '../login/login.html';
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
let refreshCountdownTimer = null;
let remainingTime = 0;
let seenQuotaNumbers = new Set();
let notificationQueue = [];
let isShowingNotification = false;


// تنظیمات
const settings = {
    showNotifications: true,
    soundNotifications: false,
    refreshInterval: 30000,
    recordsPerPage: 25,
    showNewQuotaNotifications: true,
    windowsNotifications: false
};

// تابع نمایش/مخفی کردن نوار کناری
function toggleSidebar() {
    const sidebar = document.getElementById('filterSidebar');
    const overlay = document.getElementById('sidebarOverlay');
    const icon = document.getElementById('sidebarIcon');
    
    sidebar.classList.toggle('translate-x-full');
    
    if (sidebar.classList.contains('translate-x-full')) {
        // بستن نوار کناری
        overlay.classList.add('opacity-0', 'invisible');
        icon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />';
    } else {
        // باز کردن نوار کناری
        overlay.classList.remove('opacity-0', 'invisible');
        icon.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />';
    }
}

// راه‌اندازی اولیه
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
    loadSettings();
    setupEventListeners();
    startAutoRefresh();
    
    // راه‌اندازی AudioContext با اولین تعامل کاربر
    document.addEventListener('click', function initAudioOnFirstInteraction() {
        initAudioContext();
        // حذف شنونده رویداد پس از اولین اجرا
        document.removeEventListener('click', initAudioOnFirstInteraction);
    }, { once: true });
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
        const response = await fetch('api/realTimeLoadingData.php?action=getRealTimeData');
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
    
    // نوار کناری
    document.getElementById('toggleSidebar').addEventListener('click', toggleSidebar);
    
    // بستن نوار کناری با کلیک روی overlay
    document.getElementById('sidebarOverlay').addEventListener('click', function() {
        const sidebar = document.getElementById('filterSidebar');
        if (!sidebar.classList.contains('translate-x-full')) {
            toggleSidebar();
        }
    });
    
    // بستن نوار کناری با کلیک خارج از آن
    document.addEventListener('click', function(event) {
        const sidebar = document.getElementById('filterSidebar');
        const toggleBtn = document.getElementById('toggleSidebar');
        const overlay = document.getElementById('sidebarOverlay');
        
        if (!sidebar.contains(event.target) && !toggleBtn.contains(event.target) && !overlay.contains(event.target)) {
            if (!sidebar.classList.contains('translate-x-full')) {
                toggleSidebar();
            }
        }
    });
    
    // دکمه بازگشت به داشبورد اصلی
    const backToDashboardButton = document.getElementById('backToDashboard');
    if (backToDashboardButton) {
        backToDashboardButton.addEventListener('click', () => {
            window.location.href = '../kotazh-reports/index.html';
        });
    }
    
    // دکمه خروج
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', logout);
    }
    
    // جستجو
    document.getElementById('kotazhSearch').addEventListener('input', handleSearch);
    document.getElementById('voucherSearch').addEventListener('change', handleSearch);
    document.getElementById('tableSearch').addEventListener('input', handleTableSearch);
    
    // فیلترها
    document.getElementById('carrierFilter').addEventListener('change', applyFilters);
    document.getElementById('warehouseFilter').addEventListener('change', applyFilters);
    
    // مرتب‌سازی جدول
    document.querySelectorAll('th[data-sort]').forEach(th => {
        th.addEventListener('click', () => handleSort(th.dataset.sort, th.dataset.defaultSort));
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
    const voucherSearch = document.getElementById('voucherSearch').value;
    const tableSearch = document.getElementById('tableSearch').value.toLowerCase();
    
    filteredData = allData.filter(item => {
        const matchKotazh = !kotazhSearch || item.loadingQuotaNumber.toString().toLowerCase().includes(kotazhSearch);
        const matchVoucher = voucherSearch === 'all' || item.shipName === voucherSearch;
        
        // اضافه کردن شرط جستجوی جدول
        let matchTableSearch = true;
        if (tableSearch) {
            matchTableSearch = false;
            // جستجو در تمام فیلدهای جدول
            for (const key in item) {
                if (item[key] && item[key].toString().toLowerCase().includes(tableSearch)) {
                    matchTableSearch = true;
                    break;
                }
            }
        }
        
        return matchKotazh && matchVoucher && matchTableSearch;
    });
    
    currentPage = 1;
    updateTable();
    updateStatistics();
}

// جستجوی جدول
function handleTableSearch() {
    const tableSearch = document.getElementById('tableSearch').value.toLowerCase();
    
    filteredData = allData.filter(item => {
        // اگر فیلد جستجو خالی باشد، همه رکوردها را نمایش بده
        if (!tableSearch) return true;
        
        // جستجو در تمام فیلدهای جدول
        for (const key in item) {
            if (item[key] && item[key].toString().toLowerCase().includes(tableSearch)) {
                return true;
            }
        }
        
        return false;
    });
    
    currentPage = 1;
    updateTable();
    updateStatistics();
}

// اعمال فیلترها
function applyFilters() {
    const carrierFilter = document.getElementById('carrierFilter').value;
    const warehouseFilter = document.getElementById('warehouseFilter').value;
    
    filteredData = allData.filter(item => {
        const matchCarrier = carrierFilter === 'all' || item.shippingCompany === carrierFilter;
        const matchWarehouse = warehouseFilter === 'all' || item.loadingWarehouse === warehouseFilter;
        
        return matchCarrier && matchWarehouse;
    });
    
    currentPage = 1;
    updateTable();
    updateStatistics();
}



// پاک کردن فیلترها
function resetFilters() {
    document.getElementById('kotazhSearch').value = '';
    document.getElementById('voucherSearch').value = 'all';
    document.getElementById('carrierFilter').value = 'all';
    document.getElementById('warehouseFilter').value = 'all';
    document.getElementById('tableSearch').value = '';
    
    filteredData = [...allData];
    currentPage = 1;
    updateTable();
    updateStatistics();
}

// مرتب‌سازی
function handleSort(column, defaultDirection) {
    if (sortColumn === column) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        sortColumn = column;
        sortDirection = (defaultDirection === 'desc' || defaultDirection === 'asc') ? defaultDirection : 'asc';
    }

    const numericColumns = ['totalNetWeight', 'entryVouchers', 'exitVouchers'];
    const normalizeNumber = (val) => {
        if (val === null || val === undefined) return 0;
        if (typeof val === 'number') return val;
        const str = String(val).replace(/[\s,٬،]/g, ''); // حذف فاصله و جداکننده‌های رایج
        const num = parseFloat(str);
        return isNaN(num) ? 0 : num;
    };

    filteredData.sort((a, b) => {
        let aVal = a[column];
        let bVal = b[column];

        if (numericColumns.includes(column)) {
            aVal = normalizeNumber(aVal);
            bVal = normalizeNumber(bVal);
        } else {
            aVal = aVal ?? '';
            bVal = bVal ?? '';
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
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">${(item.totalNetWeight || 0).toLocaleString()}</td>
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
    const totalEntryVouchers = filteredData.reduce((sum, item) => sum + (item.entryVouchers || 0), 0);
    const totalExitVouchers = filteredData.reduce((sum, item) => sum + (item.exitVouchers || 0), 0);
    const uniqueCarriers = new Set(filteredData.map(item => item.shippingCompany)).size;
    
    document.getElementById('totalVouchers').textContent = totalVouchers.toLocaleString();
    document.getElementById('totalWeight').textContent = totalWeight.toLocaleString();
    document.getElementById('totalEntryVouchers').textContent = totalEntryVouchers.toLocaleString();
    document.getElementById('totalExitVouchers').textContent = totalExitVouchers.toLocaleString();
    document.getElementById('activeCarriers').textContent = uniqueCarriers.toLocaleString();
}

// پر کردن فیلترها
function populateFilters() {
    const carrierFilter = document.getElementById('carrierFilter');
    const warehouseFilter = document.getElementById('warehouseFilter');
    const voucherSearch = document.getElementById('voucherSearch');
    
    const uniqueCarriers = [...new Set(allData.map(item => item.shippingCompany))].filter(Boolean).sort();
    const uniqueWarehouses = [...new Set(allData.map(item => item.loadingWarehouse))].filter(Boolean).sort();
    const uniqueShips = [...new Set(allData.map(item => item.shipName))].filter(Boolean).sort();
    
    // پاک کردن گزینه‌های قبلی
    carrierFilter.innerHTML = '<option value="all">همه شرکت‌ها</option>';
    warehouseFilter.innerHTML = '<option value="all">همه انبارها</option>';
    voucherSearch.innerHTML = '<option value="all">همه کشتی‌ها</option>';
    
    uniqueCarriers.forEach(carrier => {
        const option = document.createElement('option');
        option.value = carrier;
        option.textContent = carrier;
        carrierFilter.appendChild(option);
    });
    
    uniqueWarehouses.forEach(warehouse => {
        const option = document.createElement('option');
        option.value = warehouse;
        option.textContent = warehouse;
        warehouseFilter.appendChild(option);
    });
    
    uniqueShips.forEach(ship => {
        const option = document.createElement('option');
        option.value = ship;
        option.textContent = ship;
        voucherSearch.appendChild(option);
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
    
    // بارگذاری تنظیم اعلان کوتاژ جدید
    const newQuotaCheckbox = document.getElementById('showNewQuotaNotifications');
    if (newQuotaCheckbox) {
        newQuotaCheckbox.checked = settings.showNewQuotaNotifications;
    }
    
    // بارگذاری تنظیم اعلان ویندوز
    const windowsNotificationCheckbox = document.getElementById('windowsNotifications');
    if (windowsNotificationCheckbox) {
        windowsNotificationCheckbox.checked = settings.windowsNotifications;
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
    
    // اضافه کردن تنظیم اعلان ویندوز
    const windowsNotificationCheckbox = document.getElementById('windowsNotifications');
    if (windowsNotificationCheckbox) {
        settings.windowsNotifications = windowsNotificationCheckbox.checked;
    }
    
    recordsPerPage = settings.recordsPerPage;
    refreshInterval = settings.refreshInterval;
    
    localStorage.setItem('dashboardSettings', JSON.stringify(settings));
    
    // راه‌اندازی مجدد تایمر
    startAutoRefresh();
    
    // بروزرسانی جدول
    currentPage = 1;
    updateTable();
    
    // بروزرسانی نمایشگر تایمر
    updateRefreshTimer();
    
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
        const response = await fetch('api/realTimeLoadingData.php?action=getRealTimeData');
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
                
                addNotificationToQueue(
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
    
    if (refreshCountdownTimer) {
        clearInterval(refreshCountdownTimer);
    }
    
    // تنظیم زمان باقی‌مانده
    remainingTime = Math.floor(refreshInterval / 1000);
    
    // شروع تایمر شمارش معکوس
    startRefreshCountdown();
    
    autoRefreshTimer = setInterval(() => {
        loadRealTimeData(); // استفاده از داده‌های لحظه‌ای برای بروزرسانی
        // ریست کردن تایمر شمارش معکوس
        remainingTime = Math.floor(refreshInterval / 1000);
    }, refreshInterval);
}

// شروع تایمر شمارش معکوس
function startRefreshCountdown() {
    refreshCountdownTimer = setInterval(() => {
        remainingTime--;
        
        if (remainingTime <= 0) {
            remainingTime = Math.floor(refreshInterval / 1000);
        }
        
        updateRefreshTimer();
    }, 1000);
    
    // بروزرسانی اولیه
    updateRefreshTimer();
}

// بروزرسانی نمایشگر تایمر
function updateRefreshTimer() {
    const timerElement = document.getElementById('refreshTimer');
    const progressElement = document.getElementById('refreshProgress');
    if (!timerElement || !progressElement) return;
    
    if (remainingTime <= 0) {
        timerElement.textContent = '--';
        timerElement.style.color = '';
        progressElement.style.strokeDashoffset = '100';
        return;
    }
    
    const minutes = Math.floor(remainingTime / 60);
    const seconds = remainingTime % 60;
    
    if (minutes > 0) {
        timerElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    } else {
        timerElement.textContent = `${seconds}`;
    }
    
    // محاسبه درصد پیشرفت
    const totalTime = refreshInterval / 1000;
    const progress = ((totalTime - remainingTime) / totalTime) * 100;
    const dashOffset = 100 - progress;
    
    // به‌روزرسانی دایره پیشرفت
    progressElement.style.strokeDashoffset = dashOffset;
    
    // تغییر رنگ بر اساس زمان باقی‌مانده
    const container = timerElement.parentElement;
    if (remainingTime <= 5) {
        container.className = container.className.replace(/bg-orange-\d+/g, 'bg-red-100').replace(/dark:bg-orange-\d+/g, 'dark:bg-red-900');
        timerElement.className = timerElement.className.replace(/text-orange-\d+/g, 'text-red-600').replace(/dark:text-orange-\d+/g, 'dark:text-red-400');
        container.querySelector('.text-orange-600, .text-red-600').className = 'text-xs sm:text-sm text-red-600 dark:text-red-400';
        progressElement.classList.remove('text-orange-500', 'dark:text-orange-400');
        progressElement.classList.add('text-red-500', 'dark:text-red-400');
    } else {
        container.className = container.className.replace(/bg-red-\d+/g, 'bg-orange-100').replace(/dark:bg-red-\d+/g, 'dark:bg-orange-900');
        timerElement.className = timerElement.className.replace(/text-red-\d+/g, 'text-orange-600').replace(/dark:text-red-\d+/g, 'dark:text-orange-400');
        container.querySelector('.text-red-600, .text-orange-600').className = 'text-xs sm:text-sm text-orange-600 dark:text-orange-400';
        progressElement.classList.remove('text-red-500', 'dark:text-red-400');
        progressElement.classList.add('text-orange-500', 'dark:text-orange-400');
    }
}

// تابع updateCurrentTime حذف شد چون المنت currentTime در HTML وجود ندارد

// نمایش لودینگ
function showLoading() {
    document.getElementById('loadingOverlay').classList.remove('hidden');
}

// مخفی کردن لودینگ
function hideLoading() {
    document.getElementById('loadingOverlay').classList.add('hidden');
}

// اضافه کردن نوتیفیکیشن به صف
function addNotificationToQueue(message, type = 'info') {
    if (!settings.showNotifications) return;
    
    notificationQueue.push({ message, type });
    
    if (!isShowingNotification) {
        processNotificationQueue();
    }
}

// پردازش صف نوتیفیکیشن‌ها
function processNotificationQueue() {
    if (notificationQueue.length === 0) {
        isShowingNotification = false;
        return;
    }
    
    isShowingNotification = true;
    const { message, type } = notificationQueue.shift();
    
    showSingleNotification(message, type, () => {
        // بعد از 5 ثانیه، نوتیفیکیشن بعدی را نمایش بده
        setTimeout(() => {
            processNotificationQueue();
        }, 500); // فاصله کوتاه بین نوتیفیکیشن‌ها
    });
}

// نمایش یک نوتیفیکیشن
function showSingleNotification(message, type, callback) {
    const notificationContainer = document.getElementById('notificationContainer') || createNotificationContainer();
    
    // ایجاد المنت نوتیفیکیشن جدید
    const notification = document.createElement('div');
    notification.className = `notification-item p-1 rounded-lg shadow-lg transform transition-all duration-300 mb-1 ${
        type === 'success' ? 'bg-green-500' :
        type === 'error' ? 'bg-red-500' :
        type === 'warning' ? 'bg-yellow-500' :
        'bg-blue-500'
    } text-white translate-x-full opacity-0`;
    
    notification.innerHTML = `
        <div class="flex items-center justify-between">
            <span class="text-sm font-medium">${message}</span>
            <button class="ml-2 text-white hover:text-gray-200 transition-colors" onclick="this.parentElement.parentElement.remove()">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>
    `;
    
    // اضافه کردن به کانتینر
    notificationContainer.appendChild(notification);
    
    // انیمیشن ورود
    setTimeout(() => {
        notification.classList.remove('translate-x-full', 'opacity-0');
        notification.classList.add('translate-x-0', 'opacity-100');
    }, 100);
    
    // هدایت نوتیفیکیشن‌های قبلی به بالا
    const existingNotifications = notificationContainer.querySelectorAll('.notification-item');
    existingNotifications.forEach((item, index) => {
        if (item !== notification) {
            item.style.transform = `translateY(-${(existingNotifications.length - index - 1) * 5}px)`;
        }
    });
    
    if (settings.showNotifications && !settings.windowsNotifications && "Notification" in window && Notification.permission === "granted") {
        const browserNotification = new Notification('ATK Cargo', {
            body: message,
            icon: '/assets/images/logo.png',
            badge: '/assets/images/badge.png',
            tag: 'atk-cargo-notification'
        });
        
        // بستن خودکار بعد از 5 ثانیه
        setTimeout(() => {
            browserNotification.close();
        }, 5000);
    }
    
    // نمایش اعلان ویندوز
    if (settings.windowsNotifications) {
        showWindowsNotification('ATK Cargo', message);
    }
    
    // پخش صدا
    if (settings.soundNotifications) {
        playNotificationSound();
    }
    
    // حذف خودکار بعد از 5 ثانیه
    setTimeout(() => {
        if (notification.parentElement) {
            notification.classList.remove('translate-x-0', 'opacity-100');
            notification.classList.add('translate-x-full', 'opacity-0');
            
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.remove();
                }
            }, 300);
        }
    }, 5000);
    
    if (callback) callback();
}

// ایجاد کانتینر نوتیفیکیشن
function createNotificationContainer() {
    const container = document.createElement('div');
    container.id = 'notificationContainer';
    container.className = 'fixed bottom-4 right-4 z-50 max-w-sm';
    document.body.appendChild(container);
    return container;
}

// تابع نمایش اعلان ویندوز
function showWindowsNotification(title, message) {
    try {
        // ابتدا تلاش برای استفاده از Windows Runtime API تا فقط یک اعلان نمایش داده شود
        if (window.Windows && window.Windows.UI && window.Windows.UI.Notifications) {
            const ToastNotificationManager = window.Windows.UI.Notifications.ToastNotificationManager;
            const ToastTemplateType = window.Windows.UI.Notifications.ToastTemplateType;

            const template = ToastNotificationManager.getTemplateContent(ToastTemplateType.toastText02);
            const textNodes = template.getElementsByTagName('text');

            textNodes[0].appendChild(template.createTextNode(title));
            textNodes[1].appendChild(template.createTextNode(message));

            const toast = new window.Windows.UI.Notifications.ToastNotification(template);
            ToastNotificationManager.createToastNotifier().show(toast);
            return; // جلوگیری از نمایش اعلان دوم از طریق Notification API
        }

        // در غیر این صورت، از Notification API مرورگر استفاده کن
        if ("Notification" in window) {
            if (Notification.permission === "granted") {
                createWindowsNotification(title, message);
            } else if (Notification.permission === "default") {
                Notification.requestPermission().then(function (permission) {
                    if (permission === "granted") {
                        createWindowsNotification(title, message);
                    }
                });
            }
        }
    } catch (error) {
        console.warn('خطا در نمایش اعلان ویندوز:', error);
    }
}

// ایجاد اعلان ویندوز
function createWindowsNotification(title, message) {
    try {
        const notification = new Notification(title, {
            body: message,
            icon: '/assets/images/logo.png',
            badge: '/assets/images/badge.png',
            tag: 'atk-cargo-windows-notification',
            requireInteraction: true,
            silent: !settings.soundNotifications
            // حذف ویژگی actions چون فقط در ServiceWorkerRegistration.showNotification پشتیبانی می‌شود
        });
        
        // مدیریت کلیک روی اعلان
        notification.onclick = function() {
            window.focus();
            notification.close();
        };
        
        // بستن خودکار بعد از 10 ثانیه
        setTimeout(() => {
            notification.close();
        }, 10000);
        
    } catch (error) {
        console.warn('خطا در ایجاد اعلان ویندوز:', error);
    }
}

// متغیر سراسری برای AudioContext
let audioContextInstance = null;

// تابع راه‌اندازی AudioContext با تعامل کاربر
function initAudioContext() {
    if (!audioContextInstance) {
        try {
            audioContextInstance = new (window.AudioContext || window.webkitAudioContext)();
            // اگر AudioContext در حالت تعلیق است، آن را از حالت تعلیق خارج کنیم
            if (audioContextInstance.state === 'suspended') {
                audioContextInstance.resume();
            }
        } catch (error) {
            console.warn('خطا در راه‌اندازی AudioContext:', error);
        }
    }
    return audioContextInstance;
}

// تابع پخش صدای اعلان
function playNotificationSound() {
    try {
        // بررسی وجود AudioContext
        const audioContext = audioContextInstance;
        if (!audioContext) {
            console.warn('AudioContext هنوز راه‌اندازی نشده است');
            return;
        }
        
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
        oscillator.frequency.setValueAtTime(600, audioContext.currentTime + 0.1);
        oscillator.frequency.setValueAtTime(800, audioContext.currentTime + 0.2);
        
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.3);
    } catch (error) {
        console.warn('خطا در پخش صدای اعلان:', error);
    }
}

// تابع سازگار با کد قبلی
function showNotification(message, type = 'info') {
    addNotificationToQueue(message, type);
}

// مدیریت خطاها
window.addEventListener('error', function(e) {
    showNotification('خطایی در سیستم رخ داده است', 'error');
});

// مدیریت خطاهای Promise
window.addEventListener('unhandledrejection', function(e) {
    showNotification('خطا در ارتباط با سرور', 'error');
});