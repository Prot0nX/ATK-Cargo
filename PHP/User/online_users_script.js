// پنل مدیریت کاربران آنلاین ATK — معماری ماژولار ES6+. بدون Tailwind/Font Awesome؛ کلاس‌ها معنایی‌اند و از assets/app.css + ../assets/ui/core.css می‌آیند. به‌خاطر CSP سخت‌گیرانه‌ی این پنل (script-src/style-src 'self'، بدون unsafe-inline) هیچ‌جا از onclick="" یا style="" استفاده نمی‌شود؛ همه‌چیز با addEventListener و کلاس/attribute انجام می‌شود.

// ===== پیکربندی =====
const CONFIG = {
    API_URL: 'online_users_api.php',
    REFRESH_INTERVAL: 60000, // ۶۰ ثانیه
    DEBOUNCE_DELAY: 300,
    HISTORY_DAYS: 90
};

// ===== ابزارهای عمومی =====
const Utils = {
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func(...args), wait);
        };
    },

    toPersianDigits(str) {
        if (str === null || str === undefined) return '';
        return str.toString().replace(/\d/g, d => '۰۱۲۳۴۵۶۷۸۹'[d]);
    }
};

// ===== مدیریت تم ===== الگوی دقیق toggleTheme در Quota_Reports/assets/app.js: data-theme + کوکی سمت سرور (بدون FOUC).
class ThemeManager {
    constructor() {
        this.toggleBtn = document.getElementById('themeToggle');
        if (this.toggleBtn) {
            this.toggleBtn.addEventListener('click', () => this.toggle());
        }
    }

    toggle() {
        const root = document.documentElement;
        const isDark = root.getAttribute('data-theme') === 'dark'
            || (!root.hasAttribute('data-theme') && window.matchMedia('(prefers-color-scheme: dark)').matches);
        const next = isDark ? 'light' : 'dark';
        root.setAttribute('data-theme', next);
        document.cookie = 'online_users_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }
}

// ===== ارتباط با API =====
class ApiManager {
    static csrfToken() {
        const meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.content : '';
    }

    static async request(action, params = {}, method = 'GET') {
        const url = new URL(CONFIG.API_URL, window.location.href);
        url.searchParams.set('action', action);

        const options = {
            method,
            credentials: 'same-origin',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        };

        if (method === 'GET') {
            Object.keys(params).forEach(key => url.searchParams.set(key, params[key]));
        } else {
            options.headers['Content-Type'] = 'application/json';
            options.headers['X-CSRF-Token'] = ApiManager.csrfToken();
            options.body = JSON.stringify(params);
        }

        const response = await fetch(url, options);
        if (response.status === 401) {
            window.location.href = 'login.php';
            throw new Error('نشست منقضی شده است.');
        }

        return response.json();
    }
}

// ===== مدیریت رابط کاربری =====
class UIManager {
    constructor() {
        this.el = {
            usersGrid: document.getElementById('usersGrid'),
            skeletonLoader: document.getElementById('skeletonLoader'),
            emptyState: document.getElementById('emptyState'),
            activeSessionsCount: document.getElementById('activeSessionsCount'),
            todayLoginsCount: document.getElementById('todayLoginsCount'),
            lastUpdate: document.getElementById('lastUpdate'),
            refreshIcon: document.querySelector('#refreshBtn .icon'),
            totalCount: document.getElementById('totalCount'),
            toasts: document.getElementById('toasts')
        };
    }

    toast(message, type) {
        const node = document.createElement('div');
        node.className = 'toast toast-' + (type || 'success');
        node.textContent = message;
        this.el.toasts.appendChild(node);
        setTimeout(() => node.remove(), 3500);
    }

    showLoading() {
        this.el.skeletonLoader.classList.remove('is-hidden');
        if (this.el.refreshIcon) this.el.refreshIcon.classList.add('is-spinning');
    }

    hideLoading() {
        this.el.skeletonLoader.classList.add('is-hidden');
        if (this.el.refreshIcon) this.el.refreshIcon.classList.remove('is-spinning');
    }

    showEmptyState() {
        this.el.emptyState.classList.remove('is-hidden');
        this.el.usersGrid.classList.add('is-hidden');
    }

    hideEmptyState() {
        this.el.emptyState.classList.add('is-hidden');
        this.el.usersGrid.classList.remove('is-hidden');
    }

    updateStats(activeCount, todayCount, lastUpdate, totalCount) {
        this.el.activeSessionsCount.textContent = Utils.toPersianDigits(activeCount);
        this.el.todayLoginsCount.textContent = Utils.toPersianDigits(todayCount);
        if (lastUpdate) this.el.lastUpdate.textContent = lastUpdate;
        this.el.totalCount.textContent = totalCount ? Utils.toPersianDigits(totalCount) + ' ردیف' : '';
    }

    renderUsers(users) {
        this.el.usersGrid.innerHTML = '';

        if (!users || users.length === 0) {
            this.showEmptyState();
            return;
        }
        this.hideEmptyState();

        const groups = usersManagerInstance.groupUsersByDate(users);
        groups.forEach((group, index) => {
            this.el.usersGrid.appendChild(this.createDateGroup(group, index === 0));
        });
    }

    createDateGroup(group, isOpen) {
        const section = document.createElement('div');
        section.className = 'date-group';

        const headerId = 'header-' + group.key;
        const bodyId = 'content-' + group.key;

        const header = document.createElement('button');
        header.type = 'button';
        header.className = 'date-group-header';
        header.id = headerId;
        header.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        header.setAttribute('aria-controls', bodyId);
        header.innerHTML = `
            <span class="date-group-title">
                <span class="date-group-icon">
                    <svg class="icon" aria-hidden="true"><use href="#calendar"></use></svg>
                </span>
                <span>
                    <span class="date-group-label">${Utils.escapeHtml(group.label)}</span>
                    <span class="date-group-count">${Utils.toPersianDigits(group.users.length)} کاربر</span>
                </span>
            </span>
            <svg class="icon chevron-icon" aria-hidden="true"><use href="#chevron-down"></use></svg>
        `;

        const body = document.createElement('div');
        body.className = 'date-group-body user-grid' + (isOpen ? '' : ' is-hidden');
        body.id = bodyId;
        group.users.forEach(user => body.appendChild(this.createUserCard(user)));

        section.appendChild(header);
        section.appendChild(body);
        return section;
    }

    static toggleDateGroup(header) {
        const body = document.getElementById(header.getAttribute('aria-controls'));
        if (!body) return;

        // بستن سایر گروه‌ها قبل از باز کردن این یکی — همان رفتار آکاردئون قبلی.
        document.querySelectorAll('.date-group-header').forEach(otherHeader => {
            if (otherHeader === header) return;
            otherHeader.setAttribute('aria-expanded', 'false');
            const otherBody = document.getElementById(otherHeader.getAttribute('aria-controls'));
            if (otherBody) otherBody.classList.add('is-hidden');
        });

        const isOpen = header.getAttribute('aria-expanded') === 'true';
        header.setAttribute('aria-expanded', isOpen ? 'false' : 'true');
        body.classList.toggle('is-hidden', isOpen);
    }

    getRoleMeta(type) {
        const map = {
            admin: { text: 'مدیر سیستم', icon: 'user-shield', roleClass: 'is-role-admin', badge: 'badge-warning' },
            operator: { text: 'اپراتور', icon: 'headset', roleClass: 'is-role-operator', badge: 'badge-info' },
            verifier: { text: 'تأیید کننده', icon: 'user-check', roleClass: 'is-role-verifier', badge: 'badge-active' },
            user: { text: 'کاربر عادی', icon: 'user', roleClass: '', badge: '' }
        };
        return map[type] || map.user;
    }

    createUserCard(user) {
        const isOnline = !user.logout_time && !user.logout_time_jalali;
        const role = this.getRoleMeta(user.userType);
        const menuId = 'userMenu-' + user.username + '-' + user.device_id;

        const card = document.createElement('div');
        card.className = 'user-card';

        card.innerHTML = `
            <div class="user-card-top">
                <div class="user-card-identity">
                    <span class="user-card-avatar ${role.roleClass}">
                        <svg class="icon" aria-hidden="true"><use href="#${role.icon}"></use></svg>
                        <span class="user-card-status-dot${isOnline ? ' is-online' : ''}"></span>
                    </span>
                    <span class="user-card-name-group">
                        <span class="user-card-name">${Utils.escapeHtml(user.username)}</span>
                        <span class="badge user-card-role ${role.badge}">${role.text}</span>
                    </span>
                </div>
                <div class="user-card-menu">
                    <button type="button" class="btn-icon" data-menu-toggle aria-label="گزینه‌های بیشتر" aria-haspopup="true">
                        <svg class="icon" aria-hidden="true"><use href="#ellipsis-vertical"></use></svg>
                    </button>
                    <div class="user-card-menu-panel is-hidden" id="${menuId}">
                        <button type="button" class="menu-item" data-action="details" data-username="${Utils.escapeAttr(user.username)}" data-device-id="${Utils.escapeAttr(user.device_id)}">
                            <svg class="icon" aria-hidden="true"><use href="#id-card"></use></svg>
                            جزئیات کاربر
                        </button>
                        <button type="button" class="menu-item" data-action="activity" data-username="${Utils.escapeAttr(user.username)}" data-device-id="${Utils.escapeAttr(user.device_id)}">
                            <svg class="icon" aria-hidden="true"><use href="#history"></use></svg>
                            سابقه فعالیت
                        </button>
                        ${isOnline ? `
                        <button type="button" class="menu-item is-danger" data-action="logout" data-username="${Utils.escapeAttr(user.username)}" data-device-id="${Utils.escapeAttr(user.device_id)}">
                            <svg class="icon" aria-hidden="true"><use href="#sign-out"></use></svg>
                            خروج کاربر
                        </button>
                        ` : ''}
                        <button type="button" class="menu-item" data-action="export" data-username="${Utils.escapeAttr(user.username)}" data-device-id="${Utils.escapeAttr(user.device_id)}">
                            <svg class="icon" aria-hidden="true"><use href="#download"></use></svg>
                            خروجی اطلاعات
                        </button>
                    </div>
                </div>
            </div>

            <div class="user-card-meta">
                <div class="user-card-meta-row">
                    <svg class="icon" aria-hidden="true"><use href="#mobile"></use></svg>
                    <span class="user-card-meta-value" title="${Utils.escapeAttr(user.device_model || 'نامشخص')}">${Utils.escapeHtml(user.device_model || 'نامشخص')}</span>
                </div>
                ${user.app_version ? `
                <div class="user-card-meta-row">
                    <svg class="icon" aria-hidden="true"><use href="#code-branch"></use></svg>
                    <span class="user-card-meta-value">نسخه: ${Utils.escapeHtml(user.app_version)}</span>
                </div>
                ` : ''}
                <div class="user-card-meta-row">
                    <svg class="icon" aria-hidden="true"><use href="#sign-in"></use></svg>
                    <span class="user-card-meta-value">ورود: ${Utils.escapeHtml(user.login_time_jalali || '-')}</span>
                </div>
                <div class="user-card-meta-row">
                    <svg class="icon" aria-hidden="true"><use href="#history"></use></svg>
                    <span class="user-card-meta-value">آخرین فعالیت: ${Utils.escapeHtml(user.last_activity_jalali || '-')}</span>
                </div>
                ${!isOnline ? `
                <div class="user-card-meta-row">
                    <svg class="icon" aria-hidden="true"><use href="#sign-out"></use></svg>
                    <span class="user-card-meta-value">خروج: ${Utils.escapeHtml(user.logout_time_jalali || '-')}</span>
                </div>
                ` : ''}
                <div class="user-card-meta-row">
                    <svg class="icon" aria-hidden="true"><use href="#network"></use></svg>
                    <span class="user-card-meta-value">${Utils.escapeHtml(user.ip_address || '-')}</span>
                </div>
            </div>

            <div class="user-card-footer">
                <button type="button" class="btn btn-ghost btn-sm" data-action="details" data-username="${Utils.escapeAttr(user.username)}" data-device-id="${Utils.escapeAttr(user.device_id)}">جزئیات</button>
                ${isOnline ? `<button type="button" class="btn btn-danger btn-sm" data-action="logout" data-username="${Utils.escapeAttr(user.username)}" data-device-id="${Utils.escapeAttr(user.device_id)}">خروج</button>` : ''}
            </div>
        `;

        return card;
    }
}

Utils.escapeHtml = function (value) {
    const div = document.createElement('div');
    div.textContent = value === null || value === undefined ? '' : String(value);
    return div.innerHTML;
};

Utils.escapeAttr = function (value) {
    return Utils.escapeHtml(value).replace(/"/g, '&quot;');
};

// ===== مدیریت دیالوگ‌ها (dialog بومی — انیمیشن/backdrop از core.css) =====
class ModalManager {
    static show(id) {
        const dialog = document.getElementById(id);
        if (dialog && !dialog.open) dialog.showModal();
    }

    static hide(id) {
        const dialog = document.getElementById(id);
        if (dialog && dialog.open) dialog.close();
    }

    static showConfirm(message, onConfirm) {
        const textEl = document.getElementById('logoutConfirmText');
        const confirmBtn = document.getElementById('confirmLogout');
        const cancelBtn = document.getElementById('cancelLogout');

        if (textEl) textEl.textContent = message;

        // کلون کردن دکمه‌ها تا listener بار قبلی جمع نشود (چون این دیالوگ برای چند اکشن مختلف دوباره استفاده می‌شود).
        const newConfirmBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
        const newCancelBtn = cancelBtn.cloneNode(true);
        cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);

        newConfirmBtn.addEventListener('click', () => {
            onConfirm();
            ModalManager.hide('logoutConfirmModal');
        });
        newCancelBtn.addEventListener('click', () => ModalManager.hide('logoutConfirmModal'));

        ModalManager.show('logoutConfirmModal');
    }
}

// ===== کنترلر اصلی =====
class UsersManager {
    constructor() {
        this.users = [];
        this.filteredUsers = [];
        this.filters = { time: 'all', status: 'all', userType: 'all', search: '' };
        this.ui = new UIManager();
        this.autoRefreshInterval = null;

        this.init();
    }

    async init() {
        await this.loadData();
        this.setupEventListeners();
        this.setupKeyboardShortcuts();
    }

    async loadData() {
        try {
            this.ui.showLoading();
            const response = await ApiManager.request('get_all_sessions', {
                time_filter: 'all',
                status_filter: 'all'
            }, 'GET');

            if (response.success) {
                this.users = response.sessions || [];
                this.applyFilters();
                const todayCount = this.calculateTodayLogins(this.users);
                this.ui.updateStats(response.active_count || 0, todayCount, response.last_update, response.total_count);
            } else {
                this.ui.toast(response.message || 'خطا در دریافت اطلاعات کاربران', 'error');
            }
        } catch (error) {
            console.error('Load Data Error:', error);
            this.ui.toast('خطا در برقراری ارتباط با سرور', 'error');
        } finally {
            this.ui.hideLoading();
        }
    }

    calculateTodayLogins(users) {
        const today = new Date().toISOString().split('T')[0];
        return users.filter(u => u.login_time && u.login_time.startsWith(today)).length;
    }

    groupUsersByDate(users) {
        const groups = {};

        users.forEach(user => {
            const dateKey = user.login_time_jalali ? user.login_time_jalali.split(' ')[0] : 'نامشخص';
            if (!groups[dateKey]) {
                groups[dateKey] = { label: dateKey, users: [], key: dateKey.replace(/\//g, '-') };
            }
            groups[dateKey].users.push(user);
        });

        return Object.values(groups).sort((a, b) => {
            if (a.label === 'نامشخص') return 1;
            if (b.label === 'نامشخص') return -1;
            return b.label.localeCompare(a.label);
        });
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

        const filtersToggle = document.getElementById('filtersToggle');
        const filtersSection = document.getElementById('filtersSection');
        filtersToggle.addEventListener('click', () => {
            const isOpen = filtersSection.classList.toggle('is-open');
            filtersToggle.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        });

        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleFilterClick(e));
        });

        document.getElementById('clearFiltersBtn').addEventListener('click', () => this.clearAllFilters());
        document.getElementById('emptyClearFiltersBtn').addEventListener('click', () => this.clearAllFilters());

        document.getElementById('autoRefresh').addEventListener('change', (e) => {
            if (e.target.checked) this.startAutoRefresh();
            else this.stopAutoRefresh();
        });

        document.getElementById('exportCsvBtn').addEventListener('click', () => this.exportToCSV());
        document.getElementById('advancedStatsBtn').addEventListener('click', () => this.showAdvancedStats());

        // منوی «مدیریت خروج» در نوار ابزار
        const logoutBtn = document.getElementById('logoutUsersBtn');
        const logoutMenu = document.getElementById('logoutDropdownMenu');
        logoutBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            logoutMenu.classList.toggle('is-hidden');
        });
        document.querySelectorAll('.logout-option').forEach(opt => {
            opt.addEventListener('click', () => {
                this.confirmLogoutAll(opt.dataset.type);
                logoutMenu.classList.add('is-hidden');
            });
        });

        // بستن منوهای باز (کارت‌ها + منوی خروج) با کلیک بیرون از آن‌ها
        document.addEventListener('click', (e) => {
            if (!logoutBtn.contains(e.target) && !logoutMenu.contains(e.target)) {
                logoutMenu.classList.add('is-hidden');
            }
            document.querySelectorAll('.user-card-menu-panel:not(.is-hidden)').forEach(menu => {
                if (!menu.parentElement.contains(e.target)) menu.classList.add('is-hidden');
            });
        });

        // رویدادهای گرید کاربران (نمایش/آکاردئون/منو/اکشن‌ها) با delegation — کارت‌ها پویا ساخته می‌شوند.
        document.getElementById('usersGrid').addEventListener('click', (e) => {
            const header = e.target.closest('.date-group-header');
            if (header) {
                UIManager.toggleDateGroup(header);
                return;
            }

            const menuToggle = e.target.closest('[data-menu-toggle]');
            if (menuToggle) {
                e.stopPropagation();
                const panel = menuToggle.nextElementSibling;
                document.querySelectorAll('.user-card-menu-panel').forEach(p => {
                    if (p !== panel) p.classList.add('is-hidden');
                });
                panel.classList.toggle('is-hidden');
                return;
            }

            const actionBtn = e.target.closest('[data-action]');
            if (actionBtn) {
                const { action, username, deviceId } = actionBtn.dataset;
                this.handleCardAction(action, username, deviceId);
                document.querySelectorAll('.user-card-menu-panel').forEach(p => p.classList.add('is-hidden'));
            }
        });

        // دکمه‌های data-close روی هر سه دیالوگ — همان الگوی Quota_Reports.
        document.querySelectorAll('[data-close]').forEach(button => {
            button.addEventListener('click', () => button.closest('dialog').close());
        });
    }

    handleCardAction(action, username, deviceId) {
        if (action === 'details') this.showDetails(username, deviceId);
        else if (action === 'activity') this.showActivityHistory(username, deviceId);
        else if (action === 'logout') this.confirmLogout(username, deviceId);
        else if (action === 'export') this.exportUserData(username, deviceId);
    }

    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey && e.key === 'k') || (e.key === '/' && document.activeElement.tagName !== 'INPUT')) {
                e.preventDefault();
                document.getElementById('searchInput').focus();
            }
        });
    }

    handleFilterClick(e) {
        const btn = e.target.closest('.filter-btn');
        if (!btn) return;
        const group = btn.parentElement;

        group.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('is-selected'));
        btn.classList.add('is-selected');

        if (btn.dataset.filter) this.filters.time = btn.dataset.filter;
        if (btn.dataset.status) this.filters.status = btn.dataset.status;
        if (btn.dataset.usertype) this.filters.userType = btn.dataset.usertype;

        this.applyFilters();
    }

    applyFilters() {
        this.filteredUsers = this.users.filter(user => {
            if (this.filters.search) {
                const searchable = [user.username, user.device_model, user.ip_address, user.userType]
                    .join(' ').toLowerCase();
                if (!searchable.includes(this.filters.search)) return false;
            }

            const isOnline = !user.logout_time;
            if (this.filters.status === 'online' && !isOnline) return false;
            if (this.filters.status === 'offline' && isOnline) return false;
            if (this.filters.userType !== 'all' && user.userType !== this.filters.userType) return false;

            if (this.filters.time !== 'all') {
                const loginTime = new Date(user.login_time).getTime();
                const now = Date.now();
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
        document.getElementById('clearSearch').classList.toggle('is-hidden', !show);
    }

    updateActiveFiltersCount() {
        let count = 0;
        if (this.filters.time !== 'all') count++;
        if (this.filters.status !== 'all') count++;
        if (this.filters.userType !== 'all') count++;
        if (this.filters.search) count++;
        document.getElementById('activeFiltersCount').textContent = Utils.toPersianDigits(count);
    }

    clearAllFilters() {
        document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('is-selected'));
        document.querySelector('[data-filter="all"]').classList.add('is-selected');
        document.querySelector('[data-status="all"]').classList.add('is-selected');
        document.querySelector('[data-usertype="all"]').classList.add('is-selected');

        const searchInput = document.getElementById('searchInput');
        searchInput.value = '';
        this.toggleClearSearchBtn(false);

        this.filters = { time: 'all', status: 'all', userType: 'all', search: '' };
        this.applyFilters();
    }

    startAutoRefresh() {
        if (this.autoRefreshInterval) clearInterval(this.autoRefreshInterval);

        let countdown = 60;
        const timerEl = document.getElementById('refreshTimer');
        timerEl.classList.remove('is-hidden');

        this.autoRefreshInterval = setInterval(() => {
            countdown--;
            timerEl.textContent = `(${Utils.toPersianDigits(countdown)})`;
            if (countdown <= 0) {
                this.loadData();
                countdown = 60;
            }
        }, 1000);
    }

    stopAutoRefresh() {
        if (this.autoRefreshInterval) clearInterval(this.autoRefreshInterval);
        document.getElementById('refreshTimer').classList.add('is-hidden');
    }

    findUser(username, deviceId) {
        return this.users.find(u => u.username === username && u.device_id === deviceId);
    }

    showDetails(username, deviceId) {
        const user = this.findUser(username, deviceId);
        if (!user) return;

        const rows = [
            ['نام کاربری', Utils.escapeHtml(user.username)],
            ['نقش', this.ui.getRoleMeta(user.userType).text],
            ['دستگاه', `<code>${Utils.escapeHtml(user.device_model || 'نامشخص')}</code>`]
        ];
        if (user.app_version) rows.push(['نسخه برنامه', `<code>${Utils.escapeHtml(user.app_version)}</code>`]);
        rows.push(['آدرس IP', `<code>${Utils.escapeHtml(user.ip_address || '-')}</code>`]);
        rows.push(['زمان ورود', Utils.escapeHtml(user.login_time_jalali || '-')]);
        rows.push(['آخرین فعالیت', Utils.escapeHtml(user.last_activity_jalali || '-')]);
        if (user.logout_time_jalali) rows.push(['زمان خروج', Utils.escapeHtml(user.logout_time_jalali)]);

        document.getElementById('modalContent').innerHTML = '<dl class="detail-list">' +
            rows.map(([dt, dd]) => `<dt>${dt}</dt><dd>${dd}</dd>`).join('') +
            '</dl>';

        ModalManager.show('userModal');
    }

    showActivityHistory(username, deviceId) {
        const user = this.findUser(username, deviceId);
        if (!user) return;

        const rows = [
            ['زمان ورود', Utils.escapeHtml(user.login_time_jalali || '-')],
            ['آخرین فعالیت', Utils.escapeHtml(user.last_activity_jalali || '-')]
        ];
        if (user.logout_time_jalali) rows.push(['زمان خروج', Utils.escapeHtml(user.logout_time_jalali)]);
        rows.push(['مدت فعالیت', this.calculateActivityDuration(user)]);
        rows.push(['مدل دستگاه', Utils.escapeHtml(user.device_model || 'نامشخص')]);
        if (user.app_version) rows.push(['نسخه برنامه', Utils.escapeHtml(user.app_version)]);
        rows.push(['IP', `<code>${Utils.escapeHtml(user.ip_address || '-')}</code>`]);

        document.getElementById('activityModalContent').innerHTML = '<dl class="detail-list">' +
            rows.map(([dt, dd]) => `<dt>${dt}</dt><dd>${dd}</dd>`).join('') +
            '</dl>';

        ModalManager.show('activityModal');
    }

    calculateActivityDuration(user) {
        if (!user.login_time) return 'نامشخص';
        const login = new Date(user.login_time);
        const logout = user.logout_time ? new Date(user.logout_time) : new Date();
        const diff = logout - login;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        return hours > 0
            ? `${Utils.toPersianDigits(hours)} ساعت و ${Utils.toPersianDigits(minutes)} دقیقه`
            : `${Utils.toPersianDigits(minutes)} دقیقه`;
    }

    confirmLogout(username, deviceId) {
        ModalManager.showConfirm(
            `آیا مطمئن هستید که می‌خواهید کاربر ${username} را خارج کنید؟`,
            () => this.performLogout(username, deviceId)
        );
    }

    async performLogout(username, deviceId) {
        try {
            const response = await ApiManager.request('force_logout', { username, device_id: deviceId }, 'POST');
            if (response.success) {
                this.ui.toast('کاربر با موفقیت خارج شد.', 'success');
                this.loadData();
            } else {
                this.ui.toast('خطا در خروج کاربر: ' + (response.message || response.error || ''), 'error');
            }
        } catch (e) {
            this.ui.toast('خطا در برقراری ارتباط', 'error');
        }
    }

    confirmLogoutAll(type) {
        const messages = {
            all: 'آیا از خروج همه‌ی کاربران اطمینان دارید؟',
            'except-admin': 'آیا از خروج همه‌ی کاربران به‌جز مدیران اطمینان دارید؟',
            operators: 'آیا از خروج همه‌ی اپراتورها اطمینان دارید؟'
        };
        ModalManager.showConfirm(messages[type] || messages.all, () => this.logoutAll(type));
    }

    async logoutAll(type) {
        try {
            const response = await ApiManager.request('logout_all_users', { type }, 'POST');
            if (response.success) {
                this.ui.toast(response.message || 'کاربران با موفقیت خارج شدند.', 'success');
                this.loadData();
            } else {
                this.ui.toast(response.message || 'خطا در خروج کاربران', 'error');
            }
        } catch (e) {
            this.ui.toast('خطا در برقراری ارتباط', 'error');
        }
    }

    exportUserData(username, deviceId) {
        const user = this.findUser(username, deviceId);
        if (!user) return;

        const rows = [
            ['نام_کاربری', user.username],
            ['نقش', user.userType],
            ['دستگاه', user.device_model || ''],
            ['نسخه_برنامه', user.app_version || 'نامشخص'],
            ['IP', user.ip_address || ''],
            ['زمان_ورود', user.login_time_jalali || ''],
            ['آخرین_فعالیت', user.last_activity_jalali || ''],
            ['زمان_خروج', user.logout_time_jalali || 'هنوز آنلاین است']
        ];

        this.downloadCsv(
            rows.map(r => r[0]).join(','),
            [rows.map(r => `"${String(r[1]).replace(/"/g, '""')}"`).join(',')],
            `user_${username}_${Date.now()}.csv`
        );
    }

    exportToCSV() {
        if (!this.filteredUsers.length) {
            this.ui.toast('داده‌ای برای خروجی وجود ندارد', 'error');
            return;
        }

        const headers = 'نام کاربری,نقش,دستگاه,IP,زمان ورود,زمان خروج,وضعیت';
        const lines = this.filteredUsers.map(u => [
            u.username, u.userType, u.device_model, u.ip_address,
            u.login_time_jalali, u.logout_time_jalali || '-',
            !u.logout_time ? 'آنلاین' : 'آفلاین'
        ].map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','));

        this.downloadCsv(headers, lines, `users_export_${Date.now()}.csv`);
    }

    downloadCsv(headerLine, dataLines, filename) {
        const csvContent = '﻿' + [headerLine, ...dataLines].join('\n');
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
    }

    showAdvancedStats() {
        const content = document.getElementById('statsModalContent');
        const users = this.users;
        const total = users.length;
        const online = users.filter(u => !u.logout_time).length;
        const offline = total - online;

        const userTypes = ['admin', 'operator', 'verifier', 'user'];
        const typeStats = userTypes.map(type => {
            const typeUsers = users.filter(u => u.userType === type);
            return {
                type,
                meta: this.ui.getRoleMeta(type),
                total: typeUsers.length,
                online: typeUsers.filter(u => !u.logout_time).length,
                percentage: total > 0 ? Math.round((typeUsers.length / total) * 100) : 0
            };
        });

        const deviceStats = {};
        users.forEach(user => {
            const device = user.device_model || 'نامشخص';
            if (!deviceStats[device]) deviceStats[device] = { name: device, count: 0, online: 0 };
            deviceStats[device].count++;
            if (!user.logout_time) deviceStats[device].online++;
        });
        const topDevices = Object.values(deviceStats).sort((a, b) => b.count - a.count).slice(0, 5);

        const completedSessions = users.filter(u => u.logout_time);
        const avgDuration = completedSessions.length > 0
            ? Math.floor(completedSessions.reduce((sum, u) => sum + (new Date(u.logout_time) - new Date(u.login_time)), 0)
                / completedSessions.length / (1000 * 60))
            : 0;

        content.innerHTML = `
            <div class="stat-mini-grid">
                <div class="stat-mini"><p class="stat-label">کل جلسات</p><p class="stat-value">${Utils.toPersianDigits(total)}</p></div>
                <div class="stat-mini"><p class="stat-label">آنلاین</p><p class="stat-value">${Utils.toPersianDigits(online)}</p></div>
                <div class="stat-mini"><p class="stat-label">آفلاین</p><p class="stat-value">${Utils.toPersianDigits(offline)}</p></div>
                <div class="stat-mini"><p class="stat-label">میانگین جلسه (دقیقه)</p><p class="stat-value">${Utils.toPersianDigits(avgDuration)}</p></div>
            </div>

            <h3>توزیع کاربران بر اساس نقش</h3>
            <div class="role-bar-section">
                ${typeStats.map(stat => `
                    <div class="role-bar-row">
                        <span class="role-bar-icon"><svg class="icon" aria-hidden="true"><use href="#${stat.meta.icon}"></use></svg></span>
                        <span class="muted role-bar-label">${stat.meta.text}</span>
                        <progress class="role-bar-track progress-fill" value="${stat.percentage}" max="100"></progress>
                        <span class="muted role-bar-percent">${Utils.toPersianDigits(stat.percentage)}٪</span>
                    </div>
                `).join('')}
            </div>

            <h3>پراستفاده‌ترین دستگاه‌ها</h3>
            <div class="role-bar-section">
                ${topDevices.length ? topDevices.map(device => `
                    <div class="role-bar-row">
                        <span class="muted device-bar-name" title="${Utils.escapeAttr(device.name)}">${Utils.escapeHtml(device.name)}</span>
                        <progress class="role-bar-track progress-fill" value="${total > 0 ? Math.round(device.count / total * 100) : 0}" max="100"></progress>
                        <span class="muted device-bar-count">${Utils.toPersianDigits(device.online)} / ${Utils.toPersianDigits(device.count)}</span>
                    </div>
                `).join('') : '<p class="muted">داده‌ای برای نمایش وجود ندارد</p>'}
            </div>
        `;

        ModalManager.show('statsModal');
    }
}

// ===== راه‌اندازی سراسری =====
let usersManagerInstance;

document.addEventListener('DOMContentLoaded', () => {
    new ThemeManager();
    usersManagerInstance = new UsersManager();
});
