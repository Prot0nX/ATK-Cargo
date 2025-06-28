/**
 * فایل جاوا اسکریپت مدیریت کاربران آنلاین - نسخه 2.0
 * بازطراحی شده با معماری مدرن و بهینه‌سازی عملکرد
 */

// متغیرهای سراسری
let currentFilter = 'all';
let autoRefreshInterval = null;
let isLoading = false;
let lastUpdateTime = null;
let retryCount = 0;
const MAX_RETRY_COUNT = 3;

// تنظیمات پیکربندی
const CONFIG = {
    API_BASE_URL: 'online_users_api.php',
    REFRESH_INTERVAL: 30000, // 30 ثانیه
    ANIMATION_DURATION: 300,
    DEBOUNCE_DELAY: 500,
    RETRY_DELAY: 2000,
    CACHE_DURATION: 60000, // 1 دقیقه
    PERFORMANCE_MONITORING: true
};

// کش داده‌ها
const dataCache = new Map();

// ارجاع به المان‌های DOM
const elements = {
    // کانتینرهای اصلی
    usersGrid: document.getElementById('usersGrid'),
    loadingState: document.getElementById('loadingState'),
    emptyState: document.getElementById('emptyState'),
    
    // آمار
    activeSessionsCount: document.getElementById('activeSessionsCount'),
    todayLoginsCount: document.getElementById('todayLoginsCount'),
    lastUpdate: document.getElementById('lastUpdate'),
    
    // کنترل‌ها
    refreshBtn: document.getElementById('refreshBtn'),
    autoRefresh: document.getElementById('autoRefresh'),
    refreshIcon: document.querySelector('#refreshBtn i'),
    updateIcon: document.querySelector('#updateIcon'),
    
    // مودال
    userModal: document.getElementById('userModal'),
    modalContent: document.getElementById('modalContent'),
    closeModal: document.getElementById('closeModal'),
    
    // فیلترها
    filterButtons: document.querySelectorAll('.filter-btn')
};

// کلاس مدیریت تم
class ThemeManager {
    constructor() {
        this.currentTheme = localStorage.getItem('theme') || 'light';
        this.init();
    }
    
    init() {
        this.applyTheme(this.currentTheme);
        this.setupEventListeners();
    }
    
    applyTheme(theme) {
        if (theme === 'dark') {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
        this.currentTheme = theme;
        localStorage.setItem('theme', theme);
    }
    
    toggle() {
        const newTheme = this.currentTheme === 'light' ? 'dark' : 'light';
        this.applyTheme(newTheme);
    }
    
    setupEventListeners() {
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => this.toggle());
        }
    }
}

// کلاس مدیریت API با قابلیت‌های پیشرفته
class ApiManager {
    static async makeRequest(endpoint, options = {}) {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000); // 10 ثانیه timeout
        
        try {
            const defaultOptions = {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                signal: controller.signal
            };
            
            const finalOptions = { ...defaultOptions, ...options };
            
            const response = await fetch(`${CONFIG.API_BASE_URL}?action=${endpoint}`, finalOptions);
            clearTimeout(timeoutId);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('HTTP Error Response:', {
                    status: response.status,
                    statusText: response.statusText,
                    body: errorText
                });
                throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
            }
            
            const data = await response.json();
            
            // کش کردن داده‌ها
            if (finalOptions.method === 'GET') {
                dataCache.set(endpoint, {
                    data,
                    timestamp: Date.now()
                });
            }
            
            return data;
        } catch (error) {
            clearTimeout(timeoutId);
            
            if (error.name === 'AbortError') {
                throw new Error('درخواست منقضی شد');
            }
            
            // بررسی کش در صورت خطا
            if (options.method === 'GET' || !options.method) {
                const cached = this.getCachedData(endpoint);
                if (cached) {
                    console.warn('استفاده از داده‌های کش شده به دلیل خطا در درخواست');
                    return cached;
                }
            }
            
            throw error;
        }
    }
    
    static getCachedData(endpoint) {
        const cached = dataCache.get(endpoint);
        if (cached && (Date.now() - cached.timestamp) < CONFIG.CACHE_DURATION) {
            return cached.data;
        }
        return null;
    }
    
    static async getRequest(endpoint) {
        return this.makeRequest(endpoint);
    }
    
    static async postRequest(endpoint, data) {
        return this.makeRequest(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
    
    static clearCache() {
        dataCache.clear();
    }
}

// کلاس مدیریت کاربران با قابلیت‌های پیشرفته
class UsersManager {
    constructor() {
        this.users = [];
        this.stats = {};
        this.isInitialized = false;
        this.performanceMetrics = {
            loadTimes: [],
            errorCount: 0,
            successCount: 0
        };
    }
    
    async loadUsers(filter = 'all') {
        if (isLoading) return;
        
        const startTime = performance.now();
        isLoading = true;
        retryCount = 0;
        
        try {
            this.showLoading();
            await this.fetchUsersWithRetry(filter);
            this.performanceMetrics.successCount++;
        } catch (error) {
            console.error('خطا در بارگذاری کاربران:', error);
            this.showError('خطا در بارگذاری اطلاعات کاربران: ' + error.message);
            this.performanceMetrics.errorCount++;
        } finally {
            isLoading = false;
            this.hideLoading();
            
            const loadTime = performance.now() - startTime;
            this.performanceMetrics.loadTimes.push(loadTime);
            
            if (CONFIG.PERFORMANCE_MONITORING) {
                console.log(`زمان بارگذاری: ${loadTime.toFixed(2)}ms`);
            }
        }
    }
    
    async fetchUsersWithRetry(filter) {
        try {
            let response;
            if (filter === 'all') {
                response = await ApiManager.getRequest('get_online_users');
            } else {
                response = await ApiManager.getRequest(`filter_by_time&filter=${filter}`);
            }
            
            if (response.success) {
                this.users = response.users || [];
                this.renderUsers();
                this.updateLastUpdateTime();
                retryCount = 0;
            } else {
                throw new Error(response.error || 'خطا در دریافت اطلاعات');
            }
        } catch (error) {
            retryCount++;
            
            if (retryCount < MAX_RETRY_COUNT) {
                console.warn(`تلاش مجدد ${retryCount}/${MAX_RETRY_COUNT}`);
                await this.delay(CONFIG.RETRY_DELAY * retryCount);
                return this.fetchUsersWithRetry(filter);
            }
            
            throw error;
        }
    }
    
    async loadStats() {
        try {
            const response = await ApiManager.getRequest('get_session_stats');
            
            if (response.success) {
                this.stats = response.stats;
                this.renderStats();
            }
        } catch (error) {
            console.error('خطا در بارگذاری آمار:', error);
        }
    }
    
    renderStats() {
        if (!this.stats) return;
        
        const animations = [
            { element: elements.activeSessionsCount, value: this.users.length || 0 },
            { element: elements.todayLoginsCount, value: this.stats.today_logins || 0 }
        ];
        
        animations.forEach(({ element, value }, index) => {
            if (element) {
                setTimeout(() => {
                    this.animateNumber(element, value);
                }, index * 100);
            }
        });
    }
    
    animateNumber(element, targetValue) {
        const currentValue = parseInt(element.textContent) || 0;
        const duration = 1000;
        const steps = 30;
        const stepValue = (targetValue - currentValue) / steps;
        const stepDuration = duration / steps;
        
        let currentStep = 0;
        
        const timer = setInterval(() => {
            currentStep++;
            const newValue = Math.round(currentValue + (stepValue * currentStep));
            element.textContent = newValue;
            
            if (currentStep >= steps) {
                clearInterval(timer);
                element.textContent = targetValue;
            }
        }, stepDuration);
    }
    
    renderUsers() {
        if (!elements.usersGrid) return;
        
        if (this.users.length === 0) {
            this.showEmptyState();
            return;
        }
        
        this.hideEmptyState();
        
        // استفاده از DocumentFragment برای بهبود عملکرد
        const fragment = document.createDocumentFragment();
        
        this.users.forEach((user, index) => {
            const userCard = this.createUserCard(user, index);
            fragment.appendChild(userCard);
        });
        
        // پاک کردن محتوای قبلی و اضافه کردن کارت‌های جدید
        elements.usersGrid.innerHTML = '';
        elements.usersGrid.appendChild(fragment);
        
        // اضافه کردن انیمیشن‌های تدریجی
        this.addStaggeredAnimations();
    }
    
    addStaggeredAnimations() {
        const cards = elements.usersGrid.querySelectorAll('.user-card');
        cards.forEach((card, index) => {
            card.style.animationDelay = `${index * 50}ms`;
            // انیمیشن حذف شده
        });
    }
    
    createUserCard(user, index) {
        const card = document.createElement('div');
        const userTypeColor = this.getUserTypeColor(user.userType);
        
        card.className = `user-card bg-white dark:bg-gray-800 rounded-2xl shadow-lg hover:shadow-xl 
                         transition-all duration-300 p-6 border border-gray-200 dark:border-gray-700 
                         hover-lift cursor-pointer glass-effect`;
        
        card.innerHTML = `
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <div class="flex items-center space-x-3 space-x-reverse mb-2">
                        <div class="relative">
                            <div class="w-12 h-12 bg-gradient-to-br from-${userTypeColor}-400 to-${userTypeColor}-600 
                                        rounded-full flex items-center justify-center text-white font-bold text-lg 
                                        shadow-lg">
                                ${user.username.charAt(0).toUpperCase()}
                            </div>
                        </div>
                        <div>
                            <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-1">
                                ${user.username}
                            </h3>
                            <span class="user-type ${user.userType} inline-block">
                                <span>${this.getUserTypeText(user.userType)}</span>
                            </span>
                        </div>
                    </div>
                    

                </div>
            </div>
            
            <div class="space-y-3 mb-4">
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-mobile-alt text-blue-600 dark:text-blue-400 text-xs"></i>
                    </div>
                    <span class="font-medium">${user.device_model || 'نامشخص'}</span>
                </div>
                
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-green-100 dark:bg-green-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-clock text-green-600 dark:text-green-400 text-xs"></i>
                    </div>
                    <span class="font-medium">${user.login_time_jalali}</span>
                </div>
                
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-purple-100 dark:bg-purple-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-network-wired text-purple-600 dark:text-purple-400 text-xs"></i>
                    </div>
                    <span class="font-medium font-mono">${user.ip_address}</span>
                </div>
            </div>
            
            <div class="flex space-x-3 space-x-reverse pt-4 border-t border-gray-200 dark:border-gray-700">
                <button class="action-btn btn-details flex-1" 
                        onclick="showUserDetails('${user.username}', '${user.device_id}')" 
                        title="مشاهده جزئیات">
                    <i class="fas fa-info-circle"></i>
                    <span>جزئیات</span>
                </button>
                
                <button class="action-btn btn-logout" 
                        onclick="confirmForceLogout('${user.username}', '${user.device_id}')" 
                        title="خروج اجباری">
                    <i class="fas fa-sign-out-alt"></i>
                    <span>خروج</span>
                </button>
            </div>
        `;
        
        // اضافه کردن event listener برای کلیک روی کارت
        card.addEventListener('click', (e) => {
            if (!e.target.closest('button')) {
                showUserDetails(user.username, user.device_id);
            }
        });
        
        return card;
    }
    
    getUserTypeText(userType) {
        const types = {
            'admin': 'مدیر سیستم',
            'operator': 'اپراتور',
            'verifier': 'تأیید کننده',
            'user': 'کاربر عادی'
        };
        return types[userType] || userType;
    }
    
    getUserTypeColor(userType) {
        const colors = {
            'admin': 'red',
            'operator': 'blue',
            'verifier': 'purple',
            'user': 'gray'
        };
        return colors[userType] || 'gray';
    }
    
    showLoading() {
        if (elements.loadingState) {
            elements.loadingState.classList.remove('hidden');
            // انیمیشن حذف شده
        }
        
        if (elements.usersGrid) {
            elements.usersGrid.classList.add('hidden');
        }
        
        if (elements.emptyState) {
            elements.emptyState.classList.add('hidden');
        }
        
        // انیمیشن آیکون بروزرسانی
        if (elements.refreshIcon) {
            elements.refreshIcon.classList.add('fa-spin');
        }
    }
    
    hideLoading() {
        if (elements.loadingState) {
            elements.loadingState.classList.add('hidden');
        }
        
        if (elements.usersGrid) {
            elements.usersGrid.classList.remove('hidden');
        }
        
        // توقف انیمیشن
        if (elements.refreshIcon) {
            elements.refreshIcon.classList.remove('fa-spin');
        }
    }
    
    showEmptyState() {
        if (elements.emptyState) {
            elements.emptyState.classList.remove('hidden');
            // انیمیشن حذف شده
        }
        
        if (elements.usersGrid) {
            elements.usersGrid.classList.add('hidden');
        }
    }
    
    hideEmptyState() {
        if (elements.emptyState) {
            elements.emptyState.classList.add('hidden');
        }
        
        if (elements.usersGrid) {
            elements.usersGrid.classList.remove('hidden');
        }
    }
    
    showError(message) {
        // ایجاد toast notification
        const toast = this.createToast(message, 'error');
        document.body.appendChild(toast);
        
        // حذف toast بعد از 5 ثانیه
        setTimeout(() => {
            toast.remove();
        }, 5000);
    }
    
    showSuccess(message) {
        const toast = this.createToast(message, 'success');
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
    
    createToast(message, type) {
        const toast = document.createElement('div');
        const bgColor = type === 'error' ? 'bg-red-500' : 'bg-green-500';
        const icon = type === 'error' ? 'fa-exclamation-triangle' : 'fa-check-circle';
        
        toast.className = `fixed top-4 right-4 ${bgColor} text-white px-6 py-4 rounded-lg shadow-lg z-50 
                          max-w-md`;
        
        toast.innerHTML = `
            <div class="flex items-center space-x-3 space-x-reverse">
                <i class="fas ${icon} text-lg"></i>
                <span class="font-medium">${message}</span>
                <button class="mr-2 hover:bg-white hover:bg-opacity-20 rounded p-1" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        
        return toast;
    }
    
    updateLastUpdateTime() {
        lastUpdateTime = new Date();
        if (elements.lastUpdate) {
            elements.lastUpdate.textContent = this.formatTime(lastUpdateTime);
        }
        
        // انیمیشن آیکون بروزرسانی
        if (elements.updateIcon) {
            // انیمیشن حذف شده
            setTimeout(() => {
                // انیمیشن حذف شده
            }, 1000);
        }
    }
    
    formatTime(date) {
        return date.toLocaleTimeString('fa-IR', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }
    
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
    
    getPerformanceMetrics() {
        const avgLoadTime = this.performanceMetrics.loadTimes.length > 0 
            ? this.performanceMetrics.loadTimes.reduce((a, b) => a + b) / this.performanceMetrics.loadTimes.length 
            : 0;
        
        return {
            averageLoadTime: avgLoadTime.toFixed(2),
            successRate: ((this.performanceMetrics.successCount / (this.performanceMetrics.successCount + this.performanceMetrics.errorCount)) * 100).toFixed(2),
            totalRequests: this.performanceMetrics.successCount + this.performanceMetrics.errorCount
        };
    }
    
    removeUserCard(username, deviceId) {
        try {
            // پیدا کردن تمام کارت‌های کاربر
            const userCards = elements.usersGrid.querySelectorAll('.user-card');
            
            userCards.forEach(card => {
                // بررسی محتوای کارت برای پیدا کردن کاربر مورد نظر
                const usernameElement = card.querySelector('h3');
                const logoutButton = card.querySelector('button[onclick*="confirmForceLogout"]');
                
                if (usernameElement && logoutButton) {
                    const cardUsername = usernameElement.textContent.trim();
                    const onclickAttr = logoutButton.getAttribute('onclick');
                    
                    // بررسی اینکه این کارت متعلق به کاربر مورد نظر است
                    if (cardUsername === username && onclickAttr.includes(deviceId)) {
                        // انیمیشن fade out
                        card.style.transition = 'all 0.3s ease-out';
                        card.style.opacity = '0';
                        card.style.transform = 'scale(0.95)';
                        
                        // حذف کارت بعد از انیمیشن
                        setTimeout(() => {
                            if (card.parentNode) {
                                card.remove();
                            }
                        }, 300);
                        
                        console.log(`کارت کاربر ${username} با device_id ${deviceId} حذف شد`);
                    }
                }
            });
            
            // حذف کاربر از آرایه کاربران
            this.users = this.users.filter(user => 
                !(user.username === username && user.device_id === deviceId)
            );
            
            // بروزرسانی فوری آمار
            this.renderStats();
            
        } catch (error) {
            console.error('خطا در حذف کارت کاربر:', error);
        }
    }
}

// کلاس مدیریت مودال با انیمیشن‌های پیشرفته
class ModalManager {
    static show(content) {
        if (elements.modalContent && elements.userModal) {
            elements.modalContent.innerHTML = content;
            elements.userModal.classList.remove('hidden');
            // انیمیشن حذف شده
            document.body.style.overflow = 'hidden';
            
            // فوکوس روی مودال برای دسترسی بهتر
            elements.userModal.focus();
        }
    }
    
    static hide() {
        if (elements.userModal) {
            elements.userModal.classList.add('hidden');
            // انیمیشن حذف شده
            document.body.style.overflow = 'auto';
        }
    }
    
    static showConfirm(message, onConfirm, onCancel) {
        const confirmModal = `
            <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 max-w-md mx-auto">
                <div class="text-center">
                    <div class="w-16 h-16 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center mx-auto mb-4">
                        <i class="fas fa-exclamation-triangle text-yellow-600 dark:text-yellow-400 text-2xl"></i>
                    </div>
                    <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-2">تأیید عملیات</h3>
                    <p class="text-gray-600 dark:text-gray-400 mb-6">${message}</p>
                    <div class="flex space-x-3 space-x-reverse">
                        <button class="modal-btn modal-btn-danger flex-1" onclick="${onConfirm}">
                            <i class="fas fa-check"></i>
                            <span>تأیید</span>
                        </button>
                        <button class="modal-btn modal-btn-secondary flex-1" onclick="${onCancel}">
                            <i class="fas fa-times"></i>
                            <span>انصراف</span>
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        this.show(confirmModal);
    }
}

// کلاس مدیریت بروزرسانی خودکار
class AutoRefreshManager {
    static start() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
        }
        
        autoRefreshInterval = setInterval(async () => {
            if (usersManager && !isLoading) {
                try {
                    await usersManager.loadUsers(currentFilter);
                    await usersManager.loadStats();
                } catch (error) {
                    console.error('خطا در بروزرسانی خودکار:', error);
                }
            }
        }, CONFIG.REFRESH_INTERVAL);
        
        console.log('بروزرسانی خودکار فعال شد');
    }
    
    static stop() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
            autoRefreshInterval = null;
            console.log('بروزرسانی خودکار غیرفعال شد');
        }
    }
    
    static isActive() {
        return autoRefreshInterval !== null;
    }
}

// کلاس مدیریت فیلترها
class FilterManager {
    static init() {
        elements.filterButtons.forEach(btn => {
            btn.addEventListener('click', this.handleFilterClick.bind(this));
        });
    }
    
    static handleFilterClick(e) {
        const button = e.currentTarget;
        const filter = button.dataset.filter;
        
        if (filter === currentFilter) return;
        
        // حذف کلاس active از همه دکمه‌ها
        elements.filterButtons.forEach(b => b.classList.remove('active'));
        
        // اضافه کردن کلاس active به دکمه کلیک شده
        button.classList.add('active');
        
        // تنظیم فیلتر جدید
        currentFilter = filter;
        
        // بارگذاری کاربران با فیلتر جدید
        usersManager.loadUsers(currentFilter);
        
        // ذخیره فیلتر در localStorage
        localStorage.setItem('selectedFilter', filter);
    }
    
    static restoreLastFilter() {
        const savedFilter = localStorage.getItem('selectedFilter');
        if (savedFilter) {
            currentFilter = savedFilter;
            const filterButton = document.querySelector(`[data-filter="${savedFilter}"]`);
            if (filterButton) {
                elements.filterButtons.forEach(b => b.classList.remove('active'));
                filterButton.classList.add('active');
            }
        }
    }
}

// نمونه‌های کلاس‌ها
const themeManager = new ThemeManager();
const usersManager = new UsersManager();

// اضافه کردن به window برای دسترسی سراسری
window.usersManager = usersManager;
window.themeManager = themeManager;

// توابع سراسری
window.showUserDetails = async function(username, deviceId) {
    try {
        const user = usersManager.users.find(u => u.username === username && u.device_id === deviceId);
        
        if (!user) {
            throw new Error('کاربر یافت نشد');
        }
        
        const userTypeColor = usersManager.getUserTypeColor(user.userType);
        
        const modalContent = `
            <div class="bg-white dark:bg-gray-800 rounded-2xl max-w-4xl mx-auto max-h-[90vh] overflow-y-auto">
                <!-- هدر مودال -->
                <div class="sticky top-0 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-6 rounded-t-2xl">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center space-x-4 space-x-reverse">
                            <div class="w-16 h-16 bg-gradient-to-br from-${userTypeColor}-400 to-${userTypeColor}-600 
                                        rounded-full flex items-center justify-center text-white font-bold text-2xl shadow-lg">
                                ${user.username.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <h2 class="text-2xl font-bold text-gray-900 dark:text-white">${user.username}</h2>
                                <p class="text-${userTypeColor}-600 dark:text-${userTypeColor}-400 font-semibold">
                                    ${usersManager.getUserTypeText(user.userType)}
                                </p>
                            </div>
                        </div>
                        <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 text-2xl" 
                                onclick="ModalManager.hide()">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
                
                <!-- محتوای مودال -->
                <div class="p-6">
                    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">

                        
                        <!-- کارت دستگاه -->
                        <div class="stats-card">
                            <div class="flex items-center justify-between p-2">
                                <div>
                                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">دستگاه</p>
                                    <p class="text-lg font-bold text-blue-600 dark:text-blue-400">
                                        ${user.device_model || 'نامشخص'}
                                    </p>
                                    <p class="text-xs text-gray-500 dark:text-gray-400 font-mono mt-2">
                                        ${user.device_id}
                                    </p>
                                </div>
                                <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-xl 
                                           flex items-center justify-center">
                                    <i class="fas fa-mobile-alt text-blue-600 dark:text-blue-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                        
                        <!-- کارت زمان ورود -->
                        <div class="stats-card">
                            <div class="flex items-center justify-between p-2">
                                <div>
                                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">زمان ورود</p>
                                    <p class="text-lg font-bold text-green-600 dark:text-green-400">
                                        ${user.login_time_jalali}
                                    </p>
                                </div>
                                <div class="w-12 h-12 bg-green-100 dark:bg-green-900 rounded-xl 
                                           flex items-center justify-center">
                                    <i class="fas fa-clock text-green-600 dark:text-green-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                        
                        ${user.last_activity_jalali ? `
                        <!-- کارت آخرین فعالیت -->
                        <div class="stats-card">
                            <div class="flex items-center justify-between p-2">
                                <div>
                                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">آخرین فعالیت</p>
                                    <p class="text-lg font-bold text-orange-600 dark:text-orange-400">
                                        ${user.last_activity_jalali}
                                    </p>
                                </div>
                                <div class="w-12 h-12 bg-orange-100 dark:bg-orange-900 rounded-xl 
                                           flex items-center justify-center">
                                    <i class="fas fa-history text-orange-600 dark:text-orange-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                        ` : ''}
                        
                        <!-- کارت آدرس IP -->
                        <div class="stats-card">
                            <div class="flex items-center justify-between p-2">
                                <div>
                                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">آدرس IP</p>
                                    <p class="text-lg font-bold text-purple-600 dark:text-purple-400 font-mono">
                                        ${user.ip_address}
                                    </p>
                                </div>
                                <div class="w-12 h-12 bg-purple-100 dark:bg-purple-900 rounded-xl 
                                           flex items-center justify-center">
                                    <i class="fas fa-network-wired text-purple-600 dark:text-purple-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- دکمه‌های عملیات -->
                    <div class="flex flex-col sm:flex-row gap-4 pt-6 border-t border-gray-200 dark:border-gray-700">
                        <button class="modal-btn modal-btn-danger flex-1" 
                                onclick="confirmForceLogout('${user.username}', '${user.device_id}')">
                            <i class="fas fa-sign-out-alt"></i>
                            <span>خروج اجباری</span>
                        </button>
                        
                        <button class="modal-btn modal-btn-secondary flex-1" 
                                onclick="ModalManager.hide()">
                            <i class="fas fa-times"></i>
                            <span>بستن</span>
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        ModalManager.show(modalContent);
    } catch (error) {
        console.error('خطا در نمایش جزئیات کاربر:', error);
        usersManager.showError('خطا در نمایش جزئیات کاربر');
    }
};

window.confirmForceLogout = function(username, deviceId) {
    ModalManager.showConfirm(
        `آیا مطمئن هستید که می‌خواهید کاربر "${username}" را از سیستم خارج کنید؟`,
        `forceLogout('${username}', '${deviceId}'); ModalManager.hide();`,
        'ModalManager.hide();'
    );
};

window.forceLogout = async function(username, deviceId) {
    try {
        console.log('Sending force logout request:', { username, device_id: deviceId });
        
        const response = await ApiManager.postRequest('force_logout', {
            username: username,
            device_id: deviceId
        });
        
        console.log('Force logout response:', response);
        
        if (response.success) {
            usersManager.showSuccess('کاربر با موفقیت از سیستم خارج شد');
            ModalManager.hide();
            
            // حذف فوری کارت کاربر از صفحه و بروزرسانی آمار
            usersManager.removeUserCard(username, deviceId);
            
            // بروزرسانی کامل لیست کاربران بعد از تاخیر کوتاه
            setTimeout(async () => {
                await usersManager.loadUsers(currentFilter);
            }, 1000);
        } else {
            throw new Error(response.message || response.error || 'خطا در خروج اجباری');
        }
    } catch (error) {
        console.error('خطا در خروج اجباری:', error);
        console.error('Error details:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        usersManager.showError('خطا در خروج اجباری کاربر: ' + error.message);
    }
};

// Event Listeners و راه‌اندازی اولیه
document.addEventListener('DOMContentLoaded', async function() {
    try {
        console.log('شروع راه‌اندازی سیستم مدیریت کاربران آنلاین...');
        
        // راه‌اندازی فیلترها
        FilterManager.init();
        FilterManager.restoreLastFilter();
        
        // بارگذاری اولیه داده‌ها
        await Promise.all([
            usersManager.loadUsers(currentFilter),
            usersManager.loadStats()
        ]);
        
        // تنظیم event listener برای دکمه بروزرسانی
        if (elements.refreshBtn) {
            elements.refreshBtn.addEventListener('click', async () => {
                await Promise.all([
                    usersManager.loadUsers(currentFilter),
                    usersManager.loadStats()
                ]);
            });
        }
        
        // تنظیم بروزرسانی خودکار
        if (elements.autoRefresh) {
            elements.autoRefresh.addEventListener('change', (e) => {
                if (e.target.checked) {
                    AutoRefreshManager.start();
                } else {
                    AutoRefreshManager.stop();
                }
                
                // ذخیره تنظیمات
                localStorage.setItem('autoRefresh', e.target.checked);
            });
            
            // بازیابی تنظیمات بروزرسانی خودکار
            const savedAutoRefresh = localStorage.getItem('autoRefresh');
            if (savedAutoRefresh !== null) {
                elements.autoRefresh.checked = savedAutoRefresh === 'true';
                if (elements.autoRefresh.checked) {
                    AutoRefreshManager.start();
                }
            } else {
                // پیش‌فرض: فعال
                elements.autoRefresh.checked = true;
                AutoRefreshManager.start();
            }
        }
        
        // تنظیم event listener برای بستن مودال
        if (elements.closeModal) {
            elements.closeModal.addEventListener('click', () => {
                ModalManager.hide();
            });
        }
        
        // بستن مودال با کلیک روی پس‌زمینه
        if (elements.userModal) {
            elements.userModal.addEventListener('click', (e) => {
                if (e.target === elements.userModal) {
                    ModalManager.hide();
                }
            });
        }
        
        // بستن مودال با کلید Escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                ModalManager.hide();
            }
        });
        
        // مدیریت visibility change برای توقف/شروع بروزرسانی
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                AutoRefreshManager.stop();
            } else if (elements.autoRefresh && elements.autoRefresh.checked) {
                AutoRefreshManager.start();
            }
        });
        
        console.log('سیستم مدیریت کاربران آنلاین با موفقیت راه‌اندازی شد');
        
        // نمایش آمار عملکرد در کنسول (فقط در حالت توسعه)
        if (CONFIG.PERFORMANCE_MONITORING) {
            setInterval(() => {
                const metrics = usersManager.getPerformanceMetrics();
                console.log('آمار عملکرد:', metrics);
            }, 60000); // هر دقیقه
        }
        
    } catch (error) {
        console.error('خطا در راه‌اندازی سیستم:', error);
        usersManager.showError('خطا در راه‌اندازی سیستم: ' + error.message);
    }
});

// مدیریت خطاهای سراسری
window.addEventListener('error', (e) => {
    console.error('خطای سراسری:', e.error);
    if (usersManager) {
        usersManager.showError('خطای غیرمنتظره رخ داد');
    }
});

// مدیریت خطاهای Promise
window.addEventListener('unhandledrejection', (e) => {
    console.error('خطای Promise:', e.reason);
    if (usersManager) {
        usersManager.showError('خطا در پردازش درخواست');
    }
    e.preventDefault();
});