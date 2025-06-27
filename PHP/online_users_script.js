// Global Variables - Updated
let currentFilter = 'all';
let autoRefreshInterval = null;
let isLoading = false;

// تنظیمات
const CONFIG = {
    API_URL: 'online_users_api.php',
    REFRESH_INTERVAL: 60000,
    ANIMATION_DELAY: 100
};

// DOM Elements
const elements = {
    usersGrid: document.getElementById('usersGrid'),
    loadingState: document.getElementById('loadingState'),
    emptyState: document.getElementById('emptyState'),
    onlineCount: document.getElementById('onlineCount'),
    activeSessionsCount: document.getElementById('activeSessionsCount'),
    todayLoginsCount: document.getElementById('todayLoginsCount'),
    lastUpdate: document.getElementById('lastUpdate'),
    refreshBtn: document.getElementById('refreshBtn'),
    refreshIcon: document.getElementById('refreshIcon'),
    updateIcon: document.getElementById('updateIcon'),
    themeToggle: document.getElementById('themeToggle'),
    autoRefresh: document.getElementById('autoRefresh'),
    userModal: document.getElementById('userModal'),
    modalContent: document.getElementById('modalContent'),
    closeModal: document.getElementById('closeModal')
};

// مدیریت تم
class ThemeManager {
    constructor() {
        this.init();
    }

    init() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        this.setTheme(savedTheme);
        
        elements.themeToggle.addEventListener('click', () => {
            this.toggleTheme();
        });
    }

    setTheme(theme) {
        if (theme === 'dark') {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
        localStorage.setItem('theme', theme);
    }

    toggleTheme() {
        const isDark = document.documentElement.classList.contains('dark');
        this.setTheme(isDark ? 'light' : 'dark');
    }
}

// مدیریت API
class ApiManager {
    static async request(action, data = {}) {
        try {
            const url = new URL(CONFIG.API_URL, window.location.origin + window.location.pathname.replace(/[^/]*$/, ''));
            url.searchParams.append('action', action);
            
            Object.keys(data).forEach(key => {
                url.searchParams.append(key, data[key]);
            });

            const response = await fetch(url);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    static async postRequest(action, data = {}) {
        try {
            const formData = new FormData();
            formData.append('action', action);
            
            Object.keys(data).forEach(key => {
                formData.append(key, data[key]);
            });

            const response = await fetch(CONFIG.API_URL, {
                method: 'POST',
                body: formData
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }
}

// مدیریت کاربران
class UsersManager {
    constructor() {
        this.users = [];
        this.stats = {};
    }

    async loadUsers(filter = 'all') {
        if (isLoading) return;
        
        isLoading = true;
        this.showLoading();
        
        try {
            const response = await ApiManager.request('filter_by_time', { filter });
            
            if (response.success) {
                this.users = response.users;
                this.updateStats(response);
                this.renderUsers();
                this.updateLastUpdateTime(response.last_update);
            } else {
                throw new Error(response.error || 'خطا در دریافت اطلاعات');
            }
        } catch (error) {
            console.error('Error loading users:', error);
            this.showError('خطا در بارگذاری اطلاعات کاربران');
        } finally {
            isLoading = false;
            this.hideLoading();
        }
    }

    async loadStats() {
        try {
            const response = await ApiManager.request('get_session_stats');
            
            if (response.success) {
                this.stats = response.stats;
                this.updateStatsDisplay();
            }
        } catch (error) {
            console.error('Error loading stats:', error);
        }
    }

    updateStats(response) {
        elements.onlineCount.textContent = response.total_count;
        elements.activeSessionsCount.textContent = response.total_count;
    }

    updateStatsDisplay() {
        if (this.stats.today_logins !== undefined) {
            elements.todayLoginsCount.textContent = this.stats.today_logins;
        }
    }

    updateLastUpdateTime(time) {
        elements.lastUpdate.textContent = time;
    }

    renderUsers() {
        if (this.users.length === 0) {
            this.showEmptyState();
            return;
        }

        this.hideEmptyState();
        
        elements.usersGrid.innerHTML = '';
        
        this.users.forEach((user, index) => {
            setTimeout(() => {
                const userCard = this.createUserCard(user);
                elements.usersGrid.appendChild(userCard);
            }, index * CONFIG.ANIMATION_DELAY);
        });
    }

    createUserCard(user) {
        const card = document.createElement('div');
        card.className = 'bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300';
        
        const statusColor = user.status === 'online' ? 'green' : 'yellow';
        const userTypeColor = usersManager.getUserTypeColor(user.userType);
        
        card.innerHTML = `
            <div class="flex items-center justify-between">
                <div>
                    <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">${user.username}</p>
                    <p class="text-lg font-bold text-${userTypeColor}-600 dark:text-${userTypeColor}-400 mb-2">${this.getUserTypeText(user.userType)}</p>
                    <div class="space-y-1">
                        <div class="flex items-center space-x-2 space-x-reverse text-xs text-gray-500 dark:text-gray-400">
                            <i class="fas fa-mobile-alt text-blue-500"></i>
                            <span>${user.device_model || 'نامشخص'}</span>
                        </div>
                        <div class="flex items-center space-x-2 space-x-reverse text-xs text-gray-500 dark:text-gray-400">
                            <i class="fas fa-clock text-green-500"></i>
                            <span>${user.login_time_jalali}</span>
                        </div>
                        <div class="flex items-center space-x-2 space-x-reverse text-xs text-gray-500 dark:text-gray-400">
                            <i class="fas fa-network-wired text-purple-500"></i>
                            <span>${user.ip_address}</span>
                        </div>
                    </div>
                </div>
                <div class="text-left">
                    <div class="bg-${statusColor}-100 dark:bg-${statusColor}-900 p-3 rounded-lg mb-3">
                        <i class="fas fa-user text-${statusColor}-600 dark:text-${statusColor}-400 text-xl"></i>
                    </div>
                    <div class="flex space-x-1 space-x-reverse">
                        <button class="bg-blue-500 hover:bg-blue-600 text-white p-2 rounded-lg transition-colors text-xs" onclick="showUserDetails('${user.username}', '${user.device_id}')" title="جزئیات">
                            <i class="fas fa-info-circle"></i>
                        </button>
                        <button class="bg-red-500 hover:bg-red-600 text-white p-2 rounded-lg transition-colors text-xs" onclick="forceLogout('${user.username}', '${user.device_id}')" title="خروج اجباری">
                            <i class="fas fa-sign-out-alt"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        return card;
    }

    getUserTypeText(userType) {
        const types = {
            'admin': 'مدیر',
            'operator': 'اپراتور',
            'verifier': 'تأیید کننده'
        };
        return types[userType] || userType;
    }

    getUserTypeColor(userType) {
        const colors = {
            'admin': 'red',
            'operator': 'blue',
            'verifier': 'purple'
        };
        return colors[userType] || 'gray';
    }

    showLoading() {
        elements.loadingState.classList.remove('hidden');
        elements.usersGrid.classList.add('hidden');
        elements.emptyState.classList.add('hidden');
        
        // انیمیشن آیکون بروزرسانی
        elements.refreshIcon.classList.add('fa-spin');
        elements.updateIcon.classList.add('animate-spin-slow');
    }

    hideLoading() {
        elements.loadingState.classList.add('hidden');
        elements.usersGrid.classList.remove('hidden');
        
        // توقف انیمیشن
        elements.refreshIcon.classList.remove('fa-spin');
        elements.updateIcon.classList.remove('animate-spin-slow');
    }

    showEmptyState() {
        elements.emptyState.classList.remove('hidden');
        elements.usersGrid.classList.add('hidden');
    }

    hideEmptyState() {
        elements.emptyState.classList.add('hidden');
        elements.usersGrid.classList.remove('hidden');
    }

    showError(message) {
        // نمایش پیام خطا
        const errorDiv = document.createElement('div');
        errorDiv.className = 'bg-red-100 dark:bg-red-900 border border-red-400 dark:border-red-600 text-red-700 dark:text-red-300 px-4 py-3 rounded-lg mb-4';
        errorDiv.innerHTML = `
            <div class="flex items-center space-x-2 space-x-reverse">
                <i class="fas fa-exclamation-triangle"></i>
                <span>${message}</span>
            </div>
        `;
        
        elements.usersGrid.parentNode.insertBefore(errorDiv, elements.usersGrid);
        
        // حذف پیام خطا بعد از 5 ثانیه
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    }
}

// مدیریت مودال
class ModalManager {
    static show(content) {
        elements.modalContent.innerHTML = content;
        elements.userModal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }

    static hide() {
        elements.userModal.classList.add('hidden');
        document.body.style.overflow = 'auto';
    }
}

// مدیریت بروزرسانی خودکار
class AutoRefreshManager {
    static start() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
        }
        
        autoRefreshInterval = setInterval(() => {
            if (usersManager) {
                usersManager.loadUsers(currentFilter);
                usersManager.loadStats();
            }
        }, CONFIG.REFRESH_INTERVAL);
    }

    static stop() {
        if (autoRefreshInterval) {
            clearInterval(autoRefreshInterval);
            autoRefreshInterval = null;
        }
    }
}

// نمونه‌های کلاس‌ها
const themeManager = new ThemeManager();
const usersManager = new UsersManager();

// توابع سراسری
window.showUserDetails = async function(username, deviceId) {
    try {
        const user = usersManager.users.find(u => u.username === username && u.device_id === deviceId);
        
        if (!user) {
            throw new Error('کاربر یافت نشد');
        }
        
        const statusColor = user.status === 'online' ? 'green' : 'yellow';
        const userTypeColor = this.getUserTypeColor(user.userType);
        
        const modalContent = `
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- کارت اطلاعات کاربر -->
                <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">نام کاربری</p>
                            <p class="text-lg font-bold text-${userTypeColor}-600 dark:text-${userTypeColor}-400">${user.username}</p>
                        </div>
                        <div class="bg-${userTypeColor}-100 dark:bg-${userTypeColor}-900 p-3 rounded-lg">
                            <i class="fas fa-user text-${userTypeColor}-600 dark:text-${userTypeColor}-400 text-xl"></i>
                        </div>
                    </div>
                    <div class="mt-4">
                        <p class="text-sm text-gray-500 dark:text-gray-400">نوع کاربر: ${usersManager.getUserTypeText(user.userType)}</p>
                    </div>
                </div>
                
                <!-- کارت وضعیت -->
                <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">وضعیت</p>
                            <p class="text-lg font-bold text-${statusColor}-600 dark:text-${statusColor}-400">${user.status_text}</p>
                        </div>
                        <div class="bg-${statusColor}-100 dark:bg-${statusColor}-900 p-3 rounded-lg">
                            <i class="fas fa-circle text-${statusColor}-600 dark:text-${statusColor}-400 text-xl"></i>
                        </div>
                    </div>
                </div>
                
                <!-- کارت دستگاه -->
                <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">دستگاه</p>
                            <p class="text-lg font-bold text-blue-600 dark:text-blue-400">${user.device_model || 'نامشخص'}</p>
                        </div>
                        <div class="bg-blue-100 dark:bg-blue-900 p-3 rounded-lg">
                            <i class="fas fa-mobile-alt text-blue-600 dark:text-blue-400 text-xl"></i>
                        </div>
                    </div>
                    <div class="mt-4">
                        <p class="text-xs text-gray-500 dark:text-gray-400 font-mono">${user.device_id}</p>
                    </div>
                </div>
                
                <!-- کارت زمان ورود -->
                <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">زمان ورود</p>
                            <p class="text-lg font-bold text-green-600 dark:text-green-400">${user.login_time_jalali}</p>
                        </div>
                        <div class="bg-green-100 dark:bg-green-900 p-3 rounded-lg">
                            <i class="fas fa-clock text-green-600 dark:text-green-400 text-xl"></i>
                        </div>
                    </div>
                </div>
                
                ${user.last_activity_jalali ? `
                <!-- کارت آخرین فعالیت -->
                <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">آخرین فعالیت</p>
                            <p class="text-lg font-bold text-orange-600 dark:text-orange-400">${user.last_activity_jalali}</p>
                        </div>
                        <div class="bg-orange-100 dark:bg-orange-900 p-3 rounded-lg">
                            <i class="fas fa-history text-orange-600 dark:text-orange-400 text-xl"></i>
                        </div>
                    </div>
                </div>
                ` : ''}
                
                <!-- کارت آدرس IP -->
                <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 border border-gray-200 dark:border-gray-700">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-gray-600 dark:text-gray-400 text-sm mb-1">آدرس IP</p>
                            <p class="text-lg font-bold text-purple-600 dark:text-purple-400 font-mono">${user.ip_address}</p>
                        </div>
                        <div class="bg-purple-100 dark:bg-purple-900 p-3 rounded-lg">
                            <i class="fas fa-network-wired text-purple-600 dark:text-purple-400 text-xl"></i>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- دکمه عملیات -->
            <div class="mt-6 flex justify-center">
                <button class="bg-red-500 hover:bg-red-600 text-white px-6 py-3 rounded-lg transition-colors flex items-center space-x-2 space-x-reverse" onclick="forceLogout('${user.username}', '${user.device_id}')">
                    <i class="fas fa-sign-out-alt"></i>
                    <span>خروج اجباری</span>
                </button>
            </div>
        `;
        
        ModalManager.show(modalContent);
    } catch (error) {
        console.error('Error showing user details:', error);
        alert('خطا در نمایش جزئیات کاربر');
    }
};

window.sendMessage = function(username) {
    // پیاده‌سازی ارسال پیام
    alert(`ارسال پیام به ${username}`);
    ModalManager.hide();
};

window.forceLogout = async function(username, deviceId) {
    if (!confirm(`آیا مطمئن هستید که می‌خواهید ${username} را از سیستم خارج کنید؟`)) {
        return;
    }
    
    try {
        console.log('Sending force logout request:', { username, device_id: deviceId });
        
        const response = await ApiManager.postRequest('force_logout', {
            username: username,
            device_id: deviceId
        });
        
        console.log('Force logout response:', response);
        
        if (response.success) {
            alert('کاربر با موفقیت از سیستم خارج شد');
            ModalManager.hide();
            usersManager.loadUsers(currentFilter);
        } else {
            throw new Error(response.message || response.error || 'خطا در خروج اجباری');
        }
    } catch (error) {
        console.error('Error forcing logout:', error);
        
        // نمایش جزئیات بیشتر خطا
        let errorMessage = 'خطا در خروج اجباری کاربر';
        if (error.message) {
            errorMessage += '\n\nجزئیات خطا: ' + error.message;
        }
        
        alert(errorMessage);
    }
};

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    // بارگذاری اولیه
    
    // بارگذاری اولیه
    usersManager.loadUsers();
    usersManager.loadStats();
    
    // دکمه بروزرسانی
    elements.refreshBtn.addEventListener('click', () => {
        usersManager.loadUsers(currentFilter);
        usersManager.loadStats();
    });
    
    // فیلترهای زمانی
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            // حذف کلاس active از همه دکمه‌ها
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            
            // اضافه کردن کلاس active به دکمه کلیک شده
            e.target.classList.add('active');
            
            // تنظیم فیلتر جدید
            currentFilter = e.target.dataset.filter;
            
            // بارگذاری کاربران با فیلتر جدید
            usersManager.loadUsers(currentFilter);
        });
    });
    
    // بروزرسانی خودکار
    elements.autoRefresh.addEventListener('change', (e) => {
        if (e.target.checked) {
            AutoRefreshManager.start();
        } else {
            AutoRefreshManager.stop();
        }
    });
    
    // بستن مودال
    elements.closeModal.addEventListener('click', () => {
        ModalManager.hide();
    });
    
    // بستن مودال با کلیک روی پس‌زمینه
    elements.userModal.addEventListener('click', (e) => {
        if (e.target === elements.userModal) {
            ModalManager.hide();
        }
    });
    
    // بستن مودال با کلید Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            ModalManager.hide();
        }
    });
    
    // تنظیم بروزرسانی خودکار پیش‌فرض
    setTimeout(() => {
        if (elements.autoRefresh) {
            elements.autoRefresh.checked = true;
            AutoRefreshManager.start();
        }
    }, 2000);
});