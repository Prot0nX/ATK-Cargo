// Authentication Check
function checkAuth() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const authToken = localStorage.getItem('authToken');

    if (!isAuthenticated || !authToken) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// Global Variables
let licenses = [];
let currentFilter = 'all';
let searchTerm = '';
let activeModal = null;

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;

    initializeTheme();
    initializeSidebar();
    initializeEventListeners();
    loadLicenses();

    // Auto refresh every 60 seconds
    setInterval(loadLicenses, 60000);
});

// --- Theme Management ---
function initializeTheme() {
    const themeToggle = document.getElementById('themeToggle');
    const savedTheme = localStorage.getItem('theme') || 'light';

    // Apply initial theme
    if (savedTheme === 'dark' || (savedTheme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }

    // Toggle event
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            document.documentElement.classList.toggle('dark');
            const isDark = document.documentElement.classList.contains('dark');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');
        });
    }
}

// --- Sidebar Management ---
function initializeSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mobileOverlay = document.getElementById('mobileOverlay');
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');

    function toggleSidebar() {
        sidebar.classList.toggle('open'); // CSS handles translation
        mobileOverlay.classList.toggle('show');

        if (sidebar.classList.contains('open')) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }
    }

    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', toggleSidebar);
    }

    if (mobileOverlay) {
        mobileOverlay.addEventListener('click', toggleSidebar);
    }

    // Close sidebar on route change (if we had routing) or on mobile item click
    document.querySelectorAll('.nav-item[data-page]').forEach(item => {
        item.addEventListener('click', () => {
            const page = item.getAttribute('data-page');

            if (window.innerWidth < 1024) {
                sidebar.classList.remove('open');
                mobileOverlay.classList.remove('show');
                document.body.style.overflow = '';
            }

            // Active state handling
            document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            // Switch pages
            switchPage(page);
        });
    });
}

// --- Page Navigation ---
function switchPage(pageName) {
    // Hide all pages
    document.querySelectorAll('.page-content').forEach(page => {
        page.classList.remove('active');
    });

    // Show selected page
    const targetPage = document.getElementById(`${pageName}Page`);
    if (targetPage) {
        targetPage.classList.add('active');
    }

    // Update page title
    const titles = {
        'dashboard': 'داشبورد مدیریت',
        'reports': 'گزارش‌ها',
        'settings': 'تنظیمات'
    };

    const pageTitle = document.getElementById('pageTitle');
    if (pageTitle && titles[pageName]) {
        pageTitle.textContent = titles[pageName];
    }

    // Load page-specific data
    if (pageName === 'reports') {
        updateReportsPage();
    }
}

// --- Data Management ---
async function loadLicenses() {
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');
    const tbody = document.getElementById('licensesTableBody');

    // Show loading only on first load or manual refresh
    if (licenses.length === 0) {
        loadingState.classList.remove('hidden');
        tbody.innerHTML = '';
        emptyState.classList.add('hidden');
    }

    try {
        const response = await fetch('manage_licenses.php?action=list');
        const data = await response.json();

        if (data.success) {
            licenses = data.licenses;
            updateStats();
            renderTable();
        } else {
            showNotification(data.message || 'خطا در دریافت اطلاعات', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('خطا در ارتباط با سرور', 'error');
    } finally {
        loadingState.classList.add('hidden');
    }
}

function updateStats() {
    const activeCount = licenses.filter(l => l.is_active).length;
    const inactiveCount = licenses.length - activeCount;

    // Animate numbers
    animateValue('activeLicenses', parseInt(document.getElementById('activeLicenses').innerText), activeCount, 1000);
    animateValue('inactiveLicenses', parseInt(document.getElementById('inactiveLicenses').innerText), inactiveCount, 1000);
    animateValue('totalCompanies', parseInt(document.getElementById('totalCompanies').innerText), licenses.length, 1000);

    // Calculate and display trends
    calculateAndDisplayTrends();
}

function calculateAndDisplayTrends() {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    // Get first day of current month and previous month
    const currentMonthStart = new Date(currentYear, currentMonth, 1);
    const previousMonthStart = new Date(currentYear, currentMonth - 1, 1);
    const previousMonthEnd = new Date(currentYear, currentMonth, 0, 23, 59, 59);

    // Filter licenses by month
    const currentMonthLicenses = licenses.filter(l => {
        const createdDate = new Date(l.created_at);
        return createdDate >= currentMonthStart;
    });

    const previousMonthLicenses = licenses.filter(l => {
        const createdDate = new Date(l.created_at);
        return createdDate >= previousMonthStart && createdDate <= previousMonthEnd;
    });

    // Calculate current and previous month stats
    const currentActiveCount = currentMonthLicenses.filter(l => l.is_active).length;
    const previousActiveCount = previousMonthLicenses.filter(l => l.is_active).length;

    const currentInactiveCount = currentMonthLicenses.filter(l => !l.is_active).length;
    const previousInactiveCount = previousMonthLicenses.filter(l => !l.is_active).length;

    const currentTotalCount = currentMonthLicenses.length;
    const previousTotalCount = previousMonthLicenses.length;

    // Update trend displays
    updateTrendDisplay('activeLicensesTrend', currentActiveCount, previousActiveCount, 'افزایش', 'کاهش');
    updateTrendDisplay('inactiveLicensesTrend', currentInactiveCount, previousInactiveCount, 'افزایش', 'کاهش');
    updateTrendDisplay('totalCompaniesTrend', currentTotalCount, previousTotalCount, 'شرکت جدید', 'کاهش');
}

function updateTrendDisplay(elementId, currentValue, previousValue, increaseText, decreaseText) {
    const element = document.getElementById(elementId);
    if (!element) return;

    const icon = element.querySelector('i');
    const valueSpan = element.querySelector('.font-medium');
    const textSpan = element.querySelector('.text-gray-400');

    // Calculate percentage change
    let percentChange = 0;
    let absoluteChange = currentValue - previousValue;

    if (previousValue > 0) {
        percentChange = Math.round((absoluteChange / previousValue) * 100);
    } else if (currentValue > 0) {
        percentChange = 100; // If previous was 0 and current is not, it's 100% increase
    }

    // Determine trend direction
    let isIncrease = absoluteChange > 0;
    let isDecrease = absoluteChange < 0;
    let isNeutral = absoluteChange === 0;

    // Update icon
    icon.className = 'ml-1 fas ';
    if (isIncrease) {
        icon.className += 'fa-arrow-up text-green-500';
        element.className = 'mt-4 flex items-center text-sm text-green-500';
    } else if (isDecrease) {
        icon.className += 'fa-arrow-down text-red-500';
        element.className = 'mt-4 flex items-center text-sm text-red-500';
    } else {
        icon.className += 'fa-minus text-gray-400';
        element.className = 'mt-4 flex items-center text-sm text-gray-400';
    }

    // Update value
    if (isNeutral) {
        valueSpan.textContent = 'بدون تغییر';
        valueSpan.className = 'font-medium text-gray-400';
        textSpan.textContent = 'نسبت به ماه قبل';
    } else {
        // For total companies, show absolute number instead of percentage
        if (elementId === 'totalCompaniesTrend') {
            valueSpan.textContent = Math.abs(absoluteChange);
            textSpan.textContent = isIncrease ? `${increaseText} این ماه` : `${decreaseText} این ماه`;
        } else {
            valueSpan.textContent = `${Math.abs(percentChange)}%`;
            textSpan.textContent = isIncrease ? `${increaseText} نسبت به ماه قبل` : `${decreaseText} نسبت به ماه قبل`;
        }
        valueSpan.className = isIncrease ? 'font-medium text-green-500' : 'font-medium text-red-500';
    }
}

function animateValue(id, start, end, duration) {
    if (start === end) return;
    const obj = document.getElementById(id);
    const range = end - start;
    let current = start;
    const increment = end > start ? 1 : -1;
    const stepTime = Math.abs(Math.floor(duration / range));

    const timer = setInterval(function () {
        current += increment;
        obj.innerHTML = current;
        if (current == end) {
            clearInterval(timer);
        }
    }, stepTime);
}

function renderTable() {
    const tbody = document.getElementById('licensesTableBody');
    const emptyState = document.getElementById('emptyState');

    // Filter data
    let filteredData = licenses.filter(license => {
        const matchesSearch = license.company_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            license.license_key.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesSearch;
    });

    if (filteredData.length === 0) {
        tbody.innerHTML = '';
        emptyState.classList.remove('hidden');
        emptyState.classList.add('flex');
        return;
    }

    emptyState.classList.add('hidden');
    emptyState.classList.remove('flex');

    tbody.innerHTML = filteredData.map(license => `
        <tr class="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors cursor-pointer" onclick="showLicenseDetails('${license.license_key}')">
            <td class="font-mono text-sm">
                <div class="flex items-center gap-2">
                    <span class="bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded select-all">${license.license_key}</span>
                    <button class="text-gray-400 hover:text-blue-500 transition-colors" onclick="event.stopPropagation(); copyToClipboard('${license.license_key}')">
                        <i class="fas fa-copy"></i>
                    </button>
                </div>
            </td>
            <td class="font-medium text-gray-900 dark:text-white">${license.company_name}</td>
            <td class="text-sm text-gray-500">${formatLastCheck(license.last_check)}</td>
            <td>
                <span class="badge ${license.is_active ? 'badge-success' : 'badge-danger'}">
                    <i class="fas ${license.is_active ? 'fa-check' : 'fa-ban'}"></i>
                    ${license.is_active ? 'فعال' : 'غیرفعال'}
                </span>
            </td>
            <td class="text-sm text-gray-500">
                ${calculateExpiry(license.created_at)}
            </td>
            <td>
                <div class="flex items-center gap-2">
                    <button class="btn btn-outline p-2 h-8 w-8 rounded-lg flex items-center justify-center text-gray-500 hover:text-blue-500 hover:border-blue-500" 
                            onclick="event.stopPropagation(); toggleLicenseStatus('${license.license_key}', ${!license.is_active})">
                        <i class="fas ${license.is_active ? 'fa-toggle-on' : 'fa-toggle-off'}"></i>
                    </button>
                    <button class="btn btn-outline p-2 h-8 w-8 rounded-lg flex items-center justify-center text-gray-500 hover:text-red-500 hover:border-red-500"
                            onclick="event.stopPropagation(); deleteLicense('${license.license_key}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// --- Actions ---
async function handleCreateLicense(e) {
    e.preventDefault();
    const companyInput = document.getElementById('companyName');
    const companyName = companyInput.value.trim();

    if (!companyName) return;

    try {
        const response = await fetch('manage_licenses.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: 'create',
                company_name: companyName
            })
        });

        const data = await response.json();

        if (data.success) {
            showNotification('لایسنس با موفقیت ایجاد شد', 'success');
            companyInput.value = '';
            loadLicenses();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        showNotification('خطا در ایجاد لایسنس', 'error');
    }
}

async function toggleLicenseStatus(key, newStatus) {
    try {
        const response = await fetch('manage_licenses.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: 'toggle',
                license_key: key
            })
        });

        const data = await response.json();

        if (data.success) {
            // Update local state
            const license = licenses.find(l => l.license_key === key);
            if (license) {
                license.is_active = newStatus;
                renderTable();
                updateStats();
            }
            showNotification(`لایسنس ${newStatus ? 'فعال' : 'غیرفعال'} شد`, 'success');
        } else {
            showNotification(data.message || 'خطا در تغییر وضعیت', 'error');
            loadLicenses(); // Revert on error
        }
    } catch (error) {
        console.error('Error toggling license:', error);
        showNotification('خطا در تغییر وضعیت', 'error');
        loadLicenses(); // Revert on error
    }
}

function deleteLicense(key) {
    Swal.fire({
        title: 'آیا مطمئن هستید؟',
        text: "این عملیات غیرقابل بازگشت است!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#3b82f6',
        confirmButtonText: 'بله، حذف کن',
        cancelButtonText: 'انصراف',
        customClass: {
            popup: 'dark:bg-gray-800 dark:text-white rounded-2xl',
            title: 'dark:text-white'
        }
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                const response = await fetch('manage_licenses.php', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        action: 'delete',
                        license_key: key
                    })
                });

                const data = await response.json();
                if (data.success) {
                    showNotification('لایسنس حذف شد', 'success');
                    loadLicenses();
                } else {
                    showNotification(data.message, 'error');
                }
            } catch (error) {
                showNotification('خطا در حذف لایسنس', 'error');
            }
        }
    });
}

// --- Modals ---
function showLicenseDetails(key) {
    const license = licenses.find(l => l.license_key === key);
    if (!license) return;

    const modal = document.getElementById('licenseModal');
    const content = document.getElementById('licenseModalContent');
    const body = document.getElementById('licenseModalBody');

    body.innerHTML = `
        <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
                <div class="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
                    <p class="text-sm text-gray-500 mb-1">نام شرکت</p>
                    <p class="font-bold text-lg">${license.company_name}</p>
                </div>
                <div class="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
                    <p class="text-sm text-gray-500 mb-1">وضعیت</p>
                    <span class="badge ${license.is_active ? 'badge-success' : 'badge-danger'}">
                        ${license.is_active ? 'فعال' : 'غیرفعال'}
                    </span>
                </div>
            </div>
            
            <div class="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
                <p class="text-sm text-gray-500 mb-2">کلید لایسنس</p>
                <div class="flex items-center gap-2 bg-white dark:bg-gray-800 p-3 rounded-lg border border-gray-200 dark:border-gray-600">
                    <code class="flex-1 font-mono text-sm">${license.license_key}</code>
                    <button class="text-blue-500 hover:text-blue-600" onclick="copyToClipboard('${license.license_key}')">
                        <i class="fas fa-copy"></i>
                    </button>
                </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                    <p class="text-gray-500">تاریخ ایجاد</p>
                    <p>${formatDate(license.created_at)}</p>
                </div>
                <div>
                    <p class="text-gray-500">آخرین بررسی</p>
                    <p>${formatLastCheck(license.last_check)}</p>
                </div>
            </div>
        </div>
    `;

    modal.classList.remove('hidden');
    // Small delay to allow display:block to apply before opacity transition
    setTimeout(() => {
        content.classList.remove('scale-95', 'opacity-0');
        content.classList.add('scale-100', 'opacity-100');
    }, 10);
}

function closeLicenseModal() {
    const modal = document.getElementById('licenseModal');
    const content = document.getElementById('licenseModalContent');

    content.classList.remove('scale-100', 'opacity-100');
    content.classList.add('scale-95', 'opacity-0');

    setTimeout(() => {
        modal.classList.add('hidden');
    }, 300);
}

// --- Utilities ---
function initializeEventListeners() {
    document.getElementById('createLicenseForm').addEventListener('submit', handleCreateLicense);

    document.getElementById('searchInput').addEventListener('input', (e) => {
        searchTerm = e.target.value;
        renderTable();
    });

    // Close modal on Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeLicenseModal();
    });
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showNotification('کپی شد', 'success');
    });
}

function showNotification(message, type = 'success') {
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
        timerProgressBar: true,
        didOpen: (toast) => {
            toast.addEventListener('mouseenter', Swal.stopTimer)
            toast.addEventListener('mouseleave', Swal.resumeTimer)
        }
    });

    Toast.fire({
        icon: type,
        title: message
    });
}

function formatDate(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('fa-IR');
}

function formatLastCheck(dateString) {
    if (!dateString) return 'هرگز';
    const date = new Date(dateString);
    const now = new Date();
    const diff = Math.floor((now - date) / 1000 / 60); // minutes

    if (diff < 1) return 'لحظاتی پیش';
    if (diff < 60) return `${diff} دقیقه پیش`;
    if (diff < 1440) return `${Math.floor(diff / 60)} ساعت پیش`;
    return formatDate(dateString);
}

function calculateExpiry(createdDate) {
    // Mock logic for expiry - usually this would come from backend
    return 'نامحدود';
}

function refreshData() {
    loadLicenses();
    showNotification('اطلاعات بروزرسانی شد', 'info');
}

// --- Reports Page ---
function updateReportsPage() {
    if (!licenses || licenses.length === 0) return;

    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    // Get first day of current month and previous month
    const currentMonthStart = new Date(currentYear, currentMonth, 1);
    const previousMonthStart = new Date(currentYear, currentMonth - 1, 1);
    const previousMonthEnd = new Date(currentYear, currentMonth, 0, 23, 59, 59);

    // Filter licenses by month
    const currentMonthLicenses = licenses.filter(l => {
        const createdDate = new Date(l.created_at);
        return createdDate >= currentMonthStart;
    });

    const previousMonthLicenses = licenses.filter(l => {
        const createdDate = new Date(l.created_at);
        return createdDate >= previousMonthStart && createdDate <= previousMonthEnd;
    });

    // Update report stats
    document.getElementById('reportTotalLicenses').textContent = licenses.length;
    document.getElementById('reportActiveLicenses').textContent = licenses.filter(l => l.is_active).length;
    document.getElementById('reportInactiveLicenses').textContent = licenses.filter(l => !l.is_active).length;
    document.getElementById('reportThisMonthCreated').textContent = currentMonthLicenses.length;
    document.getElementById('reportLastMonthCreated').textContent = previousMonthLicenses.length;
}

// --- Settings Page ---
// Initialize settings theme toggle
document.addEventListener('DOMContentLoaded', () => {
    const settingsThemeToggle = document.getElementById('settingsThemeToggle');
    if (settingsThemeToggle) {
        settingsThemeToggle.addEventListener('click', () => {
            document.documentElement.classList.toggle('dark');
            const isDark = document.documentElement.classList.contains('dark');
            localStorage.setItem('theme', isDark ? 'dark' : 'light');
        });
    }
});

// Logout handler
function handleLogout() {
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('authToken');
    window.location.href = 'login.html';
}