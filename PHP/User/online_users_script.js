/**
 * فایل جاوا اسکریپت مدیریت کاربران آنلاین - نسخه 2.0
 * بازطراحی شده با معماری مدرن و بهینه‌سازی عملکرد
 */

// متغیرهای سراسری
let currentTimeFilter = 'all';
let currentStatusFilter = 'all';
let currentUserTypeFilter = 'all';
let currentSearchQuery = '';
let autoRefreshInterval = null;
let isLoading = false;
let lastUpdateTime = null;
let retryCount = 0;
let allUsers = []; // ذخیره همه کاربران برای جستجو
let filteredUsers = []; // کاربران فیلتر شده
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
    statsModal: document.getElementById('statsModal'),
    statsModalContent: document.getElementById('statsModalContent'),
    closeStatsModal: document.getElementById('closeStatsModal'),
    
    // جستجو
    searchInput: document.getElementById('searchInput'),
    clearSearch: document.getElementById('clearSearch'),
    searchResults: document.getElementById('searchResults'),
    searchResultsText: document.getElementById('searchResultsText'),
    
    // عملیات
    clearAllFilters: document.getElementById('clearAllFilters'),
    exportCSV: document.getElementById('exportCSV'),
    showAdvancedStats: document.getElementById('showAdvancedStats'),
    
    // فیلترها
    timeFilterButtons: document.querySelectorAll('.time-filter'),
    statusFilterButtons: document.querySelectorAll('.status-filter'),
    userTypeFilterButtons: document.querySelectorAll('.user-type-filter'),
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
        this.advancedStatsManager = new AdvancedStatsManager();
    }
    
    async loadUsers(timeFilter = null, statusFilter = null, userTypeFilter = null) {
        if (isLoading) return;
        
        // استفاده از فیلترهای فعلی اگر پارامتر ارسال نشده
        timeFilter = timeFilter || currentTimeFilter;
        statusFilter = statusFilter || currentStatusFilter;
        userTypeFilter = userTypeFilter || currentUserTypeFilter;
        
        const startTime = performance.now();
        isLoading = true;
        retryCount = 0;
        
        try {
            this.showLoading();
            await this.fetchUsersWithRetry(timeFilter, statusFilter, userTypeFilter);
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
    
    async fetchUsersWithRetry(timeFilter, statusFilter, userTypeFilter) {
        try {
            let response;
            
            // ابتدا همه جلسات (آنلاین و آفلاین) را دریافت کن
            let endpoint = 'get_all_sessions';
            let params = [];
            
            if (timeFilter && timeFilter !== 'all') {
                params.push(`time_filter=${timeFilter}`);
            }
            
            if (statusFilter && statusFilter !== 'all') {
                params.push(`status_filter=${statusFilter}`);
            }
            
            if (userTypeFilter && userTypeFilter !== 'all') {
                params.push(`user_type_filter=${userTypeFilter}`);
            }
            
            if (params.length > 0) {
                endpoint += '&' + params.join('&');
            }
            
            response = await ApiManager.getRequest(endpoint);
            
            if (response.success) {
                allUsers = response.sessions || response.users || [];
                
                this.users = [...allUsers];
                this.applyAllFilters();
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
                return this.fetchUsersWithRetry(timeFilter, statusFilter, userTypeFilter);
            }
            
            throw error;
        }
    }
    

    

    

    
    applyAllFilters() {
        let filtered = [...allUsers];
        
        // فیلتر بر اساس وضعیت
        if (currentStatusFilter && currentStatusFilter !== 'all') {
            filtered = filtered.filter(user => {
                const isOnline = !user.logout_time && !user.logout_time_jalali;
                if (currentStatusFilter === 'online') {
                    return isOnline;
                } else if (currentStatusFilter === 'offline') {
                    return !isOnline;
                }
                return true;
            });
        }
        
        // فیلتر بر اساس نوع کاربر
        if (currentUserTypeFilter && currentUserTypeFilter !== 'all') {
            filtered = filtered.filter(user => {
                const userType = user.userType || user.user_type || 'user';
                // Map verifier to approver for compatibility
                if (currentUserTypeFilter === 'verifier') {
                    return userType === 'verifier' || userType === 'approver';
                }
                return userType === currentUserTypeFilter;
            });
        }
        
        // فیلتر بر اساس جستجو
        if (currentSearchQuery && currentSearchQuery.trim() !== '') {
            const query = currentSearchQuery.toLowerCase().trim();
            filtered = filtered.filter(user => {
                const username = (user.username || user.user_name || '').toLowerCase();
                const userType = (user.userType || user.user_type || '').toLowerCase();
                const deviceModel = (user.device_model || '').toLowerCase();
                const ipAddress = (user.ip_address || '').toLowerCase();
                
                return username.includes(query) || 
                       userType.includes(query) || 
                       deviceModel.includes(query) || 
                       ipAddress.includes(query);
            });
        }
        
        this.users = filtered;
        filteredUsers = [...filtered];
        this.updateSearchResults();
    }
    
    updateSearchResults() {
        if (elements.searchResults) {
            const totalUsers = allUsers.length;
            const filteredCount = this.users.length;
            
            if (currentSearchQuery || currentUserTypeFilter !== 'all' || currentStatusFilter !== 'all') {
                elements.searchResults.innerHTML = `
                    <div class="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-3 mb-4">
                        <div class="flex items-center justify-between">
                            <span class="text-blue-700 dark:text-blue-300">
                                <i class="fas fa-filter ml-2"></i>
                                نمایش ${filteredCount} از ${totalUsers} کاربر
                            </span>
                            <button onclick="clearAllFilters()" class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-200 text-sm">
                                <i class="fas fa-times ml-1"></i>
                                پاک کردن فیلترها
                            </button>
                        </div>
                    </div>
                `;
                elements.searchResults.style.display = 'block';
            } else {
                elements.searchResults.style.display = 'none';
            }
        }
    }
    
    filterUsersByStatus(statusFilter) {
        if (statusFilter === 'all') {
            return; // نمایش همه کاربران
        }
        
        this.users = this.users.filter(user => {
            const isOnline = !user.logout_time && !user.logout_time_jalali;
            if (statusFilter === 'online') {
                return isOnline;
            } else if (statusFilter === 'offline') {
                return !isOnline;
            }
            return true;
        });
    }
    
    async loadStats() {
        try {
            this.stats = this.advancedStatsManager.calculateAdvancedStats();
            this.renderStats();
        } catch (error) {
            console.error('خطا در بارگذاری آمار:', error);
        }
    }
    
    renderStats() {
        if (!this.stats) return;
        
        // محاسبه کاربران فعال (آنلاین)
        const activeUsers = this.calculateActiveUsers();
        
        // محاسبه ورودی‌های امروز
        const todayLogins = this.calculateTodayLogins();
        
        const animations = [
            { element: elements.activeSessionsCount, value: activeUsers },
            { element: elements.todayLoginsCount, value: todayLogins }
        ];
        
        animations.forEach(({ element, value }, index) => {
            if (element) {
                setTimeout(() => {
                    this.animateNumber(element, value);
                }, index * 100);
            }
        });
    }
    
    calculateActiveUsers() {
        if (!allUsers || allUsers.length === 0) return 0;
        
        return allUsers.filter(user => {
            // کاربر آنلاین است اگر logout_time نداشته باشد
            return !user.logout_time && !user.logout_time_jalali;
        }).length;
    }
    

    calculateTodayLogins() {
        if (!allUsers || allUsers.length === 0) return 0;
        
        const today = new Date();
        const todayString = today.toISOString().split('T')[0]; // YYYY-MM-DD format
        
        // محاسبه تاریخ شمسی امروز
        const todayJalali = this.getCurrentJalaliDate();
        
        return allUsers.filter(user => {
            if (!user.login_time && !user.login_time_jalali) return false;
            
            // بررسی تاریخ میلادی
            if (user.login_time) {
                const loginDate = new Date(user.login_time);
                const loginDateString = loginDate.toISOString().split('T')[0];
                if (loginDateString === todayString) return true;
            }
            
            // بررسی تاریخ شمسی
            if (user.login_time_jalali) {
                const loginJalali = user.login_time_jalali.split(' ')[0]; // فقط تاریخ
                if (loginJalali === todayJalali) return true;
            }
            
            return false;
        }).length;
    }
    
    getCurrentJalaliDate() {
        const now = new Date();
        // تبدیل ساده تاریخ میلادی به شمسی (برای مثال)
        // در صورت نیاز می‌توان از کتابخانه moment-jalaali استفاده کرد
        const year = now.getFullYear();
        const month = now.getMonth() + 1;
        const day = now.getDate();
        
        // تبدیل تقریبی به شمسی (این روش دقیق نیست و باید با کتابخانه مناسب جایگزین شود)
        const jalaliYear = year - 621;
        const jalaliMonth = month.toString().padStart(2, '0');
        const jalaliDay = day.toString().padStart(2, '0');
        
        return `${jalaliYear}/${jalaliMonth}/${jalaliDay}`;
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
        
        // دسته‌بندی کاربران بر اساس تاریخ ورود
        const groupedUsers = this.groupUsersByDate(this.users);
        
        // استفاده از DocumentFragment برای بهبود عملکرد
        const fragment = document.createDocumentFragment();
        
        // ایجاد بخش‌های دسته‌بندی شده
        Object.keys(groupedUsers).forEach((date, groupIndex) => {
            const dateSection = this.createDateSection(date, groupedUsers[date], groupIndex === 0);
            fragment.appendChild(dateSection);
            
            // اضافه کردن کارت‌های کاربر به بخش تاریخ
            const sectionId = `section-${date.replace(/[^a-zA-Z0-9]/g, '-')}`;
            const contentDiv = dateSection.querySelector(`#${sectionId}-content`);
            groupedUsers[date].forEach((user, index) => {
                const userCard = this.createUserCard(user, index);
                contentDiv.appendChild(userCard);
            });
        });
        
        // پاک کردن محتوای قبلی و اضافه کردن بخش‌های جدید
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
    
    groupUsersByDate(users) {
        const groups = {};
        
        users.forEach(user => {
            const loginDate = user.login_time_jalali || user.login_time || 'نامشخص';
            const dateOnly = loginDate.split(' ')[0]; // فقط تاریخ بدون ساعت
            
            if (!groups[dateOnly]) {
                groups[dateOnly] = [];
            }
            groups[dateOnly].push(user);
        });
        
        // مرتب‌سازی تاریخ‌ها (جدیدترین اول)
        const sortedDates = Object.keys(groups).sort((a, b) => {
            if (a === 'نامشخص') return 1;
            if (b === 'نامشخص') return -1;
            return new Date(b) - new Date(a);
        });
        
        const sortedGroups = {};
        sortedDates.forEach(date => {
            sortedGroups[date] = groups[date];
        });
        
        return sortedGroups;
    }
    
    createDateSection(date, users, isExpanded = false) {
        const section = document.createElement('div');
        section.className = 'date-section mb-3 w-full';
        
        const sectionId = `section-${date.replace(/[^a-zA-Z0-9]/g, '-')}`;
        
        section.innerHTML = `
            <div class="date-header bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg p-4 mb-4 border border-blue-200/50 dark:border-blue-700/50 cursor-pointer" 
                 onclick="toggleDateSection('${sectionId}')">
                <div class="flex items-center justify-between">
                    <div class="flex items-center space-x-3 space-x-reverse">
                        <div class="bg-blue-500 p-2 rounded-lg">
                            <i class="fas fa-calendar-day text-white"></i>
                        </div>
                        <div>
                            <h3 class="text-lg font-bold text-gray-900 dark:text-white">${date}</h3>
                            <p class="text-sm text-gray-600 dark:text-gray-400">${users.length} کاربر</p>
                        </div>
                    </div>
                    <div class="flex items-center space-x-2 space-x-reverse">

                        <i class="fas fa-chevron-${isExpanded ? 'up' : 'down'} text-gray-500 dark:text-gray-400 transition-transform duration-300" id="${sectionId}-icon"></i>
                    </div>
                </div>
            </div>
            <div class="date-content grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6 ${isExpanded ? '' : 'hidden'}" id="${sectionId}-content">
            </div>
        `;
        
        return section;
    }
    
    createUserCard(user, index) {
        const card = document.createElement('div');
        const userTypeColor = this.getUserTypeColor(user.userType || user.user_type);
        const isOnline = !user.logout_time && !user.logout_time_jalali;
        const statusColor = isOnline ? 'green' : 'red';
        const statusText = isOnline ? 'آنلاین' : 'آفلاین';
        const statusIcon = isOnline ? 'fa-circle' : 'fa-circle';
        
        card.className = `user-card bg-white dark:bg-gray-800 rounded-2xl shadow-lg hover:shadow-xl 
                         transition-all duration-300 p-6 border border-gray-200 dark:border-gray-700 
                         hover-lift cursor-pointer glass-effect w-full 
                         ${isOnline ? 'border-l-4 border-l-green-500' : 'border-l-4 border-l-red-500'}`;
        

        
        card.innerHTML = `
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <div class="flex items-center space-x-3 space-x-reverse mb-2">
                        <div class="relative">
                            <div class="w-12 h-12 bg-gradient-to-br from-${userTypeColor}-400 to-${userTypeColor}-600 
                                        rounded-full flex items-center justify-center text-white font-bold text-lg 
                                        shadow-lg">
                                ${(user.username || user.user_name || '').charAt(0).toUpperCase()}
                            </div>
                            <div class="absolute -bottom-1 -right-1 w-4 h-4 bg-${statusColor}-500 rounded-full border-2 border-white dark:border-gray-800 animate-pulse"></div>
                        </div>
                        <div>
                            <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-1">
                                ${user.username || user.user_name || 'نامشخص'}
                            </h3>
                            <div class="flex items-center space-x-2 space-x-reverse">
                                <span class="user-type ${user.userType || user.user_type} inline-flex items-center">
                                    <span>${this.getUserTypeText(user.userType || user.user_type)}</span>
                                </span>
                                <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-${statusColor}-100 text-${statusColor}-800 dark:bg-${statusColor}-900 dark:text-${statusColor}-200">
                                    <i class="fas ${statusIcon} text-${statusColor}-500 text-xs ml-1"></i>
                                    ${statusText}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="space-y-3 mb-4">
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-mobile-alt text-blue-600 dark:text-blue-400 text-xs"></i>
                    </div>
                    <span class="font-medium">${user.device_model || user.device || 'نامشخص'}</span>
                </div>
                
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-green-100 dark:bg-green-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-sign-in-alt text-green-600 dark:text-green-400 text-xs"></i>
                    </div>
                    <span class="font-medium">${user.login_time_jalali || user.login_time || 'نامشخص'}</span>
                </div>
                
                ${user.logout_time_jalali ? `
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-red-100 dark:bg-red-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-sign-out-alt text-red-600 dark:text-red-400 text-xs"></i>
                    </div>
                    <span class="font-medium">${user.logout_time_jalali}</span>
                </div>
                ` : ''}
                
                <div class="flex items-center space-x-2 space-x-reverse text-sm text-gray-600 dark:text-gray-400">
                    <div class="w-8 h-8 bg-orange-100 dark:bg-orange-900 rounded-lg flex items-center justify-center">
                        <i class="fas fa-network-wired text-orange-600 dark:text-orange-400 text-xs"></i>
                    </div>
                    <span class="font-medium font-mono">${user.ip_address || user.ip || 'نامشخص'}</span>
                </div>
                

            </div>
            
            <div class="flex space-x-3 space-x-reverse pt-4 border-t border-gray-200 dark:border-gray-700">
                <button class="action-btn btn-details flex-1" 
                        onclick="showUserDetails('${user.username || user.user_name}', '${user.device_id || user.session_id}')" 
                        title="مشاهده جزئیات">
                    <i class="fas fa-info-circle"></i>
                    <span>جزئیات</span>
                </button>
                
                ${isOnline ? `
                <button class="action-btn btn-logout" 
                        onclick="confirmForceLogout('${user.username || user.user_name}', '${user.device_id || user.session_id}')" 
                        title="خروج اجباری">
                    <i class="fas fa-sign-out-alt"></i>
                    <span>خروج</span>
                </button>
                ` : `
                <button class="action-btn btn-details" 
                        title="جلسه خاتمه یافته" disabled>
                    <i class="fas fa-check-circle"></i>
                    <span>خاتمه یافته</span>
                </button>
                `}
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
            'approver': 'تأیید کننده',
            'verifier': 'تأیید کننده'
        };
        return types[userType] || userType;
    }
    
    getUserTypeColor(userType) {
        const colors = {
            'admin': 'red',
            'operator': 'blue',
            'approver': 'purple',
            'verifier': 'purple'
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
                    await usersManager.loadUsers(currentTimeFilter, currentStatusFilter);
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
// کلاس مدیریت جستجو
class SearchManager {
    constructor() {
        this.searchTimeout = null;
        this.initializeSearch();
    }
    
    initializeSearch() {
        if (elements.searchInput) {
            elements.searchInput.addEventListener('input', (e) => {
                clearTimeout(this.searchTimeout);
                this.searchTimeout = setTimeout(() => {
                    this.handleSearch(e.target.value);
                }, 300);
            });
            
            elements.searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    clearTimeout(this.searchTimeout);
                    this.handleSearch(e.target.value);
                }
            });
        }
    }
    
    handleSearch(query) {
        currentSearchQuery = query;
        usersManager.applyAllFilters();
        usersManager.renderUsers();
    }
    
    clearSearch() {
        if (elements.searchInput) {
            elements.searchInput.value = '';
            currentSearchQuery = '';
            usersManager.applyAllFilters();
            usersManager.renderUsers();
        }
    }
}

// کلاس مدیریت آمار پیشرفته
class AdvancedStatsManager {
    constructor() {
        this.stats = {};
    }
    
    calculateAdvancedStats() {
        if (!allUsers || allUsers.length === 0) {
            return {
                totalUsers: 0,
                onlineUsers: 0,
                offlineUsers: 0,
                adminUsers: 0,
                operatorUsers: 0,
                approverUsers: 0,
                regularUsers: 0,
                guestUsers: 0,
                deviceTypes: {},
                loginTrends: []
            };
        }
        
        const stats = {
            totalUsers: allUsers.length,
            onlineUsers: 0,
            offlineUsers: 0,
            adminUsers: 0,
            operatorUsers: 0,
            approverUsers: 0,
            regularUsers: 0,
            guestUsers: 0,
            deviceTypes: {},
            loginTrends: []
        };
        

        
        allUsers.forEach(user => {
            // شمارش وضعیت‌ها
            const isOnline = !user.logout_time && !user.logout_time_jalali;
            if (isOnline) {
                stats.onlineUsers++;
            } else {
                stats.offlineUsers++;
            }
            
            // شمارش نوع کاربران
            const userType = user.userType || user.user_type || user.role || 'user';
            const userTypeStr = String(userType).toLowerCase();
            
            if (userTypeStr === 'admin' || userTypeStr === 'مدیر') {
                stats.adminUsers++;
            } else if (userTypeStr === 'operator' || userTypeStr === 'اپراتور') {
                stats.operatorUsers++;
            } else if (userTypeStr === 'approver' || userTypeStr === 'تائید کننده' || userTypeStr === 'تأیید کننده' || userTypeStr === 'تایید کننده') {
                stats.approverUsers++;
            } else if (userTypeStr === 'guest' || userTypeStr === 'مهمان') {
                stats.guestUsers++;
            } else {
                stats.regularUsers++;
            }
            
            // شمارش انواع دستگاه
            const deviceModel = user.device_model || 'نامشخص';
            stats.deviceTypes[deviceModel] = (stats.deviceTypes[deviceModel] || 0) + 1;
            


        });
        


        
        this.stats = stats;
        return stats;
    }
    
    showAdvancedStats() {
        const stats = this.calculateAdvancedStats();
        
        const statsModal = document.getElementById('statsModal');
        const statsModalContent = document.getElementById('statsModalContent');
        
        if (statsModal && statsModalContent) {
            // به‌روزرسانی محتوای مودال
            statsModalContent.innerHTML = `
                <!-- آمار کلی -->
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                    <div class="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-6 text-white transform hover:scale-105 transition-all duration-300">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-blue-100 text-sm font-medium">کل کاربران</p>
                                <p class="text-3xl font-bold mt-2">${stats.totalUsers.toLocaleString()}</p>
                            </div>
                            <div class="bg-blue-400/30 p-3 rounded-lg">
                                <i class="fas fa-users text-2xl"></i>
                            </div>
                        </div>
                    </div>
                    
                    <div class="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 text-white transform hover:scale-105 transition-all duration-300">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-green-100 text-sm font-medium">کاربران آنلاین</p>
                                <p class="text-3xl font-bold mt-2">${stats.onlineUsers.toLocaleString()}</p>
                            </div>
                            <div class="bg-green-400/30 p-3 rounded-lg">
                                <i class="fas fa-circle text-2xl"></i>
                            </div>
                        </div>
                    </div>
                    
                    <div class="bg-gradient-to-br from-red-500 to-red-600 rounded-xl p-6 text-white transform hover:scale-105 transition-all duration-300">
                        <div class="flex items-center justify-between">
                            <div>
                                <p class="text-red-100 text-sm font-medium">کاربران آفلاین</p>
                                <p class="text-3xl font-bold mt-2">${stats.offlineUsers.toLocaleString()}</p>
                            </div>
                            <div class="bg-red-400/30 p-3 rounded-lg">
                                <i class="fas fa-user-slash text-2xl"></i>
                            </div>
                        </div>
                    </div>
                    

                </div>
                
                <!-- آمار دستگاه‌ها -->
                <div class="bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-700 rounded-xl p-6 border border-gray-200/50 dark:border-gray-600/50 mb-8">
                        <h4 class="text-xl font-bold text-gray-900 dark:text-white mb-6 flex items-center">
                            <div class="bg-green-500 p-2 rounded-lg ml-3">
                                <i class="fas fa-mobile-alt text-white"></i>
                            </div>
                            انواع دستگاه‌ها
                        </h4>
                        <div class="space-y-3">
                            ${Object.entries(stats.deviceTypes)
                                .sort(([,a], [,b]) => b - a)
                                .slice(0, 6)
                                .map(([device, count], index) => {
                                    const deviceUsers = allUsers.filter(user => (user.device_model || 'نامشخص') === device);
                                    const deviceId = `device-${index}`;
                                    return `
                                        <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-600">
                                            <div class="flex items-center justify-between p-3 cursor-pointer" onclick="toggleDeviceUsers('${deviceId}')">
                                                <div class="flex items-center space-x-2 space-x-reverse">
                                                    <i id="${deviceId}-icon" class="fas fa-chevron-down text-gray-500 transition-transform duration-200"></i>
                                                    <span class="font-medium text-gray-700 dark:text-gray-300">${device || 'نامشخص'}</span>
                                                </div>
                                                <span class="bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 px-3 py-1 rounded-full text-sm font-bold">${count}</span>
                                            </div>
                                            <div id="${deviceId}" class="hidden border-t border-gray-200 dark:border-gray-600">
                                                <div class="p-3 space-y-2 max-h-40 overflow-y-auto">
                                                    ${deviceUsers.map(user => `
                                                        <div class="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-700 rounded text-sm">
                                                            <span class="text-gray-700 dark:text-gray-300">${user.username || 'نامشخص'}</span>
                                                            <span class="text-xs px-2 py-1 rounded ${
                                                                !user.logout_time && !user.logout_time_jalali 
                                                                    ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' 
                                                                    : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                                                            }">
                                                                ${!user.logout_time && !user.logout_time_jalali ? 'آنلاین' : 'آفلاین'}
                                                            </span>
                                                        </div>
                                                    `).join('')}
                                                </div>
                                            </div>
                                        </div>
                                    `;
                                }).join('')}
                            ${Object.keys(stats.deviceTypes).length === 0 ? '<div class="text-center text-gray-500 dark:text-gray-400 py-4">هیچ اطلاعاتی در دسترس نیست</div>' : ''}
                        </div>
                    </div>
                
                <!-- آمار زمانی -->

                
                <!-- دکمه‌های عملیاتی -->
                <div class="mt-8 flex justify-center space-x-4 space-x-reverse">
                    <button onclick="window.print()" class="bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white px-6 py-3 rounded-lg transition-all duration-300 hover:scale-105 shadow-lg flex items-center space-x-2 space-x-reverse">
                        <i class="fas fa-print"></i>
                        <span>چاپ گزارش</span>
                    </button>
                    <button onclick="csvExportManager.exportToCSV()" class="bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white px-6 py-3 rounded-lg transition-all duration-300 hover:scale-105 shadow-lg flex items-center space-x-2 space-x-reverse">
                        <i class="fas fa-download"></i>
                        <span>دانلود CSV</span>
                    </button>
                </div>
            `;
            
            // نمایش مودال
            statsModal.classList.remove('hidden');
            
            // اضافه کردن event listener برای بستن مودال
            const closeStatsModal = document.getElementById('closeStatsModal');
            if (closeStatsModal) {
                closeStatsModal.onclick = () => {
                    statsModal.classList.add('hidden');
                };
            }
            
            // بستن مودال با کلیک روی پس‌زمینه
            statsModal.onclick = (e) => {
                if (e.target === statsModal) {
                    statsModal.classList.add('hidden');
                }
            };
        }
    }
}

// تابع برای تغییر وضعیت نمایش کاربران هر دستگاه
function toggleDeviceUsers(deviceId) {
    const deviceSection = document.getElementById(deviceId);
    const deviceIcon = document.getElementById(deviceId + '-icon');
    
    if (deviceSection && deviceIcon) {
        if (deviceSection.classList.contains('hidden')) {
            deviceSection.classList.remove('hidden');
            deviceIcon.classList.remove('fa-chevron-down');
            deviceIcon.classList.add('fa-chevron-up');
        } else {
            deviceSection.classList.add('hidden');
            deviceIcon.classList.remove('fa-chevron-up');
            deviceIcon.classList.add('fa-chevron-down');
        }
    }
}

// کلاس مدیریت خروجی CSV
class CSVExportManager {
    constructor() {
        this.initializeExport();
    }
    
    initializeExport() {
        // تنظیم event listener برای دکمه خروجی
    }
    
    exportToCSV() {
        const dataToExport = filteredUsers.length > 0 ? filteredUsers : allUsers;
        
        if (!dataToExport || dataToExport.length === 0) {
            alert('هیچ داده‌ای برای خروجی وجود ندارد!');
            return;
        }
        
        // تعریف هدرهای CSV
        const headers = [
            'نام کاربری',
            'نوع کاربر', 
            'وضعیت',
            'مدل دستگاه',
            'زمان ورود',
            'زمان خروج',
            'آدرس IP',
            'مدت جلسه (دقیقه)'
        ];
        
        // تبدیل داده‌ها به فرمت CSV
        const csvContent = [headers.join(',')];
        
        dataToExport.forEach(user => {
            const isOnline = !user.logout_time && !user.logout_time_jalali;
            const status = isOnline ? 'آنلاین' : 'آفلاین';
            
            // محاسبه مدت جلسه
            let sessionDuration = 'نامشخص';
            if (user.login_time && user.logout_time) {
                const loginTime = new Date(user.login_time);
                const logoutTime = new Date(user.logout_time);
                const duration = Math.round((logoutTime - loginTime) / (1000 * 60));
                sessionDuration = duration > 0 ? duration.toString() : 'نامشخص';
            }
            
            const row = [
                this.escapeCSV(user.username || user.user_name || ''),
                this.escapeCSV(this.getUserTypeLabel(user.userType || user.user_type)),
                this.escapeCSV(status),
                this.escapeCSV(user.device_model || ''),
                this.escapeCSV(user.login_time_jalali || user.login_time || ''),
                this.escapeCSV(user.logout_time_jalali || user.logout_time || ''),
                this.escapeCSV(user.ip_address || ''),
                this.escapeCSV(sessionDuration)
            ];
            
            csvContent.push(row.join(','));
        });
        
        // ایجاد فایل و دانلود
        const csvString = '\uFEFF' + csvContent.join('\n'); // اضافه کردن BOM برای پشتیبانی از UTF-8
        const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' });
        
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        
        const now = new Date();
        const timestamp = now.toISOString().slice(0, 19).replace(/:/g, '-');
        link.setAttribute('download', `users_report_${timestamp}.csv`);
        
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // نمایش پیام موفقیت
        this.showExportSuccess(dataToExport.length);
    }
    
    escapeCSV(field) {
        if (field === null || field === undefined) {
            return '';
        }
        
        const stringField = String(field);
        
        // اگر فیلد شامل کاما، نقل قول یا خط جدید باشد، آن را در نقل قول قرار دهید
        if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n')) {
            return '"' + stringField.replace(/"/g, '""') + '"';
        }
        
        return stringField;
    }
    
    getUserTypeLabel(userType) {
        const typeLabels = {
            'admin': 'مدیر',
            'user': 'کاربر عادی',
            'guest': 'مهمان'
        };
        
        return typeLabels[userType] || userType || 'نامشخص';
    }
    
    showExportSuccess(count) {
        // ایجاد toast notification
        const toast = document.createElement('div');
        toast.className = 'fixed top-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 transform translate-x-full transition-transform duration-300';
        toast.innerHTML = `
            <div class="flex items-center">
                <i class="fas fa-check-circle ml-2"></i>
                <span>خروجی ${count} کاربر با موفقیت ایجاد شد</span>
            </div>
        `;
        
        document.body.appendChild(toast);
        
        // نمایش toast
        setTimeout(() => {
            toast.classList.remove('translate-x-full');
        }, 100);
        
        // حذف toast بعد از 3 ثانیه
        setTimeout(() => {
            toast.classList.add('translate-x-full');
            setTimeout(() => {
                document.body.removeChild(toast);
            }, 300);
        }, 3000);
    }
}

class FilterManager {
    static init() {
        elements.filterButtons.forEach(btn => {
            btn.addEventListener('click', this.handleFilterClick.bind(this));
        });
    }
    
    static handleFilterClick(e) {
        const button = e.currentTarget;
        const filter = button.dataset.filter;
        const filterType = button.dataset.filterType || 'status';
        
        if (filterType === 'status') {
             if (filter === currentStatusFilter) return;
             
             // حذف کلاس active از همه دکمه‌های وضعیت
             elements.filterButtons.forEach(b => {
                 if (!b.dataset.filterType || b.dataset.filterType === 'status') {
                     b.classList.remove('active');
                 }
             });
             
             // اضافه کردن کلاس active به دکمه کلیک شده
             button.classList.add('active');
             
             // تنظیم فیلتر وضعیت جدید
             currentStatusFilter = filter;
             
             // ذخیره فیلتر در localStorage
             localStorage.setItem('selectedFilter', filter);
        } else if (filterType === 'userType') {
            if (filter === currentUserTypeFilter) return;
            
            // حذف کلاس active از همه دکمه‌های نوع کاربر
            elements.filterButtons.forEach(b => {
                if (b.dataset.filterType === 'userType') {
                    b.classList.remove('active');
                }
            });
            
            // اضافه کردن کلاس active به دکمه کلیک شده
            button.classList.add('active');
            
            // تنظیم فیلتر نوع کاربر جدید
            currentUserTypeFilter = filter;
            
            // ذخیره فیلتر در localStorage
            localStorage.setItem('selectedUserTypeFilter', filter);
        }
        
        // بارگذاری کاربران با فیلترهای جدید
         usersManager.loadUsers(currentTimeFilter, currentStatusFilter, currentUserTypeFilter);
    }
    
    static restoreLastFilter() {
        const savedFilter = localStorage.getItem('selectedFilter');
        const savedUserTypeFilter = localStorage.getItem('selectedUserTypeFilter');
        
        if (savedFilter) {
             currentStatusFilter = savedFilter;
             const filterButton = document.querySelector(`[data-filter="${savedFilter}"][data-filter-type="status"], [data-filter="${savedFilter}"]:not([data-filter-type])`);
             if (filterButton) {
                 filterButton.classList.add('active');
             }
         }
        
        if (savedUserTypeFilter) {
            currentUserTypeFilter = savedUserTypeFilter;
            const userTypeButton = document.querySelector(`[data-filter="${savedUserTypeFilter}"][data-filter-type="userType"]`);
            if (userTypeButton) {
                userTypeButton.classList.add('active');
            }
        }
    }
}

// نمونه‌های کلاس‌ها
const themeManager = new ThemeManager();
const usersManager = new UsersManager();
const searchManager = new SearchManager();
const advancedStatsManager = new AdvancedStatsManager();
const csvExportManager = new CSVExportManager();

// اضافه کردن به window برای دسترسی سراسری
window.usersManager = usersManager;
window.themeManager = themeManager;
window.searchManager = searchManager;
window.advancedStatsManager = advancedStatsManager;
window.csvExportManager = csvExportManager;

// توابع سراسری جدید
window.showAdvancedStats = function() {
    advancedStatsManager.showAdvancedStats();
};

window.exportToCSV = function() {
    csvExportManager.exportToCSV();
};

window.clearAllFilters = function() {
    // پاک کردن تمام فیلترها
    currentTimeFilter = 'all';
    currentStatusFilter = 'all';
    currentUserTypeFilter = 'all';
    currentSearchQuery = '';
    
    // پاک کردن کلاس‌های active از همه دکمه‌های فیلتر
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        btn.classList.add('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        btn.classList.remove('bg-blue-100', 'dark:bg-blue-900/30', 'text-blue-700', 'dark:text-blue-300');
        btn.classList.remove('bg-green-100', 'dark:bg-green-900/30', 'text-green-700', 'dark:text-green-300');
        btn.classList.remove('bg-purple-100', 'dark:bg-purple-900/30', 'text-purple-700', 'dark:text-purple-300');
    });
    
    // فعال کردن دکمه "همه" برای هر نوع فیلتر
    // فیلتر زمان - همه
    const timeAllBtn = document.querySelector('.time-filter[data-filter="all"]');
    if (timeAllBtn) {
        timeAllBtn.classList.add('active');
        timeAllBtn.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        timeAllBtn.classList.add('bg-blue-100', 'dark:bg-blue-900/30', 'text-blue-700', 'dark:text-blue-300');
    }
    
    // فیلتر وضعیت - همه
    const statusAllBtn = document.querySelector('.status-filter[data-status="all"]');
    if (statusAllBtn) {
        statusAllBtn.classList.add('active');
        statusAllBtn.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        statusAllBtn.classList.add('bg-green-100', 'dark:bg-green-900/30', 'text-green-700', 'dark:text-green-300');
    }
    
    // فیلتر نوع کاربر - همه
    const userTypeAllBtn = document.querySelector('.user-type-filter[data-usertype="all"]');
    if (userTypeAllBtn) {
        userTypeAllBtn.classList.add('active');
        userTypeAllBtn.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        userTypeAllBtn.classList.add('bg-purple-100', 'dark:bg-purple-900/30', 'text-purple-700', 'dark:text-purple-300');
    }
    
    // پاک کردن جستجو
    if (elements.searchInput) {
        elements.searchInput.value = '';
    }
    
    // پاک کردن دکمه پاک کردن جستجو
    const clearSearch = document.getElementById('clearSearch');
    if (clearSearch) {
        clearSearch.classList.add('hidden');
    }
    
    // پاک کردن localStorage
    localStorage.removeItem('selectedFilter');
    localStorage.removeItem('selectedUserTypeFilter');
    localStorage.removeItem('selectedTimeFilter');
    
    // به‌روزرسانی شمارنده فیلترهای فعال
    updateActiveFiltersCount();
    
    // بارگذاری مجدد کاربران
    if (usersManager) {
        usersManager.applyAllFilters();
        usersManager.renderUsers();
    }
};

// توابع سراسری
window.showUserDetails = async function(username, deviceId) {
    try {
        const user = usersManager.users.find(u => u.username === username && u.device_id === deviceId);
        
        if (!user) {
            throw new Error('کاربر یافت نشد');
        }
        
        const userTypeColor = usersManager.getUserTypeColor(user.userType);
        const isOnline = !user.logout_time && !user.logout_time_jalali;
        const statusText = isOnline ? 'آنلاین' : 'آفلاین';
        const statusColor = isOnline ? 'green' : 'red';
        const statusIcon = isOnline ? 'fa-circle' : 'fa-circle';
        
        const modalContent = `
            <div class="bg-white dark:bg-gray-800 rounded-2xl max-w-4xl mx-auto max-h-[90vh] overflow-y-auto">
                <!-- هدر مودال -->
                <div class="sticky top-0 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 p-6 rounded-t-2xl">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center space-x-4 space-x-reverse">
                            <div class="relative">
                                <div class="w-16 h-16 bg-gradient-to-br from-${userTypeColor}-400 to-${userTypeColor}-600 
                                            rounded-full flex items-center justify-center text-white font-bold text-2xl shadow-lg">
                                    ${user.username.charAt(0).toUpperCase()}
                                </div>
                                <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-${statusColor}-500 rounded-full border-2 border-white dark:border-gray-800"></div>
                            </div>
                            <div>
                                <h2 class="text-2xl font-bold text-gray-900 dark:text-white">${user.username}</h2>
                                <p class="text-${userTypeColor}-600 dark:text-${userTypeColor}-400 font-semibold">
                                    ${usersManager.getUserTypeText(user.userType)}
                                </p>
                                <div class="flex items-center mt-1">
                                    <i class="fas ${statusIcon} text-${statusColor}-500 text-xs ml-2"></i>
                                    <span class="text-sm text-${statusColor}-600 dark:text-${statusColor}-400 font-medium">${statusText}</span>
                                </div>
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
                                    <i class="fas fa-sign-in-alt text-green-600 dark:text-green-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                        
                        ${user.logout_time_jalali ? `
                        <!-- کارت زمان خروج -->
                        <div class="stats-card">
                            <div class="flex items-center justify-between p-2">
                                <div>
                                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">زمان خروج</p>
                                    <p class="text-lg font-bold text-red-600 dark:text-red-400">
                                        ${user.logout_time_jalali}
                                    </p>
                                </div>
                                <div class="w-12 h-12 bg-red-100 dark:bg-red-900 rounded-xl 
                                           flex items-center justify-center">
                                    <i class="fas fa-sign-out-alt text-red-600 dark:text-red-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                        ` : ''}
                        

                        
                        <!-- کارت آدرس IP -->
                        <div class="stats-card">
                            <div class="flex items-center justify-between p-2">
                                <div>
                                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-2">آدرس IP</p>
                                    <p class="text-lg font-bold text-indigo-600 dark:text-indigo-400 font-mono">
                                        ${user.ip_address}
                                    </p>
                                </div>
                                <div class="w-12 h-12 bg-indigo-100 dark:bg-indigo-900 rounded-xl 
                                           flex items-center justify-center">
                                    <i class="fas fa-network-wired text-indigo-600 dark:text-indigo-400 text-xl"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- دکمه‌های عملیات -->
                    <div class="flex flex-col sm:flex-row gap-4 pt-6 border-t border-gray-200 dark:border-gray-700">
                        ${isOnline ? `
                        <button class="modal-btn modal-btn-danger flex-1" 
                                onclick="confirmForceLogout('${user.username}', '${user.device_id}')">
                            <i class="fas fa-sign-out-alt"></i>
                            <span>خروج اجباری</span>
                        </button>
                        ` : `
                        <button class="modal-btn modal-btn-secondary flex-1" disabled>
                            <i class="fas fa-check-circle"></i>
                            <span>جلسه خاتمه یافته</span>
                        </button>
                        `}
                        
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

// تابع خروج همه کاربران
window.logoutAllUsers = async function() {
    try {
        console.log('Sending logout all users request');
        
        const response = await ApiManager.postRequest('logout_all_users', {});
        
        console.log('Logout all users response:', response);
        
        if (response.success) {
            usersManager.showSuccess(response.message || 'تمامی کاربران با موفقیت از سیستم خارج شدند');
            ModalManager.hide();
            
            // بروزرسانی کامل لیست کاربران
            setTimeout(async () => {
                await usersManager.loadUsers(currentTimeFilter, currentStatusFilter);
                await usersManager.loadStats();
            }, 1000);
        } else {
            throw new Error(response.message || response.error || 'خطا در خروج همه کاربران');
        }
    } catch (error) {
        console.error('خطا در خروج همه کاربران:', error);
        console.error('Error details:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        usersManager.showError('خطا در خروج همه کاربران: ' + error.message);
    }
};

// تابع تایید خروج همه کاربران
window.confirmLogoutAllUsers = function() {
    ModalManager.showConfirm(
        'آیا مطمئن هستید که می‌خواهید تمامی کاربران آنلاین را از سیستم خارج کنید؟',
        'logoutAllUsers(); ModalManager.hide();',
        'ModalManager.hide();'
    );
};

// Event Listeners و راه‌اندازی اولیه
document.addEventListener('DOMContentLoaded', async function() {
    try {
        console.log('شروع راه‌اندازی سیستم مدیریت کاربران آنلاین...');
        
        // راه‌اندازی فیلترها (حذف شده چون کلاس FilterManager وجود ندارد)
        
        // بارگذاری اولیه داده‌ها
        await Promise.all([
            usersManager.loadUsers(currentTimeFilter, currentStatusFilter),
            usersManager.loadStats()
        ]);
        
        // تنظیم event listener برای دکمه بروزرسانی
        if (elements.refreshBtn) {
            elements.refreshBtn.addEventListener('click', async () => {
                await Promise.all([
                    usersManager.loadUsers(currentTimeFilter, currentStatusFilter),
                    usersManager.loadStats()
                ]);
            });
        }
        
        // تنظیم event listenerها برای فیلترهای زمانی
        elements.timeFilterButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                // حذف کلاس active از همه دکمه‌های زمانی
                elements.timeFilterButtons.forEach(b => b.classList.remove('active'));
                
                // اضافه کردن کلاس active به دکمه انتخاب شده
                btn.classList.add('active');
                
                // تنظیم فیلتر زمانی فعلی
                currentTimeFilter = btn.dataset.filter;
                
                // بارگذاری کاربران با فیلتر جدید
                usersManager.loadUsers(currentTimeFilter, currentStatusFilter);
            });
        });
        
        // تنظیم event listenerها برای فیلترهای وضعیت
        elements.statusFilterButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                // حذف کلاس active از همه دکمه‌های وضعیت
                elements.statusFilterButtons.forEach(b => b.classList.remove('active'));
                
                // اضافه کردن کلاس active به دکمه انتخاب شده
                btn.classList.add('active');
                
                // تنظیم فیلتر وضعیت فعلی
                currentStatusFilter = btn.dataset.status;
                
                // بارگذاری کاربران با فیلتر جدید
                usersManager.loadUsers(currentTimeFilter, currentStatusFilter);
            });
        });
        
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
        
        // تنظیم event listener برای دکمه پاک کردن همه فیلترها
        const clearAllFiltersBtn = document.getElementById('clearAllFilters');
        if (clearAllFiltersBtn) {
            clearAllFiltersBtn.addEventListener('click', clearAllFilters);
        }
        
        // تنظیم event listener برای دکمه پاک کردن فیلترها در قسمت جستجو
        const clearAllFiltersBtnSearch = document.getElementById('clearAllFiltersBtn');
        if (clearAllFiltersBtnSearch) {
            clearAllFiltersBtnSearch.addEventListener('click', clearAllFilters);
        }
        
        // تنظیم event listener برای دکمه خروجی CSV
        const exportCSVBtn = document.getElementById('exportCSV');
        if (exportCSVBtn) {
            exportCSVBtn.addEventListener('click', exportToCSV);
        }
        
        // تنظیم event listener برای دکمه آمار پیشرفته
        const showAdvancedStatsBtn = document.getElementById('showAdvancedStats');
        if (showAdvancedStatsBtn) {
            showAdvancedStatsBtn.addEventListener('click', showAdvancedStats);
        }
        
        // تنظیم event listener برای دکمه خروج همه
        const logoutAllBtn = document.getElementById('logoutAll');
        if (logoutAllBtn) {
            logoutAllBtn.addEventListener('click', confirmLogoutAllUsers);
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
        
        // Initialize filters functionality
        initializeFilters();
        
        // Initialize auto refresh with timer
        initializeAutoRefreshTimer();
        
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

// Initialize Filters Functionality
function initializeFilters() {
    // Toggle filters section
    const toggleFiltersBtn = document.getElementById('filtersToggle');
    const filtersSection = document.getElementById('filtersSection');
    const toggleFiltersIcon = document.getElementById('toggleIcon');
    
    if (toggleFiltersBtn && filtersSection && toggleFiltersIcon) {
        toggleFiltersBtn.addEventListener('click', function() {
            if (filtersSection.classList.contains('hidden')) {
                filtersSection.classList.remove('hidden');
                toggleFiltersIcon.classList.remove('fa-chevron-down');
                toggleFiltersIcon.classList.add('fa-chevron-up');
            } else {
                filtersSection.classList.add('hidden');
                toggleFiltersIcon.classList.remove('fa-chevron-up');
                toggleFiltersIcon.classList.add('fa-chevron-down');
            }
        });
    }
    
    // Filter buttons event listeners
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove active class from siblings
            const siblings = this.parentElement.querySelectorAll('.filter-btn');
            siblings.forEach(sibling => {
                sibling.classList.remove('active');
                sibling.classList.add('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
                sibling.classList.remove('bg-blue-100', 'dark:bg-blue-900/30', 'text-blue-700', 'dark:text-blue-300');
                sibling.classList.remove('bg-green-100', 'dark:bg-green-900/30', 'text-green-700', 'dark:text-green-300');
                sibling.classList.remove('bg-purple-100', 'dark:bg-purple-900/30', 'text-purple-700', 'dark:text-purple-300');
            });
            
            // Add active class to clicked button
            this.classList.add('active');
            this.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
            
            // Add appropriate color based on filter type
            if (this.classList.contains('time-filter')) {
                this.classList.add('bg-blue-100', 'dark:bg-blue-900/30', 'text-blue-700', 'dark:text-blue-300');
                currentTimeFilter = this.getAttribute('data-filter');
            } else if (this.classList.contains('status-filter')) {
                this.classList.add('bg-green-100', 'dark:bg-green-900/30', 'text-green-700', 'dark:text-green-300');
                currentStatusFilter = this.getAttribute('data-status');
            } else if (this.classList.contains('user-type-filter')) {
                this.classList.add('bg-purple-100', 'dark:bg-purple-900/30', 'text-purple-700', 'dark:text-purple-300');
                currentUserTypeFilter = this.getAttribute('data-usertype');
            }
            
            updateActiveFiltersCount();
            
            // Apply filters
            if (usersManager) {
                usersManager.applyAllFilters();
                usersManager.renderUsers();
            }
        });
    });
    
    // Search input
    const searchInput = document.getElementById('searchInput');
    const clearSearch = document.getElementById('clearSearch');
    
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            currentSearchQuery = this.value;
            
            if (this.value.trim() !== '') {
                if (clearSearch) clearSearch.classList.remove('hidden');
            } else {
                if (clearSearch) clearSearch.classList.add('hidden');
            }
            
            updateActiveFiltersCount();
            
            if (usersManager) {
                usersManager.applyAllFilters();
                usersManager.renderUsers();
            }
        });
    }
    
    if (clearSearch) {
        clearSearch.addEventListener('click', function() {
            if (searchInput) {
                searchInput.value = '';
                currentSearchQuery = '';
            }
            this.classList.add('hidden');
            updateActiveFiltersCount();
            
            if (usersManager) {
                usersManager.applyAllFilters();
                usersManager.renderUsers();
            }
        });
    }
    
    // Clear filters button
    const clearFiltersBtn = document.getElementById('clearFiltersBtn');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', clearAllFilters);
    }
    
    // Initialize filters count
    updateActiveFiltersCount();
}

// Update active filters count
function updateActiveFiltersCount() {
    const activeFilters = document.querySelectorAll('.filter-btn.active:not([data-filter="all"]):not([data-status="all"]):not([data-usertype="all"])');
    const searchInput = document.getElementById('searchInput');
    let count = activeFilters.length;
    
    if (searchInput && searchInput.value.trim() !== '') {
        count++;
    }
    
    const activeFiltersCount = document.getElementById('activeFiltersCount');
    if (activeFiltersCount) {
        activeFiltersCount.textContent = count;
    }
}

// Clear all filters
function clearAllFilters() {
    // Reset all filter buttons to default state
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        btn.classList.add('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        btn.classList.remove('bg-blue-100', 'dark:bg-blue-900/30', 'text-blue-700', 'dark:text-blue-300');
        btn.classList.remove('bg-green-100', 'dark:bg-green-900/30', 'text-green-700', 'dark:text-green-300');
        btn.classList.remove('bg-purple-100', 'dark:bg-purple-900/30', 'text-purple-700', 'dark:text-purple-300');
    });
    
    // Activate "all" buttons for each filter type
    // Time filter - همه
    const timeAllBtn = document.querySelector('.time-filter[data-filter="all"]');
    if (timeAllBtn) {
        timeAllBtn.classList.add('active');
        timeAllBtn.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        timeAllBtn.classList.add('bg-blue-100', 'dark:bg-blue-900/30', 'text-blue-700', 'dark:text-blue-300');
    }
    
    // Status filter - همه
    const statusAllBtn = document.querySelector('.status-filter[data-status="all"]');
    if (statusAllBtn) {
        statusAllBtn.classList.add('active');
        statusAllBtn.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        statusAllBtn.classList.add('bg-green-100', 'dark:bg-green-900/30', 'text-green-700', 'dark:text-green-300');
    }
    
    // User type filter - همه
    const userTypeAllBtn = document.querySelector('.user-type-filter[data-usertype="all"]');
    if (userTypeAllBtn) {
        userTypeAllBtn.classList.add('active');
        userTypeAllBtn.classList.remove('bg-gray-100', 'dark:bg-gray-700', 'text-gray-700', 'dark:text-gray-300');
        userTypeAllBtn.classList.add('bg-purple-100', 'dark:bg-purple-900/30', 'text-purple-700', 'dark:text-purple-300');
    }
    
    // Clear search input
    const searchInput = document.getElementById('searchInput');
    const clearSearch = document.getElementById('clearSearch');
    if (searchInput) {
        searchInput.value = '';
        currentSearchQuery = '';
    }
    if (clearSearch) {
        clearSearch.classList.add('hidden');
    }
    
    // Reset filter variables
    currentTimeFilter = 'all';
    currentStatusFilter = 'all';
    currentUserTypeFilter = 'all';
    
    updateActiveFiltersCount();
    
    // Apply filters
    if (usersManager) {
        usersManager.applyAllFilters();
        usersManager.renderUsers();
    }
}

// Auto Refresh Timer Management
let refreshTimerInterval = null;
let refreshCountdown = 30;

function initializeAutoRefreshTimer() {
    const autoRefreshCheckbox = document.getElementById('autoRefresh');
    const refreshTimer = document.getElementById('refreshTimer');
    
    if (autoRefreshCheckbox) {
        autoRefreshCheckbox.addEventListener('change', function() {
            if (this.checked) {
                startRefreshTimer();
                AutoRefreshManager.start();
            } else {
                stopRefreshTimer();
                AutoRefreshManager.stop();
            }
        });
    }
}

function startRefreshTimer() {
    const refreshTimer = document.getElementById('refreshTimer');
    if (!refreshTimer) return;
    
    refreshTimer.classList.remove('hidden');
    refreshCountdown = 30;
    
    refreshTimerInterval = setInterval(() => {
        refreshCountdown--;
        refreshTimer.textContent = `(${refreshCountdown})`;
        
        if (refreshCountdown <= 0) {
            refreshCountdown = 30;
        }
    }, 1000);
}

function stopRefreshTimer() {
    const refreshTimer = document.getElementById('refreshTimer');
    if (refreshTimer) {
        refreshTimer.classList.add('hidden');
    }
    
    if (refreshTimerInterval) {
        clearInterval(refreshTimerInterval);
        refreshTimerInterval = null;
    }
}

// Reset timer when refresh happens
function resetRefreshTimer() {
    if (refreshTimerInterval) {
        refreshCountdown = 30;
    }
}

// توابع سراسری برای دسترسی از HTML
function showUserDetails(username, deviceId) {
    userManager.showUserDetails(username, deviceId);
}

function confirmForceLogout(username, deviceId) {
    userManager.confirmForceLogout(username, deviceId);
}

function forceLogout(username, deviceId) {
    userManager.forceLogout(username, deviceId);
}

function showAdvancedStats() {
    userManager.showAdvancedStats();
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('hidden');
    }
}

function toggleDateSection(sectionId) {
    const content = document.getElementById(`${sectionId}-content`);
    const icon = document.getElementById(`${sectionId}-icon`);
    
    if (content && icon) {
        const isHidden = content.classList.contains('hidden');
        
        if (isHidden) {
            content.classList.remove('hidden');
            icon.classList.remove('fa-chevron-down');
            icon.classList.add('fa-chevron-up');
        } else {
            content.classList.add('hidden');
            icon.classList.remove('fa-chevron-up');
            icon.classList.add('fa-chevron-down');
        }
    }
}

function logoutAllUsers() {
    window.logoutAllUsers();
}

function confirmLogoutAllUsers() {
    window.confirmLogoutAllUsers();
}

// Logout Users Dropdown and Modal Management
class LogoutManager {
    constructor() {
        this.selectedLogoutType = null;
        this.init();
    }
    
    init() {
        this.setupEventListeners();
    }
    
    setupEventListeners() {
        document.addEventListener('DOMContentLoaded', () => {
            const logoutDropdown = document.getElementById('logoutDropdown');
            const logoutUsersBtn = document.getElementById('logoutUsersBtn');
            const logoutDropdownMenu = document.getElementById('logoutDropdownMenu');
            const logoutConfirmModal = document.getElementById('logoutConfirmModal');
            const logoutConfirmText = document.getElementById('logoutConfirmText');
            const cancelLogout = document.getElementById('cancelLogout');
            const confirmLogout = document.getElementById('confirmLogout');
            
            if (!logoutUsersBtn || !logoutDropdownMenu) return;
            
            // Toggle dropdown
            logoutUsersBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                logoutDropdownMenu.classList.toggle('hidden');
            });
            
            // Close dropdown when clicking outside
            document.addEventListener('click', (e) => {
                if (logoutDropdown && !logoutDropdown.contains(e.target)) {
                    logoutDropdownMenu.classList.add('hidden');
                }
            });
            
            // Handle logout option selection
            document.querySelectorAll('.logout-option').forEach(option => {
                option.addEventListener('click', () => {
                    this.selectedLogoutType = option.dataset.type;
                    logoutDropdownMenu.classList.add('hidden');
                    
                    // Set confirmation text based on type
                    const confirmText = this.getConfirmationText(this.selectedLogoutType);
                    
                    if (logoutConfirmText) {
                        logoutConfirmText.textContent = confirmText;
                    }
                    
                    if (logoutConfirmModal) {
                        logoutConfirmModal.classList.remove('hidden');
                    }
                });
            });
            
            // Cancel logout
            if (cancelLogout) {
                cancelLogout.addEventListener('click', () => {
                    if (logoutConfirmModal) {
                        logoutConfirmModal.classList.add('hidden');
                    }
                    this.selectedLogoutType = null;
                });
            }
            
            // Confirm logout
            if (confirmLogout) {
                confirmLogout.addEventListener('click', () => {
                    if (this.selectedLogoutType) {
                        this.performLogout(this.selectedLogoutType, confirmLogout, logoutConfirmModal);
                    }
                });
            }
        });
    }
    
    getConfirmationText(type) {
        switch(type) {
            case 'all':
                return 'آیا مطمئن هستید که می‌خواهید همه کاربران را از سیستم خارج کنید؟';
            case 'except-admin':
                return 'آیا مطمئن هستید که می‌خواهید همه کاربران به جز مدیران را از سیستم خارج کنید؟';
            case 'operators':
                return 'آیا مطمئن هستید که می‌خواهید همه اپراتورها را از سیستم خارج کنید؟';
            case 'verifiers':
                return 'آیا مطمئن هستید که می‌خواهید همه تأیید کننده‌ها را از سیستم خارج کنید؟';
            default:
                return 'آیا مطمئن هستید که می‌خواهید کاربران انتخاب شده را از سیستم خارج کنید؟';
        }
    }
    
    performLogout(type, confirmButton, modal) {
        // Show loading state
        const originalContent = confirmButton.innerHTML;
        confirmButton.innerHTML = '<i class="fas fa-spinner fa-spin text-sm"></i><span>در حال پردازش...</span>';
        confirmButton.disabled = true;
        
        // Here you would make an API call to logout users
        // For now, we'll just show a success message
        setTimeout(() => {
            alert(`عملیات خروج ${this.getTypeLabel(type)} با موفقیت انجام شد.`);
            
            // Reset button
            confirmButton.innerHTML = originalContent;
            confirmButton.disabled = false;
            
            // Hide modal
            if (modal) {
                modal.classList.add('hidden');
            }
            
            // Refresh users list if usersManager exists
            if (typeof usersManager !== 'undefined' && usersManager.loadUsers) {
                usersManager.loadUsers();
            }
            
            this.selectedLogoutType = null;
        }, 1500);
    }
    
    getTypeLabel(type) {
        switch(type) {
            case 'all': return 'همه کاربران';
            case 'except-admin': return 'همه کاربران به جز مدیران';
            case 'operators': return 'اپراتورها';
            case 'verifiers': return 'تأیید کننده‌ها';
            default: return 'کاربران انتخاب شده';
        }
    }
}

// Initialize Logout Manager
const logoutManager = new LogoutManager();