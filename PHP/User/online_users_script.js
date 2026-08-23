// Online Users Manager Script v3.0 Modular Architecture with Modern ES6+ Features /.

// ===== Configuration =====
const CONFIG = {
    API_URL: 'online_users_api.php',
    REFRESH_INTERVAL: 60000, // 60 seconds
    DEBOUNCE_DELAY: 300,
    ANIMATION_DURATION: 300,
    MAX_RETRY_COUNT: 3,
    RETRY_DELAY: 2000,
    CACHE_DURATION: 60000 // 1 minute cache for non-critical requests
};

// ===== Utilities =====
const Utils = {
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    formatNumber(num) {
        return new Intl.NumberFormat('fa-IR').format(num);
    },

    toPersianDigits(str) {
        if (!str) return '';
        return str.toString().replace(/\d/g, d => '۰۱۲۳۴۵۶۷۸۹'[d]);
    },

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
};

// ===== Theme Manager =====
class ThemeManager {
    constructor() {
        this.themeToggleBtn = document.getElementById('themeToggle');
        this.html = document.documentElement;
        this.init();
    }

    init() {
        if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
            this.enableDark();
        } else {
            this.disableDark();
        }

        if (this.themeToggleBtn) {
            this.themeToggleBtn.addEventListener('click', () => this.toggle());
        }
    }

    enableDark() {
        this.html.classList.add('dark');
        localStorage.theme = 'dark';
    }

    disableDark() {
        this.html.classList.remove('dark');
        localStorage.theme = 'light';
    }

    toggle() {
        if (this.html.classList.contains('dark')) {
            this.disableDark();
        } else {
            this.enableDark();
        }
    }
}

// ===== API Manager =====
class ApiManager {
    static cache = new Map();

    static async request(action, params = {}, method = 'GET', useCache = false) {
        const cacheKey = `${action}-${JSON.stringify(params)}`;

        // Check Cache
        if (useCache && method === 'GET') {
            const cached = this.cache.get(cacheKey);
            if (cached && (Date.now() - cached.timestamp < CONFIG.CACHE_DURATION)) {
                console.log('Using cached data for:', action);
                return cached.data;
            }
        }

        try {
            // Use window.location.href as base to support subdirectories
            const url = new URL(CONFIG.API_URL, window.location.href);
            url.searchParams.append('action', action);

            if (method === 'GET') {
                Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));
            }

            const options = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            };

            if (method === 'POST') {
                options.body = JSON.stringify(params);
            }

            const response = await fetch(url, options);
            if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);

            const data = await response.json();

            // Set Cache
            if (useCache && method === 'GET' && data.success) {
                this.cache.set(cacheKey, {
                    data,
                    timestamp: Date.now()
                });
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    static clearCache() {
        this.cache.clear();
    }
}

// ===== UI Manager =====
class UIManager {
    constructor() {
        this.elements = {
            usersGrid: document.getElementById('usersGrid'),
            skeletonLoader: document.getElementById('skeletonLoader'),
            loadingState: document.getElementById('loadingState'),
            emptyState: document.getElementById('emptyState'),
            activeSessionsCount: document.getElementById('activeSessionsCount'),
            todayLoginsCount: document.getElementById('todayLoginsCount'),
            lastUpdate: document.getElementById('lastUpdate'),
            refreshBtn: document.getElementById('refreshBtn'),
            refreshIcon: document.querySelector('#refreshBtn i')
        };
    }

    showLoading() {
        this.elements.skeletonLoader.classList.remove('hidden');
        this.elements.usersGrid.innerHTML = '';
        this.elements.usersGrid.appendChild(this.elements.skeletonLoader);
        this.elements.emptyState.classList.add('hidden');

        if (this.elements.refreshIcon) {
            this.elements.refreshIcon.classList.add('fa-spin');
        }
    }

    hideLoading() {
        this.elements.skeletonLoader.classList.add('hidden');
        if (this.elements.refreshIcon) {
            this.elements.refreshIcon.classList.remove('fa-spin');
        }
    }

    showEmptyState() {
        this.elements.emptyState.classList.remove('hidden');
        this.elements.usersGrid.classList.add('hidden');
    }

    hideEmptyState() {
        this.elements.emptyState.classList.add('hidden');
        this.elements.usersGrid.classList.remove('hidden');
    }

    updateStats(activeCount, todayCount, lastUpdate) {
        this.animateValue(this.elements.activeSessionsCount, parseInt(this.elements.activeSessionsCount.innerText) || 0, activeCount, 1000);
        this.animateValue(this.elements.todayLoginsCount, parseInt(this.elements.todayLoginsCount.innerText) || 0, todayCount, 1000);

        if (lastUpdate) {
            this.elements.lastUpdate.innerText = lastUpdate;
        }
    }

    animateValue(obj, start, end, duration) {
        if (!obj) return;
        let startTimestamp = null;
        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);
            obj.innerHTML = Utils.toPersianDigits(Math.floor(progress * (end - start) + start));
            if (progress < 1) {
                window.requestAnimationFrame(step);
            }
        };
        window.requestAnimationFrame(step);
    }

    renderUsers(users) {
        this.elements.usersGrid.innerHTML = '';

        if (!users || users.length === 0) {
            this.showEmptyState();
            return;
        }

        this.hideEmptyState();

        // Group users by date
        const groups = usersManagerInstance.groupUsersByDate(users);

        // Create accordion structure
        const container = document.createElement('div');
        container.className = 'col-span-full space-y-4';

        groups.forEach((group, groupIndex) => {
            const isFirstGroup = groupIndex === 0;
            const section = this.createDateSection(group, isFirstGroup);
            container.appendChild(section);
        });

        this.elements.usersGrid.appendChild(container);
    }

    createDateSection(group, isOpen = false) {
        const section = document.createElement('div');
        section.className = 'bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-sm overflow-hidden';

        const headerId = `header-${group.key}`;
        const contentId = `content-${group.key}`;

        section.innerHTML = `
            <button 
                id="${headerId}"
                class="w-full px-6 py-4 flex items-center justify-between bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 hover:from-gray-100 hover:to-gray-200 dark:hover:from-gray-600 dark:hover:to-gray-700 transition-all duration-150 group"
                onclick="UIManager.toggleDateSection('${contentId}', '${headerId}')"
            >
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center text-white shadow-lg shadow-blue-500/30">
                        <i class="fas fa-calendar-alt"></i>
                    </div>
                    <div class="text-right">
                        <h3 class="text-lg font-bold text-gray-900 dark:text-white">${group.label}</h3>
                        <p class="text-sm text-gray-500 dark:text-gray-400">${Utils.toPersianDigits(group.users.length)} کاربر</p>
                    </div>
                </div>
                <i class="fas fa-chevron-down text-gray-400 transition-transform duration-150 group-hover:text-blue-500 ${isOpen ? 'rotate-180' : ''}" data-icon></i>
            </button>
            <div 
                id="${contentId}" 
                class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 p-4 transition-all duration-150 ${isOpen ? '' : 'hidden'}"
            >
            </div>
        `;

        // Add user cards to the content area
        const contentArea = section.querySelector(`#${contentId}`);
        group.users.forEach((user, index) => {
            const card = this.createUserCard(user, index);
            contentArea.appendChild(card);
        });

        return section;
    }

    static toggleDateSection(contentId, headerId) {
        // Close all other date sections first
        document.querySelectorAll('[id^="content-"]').forEach(content => {
            if (content.id !== contentId) {
                content.classList.add('hidden');
                // Reset icon for closed sections
                const correspondingHeaderId = content.id.replace('content-', 'header-');
                const correspondingHeader = document.getElementById(correspondingHeaderId);
                if (correspondingHeader) {
                    const icon = correspondingHeader.querySelector('[data-icon]');
                    if (icon) icon.classList.remove('rotate-180');
                }
            }
        });

        const content = document.getElementById(contentId);
        const header = document.getElementById(headerId);
        const icon = header.querySelector('[data-icon]');

        if (content.classList.contains('hidden')) {
            content.classList.remove('hidden');
            icon.classList.add('rotate-180');
        } else {
            content.classList.add('hidden');
            icon.classList.remove('rotate-180');
        }
    }

    static toggleUserMenu(menuId) {
        // Close all other user menus first
        document.querySelectorAll('[id^="userMenu-"]').forEach(menu => {
            if (menu.id !== `userMenu-${menuId}`) {
                menu.classList.add('hidden');
            }
        });

        const menu = document.getElementById(`userMenu-${menuId}`);
        if (menu) {
            menu.classList.toggle('hidden');
        }
    }

    createUserCard(user, index) {
        const isOnline = !user.logout_time && !user.logout_time_jalali;
        const userTypeColor = this.getUserTypeColor(user.userType);
        const statusColor = isOnline ? 'green' : 'red';

        const div = document.createElement('div');
        div.className = 'bg-white dark:bg-gray-800 rounded-2xl p-5 border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-lg transition-all duration-300 hover:-translate-y-1 animate-slide-up';
        div.style.animationDelay = `${index * 50}ms`;

        div.innerHTML = `
            <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                        <div class="relative">
                        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-${userTypeColor}-400 to-${userTypeColor}-600 flex items-center justify-center text-white font-bold text-lg shadow-md">
                            ${this.getUserTypeIcon(user.userType)}
                            </div>
                        <div class="absolute -bottom-1 -right-1 w-4 h-4 bg-${statusColor}-500 rounded-full border-2 border-white dark:border-gray-800 ${isOnline ? 'animate-pulse' : ''}"></div>
                        </div>
                        <div>
                        <h4 class="font-bold text-gray-900 dark:text-white text-lg leading-tight">${user.username}</h4>
                        <span class="text-xs font-medium px-2 py-0.5 rounded-full bg-${userTypeColor}-50 dark:bg-${userTypeColor}-900/20 text-${userTypeColor}-600 dark:text-${userTypeColor}-400 border border-${userTypeColor}-100 dark:border-${userTypeColor}-800/50">
                            ${this.getUserTypeText(user.userType)}
                                </span>
                            </div>
                        </div>
                <div class="relative group">
                    <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors p-1" onclick="UIManager.toggleUserMenu('${user.username}-${user.device_id}')">
                        <i class="fas fa-ellipsis-v"></i>
                    </button>
                    <div id="userMenu-${user.username}-${user.device_id}" class="hidden absolute left-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 z-50 overflow-hidden transform origin-top-left transition-all">
                        <div class="p-1">
                            <button onclick="UsersManager.showDetails('${user.username}', '${user.device_id}')" class="w-full text-right px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors flex items-center gap-2">
                                <i class="fas fa-id-card text-blue-500"></i> جزئیات کاربر
                            </button>
                            <button onclick="UsersManager.viewActivityHistory('${user.username}', '${user.device_id}')" class="w-full text-right px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors flex items-center gap-2">
                                <i class="fas fa-history text-purple-500"></i> سابقه فعالیت
                            </button>
                            ${isOnline ? `
                            <button onclick="UsersManager.confirmLogout('${user.username}', '${user.device_id}')" class="w-full text-right px-3 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors flex items-center gap-2">
                                <i class="fas fa-sign-out-alt"></i> خروج کاربر
                            </button>
                            ` : ''}
                            <button onclick="UsersManager.exportUserData('${user.username}', '${user.device_id}')" class="w-full text-right px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors flex items-center gap-2">
                                <i class="fas fa-download text-green-500"></i> خروجی اطلاعات
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="space-y-2.5 mb-5">
                <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <i class="fas fa-mobile-alt w-5 text-center text-gray-400"></i>
                    <span class="font-mono text-xs opacity-90 truncate" title="${user.device_model}">${user.device_model || 'نامشخص'}</span>
                </div>
                ${user.app_version ? `
                <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <i class="fas fa-code-branch w-5 text-center text-purple-400"></i>
                    <span class="font-mono text-xs opacity-90 truncate" title="نسخه برنامه">نسخه: ${user.app_version}</span>
                </div>
                ` : ''}
                <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <i class="fas fa-sign-in-alt w-5 text-center text-green-500"></i>
                    <span class="font-mono text-xs opacity-90">ورود: ${user.login_time_jalali || '-'}</span>
                </div>
                <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <i class="fas fa-history w-5 text-center text-orange-500"></i>
                    <span class="font-mono text-xs opacity-90">آخرین فعالیت: ${user.last_activity_jalali || '-'}</span>
                </div>
                ${!isOnline ? `
                <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <i class="fas fa-sign-out-alt w-5 text-center text-red-500"></i>
                    <span class="font-mono text-xs opacity-90">خروج: ${user.logout_time_jalali || '-'}</span>
                </div>
                ` : ''}
                <div class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
                    <i class="fas fa-network-wired w-5 text-center text-blue-500"></i>
                    <span class="font-mono text-xs opacity-90">${user.ip_address}</span>
                </div>
                </div>

            <div class="flex gap-2 mt-auto pt-4 border-t border-gray-100 dark:border-gray-700">
                <button onclick="UsersManager.showDetails('${user.username}', '${user.device_id}')" class="flex-1 py-2 px-3 bg-gray-50 dark:bg-gray-700 text-gray-700 dark:text-gray-200 rounded-xl text-sm font-medium hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors">
                    جزئیات
                </button>
                ${isOnline ? `
                <button onclick="UsersManager.confirmLogout('${user.username}', '${user.device_id}')" class="flex-1 py-2 px-3 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-xl text-sm font-medium hover:bg-red-100 dark:hover:bg-red-900/40 transition-colors">
                    خروج
                </button>
                ` : ''}
            </div>
        `;

        return div;
    }

    getUserTypeIcon(type) {
        const map = {
            'admin': '<i class="fas fa-user-shield"></i>',
            'operator': '<i class="fas fa-headset"></i>',
            'verifier': '<i class="fas fa-user-check"></i>',
            'user': '<i class="fas fa-user"></i>'
        };
        return map[type] || '<i class="fas fa-user"></i>';
    }

    getUserTypeColor(type) {
        const map = {
            'admin': 'yellow',
            'operator': 'blue',
            'verifier': 'purple',
            'user': 'gray'
        };
        return map[type] || 'gray';
    }

    getUserTypeText(type) {
        const map = {
            'admin': 'مدیر سیستم',
            'operator': 'اپراتور',
            'verifier': 'تأیید کننده',
            'user': 'کاربر عادی'
        };
        return map[type] || type;
    }
}

// ===== Modal Manager =====
class ModalManager {
    static show(modalId) {
        const modal = document.getElementById(modalId);
        if (!modal) return;

        modal.classList.remove('hidden');

        const backdrop = modal.querySelector('div[class*="fixed inset-0 bg-gray-900"]');
        if (backdrop) {
            requestAnimationFrame(() => {
                backdrop.classList.remove('opacity-0');
                backdrop.classList.add('opacity-100');
            });
        }

        const panel = modal.querySelector('div[class*="transform"]');
        if (panel) {
            requestAnimationFrame(() => {
                panel.classList.remove('opacity-0', 'translate-y-4', 'scale-95');
                panel.classList.add('opacity-100', 'translate-y-0', 'scale-100');
            });
        }

        document.body.style.overflow = 'hidden';
    }

    static hide(modalId = null) {
        const modals = modalId ? [document.getElementById(modalId)] : document.querySelectorAll('[id$="Modal"]:not(.hidden)');

        modals.forEach(modal => {
            if (!modal) return;

            const backdrop = modal.querySelector('div[class*="fixed inset-0 bg-gray-900"]');
            const panel = modal.querySelector('div[class*="transform"]');

            if (backdrop) {
                backdrop.classList.remove('opacity-100');
                backdrop.classList.add('opacity-0');
            }

            if (panel) {
                panel.classList.remove('opacity-100', 'translate-y-0', 'scale-100');
                panel.classList.add('opacity-0', 'translate-y-4', 'scale-95');
            }

            setTimeout(() => {
                modal.classList.add('hidden');
                if (document.querySelectorAll('[id$="Modal"]:not(.hidden)').length === 0) {
                    document.body.style.overflow = '';
                }
            }, 300);
        });
    }

    static showConfirm(message, onConfirm) {
        const textEl = document.getElementById('logoutConfirmText');
        const confirmBtn = document.getElementById('confirmLogout');
        const cancelBtn = document.getElementById('cancelLogout');

        if (textEl) textEl.textContent = message;

        const newConfirmBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

        const newCancelBtn = cancelBtn.cloneNode(true);
        cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);

        newConfirmBtn.addEventListener('click', () => {
            onConfirm();
            this.hide('logoutConfirmModal');
        });

        newCancelBtn.addEventListener('click', () => {
            this.hide('logoutConfirmModal');
        });

        this.show('logoutConfirmModal');
    }
}

// ===== Users Manager (Main Controller) =====
class UsersManager {
    constructor() {
        this.users = [];
        this.filteredUsers = [];
        this.filters = {
            time: 'all',
            status: 'all',
            userType: 'all',
            search: ''
        };
        this.ui = new UIManager();
        this.autoRefreshInterval = null;

        this.init();
    }

    async init() {
        await this.loadData();
        this.setupEventListeners();
        this.setupKeyboardShortcuts();
        // Auto-refresh is disabled by default, user must enable it
    }

    async loadData() {
        try {
            this.ui.showLoading();
            // Clear Cache on manual refresh if needed, but here we just fetch
            const response = await ApiManager.request('get_all_sessions', {
                time_filter: 'all',
                status_filter: 'all'
            }, 'GET', false); // Set useCache=true if desired, but live data usually needs fresh fetch

            if (response.success) {
                this.users = response.sessions || [];
                this.applyFilters();

                const activeCount = response.active_count || 0;
                const todayCount = this.calculateTodayLogins(this.users);

                this.ui.updateStats(activeCount, todayCount, response.last_update);
            } else {
                console.error('Failed to load users:', response.error);
            }
        } catch (error) {
            console.error('Load Data Error:', error);
        } finally {
            this.ui.hideLoading();
        }
    }

    calculateTodayLogins(users) {
        const today = new Date().toISOString().split('T')[0];
        return users.filter(u => {
            if (!u.login_time) return false;
            return u.login_time.startsWith(today);
        }).length;
    }

    groupUsersByDate(users) {
        const groups = {};
        
        users.forEach(user => {
            // Extract date from login_time_jalali (format: yyyy/mm/dd HH:MM:SS)
            let dateKey = '';
            if (user.login_time_jalali) {
                dateKey = user.login_time_jalali.split(' ')[0]; // Get just the date part
            } else if (user.login_time) {
                // Fallback: convert Gregorian date to Persian if needed
                dateKey = 'نامشخص';
            } else {
                dateKey = 'نامشخص';
            }
            
            if (!groups[dateKey]) {
                groups[dateKey] = {
                    label: dateKey,
                    users: [],
                    key: dateKey.replace(/\//g, '-') // Replace slashes for valid HTML IDs
                };
            }
            
            groups[dateKey].users.push(user);
        });

        // Sort groups by date in descending order (newest first)
        const sortedGroups = Object.values(groups).sort((a, b) => {
            if (a.label === 'نامشخص') return 1;
            if (b.label === 'نامشخص') return -1;
            
            // Compare Persian dates (yyyy/mm/dd format)
            return b.label.localeCompare(a.label);
        });

        return sortedGroups;
    }

    setupEventListeners() {
        document.getElementById('refreshBtn').addEventListener('click', () => this.loadData());

        const searchInput = document.getElementById('searchInput');
        searchInput.addEventListener('input', Utils.debounce((e) => {
            this.filters.search = e.target.value.toLowerCase();
            this.toggleClearSearchBtn(this.filters.search.length > 0);
            this.applyFilters();
        }, CONFIG.DEBOUNCE_DELAY));

        document.getElementById('clearSearch').addEventListener('click', () => {
            searchInput.value = '';
            this.filters.search = '';
            this.toggleClearSearchBtn(false);
            this.applyFilters();
            searchInput.focus();
        });

        document.getElementById('filtersToggle').addEventListener('click', () => {
            const section = document.getElementById('filtersSection');
            const icon = document.getElementById('toggleIcon');
            section.classList.toggle('hidden');
            icon.classList.toggle('rotate-180');
        });

        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleFilterClick(e));
        });

        document.getElementById('autoRefresh').addEventListener('change', (e) => {
            if (e.target.checked) {
                this.startAutoRefresh();
            } else {
                this.stopAutoRefresh();
            }
        });

        const logoutBtn = document.getElementById('logoutUsersBtn');
        const logoutMenu = document.getElementById('logoutDropdownMenu');

        logoutBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            logoutMenu.classList.toggle('hidden');
        });

        document.addEventListener('click', (e) => {
            if (!logoutBtn.contains(e.target) && !logoutMenu.contains(e.target)) {
                logoutMenu.classList.add('hidden');
            }
            
            // Close user menus when clicking outside
            const userMenus = document.querySelectorAll('[id^="userMenu-"]');
            userMenus.forEach(menu => {
                const menuButton = document.querySelector(`[onclick*="${menu.id.replace('userMenu-', '')}"]`);
                if (!menu.contains(e.target) && (!menuButton || !menuButton.contains(e.target))) {
                    menu.classList.add('hidden');
                }
            });
        });

        document.querySelectorAll('.logout-option').forEach(opt => {
            opt.addEventListener('click', () => {
                const type = opt.dataset.type;
                this.confirmLogoutAll(type);
                logoutMenu.classList.add('hidden');
            });
        });
    }

    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Search: Ctrl+K or /
            if ((e.ctrlKey && e.key === 'k') || (e.key === '/' && document.activeElement.tagName !== 'INPUT')) {
                e.preventDefault();
                document.getElementById('searchInput').focus();
            }

            // Close Modal: Esc
            if (e.key === 'Escape') {
                ModalManager.hide();
                document.getElementById('logoutDropdownMenu').classList.add('hidden');
            }

            // Refresh: Ctrl+R (Override default?) - Maybe just custom R key if not focused
            // if (e.key === 'r' && !e.ctrlKey && document.activeElement.tagName !== 'INPUT') {
            //     this.loadData();
            // }
        });
    }

    handleFilterClick(e) {
        const btn = e.target;
        const group = btn.parentElement;

        group.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        if (btn.dataset.filter) this.filters.time = btn.dataset.filter;
        if (btn.dataset.status) this.filters.status = btn.dataset.status;
        if (btn.dataset.usertype) this.filters.userType = btn.dataset.usertype;

        this.applyFilters();
    }

    applyFilters() {
        this.filteredUsers = this.users.filter(user => {
            if (this.filters.search) {
                const term = this.filters.search;
                const searchable = [
                    user.username,
                    user.device_model,
                    user.ip_address,
                    user.userType
                ].join(' ').toLowerCase();
                if (!searchable.includes(term)) return false;
            }

            const isOnline = !user.logout_time;
            if (this.filters.status === 'online' && !isOnline) return false;
            if (this.filters.status === 'offline' && isOnline) return false;

            if (this.filters.userType !== 'all' && user.userType !== this.filters.userType) return false;

            if (this.filters.time !== 'all') {
                const loginTime = new Date(user.login_time).getTime();
                const now = new Date().getTime();
                const hoursDiff = (now - loginTime) / (1000 * 60 * 60);

                if (this.filters.time === '24h' && hoursDiff > 24) return false;
                if (this.filters.time === 'today') {
                    const today = new Date().setHours(0, 0, 0, 0);
                    if (loginTime < today) return false;
                }
            }

            return true;
        });

        this.updateActiveFiltersCount();
        this.ui.renderUsers(this.filteredUsers);
    }

    toggleClearSearchBtn(show) {
        const btn = document.getElementById('clearSearch');
        if (show) btn.classList.remove('hidden');
        else btn.classList.add('hidden');
    }

    updateActiveFiltersCount() {
        let count = 0;
        if (this.filters.time !== 'all') count++;
        if (this.filters.status !== 'all') count++;
        if (this.filters.userType !== 'all') count++;
        if (this.filters.search) count++;

        document.getElementById('activeFiltersCount').innerText = Utils.toPersianDigits(count);
    }

    startAutoRefresh() {
        if (this.autoRefreshInterval) clearInterval(this.autoRefreshInterval);

        let countdown = 60;
        const timerEl = document.getElementById('refreshTimer');
        timerEl.classList.remove('hidden');

        this.autoRefreshInterval = setInterval(() => {
            countdown--;
            timerEl.innerText = `(${Utils.toPersianDigits(countdown)})`;

            if (countdown <= 0) {
                this.loadData();
                countdown = 60;
            }
        }, 1000);
    }

    stopAutoRefresh() {
        if (this.autoRefreshInterval) clearInterval(this.autoRefreshInterval);
        const timerEl = document.getElementById('refreshTimer');
        timerEl.classList.add('hidden');
    }

    static showDetails(username, deviceId) {
        const user = usersManagerInstance.users.find(u => u.username === username && u.device_id === deviceId);
        if (!user) return;

        const modalContent = document.getElementById('modalContent');

        modalContent.innerHTML = `
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="bg-gray-50 dark:bg-gray-700/30 p-4 rounded-xl border border-gray-100 dark:border-gray-700">
                    <span class="text-xs text-gray-500 dark:text-gray-400 block mb-1">نام کاربری</span>
                    <span class="text-lg font-bold text-gray-900 dark:text-white">${user.username}</span>
                                </div>
                <div class="bg-gray-50 dark:bg-gray-700/30 p-4 rounded-xl border border-gray-100 dark:border-gray-700">
                    <span class="text-xs text-gray-500 dark:text-gray-400 block mb-1">نقش</span>
                    <span class="text-lg font-medium text-gray-900 dark:text-white">${user.userType}</span>
                                </div>
                <div class="bg-gray-50 dark:bg-gray-700/30 p-4 rounded-xl border border-gray-100 dark:border-gray-700">
                    <span class="text-xs text-gray-500 dark:text-gray-400 block mb-1">دستگاه</span>
                    <span class="text-lg font-medium text-gray-900 dark:text-white dir-ltr text-right font-mono">${user.device_model}</span>
                            </div>
                ${user.app_version ? `
                <div class="bg-gray-50 dark:bg-gray-700/30 p-4 rounded-xl border border-gray-100 dark:border-gray-700">
                    <span class="text-xs text-gray-500 dark:text-gray-400 block mb-1">نسخه برنامه</span>
                    <span class="text-lg font-medium text-gray-900 dark:text-white dir-ltr text-right font-mono">${user.app_version}</span>
                </div>
                ` : ''}
                <div class="bg-gray-50 dark:bg-gray-700/30 p-4 rounded-xl border border-gray-100 dark:border-gray-700">
                    <span class="text-xs text-gray-500 dark:text-gray-400 block mb-1">IP Address</span>
                    <span class="text-lg font-medium text-gray-900 dark:text-white font-mono">${user.ip_address}</span>
                        </div>
                <div class="bg-green-50 dark:bg-green-900/20 p-4 rounded-xl border border-green-100 dark:border-green-900/30">
                    <span class="text-xs text-green-600 dark:text-green-400 block mb-1 flex items-center gap-1">
                        <i class="fas fa-sign-in-alt"></i> زمان ورود
                    </span>
                    <span class="text-lg font-bold text-green-700 dark:text-green-300 font-mono">${user.login_time_jalali || '-'}</span>
                </div>
                <div class="bg-orange-50 dark:bg-orange-900/20 p-4 rounded-xl border border-orange-100 dark:border-orange-900/30">
                    <span class="text-xs text-orange-600 dark:text-orange-400 block mb-1 flex items-center gap-1">
                        <i class="fas fa-history"></i> آخرین فعالیت
                    </span>
                    <span class="text-lg font-bold text-orange-700 dark:text-orange-300 font-mono">${user.last_activity_jalali || '-'}</span>
                </div>
                ${user.logout_time_jalali ? `
                <div class="bg-red-50 dark:bg-red-900/20 p-4 rounded-xl border border-red-100 dark:border-red-900/30 col-span-1 md:col-span-2">
                    <span class="text-xs text-red-600 dark:text-red-400 block mb-1 flex items-center gap-1">
                        <i class="fas fa-sign-out-alt"></i> زمان خروج
                    </span>
                    <span class="text-lg font-bold text-red-700 dark:text-red-300 font-mono">${user.logout_time_jalali}</span>
                </div>
                ` : ''}
            </div>
        `;

        ModalManager.show('userModal');
    }

    static confirmLogout(username, deviceId) {
        ModalManager.showConfirm(
            `آیا مطمئن هستید که می‌خواهید کاربر ${username} را خارج کنید؟`,
            () => this.performLogout(username, deviceId)
        );
    }

    static async performLogout(username, deviceId) {
        try {
            const response = await ApiManager.request('force_logout', { username, device_id: deviceId }, 'POST');
            if (response.success) {
                usersManagerInstance.loadData();
            } else {
                alert('خطا در خروج کاربر: ' + response.error);
            }
        } catch (e) {
            alert('خطا در برقراری ارتباط');
        }
    }

    static viewActivityHistory(username, deviceId) {
        const user = usersManagerInstance.users.find(u => u.username === username && u.device_id === deviceId);
        if (!user) return;

        const modalContent = document.getElementById('modalContent');
        
        modalContent.innerHTML = `
            <div class="space-y-6">
                <div class="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-xl border border-blue-100 dark:border-blue-900/30">
                    <h4 class="text-lg font-bold text-blue-900 dark:text-blue-300 mb-3 flex items-center gap-2">
                        <i class="fas fa-history"></i> سابقه فعالیت ${username}
                    </h4>
                    <div class="space-y-3">
                        <div class="flex justify-between items-center">
                            <span class="text-sm text-gray-600 dark:text-gray-400">زمان ورود:</span>
                            <span class="text-sm font-mono font-medium text-gray-900 dark:text-white">${user.login_time_jalali || '-'}</span>
                        </div>
                        <div class="flex justify-between items-center">
                            <span class="text-sm text-gray-600 dark:text-gray-400">آخرین فعالیت:</span>
                            <span class="text-sm font-mono font-medium text-gray-900 dark:text-white">${user.last_activity_jalali || '-'}</span>
                        </div>
                        ${user.logout_time_jalali ? `
                        <div class="flex justify-between items-center">
                            <span class="text-sm text-gray-600 dark:text-gray-400">زمان خروج:</span>
                            <span class="text-sm font-mono font-medium text-red-600 dark:text-red-400">${user.logout_time_jalali}</span>
                        </div>
                        ` : ''}
                        <div class="flex justify-between items-center">
                            <span class="text-sm text-gray-600 dark:text-gray-400">مدت فعالیت:</span>
                            <span class="text-sm font-mono font-medium text-gray-900 dark:text-white">${this.calculateActivityDuration(user)}</span>
                        </div>
                    </div>
                </div>
                
                <div class="bg-gray-50 dark:bg-gray-700/30 p-4 rounded-xl">
                    <h5 class="text-sm font-bold text-gray-700 dark:text-gray-300 mb-2">اطلاعات دستگاه</h5>
                    <div class="grid grid-cols-2 gap-3 text-sm">
                        <div>
                            <span class="text-gray-500 dark:text-gray-400">مدل:</span>
                            <span class="font-mono text-gray-900 dark:text-white mr-2">${user.device_model || 'نامشخص'}</span>
                        </div>
                        ${user.app_version ? `
                        <div>
                            <span class="text-gray-500 dark:text-gray-400">نسخه برنامه:</span>
                            <span class="font-mono text-gray-900 dark:text-white mr-2">${user.app_version}</span>
                        </div>
                        ` : ''}
                        <div>
                            <span class="text-gray-500 dark:text-gray-400">IP:</span>
                            <span class="font-mono text-gray-900 dark:text-white mr-2">${user.ip_address}</span>
                        </div>
                    </div>
                </div>
            </div>
        `;

        ModalManager.show('userModal');
    }

    static calculateActivityDuration(user) {
        if (!user.login_time) return 'نامشخص';
        
        const login = new Date(user.login_time);
        const logout = user.logout_time ? new Date(user.logout_time) : new Date();
        const diff = logout - login;
        
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        
        if (hours > 0) {
            return `${Utils.toPersianDigits(hours)} ساعت و ${Utils.toPersianDigits(minutes)} دقیقه`;
        } else {
            return `${Utils.toPersianDigits(minutes)} دقیقه`;
        }
    }

    static exportUserData(username, deviceId) {
        const user = usersManagerInstance.users.find(u => u.username === username && u.device_id === deviceId);
        if (!user) return;

        const userData = {
            نام_کاربری: user.username,
            نقش: user.userType,
            دستگاه: user.device_model,
            نسخه_برنامه: user.app_version || 'نامشخص',
            IP: user.ip_address,
            زمان_ورود: user.login_time_jalali,
            آخرین_فعالیت: user.last_activity_jalali,
            زمان_خروج: user.logout_time_jalali || 'هنوز آنلاین است'
        };

        const csvContent = "data:text/csv;charset=utf-8," 
            + Object.keys(userData).join(",") + "\n"
            + Object.values(userData).join(",");

        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `user_${username}_${Date.now()}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    static exportDetailedReport() {
        const users = usersManagerInstance.users;
        const now = new Date();
        const reportData = {
            گزارش: 'آمار جامع کاربران سیستم',
            تاریخ_تولید: now.toLocaleDateString('fa-IR'),
            زمان_تولید: now.toLocaleTimeString('fa-IR'),
            کل_جلسات: users.length,
            کاربران_آنلاین: users.filter(u => !u.logout_time).length,
            کاربران_آفلاین: users.filter(u => u.logout_time).length,
            مدیران_سیستم: users.filter(u => u.userType === 'admin').length,
            اپراتورها: users.filter(u => u.userType === 'operator').length,
            تایید_کنندگان: users.filter(u => u.userType === 'verifier').length,
            کاربران_عادی: users.filter(u => u.userType === 'user').length
        };

        // Create detailed CSV content
        let csvContent = "data:text/csv;charset=utf-8,BOM\ufeff";
        
        // Add summary section
        csvContent += "خلاصه گزارش\n";
        csvContent += "نام,مقدار\n";
        Object.entries(reportData).forEach(([key, value]) => {
            csvContent += `"${key}","${value}"\n`;
        });
        
        csvContent += "\n\nجزئیات کاربران\n";
        csvContent += "نام کاربری,نقش,دستگاه,نسخه برنامه,IP,زمان ورود,آخرین فعالیت,زمان خروج,وضعیت,مدت جلسه\n";
        
        users.forEach(user => {
            const duration = UsersManager.calculateActivityDuration(user);
            const status = !user.logout_time ? 'آنلاین' : 'آفلاین';
            
            csvContent += `"${user.username}","${user.userType}","${user.device_model || 'نامشخص'}","${user.app_version || 'نامشخص'}","${user.ip_address}","${user.login_time_jalali || '-'}","${user.last_activity_jalali || '-'}","${user.logout_time_jalali || '-'}","${status}","${duration}"\n`;
        });

        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `detailed_report_${Date.now()}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    confirmLogoutAll(type) {
        let msg = 'آیا از خروج همه کاربران اطمینان دارید؟';
        if (type === 'except-admin') msg = 'خروج همه به جز مدیران؟';
        if (type === 'operators') msg = 'خروج همه اپراتورها؟';

        ModalManager.showConfirm(msg, () => {
            if (type === 'all') {
                this.logoutAll();
            } else {
                alert('این قابلیت در حال پیاده‌سازی است');
            }
        });
    }

    async logoutAll() {
        try {
            const response = await ApiManager.request('logout_all_users', {}, 'POST');
            if (response.success) {
                this.loadData();
            }
        } catch (e) {
            console.error(e);
        }
    }
}

// ===== Global Initialization =====
let usersManagerInstance;

document.addEventListener('DOMContentLoaded', () => {
    new ThemeManager();
    usersManagerInstance = new UsersManager();

    window.UsersManager = UsersManager;
    window.ModalManager = ModalManager;
    window.UIManager = UIManager;
    window.showAdvancedStats = () => {
        const content = document.getElementById('statsModalContent');
        const users = usersManagerInstance.users;
        const total = users.length;
        const online = users.filter(u => !u.logout_time).length;
        const offline = total - online;

        // Calculate statistics by user type
        const userTypes = ['admin', 'operator', 'verifier', 'user'];
        const typeStats = userTypes.map(type => {
            const typeUsers = users.filter(u => u.userType === type);
            const typeOnline = typeUsers.filter(u => !u.logout_time).length;
            return {
                type,
                label: UIManager.prototype.getUserTypeText(type),
                total: typeUsers.length,
                online: typeOnline,
                percentage: total > 0 ? ((typeUsers.length / total) * 100).toFixed(1) : 0
            };
        });

        // Calculate device statistics
        const deviceStats = {};
        users.forEach(user => {
            const device = user.device_model || 'نامشخص';
            if (!deviceStats[device]) {
                deviceStats[device] = { name: device, count: 0, online: 0 };
            }
            deviceStats[device].count++;
            if (!user.logout_time) deviceStats[device].online++;
        });

        const topDevices = Object.values(deviceStats)
            .sort((a, b) => b.count - a.count)
            .slice(0, 5);

        // Calculate hourly activity for today
        const hourlyActivity = new Array(24).fill(0);
        const today = new Date().toDateString();
        
        users.forEach(user => {
            if (user.login_time && new Date(user.login_time).toDateString() === today) {
                const hour = new Date(user.login_time).getHours();
                hourlyActivity[hour]++;
            }
        });

        // Calculate average session duration
        const completedSessions = users.filter(u => u.logout_time);
        let totalDuration = 0;
        completedSessions.forEach(user => {
            const duration = new Date(user.logout_time) - new Date(user.login_time);
            totalDuration += duration;
        });
        const avgDuration = completedSessions.length > 0 ? 
            Math.floor(totalDuration / completedSessions.length / (1000 * 60)) : 0;

        content.innerHTML = `
            <div class="space-y-6">
                <!-- Overview Cards -->
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                    <div class="p-4 bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-2xl text-center border border-blue-200 dark:border-blue-800">
                        <div class="text-3xl font-bold text-blue-600 dark:text-blue-400">${Utils.toPersianDigits(total)}</div>
                        <div class="text-sm text-blue-600/70 dark:text-blue-400/70">کل جلسات</div>
                    </div>
                    <div class="p-4 bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-2xl text-center border border-green-200 dark:border-green-800">
                        <div class="text-3xl font-bold text-green-600 dark:text-green-400">${Utils.toPersianDigits(online)}</div>
                        <div class="text-sm text-green-600/70 dark:text-green-400/70">آنلاین</div>
                    </div>
                    <div class="p-4 bg-gradient-to-br from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 rounded-2xl text-center border border-red-200 dark:border-red-800">
                        <div class="text-3xl font-bold text-red-600 dark:text-red-400">${Utils.toPersianDigits(offline)}</div>
                        <div class="text-sm text-red-600/70 dark:text-red-400/70">آفلاین</div>
                    </div>
                    <div class="p-4 bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-2xl text-center border border-purple-200 dark:border-purple-800">
                        <div class="text-3xl font-bold text-purple-600 dark:text-purple-400">${Utils.toPersianDigits(avgDuration)}</div>
                        <div class="text-sm text-purple-600/70 dark:text-purple-400/70">میانگین جلسه (دقیقه)</div>
                    </div>
                </div>

                <!-- User Type Distribution -->
                <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 border border-gray-200 dark:border-gray-700">
                    <h4 class="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                        <i class="fas fa-users text-blue-500"></i>
                        توزیع کاربران بر اساس نقش
                    </h4>
                    <div class="space-y-3">
                        ${typeStats.map(stat => `
                            <div class="flex items-center justify-between">
                                <div class="flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-full bg-${UIManager.prototype.getUserTypeColor(stat.type)}-100 dark:bg-${UIManager.prototype.getUserTypeColor(stat.type)}-900/30 flex items-center justify-center">
                                        ${UIManager.prototype.getUserTypeIcon(stat.type)}
                                    </div>
                                    <span class="text-sm font-medium text-gray-700 dark:text-gray-300">${stat.label}</span>
                                </div>
                                <div class="flex items-center gap-4">
                                    <div class="flex items-center gap-2">
                                        <span class="text-sm text-gray-500 dark:text-gray-400">${Utils.toPersianDigits(stat.online)} آنلاین</span>
                                        <span class="text-sm text-gray-500 dark:text-gray-400">/ ${Utils.toPersianDigits(stat.total)} کل</span>
                                    </div>
                                    <div class="w-24 bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                                        <div class="bg-${UIManager.prototype.getUserTypeColor(stat.type)}-500 h-2 rounded-full transition-all duration-500" style="width: ${stat.percentage}%"></div>
                                    </div>
                                    <span class="text-sm font-bold text-gray-900 dark:text-white">${Utils.toPersianDigits(stat.percentage)}%</span>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>

                <!-- Device Statistics -->
                <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 border border-gray-200 dark:border-gray-700">
                    <h4 class="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                        <i class="fas fa-mobile-alt text-green-500"></i>
                        پراستفاده‌ترین دستگاه‌ها
                    </h4>
                    <div class="space-y-3">
                        ${topDevices.map((device, index) => `
                            <div class="flex items-center justify-between">
                                <div class="flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center text-sm font-bold text-gray-600 dark:text-gray-400">
                                        ${index + 1}
                                    </div>
                                    <span class="text-sm font-medium text-gray-700 dark:text-gray-300 truncate max-w-[200px]" title="${device.name}">${device.name}</span>
                                </div>
                                <div class="flex items-center gap-4">
                                    <div class="flex items-center gap-2">
                                        <span class="text-sm text-green-500">${Utils.toPersianDigits(device.online)} آنلاین</span>
                                        <span class="text-sm text-gray-500 dark:text-gray-400">/ ${Utils.toPersianDigits(device.count)} کل</span>
                                    </div>
                                    <div class="w-24 bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                                        <div class="bg-green-500 h-2 rounded-full transition-all duration-500" style="width: ${total > 0 ? (device.count / total * 100) : 0}%"></div>
                                    </div>
                                </div>
                            </div>
                        `).join('')}
                        ${topDevices.length === 0 ? '<div class="text-center text-gray-500 dark:text-gray-400 py-4">داده‌ای برای نمایش وجود ندارد</div>' : ''}
                    </div>
                </div>

                <!-- Hourly Activity Chart -->
                <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 border border-gray-200 dark:border-gray-700">
                    <h4 class="text-lg font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                        <i class="fas fa-chart-bar text-orange-500"></i>
                        فعالیت ساعتی امروز
                    </h4>
                    <div class="h-40 flex items-end justify-between gap-1">
                        ${hourlyActivity.map((count, hour) => `
                            <div class="flex-1 flex flex-col items-center gap-1">
                                <div class="w-full bg-gradient-to-t from-orange-500 to-orange-400 rounded-t transition-all duration-500 hover:from-orange-600 hover:to-orange-500" 
                                     style="height: ${Math.max(count * 5, 2)}%; min-height: 4px;"
                                     title="ساعت ${Utils.toPersianDigits(hour)}: ${Utils.toPersianDigits(count)} کاربر">
                                </div>
                                <span class="text-xs text-gray-500 dark:text-gray-400">${Utils.toPersianDigits(hour)}</span>
                            </div>
                        `).join('')}
                    </div>
                </div>

                <!-- Quick Actions -->
                <div class="flex gap-3 justify-center pt-4 border-t border-gray-200 dark:border-gray-700">
                    <button onclick="UsersManager.exportDetailedReport()" class="px-4 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors flex items-center gap-2">
                        <i class="fas fa-file-export"></i>
                        خروجی گزارش کامل
                    </button>
                    <button onclick="window.print()" class="px-4 py-2 bg-gray-600 text-white rounded-xl hover:bg-gray-700 transition-colors flex items-center gap-2">
                        <i class="fas fa-print"></i>
                        چاپ گزارش
                    </button>
                </div>
            </div>
        `;

        ModalManager.show('statsModal');
    };

    window.closeModal = (id) => ModalManager.hide(id);

    window.exportToCSV = () => {
        const users = usersManagerInstance.filteredUsers;
        if (!users.length) return alert('داده‌ای برای خروجی وجود ندارد');

        const headers = ['نام کاربری', 'نقش', 'دستگاه', 'IP', 'زمان ورود', 'زمان خروج', 'وضعیت'];
        const csvContent = [
            headers.join(','),
            ...users.map(u => [
                u.username,
                u.userType,
                u.device_model,
                u.ip_address,
                u.login_time_jalali,
                u.logout_time_jalali || '-',
                !u.logout_time ? 'آنلاین' : 'آفلاین'
            ].join(','))
        ].join('\n');

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `users_export_${new Date().getTime()}.csv`;
        link.click();
    };

    window.clearAllFilters = () => {
        const btns = document.querySelectorAll('.filter-btn');
        btns.forEach(btn => btn.classList.remove('active'));

        document.querySelector('[data-filter="all"]').classList.add('active');
        document.querySelector('[data-status="all"]').classList.add('active');
        document.querySelector('[data-usertype="all"]').classList.add('active');

        document.getElementById('searchInput').value = '';
        document.getElementById('clearSearch').classList.add('hidden');

        usersManagerInstance.filters = { time: 'all', status: 'all', userType: 'all', search: '' };
        usersManagerInstance.applyFilters();
    };
});