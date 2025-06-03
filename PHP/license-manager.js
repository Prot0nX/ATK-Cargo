// بررسی احراز هویت
function checkAuth() {
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    const authToken = localStorage.getItem('authToken');
    
    if (!isAuthenticated || !authToken) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

// تنظیمات اولیه
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    
    loadLicenses();
    initializeThemeToggle();
    initializeEventListeners();
    initializeModals();
    loadUserPreferences();
    getCsrfToken();
    
    // بررسی اسکرول افقی در لود اولیه
    checkTableOverflow();
    
    // بررسی مجدد در تغییر سایز صفحه
    window.addEventListener('resize', checkTableOverflow);
});

// متغیرهای سراسری
let licenses = [];
let currentFilter = 'all';
let searchTerm = '';
let editModal;
let activeModal = null;
let currentLicenseKey = null; // برای نگهداری کلید لایسنس فعلی
let currentSort = {
    column: null,
    direction: 'desc'
};

// مدیریت تم و شخصی‌سازی
function initializeThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');
    const prefersDarkScheme = window.matchMedia('(prefers-color-scheme: dark)');
    const icon = themeToggle.querySelector('i');
    
    // تنظیم تم اولیه
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark' || (!savedTheme && prefersDarkScheme.matches)) {
        document.documentElement.classList.add('dark');
        icon.classList.remove('fa-sun');
        icon.classList.add('fa-moon');
    }
    
    themeToggle.addEventListener('click', () => {
        document.documentElement.classList.toggle('dark');
        icon.classList.toggle('fa-sun');
        icon.classList.toggle('fa-moon');
        
        localStorage.setItem('theme', document.documentElement.classList.contains('dark') ? 'dark' : 'light');
    });
}

// خروج از حساب کاربری
function handleLogout() {
    localStorage.removeItem('isAuthenticated');
    localStorage.removeItem('authToken');
    window.location.href = 'login.html';
}

function confirmLogout() {
    handleLogout();
    hideModal('logoutModal');
}

// ذخیره تنظیمات کاربر
function saveUserPreferences() {
    const preferences = {
        primaryColor: document.getElementById('primaryColor').value,
        fontSize: document.getElementById('fontSize').value,
        tableLayout: document.getElementById('tableLayout').value,
        animationsEnabled: document.getElementById('animationsEnabled').checked
    };
    
    localStorage.setItem('userPreferences', JSON.stringify(preferences));
    applyUserPreferences(preferences);
    showNotification('تنظیمات با موفقیت ذخیره شد', 'success');
}

// بارگذاری تنظیمات کاربر
function loadUserPreferences() {
    const savedPreferences = localStorage.getItem('userPreferences');
    if (savedPreferences) {
        const preferences = JSON.parse(savedPreferences);
        
        // تنظیم مقادیر در فرم تنظیمات
        document.getElementById('primaryColor').value = preferences.primaryColor;
        document.getElementById('fontSize').value = preferences.fontSize;
        document.getElementById('tableLayout').value = preferences.tableLayout;
        document.getElementById('animationsEnabled').checked = preferences.animationsEnabled;
        
        // اعمال تنظیمات
        applyUserPreferences(preferences);
    }
}

// اعمال تنظیمات کاربر
function applyUserPreferences(preferences) {
    // تنظیم رنگ اصلی
    document.documentElement.style.setProperty('--color-primary', preferences.primaryColor);
    
    // تنظیم اندازه فونت
    document.documentElement.style.setProperty('--base-font-size', preferences.fontSize + 'px');
    
    // تنظیم طرح جدول
    const table = document.querySelector('table');
    table.className = preferences.tableLayout === 'compact' ? 'table-compact' : 'table-normal';
    
    // تنظیم انیمیشن‌ها
    if (!preferences.animationsEnabled) {
        document.documentElement.classList.add('no-animations');
    } else {
        document.documentElement.classList.remove('no-animations');
    }
}

// مدیریت رویدادها
function initializeEventListeners() {
    // فرم ایجاد لایسنس
    document.getElementById('createLicenseForm').addEventListener('submit', handleCreateLicense);
    
    // جستجو
    document.getElementById('searchInput').addEventListener('input', (e) => {
        searchTerm = e.target.value;
        filterAndDisplayLicenses();
    });
    
    // فیلترها
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.filter-btn').forEach(b => {
                b.classList.remove('bg-primary', 'text-white');
                b.classList.add('bg-white', 'dark:bg-gray-800');
            });
            e.target.classList.remove('bg-white', 'dark:bg-gray-800');
            e.target.classList.add('bg-primary', 'text-white');
            currentFilter = e.target.dataset.filter;
            filterAndDisplayLicenses();
        });
    });

    document.querySelectorAll('.edit-license').forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const row = button.closest('tr');
            const licenseKey = row.dataset.licenseKey;
            const companyName = row.querySelector('td:nth-child(2)').textContent;
            
            document.getElementById('editLicenseKey').value = licenseKey;
            document.getElementById('editCompanyName').value = companyName;
            
            const editModal = new bootstrap.Modal(document.getElementById('editLicenseModal'));
            editModal.show();
        });
    });

    // دکمه ذخیره تغییرات در مدال ویرایش
    document.getElementById('saveEditBtn').addEventListener('click', function() {
        const licenseKey = document.getElementById('editLicenseKey').textContent;
        const companyName = document.getElementById('editCompanyName').value;
        
        if (!companyName.trim()) {
            showNotification('لطفاً نام شرکت را وارد کنید', 'warning');
            return;
        }
        
        handleEditSave(licenseKey);
    });

    // اضافه کردن event listener برای دکمه تایید حذف
    document.getElementById('confirmDeleteBtn').addEventListener('click', () => {
        if (currentLicenseKey) {
            handleDelete(currentLicenseKey);
        }
    });
    
    // اضافه کردن event listener برای مرتب‌سازی
    const sortableHeaders = document.querySelectorAll('.sortable');
    sortableHeaders.forEach(header => {
        header.addEventListener('click', () => {
            sortLicenses(header.dataset.sort);
        });
    });
}

// مدیریت مدال‌ها
function initializeModals() {
    // اضافه کردن event listener برای بستن مدال با کلیک روی پس‌زمینه
    document.querySelectorAll('[id$="Modal"]').forEach(modal => {
        const backdrop = modal.querySelector('.bg-black\\/50');
        if (backdrop) {
            backdrop.addEventListener('click', () => hideModal(modal.id));
        }
    });

    // اضافه کردن event listener برای کلید ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && activeModal) {
            hideModal(activeModal);
        }
    });
}

function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;

    // مخفی کردن مدال فعال قبلی
    if (activeModal) {
        hideModal(activeModal);
    }

    // نمایش مدال جدید
    modal.classList.remove('hidden');
    document.body.classList.add('overflow-hidden');
    activeModal = modalId;

    // اضافه کردن انیمیشن
    const modalContent = modal.querySelector('.bg-white');
    modalContent.classList.add('animate-modal-show');
}

function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;

    modal.classList.add('hidden');
    document.body.classList.remove('overflow-hidden');
    activeModal = null;
}

// دریافت لیست لایسنس‌ها
async function loadLicenses() {
    try {
        const response = await fetch('manage_licenses.php?action=list', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        if (data.success) {
            licenses = data.licenses;
            updateStats();
            filterAndDisplayLicenses();
        } else {
            showNotification(data.message || 'خطا در دریافت لیست لایسنس‌ها', 'error');
        }
    } catch (error) {
        console.error('Error loading licenses:', error);
        showNotification('خطا در برقراری ارتباط با سرور', 'error');
    }
}

// بررسی تشابه نام شرکت
async function checkCompanySimilarity(companyName, currentLicenseKey = null) {
    const similarCompanies = licenses.filter(license => {
        // اگر در حال ویرایش هستیم، لایسنس فعلی رو نادیده بگیر
        if (currentLicenseKey && license.license_key === currentLicenseKey) {
            return false;
        }
        return license.company_name.toLowerCase().includes(companyName.toLowerCase()) ||
               companyName.toLowerCase().includes(license.company_name.toLowerCase());
    });
    
    if (similarCompanies.length > 0) {
        const result = await Swal.fire({
            title: 'هشدار!',
            html: `شرکت‌های مشابه یافت شد:<br><br>` +
                  similarCompanies.map(c => `- ${c.company_name}`).join('<br>') +
                  '<br><br>آیا از ثبت این نام شرکت اطمینان دارید؟',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'بله، ثبت شود',
            cancelButtonText: 'خیر، انصراف'
        });
        return result.isConfirmed;
    }
    return true;
}

// بررسی تکراری بودن لایسنس
function isLicenseKeyDuplicate(licenseKey) {
    return licenses.some(license => license.license_key === licenseKey);
}

// ایجاد لایسنس جدید
async function handleCreateLicense(e) {
    e.preventDefault();
    
    const companyName = document.getElementById('companyName').value.trim();
    
    if (!companyName) {
        showNotification('لطفاً نام شرکت را وارد کنید', 'warning');
        return;
    }
    
    try {
        // بررسی تشابه نام شرکت
        const shouldProceed = await checkCompanySimilarity(companyName);
        if (!shouldProceed) {
            return;
        }
        
        const response = await fetch('manage_licenses.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'create',
                company_name: companyName
            })
        });
        
        const data = await response.json();
        if (data.success) {
            // بررسی تکراری نبودن لایسنس
            if (isLicenseKeyDuplicate(data.license_key)) {
                showNotification('خطا: این لایسنس قبلاً در سیستم ثبت شده است', 'error');
                return;
            }
            
            showNotification('لایسنس با موفقیت ایجاد شد', 'success');
            document.getElementById('createLicenseForm').reset();
            loadLicenses();
        } else {
            showNotification(data.message || 'خطا در ایجاد لایسنس', 'error');
        }
    } catch (error) {
        console.error('Error creating license:', error);
        showNotification('خطا در برقراری ارتباط با سرور', 'error');
    }
}

// کپی کردن کلید لایسنس
async function copyLicenseKey(licenseKey) {
    try {
        await navigator.clipboard.writeText(licenseKey);
        showNotification('کلید لایسنس با موفقیت کپی شد', 'success', 3000);
    } catch (error) {
        showNotification('خطا در کپی کردن کلید لایسنس', 'error');
    }
}

// نمایش نوتیفیکیشن
function showNotification(message, type = 'success', duration = 5000) {
    const iconMap = {
        success: 'fa-check-circle',
        error: 'fa-times-circle',
        warning: 'fa-exclamation-circle',
        info: 'fa-info-circle'
    };

    const colorMap = {
        success: 'bg-green-50 text-success dark:bg-green-900/30',
        error: 'bg-red-50 text-danger dark:bg-red-900/30',
        warning: 'bg-yellow-50 text-warning dark:bg-yellow-900/30',
        info: 'bg-blue-50 text-info dark:bg-blue-900/30'
    };

    Swal.fire({
        text: message,
        icon: type,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: duration,
        timerProgressBar: true,
        customClass: {
            popup: `rounded-xl shadow-lg ${colorMap[type]}`,
            title: 'text-sm font-medium'
        }
    });
}

// فیلتر و نمایش لایسنس‌ها
function filterAndDisplayLicenses() {
    let filteredLicenses = [...licenses];
    
    // اعمال جستجو
    if (searchTerm) {
        filteredLicenses = filteredLicenses.filter(license => 
            license.company_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            license.license_key.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }
    
    // اعمال فیلتر
    switch (currentFilter) {
        case 'active':
            filteredLicenses = filteredLicenses.filter(license => license.is_active);
            break;
        case 'inactive':
            filteredLicenses = filteredLicenses.filter(license => !license.is_active);
            break;
        case 'expiring':
            filteredLicenses = []; // چون تاریخ انقضا نداریم، این فیلتر غیرفعال می‌شود
            break;
    }
    
    displayLicenses(filteredLicenses);
}

// نمایش لایسنس‌ها در جدول
function displayLicenses(licensesToShow) {
    const tbody = document.getElementById('licensesTableBody');
    tbody.innerHTML = '';
    
    licensesToShow.forEach(license => {
        const lastCheckStatus = getLastCheckStatus(license.last_check);
        const row = document.createElement('tr');
        row.className = 'hover:bg-white/30 dark:hover:bg-gray-700/50 transition-colors';
        
        row.innerHTML = `
            <td class="py-4 px-6">
                <div class="flex flex-col gap-1">
                    <span class="font-medium text-gray-800 dark:text-white">${license.company_name}</span>
                    <span class="text-sm text-gray-300 dark:text-gray-400">ایجاد شده در ${formatDate(license.created_at)}</span>
                </div>
            </td>
            <td class="py-4 px-6">
                <div class="flex items-center gap-2">
                    <code class="px-3 py-1 bg-gray-100/80 dark:bg-gray-700 rounded-lg font-mono text-sm text-gray-800 dark:text-white cursor-pointer hover:bg-primary hover:text-white transition-colors" onclick="copyLicenseKey('${license.license_key}')">
                        ${license.license_key}
                    </code>
                    <button class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors" onclick="copyLicenseKey('${license.license_key}')">
                        <i class="fas fa-copy text-gray-600 hover:text-gray-800 dark:text-gray-400 dark:hover:text-gray-300"></i>
                    </button>
                </div>
            </td>
            <td class="py-4 px-6">
                <div class="flex items-center gap-3 ${lastCheckStatus.class} px-3 py-2 rounded-lg">
                    <i class="fas ${lastCheckStatus.icon}"></i>
                    <div class="flex flex-col">
                        <span class="font-medium">${lastCheckStatus.text}</span>
                        <span class="text-sm opacity-90">${formatLastCheck(license.last_check)}</span>
                    </div>
                </div>
            </td>
            <td class="py-4 px-6">
                <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium ${license.is_active ? 'bg-green-50 text-green-800 dark:bg-green-900/30 dark:text-green-300' : 'bg-red-50 text-red-800 dark:bg-red-900/30 dark:text-red-300'}">
                    <i class="fas ${license.is_active ? 'fa-check-circle' : 'fa-times-circle'}"></i>
                    ${license.is_active ? 'فعال' : 'غیرفعال'}
                </span>
            </td>
            <td class="py-4 px-6">
                <div class="flex items-center gap-2">
                    <button onclick="toggleLicense('${license.license_key}')"
                            class="p-2 rounded-lg ${license.is_active ? 'bg-red-50 text-red-800 hover:bg-red-100 dark:bg-red-900/30 dark:text-red-300 dark:hover:bg-red-900/50' : 'bg-green-50 text-green-800 hover:bg-green-100 dark:bg-green-900/30 dark:text-green-300 dark:hover:bg-green-900/50'} transition-colors">
                        <i class="fas ${license.is_active ? 'fa-ban' : 'fa-check'}"></i>
                    </button>
                    <button onclick="showEditModal('${license.license_key}', '${license.company_name}')"
                            class="p-2 rounded-lg bg-blue-50 text-blue-800 hover:bg-blue-100 dark:bg-blue-900/30 dark:text-blue-300 dark:hover:bg-blue-900/50 transition-colors">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="showLicenseDetails('${license.license_key}')"
                            class="p-2 rounded-lg bg-purple-50 text-purple-800 hover:bg-purple-100 dark:bg-purple-900/30 dark:text-purple-300 dark:hover:bg-purple-900/50 transition-colors">
                        <i class="fas fa-info-circle"></i>
                    </button>
                    <button onclick="showDeleteModal('${license.license_key}')"
                            class="p-2 rounded-lg bg-red-50 text-red-800 hover:bg-red-100 dark:bg-red-900/30 dark:text-red-300 dark:hover:bg-red-900/50 transition-colors">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// تابع بررسی وضعیت آخرین چک لایسنس
function getLastCheckStatus(lastCheck) {
    if (!lastCheck) {
        return {
            text: 'هنوز استفاده نشده',
            icon: 'fa-clock',
            class: 'text-gray-700 dark:text-gray-400 bg-gray-50/80 dark:bg-gray-700'
        };
    }

    const lastCheckDate = new Date(lastCheck);
    const now = new Date();
    const diffHours = Math.abs(now - lastCheckDate) / 36e5;

    if (diffHours < 24) {
        return {
            text: 'فعال',
            icon: 'fa-check-circle',
            class: 'text-green-800 dark:text-green-300 bg-green-50/80 dark:bg-green-900/30'
        };
    } else if (diffHours < 72) {
        return {
            text: 'نیاز به بررسی',
            icon: 'fa-exclamation-circle',
            class: 'text-yellow-800 dark:text-yellow-300 bg-yellow-50/80 dark:bg-yellow-900/30'
        };
    } else {
        return {
            text: 'غیرفعال',
            icon: 'fa-times-circle',
            class: 'text-red-800 dark:text-red-300 bg-red-50/80 dark:bg-red-900/30'
        };
    }
}

// تابع فرمت‌بندی زمان آخرین چک
function formatLastCheck(lastCheck) {
    if (!lastCheck) return 'هنوز استفاده نشده';
    
    const lastCheckDate = new Date(lastCheck);
    const now = new Date();
    const diffMinutes = Math.floor((now - lastCheckDate) / 60000);
    
    if (diffMinutes < 60) {
        return `${diffMinutes} دقیقه پیش`;
    } else if (diffMinutes < 1440) { // کمتر از 24 ساعت
        const hours = Math.floor(diffMinutes / 60);
        return `${hours} ساعت پیش`;
    } else {
        return formatDate(lastCheck);
    }
}

// نمایش جزئیات لایسنس
function showLicenseDetails(licenseKey) {
    const license = licenses.find(l => l.license_key === licenseKey);
    if (!license) return;

    const lastCheckStatus = getLastCheckStatus(license.last_check);
    const detailsContent = document.querySelector('.license-details-content');
    detailsContent.innerHTML = `
        <div class="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
            <div class="grid gap-4">
                <div class="flex justify-between items-center">
                    <span class="text-gray-500 dark:text-gray-400">نام شرکت:</span>
                    <span class="font-medium text-gray-900 dark:text-white">${license.company_name}</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-gray-500 dark:text-gray-400">کلید لایسنس:</span>
                    <div class="flex items-center gap-2">
                        <code class="px-3 py-1 bg-gray-100 dark:bg-gray-600 rounded-lg font-mono text-sm">${license.license_key}</code>
                        <button onclick="copyLicenseKey('${license.license_key}')"
                                class="p-1.5 hover:bg-gray-200 dark:hover:bg-gray-500 rounded-lg transition-colors">
                            <i class="fas fa-copy text-gray-400"></i>
                        </button>
                    </div>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-gray-500 dark:text-gray-400">وضعیت:</span>
                    <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium ${license.is_active ? 'bg-green-50 text-success dark:bg-green-900/30' : 'bg-red-50 text-danger dark:bg-red-900/30'}">
                        <i class="fas ${license.is_active ? 'fa-check-circle' : 'fa-times-circle'}"></i>
                        ${license.is_active ? 'فعال' : 'غیرفعال'}
                    </span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-gray-500 dark:text-gray-400">تاریخ ایجاد:</span>
                    <span class="text-gray-900 dark:text-white">${formatDate(license.created_at)}</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-gray-500 dark:text-gray-400">تاریخ فعال‌سازی:</span>
                    <span class="text-gray-900 dark:text-white">${formatDate(license.activation_date)}</span>
                </div>
                <div class="flex justify-between items-center">
                    <span class="text-gray-500 dark:text-gray-400">آخرین استفاده:</span>
                    <div class="flex items-center gap-2 ${lastCheckStatus.class} px-3 py-1.5 rounded-lg">
                        <i class="fas ${lastCheckStatus.icon}"></i>
                        <span>${formatLastCheck(license.last_check)}</span>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    showModal('licenseDetailsModal');
}

// به‌روزرسانی آمار
function updateStats() {
    const activeCount = licenses.filter(license => license.is_active).length;
    const inactiveCount = licenses.filter(license => !license.is_active).length;
    const recentlyUsedCount = licenses.filter(license => {
        if (!license.last_check) return false;
        const lastCheckDate = new Date(license.last_check);
        const now = new Date();
        const diffHours = Math.abs(now - lastCheckDate) / 36e5;
        return diffHours < 24;
    }).length;
    
    const needsReviewCount = licenses.filter(license => {
        if (!license.last_check) return true;
        const lastCheckDate = new Date(license.last_check);
        const now = new Date();
        const diffHours = Math.abs(now - lastCheckDate) / 36e5;
        return diffHours >= 24 && diffHours < 72;
    }).length;

    document.getElementById('activeCount').textContent = activeCount;
    document.getElementById('inactiveCount').textContent = inactiveCount;
    document.getElementById('recentlyUsedCount').textContent = recentlyUsedCount;
    document.getElementById('totalCount').textContent = licenses.length;
    document.getElementById('needsReviewCount').textContent = needsReviewCount;
}

// تغییر وضعیت لایسنس
async function toggleLicense(licenseKey) {
    try {
        const response = await fetch('manage_licenses.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'toggle',
                license_key: licenseKey
            })
        });
        
        const data = await response.json();
        if (data.success) {
            showNotification('وضعیت لایسنس با موفقیت تغییر کرد', 'success');
            loadLicenses();
        } else {
            showNotification(data.message, 'error');
        }
    } catch (error) {
        console.error('Error toggling license:', error);
        showNotification('خطا در برقراری ارتباط با سرور', 'error');
    }
}

// نمایش مدال ویرایش
function showEditModal(licenseKey, companyName) {
    document.getElementById('editLicenseKey').textContent = licenseKey;
    document.getElementById('editCompanyName').value = companyName;
    showModal('editLicenseModal');
}

// ذخیره تغییرات ویرایش
async function handleEditSave(licenseKey) {
    const companyName = document.getElementById('editCompanyName').value.trim();
    
    if (!companyName) {
        showNotification('لطفاً نام شرکت را وارد کنید', 'warning');
        return;
    }
    
    // بررسی تشابه نام شرکت با در نظر گرفتن لایسنس فعلی
    const shouldProceed = await checkCompanySimilarity(companyName, licenseKey);
    if (!shouldProceed) return;
    
    try {
        const response = await fetch('manage_licenses.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'edit',
                license_key: licenseKey,
                company_name: companyName
            })
        });
        
        const data = await response.json();
        if (data.success) {
            hideModal('editLicenseModal');
            showNotification('لایسنس با موفقیت ویرایش شد', 'success');
            await loadLicenses();
        } else {
            showNotification(data.message || 'خطا در ویرایش لایسنس', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('خطا در برقراری ارتباط با سرور', 'error');
    }
}

// نمایش مدال حذف
function showDeleteModal(licenseKey) {
    const license = licenses.find(l => l.license_key === licenseKey);
    if (!license) return;

    currentLicenseKey = licenseKey; // ذخیره کلید لایسنس فعلی
    const modal = document.getElementById('deleteLicenseModal');
    modal.querySelector('.company-name').textContent = license.company_name;
    modal.querySelector('.license-key').textContent = license.license_key;
    
    showModal('deleteLicenseModal');
}

// حذف لایسنس
async function handleDelete(licenseKey) {
    try {
        const response = await fetch('manage_licenses.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'delete',
                license_key: licenseKey
            })
        });
        
        const data = await response.json();
        if (data.success) {
            hideModal('deleteLicenseModal');
            showNotification('لایسنس با موفقیت حذف شد', 'success');
            await loadLicenses(); // بارگذاری مجدد لیست لایسنس‌ها
        } else {
            showNotification(data.message || 'خطا در حذف لایسنس', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification('خطا در برقراری ارتباط با سرور', 'error');
    }
}

// تبدیل تاریخ به فرمت فارسی
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const options = { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    };
    return new Intl.DateTimeFormat('fa-IR', options).format(date);
}

// تابع نمایش پیام‌ها
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} notification`;
    alertDiv.textContent = message;
    
    document.body.appendChild(alertDiv);
    
    // حذف اتوماتیک پیام بعد از 3 ثانیه
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

// مدیریت توکن CSRF
function getCsrfToken() {
    return fetch('get_csrf_token.php')
        .then(response => response.json())
        .then(data => {
            document.getElementById('csrf_token').value = data.token;
            document.getElementById('edit_csrf_token').value = data.token;
        })
        .catch(error => console.error('خطا در دریافت توکن CSRF:', error));
}

// بروزرسانی توکن CSRF قبل از ارسال فرم‌ها
document.getElementById('createLicenseForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    await getCsrfToken();
    // ... existing form submission code ...
});

document.getElementById('saveEditBtn').addEventListener('click', async () => {
    await getCsrfToken();
    // ... existing edit submission code ...
}); 

// بررسی اسکرول افقی جدول
function checkTableOverflow() {
    const tableContainer = document.querySelector('.responsive-table');
    const tableScroll = document.querySelector('.table-scroll');
    
    if (tableScroll.scrollWidth > tableScroll.clientWidth) {
        tableContainer.classList.add('has-overflow');
    } else {
        tableContainer.classList.remove('has-overflow');
    }
}

// تابع مرتب‌سازی لایسنس‌ها
function sortLicenses(column) {
    const headers = document.querySelectorAll('.sortable');
    headers.forEach(header => {
        if (header.dataset.sort !== column) {
            header.classList.remove('asc', 'desc');
            header.querySelector('i').className = 'fas fa-sort ml-1';
        }
    });

    const header = document.querySelector(`[data-sort="${column}"]`);
    if (currentSort.column === column) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.column = column;
        currentSort.direction = 'desc';
    }

    header.classList.remove('asc', 'desc');
    header.classList.add(currentSort.direction);
    header.querySelector('i').className = `fas fa-sort-${currentSort.direction === 'asc' ? 'up' : 'down'} ml-1`;

    licenses.sort((a, b) => {
        let comparison = 0;
        
        if (column === 'last_check') {
            const dateA = a.last_check ? new Date(a.last_check) : new Date(0);
            const dateB = b.last_check ? new Date(b.last_check) : new Date(0);
            comparison = dateA - dateB;
        } else if (column === 'status') {
            const statusA = getLastCheckStatus(a.last_check).text;
            const statusB = getLastCheckStatus(b.last_check).text;
            comparison = statusA.localeCompare(statusB);
        }

        return currentSort.direction === 'asc' ? comparison : -comparison;
    });

    updateLicensesTable();
}

// به‌روزرسانی جدول با انیمیشن
function updateLicensesTable() {
    const tbody = document.getElementById('licensesTableBody');
    tbody.innerHTML = '';
    
    licenses.forEach(license => {
        const status = getLastCheckStatus(license.last_check);
        const row = document.createElement('tr');
        row.className = 'sort-animation';
        
        row.innerHTML = `
            <td class="text-right py-4 px-6 text-sm text-gray-200">${license.company}</td>
            <td class="text-right py-4 px-6 text-sm text-gray-200">${license.key}</td>
            <td class="text-right py-4 px-6 text-sm text-gray-200">${formatDate(license.last_check)}</td>
            <td class="text-right py-4 px-6 text-sm">
                <span class="inline-flex items-center">
                    <i class="fas ${status.icon} mr-2 ${status.class}"></i>
                    <span class="${status.class}">${status.text}</span>
                </span>
            </td>
            <td class="text-right py-4 px-6">
                <button onclick="editLicense('${license.key}')" class="text-blue-400 hover:text-blue-500 ml-2">
                    <i class="fas fa-edit"></i>
                </button>
                <button onclick="deleteLicense('${license.key}')" class="text-red-400 hover:text-red-500">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </td>
        `;
        
        tbody.appendChild(row);
    });
}