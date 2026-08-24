// داشبورد لحظه‌ای بارگیری — جاوااسکریپت وانیلا بدون وابستگی خارجی، هم‌معماری با Quota_Reports/assets/app.js. تفاوت اصلی: این پنل به‌صورت خودکار poll می‌کند و از پشتیبانی ETag/304 سرور (AnalyticsController) واقعاً استفاده می‌کند — چیزی که در نسخه‌ی قدیمی این داشبورد اصلاً به کار گرفته نشده بود.

(function () {
    'use strict';

    var SETTINGS_KEY = 'rtd_settings';
    var SORT_TYPES = {
        loadingQuotaNumber: 'number', shipName: 'text', loadingWarehouse: 'text',
        shippingCompany: 'text', cargoType: 'text', cargoOwner: 'text',
        entryVouchers: 'number', exitVouchers: 'number', totalNetWeight: 'number'
    };
    var REFRESH_RING_CIRCUMFERENCE = 97.39;
    var NEW_ARRIVAL_TOAST_LIMIT = 4;

    var state = {
        rows: [],
        filteredRows: [],
        initialized: false,
        updatedKeys: {},
        lastEtag: null,
        search: '',
        shipFilter: '',
        carrierFilter: '',
        warehouseFilter: '',
        sortField: null,
        sortDir: 'asc',
        currentPage: 1,
        shiftInfo: null,
        settings: loadSettings(),
        refreshTimer: null,
        countdownTimer: null,
        countdownRemainingMs: 0,
        isPolling: false
    };

    var el = {
        toasts: document.getElementById('toasts'),
        connectionStatus: document.getElementById('connectionStatus'),
        dashboardCount: document.getElementById('dashboardCount'),
        shiftBadge: document.getElementById('shiftBadge'),

        searchForm: document.getElementById('searchForm'),
        searchInput: document.getElementById('searchInput'),
        shipFilter: document.getElementById('shipFilter'),
        carrierFilter: document.getElementById('carrierFilter'),
        warehouseFilter: document.getElementById('warehouseFilter'),

        refreshBtn: document.getElementById('refreshBtn'),
        refreshRing: document.getElementById('refreshRing'),
        refreshRingProgress: document.getElementById('refreshRingProgress'),
        refreshRingLabel: document.getElementById('refreshRingLabel'),

        tableHeaderRow: document.getElementById('tableHeaderRow'),
        dataBody: document.getElementById('dataBody'),
        tableLoading: document.getElementById('tableLoading'),
        tableEmpty: document.getElementById('tableEmpty'),

        pagination: document.getElementById('pagination'),
        prevPageBtn: document.getElementById('prevPageBtn'),
        nextPageBtn: document.getElementById('nextPageBtn'),
        pageInfo: document.getElementById('pageInfo'),

        settingsBtn: document.getElementById('settingsBtn'),
        settingsDialog: document.getElementById('settingsDialog'),
        refreshIntervalInput: document.getElementById('refreshIntervalInput'),
        pageSizeInput: document.getElementById('pageSizeInput'),
        notifyToastInput: document.getElementById('notifyToastInput'),
        notifySoundInput: document.getElementById('notifySoundInput'),
        notifyBrowserInput: document.getElementById('notifyBrowserInput'),

        stats: {
            quotaCount: document.getElementById('statQuotaCount'),
            entry: document.getElementById('statEntry'),
            exit: document.getElementById('statExit'),
            weight: document.getElementById('statWeight'),
            carriers: document.getElementById('statCarriers')
        }
    };

    /* تنظیمات (localStorage — ترجیح نمایشی، نه امنیتی) */
    function defaultSettings() {
        return { refreshInterval: 30000, pageSize: 25, notifyToast: true, notifySound: false, notifyBrowser: false };
    }

    function loadSettings() {
        try {
            var raw = window.localStorage.getItem(SETTINGS_KEY);
            if (!raw) { return defaultSettings(); }
            var parsed = JSON.parse(raw);
            return Object.assign(defaultSettings(), parsed);
        } catch (err) {
            return defaultSettings();
        }
    }

    function saveSettings() {
        try {
            window.localStorage.setItem(SETTINGS_KEY, JSON.stringify(state.settings));
        } catch (err) { /* حالت خصوصی مرورگر یا ذخیره‌سازی پر — نادیده گرفته می‌شود */ }
    }

    function applySettingsToForm() {
        el.refreshIntervalInput.value = String(state.settings.refreshInterval);
        el.pageSizeInput.value = String(state.settings.pageSize);
        el.notifyToastInput.checked = state.settings.notifyToast;
        el.notifySoundInput.checked = state.settings.notifySound;
        el.notifyBrowserInput.checked = state.settings.notifyBrowser;
    }

    /* توست */
    function toast(message, type) {
        var node = document.createElement('div');
        node.className = 'toast toast-' + (type || 'success');
        node.textContent = message;
        el.toasts.appendChild(node);
        setTimeout(function () { node.remove(); }, 3500);
    }

    /* قالب‌بندی اعداد */
    function formatNumber(value, decimals) {
        var n = Number(value);
        if (isNaN(n)) { return '—'; }
        return n.toLocaleString('en-US', { maximumFractionDigits: decimals || 0, minimumFractionDigits: 0 });
    }

    function cell(text, className) {
        var td = document.createElement('td');
        if (className) { td.className = className; }
        td.textContent = text === undefined || text === null || text === '' ? '—' : text;
        return td;
    }

    function rowKey(row) {
        return [row.loadingQuotaNumber, row.shipName, row.loadingWarehouse, row.shippingCompany, row.cargoType].join('|');
    }

    /* ارتباط با سرور: fetch با پشتیبانی واقعی ETag/If-None-Match ---------- AnalyticsController::sendCacheableRealTimeResponse از قبل ETag/304 می‌فرستد؛ این‌جا برای اولین بار یک کلاینت واقعاً از آن استفاده می‌کند تا در صورت بدون‌تغییر بودن داده، هیچ بدنه‌ی JSON کامل دوباره منتقل نشود. */
    function fetchRealtime() {
        var headers = {};
        if (state.lastEtag) { headers['If-None-Match'] = state.lastEtag; }

        return fetch('api.php', { method: 'GET', credentials: 'same-origin', headers: headers })
            .then(function (response) {
                if (response.status === 401) {
                    window.location.href = '../Quota_Reports/login.php?return=' + encodeURIComponent('../Realtime_Dashboard/index.php');
                    return Promise.reject(new Error('unauthenticated'));
                }
                if (response.status === 304) {
                    return null;
                }
                var etag = response.headers.get('ETag');
                if (etag) { state.lastEtag = etag; }
                return response.json().then(function (data) {
                    // برخلاف Quota_Reports/api.php، پاسخ موفق AnalyticsController::sendCacheableRealTimeResponse پوشش {success:true,...} ندارد (فقط {shiftInfo,data} خام) — success فقط روی خطا ست می‌شود؛ پس معیار موفقیت وضعیت HTTP است، نه data.success.
                    if (!response.ok) {
                        throw new Error((data && data.message) || 'خطای ناشناخته رخ داد.');
                    }
                    return data;
                });
            });
    }

    /* تشخیص کوتاژهای تازه‌وارد و ردیف‌های تغییرکرده */
    function mergeAndDiff(newRows) {
        var prevByKey = {};
        state.rows.forEach(function (r) { prevByKey[rowKey(r)] = r; });

        var updatedKeys = {};
        var arrivals = [];

        newRows.forEach(function (r) {
            var key = rowKey(r);
            var prev = prevByKey[key];
            if (!prev) {
                if (state.initialized) { arrivals.push(r); }
            } else if (Number(prev.totalVouchers) !== Number(r.totalVouchers) ||
                Number(prev.totalNetWeight) !== Number(r.totalNetWeight)) {
                updatedKeys[key] = true;
            }
        });

        state.rows = newRows;
        state.updatedKeys = updatedKeys;
        state.initialized = true;
        return arrivals;
    }

    function notifyArrivals(arrivals) {
        if (arrivals.length === 0) { return; }

        if (state.settings.notifyToast) {
            arrivals.slice(0, NEW_ARRIVAL_TOAST_LIMIT).forEach(function (r) {
                toast('کوتاژ جدید وارد شیفت شد: ' + r.loadingQuotaNumber + ' — ' + (r.shipName || ''), 'success');
            });
            if (arrivals.length > NEW_ARRIVAL_TOAST_LIMIT) {
                toast('و ' + (arrivals.length - NEW_ARRIVAL_TOAST_LIMIT) + ' مورد دیگر…', 'success');
            }
        }

        if (state.settings.notifySound) { playNotificationSound(); }

        if (state.settings.notifyBrowser && window.Notification && Notification.permission === 'granted') {
            arrivals.slice(0, NEW_ARRIVAL_TOAST_LIMIT).forEach(function (r) {
                new Notification('کوتاژ جدید', { body: r.loadingQuotaNumber + ' — ' + (r.shipName || '') });
            });
        }
    }

    function playNotificationSound() {
        try {
            var AudioCtx = window.AudioContext || window.webkitAudioContext;
            if (!AudioCtx) { return; }
            var ctx = new AudioCtx();
            var oscillator = ctx.createOscillator();
            var gain = ctx.createGain();
            oscillator.type = 'sine';
            oscillator.frequency.setValueAtTime(880, ctx.currentTime);
            gain.gain.setValueAtTime(0.001, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.15, ctx.currentTime + 0.02);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
            oscillator.connect(gain);
            gain.connect(ctx.destination);
            oscillator.start();
            oscillator.stop(ctx.currentTime + 0.35);
        } catch (err) { /* پخش صدا اختیاری است؛ خطا نباید جریان اصلی را متوقف کند */ }
    }

    /* وضعیت اتصال */
    function setConnectionStatus(isOnline) {
        el.connectionStatus.classList.toggle('is-offline', !isOnline);
        el.connectionStatus.querySelector('span').textContent = isOnline ? 'زنده' : 'قطع شده';
    }

    /* شیفت جاری */
    function renderShiftBadge(shiftInfo) {
        if (!shiftInfo) { return; }
        el.shiftBadge.textContent = 'شیفت ' + shiftInfo.type;
    }

    /* فیلترهای انتخابی (کشتی/باربری/انبار) */
    function populateSelectOptions(select, values, placeholder) {
        var current = select.value;
        select.replaceChildren();
        var opt = document.createElement('option');
        opt.value = '';
        opt.textContent = placeholder;
        select.appendChild(opt);
        values.forEach(function (v) {
            var option = document.createElement('option');
            option.value = v;
            option.textContent = v;
            select.appendChild(option);
        });
        if (values.indexOf(current) !== -1) { select.value = current; }
    }

    function uniqueSorted(values) {
        return Array.from(new Set(values.filter(Boolean))).sort(function (a, b) {
            return a.localeCompare(b, 'fa');
        });
    }

    function refreshFilterOptions() {
        populateSelectOptions(el.shipFilter, uniqueSorted(state.rows.map(function (r) { return r.shipName; })), 'همه‌ی کشتی‌ها');
        populateSelectOptions(el.carrierFilter, uniqueSorted(state.rows.map(function (r) { return r.shippingCompany; })), 'همه‌ی باربری‌ها');
        populateSelectOptions(el.warehouseFilter, uniqueSorted(state.rows.map(function (r) { return r.loadingWarehouse; })), 'همه‌ی انبارها');
    }

    /* فیلتر/جستجو/مرتب‌سازی سمت کلاینت */
    function compareValues(a, b, type) {
        if (type === 'number') { return (Number(a) || 0) - (Number(b) || 0); }
        return (a || '').toString().localeCompare((b || '').toString(), 'fa');
    }

    function applyFilters() {
        var term = state.search.trim().toLowerCase();

        var rows = state.rows.filter(function (r) {
            if (state.shipFilter && r.shipName !== state.shipFilter) { return false; }
            if (state.carrierFilter && r.shippingCompany !== state.carrierFilter) { return false; }
            if (state.warehouseFilter && r.loadingWarehouse !== state.warehouseFilter) { return false; }
            if (term) {
                var haystack = [r.loadingQuotaNumber, r.shipName, r.loadingWarehouse, r.shippingCompany, r.cargoType, r.cargoOwner];
                return haystack.some(function (v) { return (v || '').toString().toLowerCase().indexOf(term) !== -1; });
            }
            return true;
        });

        if (state.sortField) {
            var field = state.sortField;
            var type = SORT_TYPES[field];
            var dir = state.sortDir === 'desc' ? -1 : 1;
            rows.sort(function (a, b) { return compareValues(a[field], b[field], type) * dir; });
        }

        state.filteredRows = rows;
        if (state.currentPage < 1) { state.currentPage = 1; }

        renderStats(rows);
        renderTable();
    }

    /* کارت‌های آماری */
    function renderStats(rows) {
        var entry = 0, exit = 0, weight = 0;
        var carriers = new Set();
        rows.forEach(function (r) {
            entry += Number(r.entryVouchers) || 0;
            exit += Number(r.exitVouchers) || 0;
            weight += Number(r.totalNetWeight) || 0;
            if (r.shippingCompany) { carriers.add(r.shippingCompany); }
        });

        el.stats.quotaCount.textContent = formatNumber(rows.length);
        el.stats.entry.textContent = formatNumber(entry);
        el.stats.exit.textContent = formatNumber(exit);
        el.stats.weight.textContent = formatNumber(weight / 1000, 2);
        el.stats.carriers.textContent = formatNumber(carriers.size);
    }

    /* جدول */
    function buildRow(row) {
        var tr = document.createElement('tr');
        var key = rowKey(row);
        tr.dataset.key = key;
        if (state.updatedKeys[key]) { tr.className = 'is-updated'; }

        tr.appendChild(cell(row.loadingQuotaNumber));
        tr.appendChild(cell(row.shipName));
        tr.appendChild(cell(row.loadingWarehouse));
        tr.appendChild(cell(row.shippingCompany));
        tr.appendChild(cell(row.cargoType));
        tr.appendChild(cell(row.cargoOwner));
        tr.appendChild(cell(formatNumber(row.entryVouchers)));
        tr.appendChild(cell(formatNumber(row.exitVouchers)));
        tr.appendChild(cell(formatNumber(row.totalNetWeight)));

        return tr;
    }

    function renderTable() {
        var pageSize = state.settings.pageSize;
        var total = state.filteredRows.length;
        var totalPages = Math.max(1, Math.ceil(total / pageSize));
        if (state.currentPage > totalPages) { state.currentPage = totalPages; }

        var start = (state.currentPage - 1) * pageSize;
        var pageRows = state.filteredRows.slice(start, start + pageSize);

        el.dashboardCount.textContent = formatNumber(total) + ' کوتاژ';
        el.dataBody.replaceChildren();

        if (pageRows.length === 0) {
            el.tableEmpty.classList.remove('is-hidden');
        } else {
            el.tableEmpty.classList.add('is-hidden');
            var fragment = document.createDocumentFragment();
            pageRows.forEach(function (row) { fragment.appendChild(buildRow(row)); });
            el.dataBody.appendChild(fragment);
        }

        el.pagination.classList.toggle('is-hidden', total <= pageSize);
        el.pageInfo.textContent = total === 0 ? '—' :
            (start + 1) + '–' + Math.min(start + pageSize, total) + ' از ' + total;
        el.prevPageBtn.disabled = state.currentPage <= 1;
        el.nextPageBtn.disabled = state.currentPage >= totalPages;
    }

    /* بازخوانی داده */
    function poll(isManual) {
        if (state.isPolling) { return Promise.resolve(); }
        state.isPolling = true;
        if (isManual) { el.tableLoading.classList.remove('is-hidden'); }

        return fetchRealtime()
            .then(function (data) {
                setConnectionStatus(true);
                if (data === null) {
                    // 304 — داده تغییری نکرده؛ فقط رندر شیفت/جدول با state فعلی کافی است.
                    return;
                }
                state.shiftInfo = data.shiftInfo;
                renderShiftBadge(state.shiftInfo);
                var arrivals = mergeAndDiff(data.data || []);
                refreshFilterOptions();
                applyFilters();
                notifyArrivals(arrivals);
            })
            .catch(function (error) {
                if (error.message === 'unauthenticated') { return; }
                setConnectionStatus(false);
                if (isManual) { toast(error.message, 'error'); }
            })
            .finally(function () {
                state.isPolling = false;
                el.tableLoading.classList.add('is-hidden');
                resetCountdown();
            });
    }

    /* شمارش‌معکوس بازخوانی خودکار */
    function resetCountdown() {
        state.countdownRemainingMs = state.settings.refreshInterval;
        updateCountdownDisplay();
    }

    function updateCountdownDisplay() {
        var totalSeconds = Math.round(state.settings.refreshInterval / 1000);
        var remainingSeconds = Math.max(0, Math.round(state.countdownRemainingMs / 1000));
        el.refreshRingLabel.textContent = String(remainingSeconds);

        var fraction = totalSeconds > 0 ? remainingSeconds / totalSeconds : 0;
        var offset = REFRESH_RING_CIRCUMFERENCE * (1 - fraction);
        el.refreshRingProgress.setAttribute('stroke-dashoffset', String(offset));
        el.refreshRing.classList.toggle('is-urgent', remainingSeconds <= 5);
    }

    function tickCountdown() {
        state.countdownRemainingMs -= 1000;
        if (state.countdownRemainingMs <= 0) {
            poll(false);
            return;
        }
        updateCountdownDisplay();
    }

    function restartTimers() {
        if (state.countdownTimer) { clearInterval(state.countdownTimer); }
        resetCountdown();
        state.countdownTimer = setInterval(tickCountdown, 1000);
    }

    /* پوسته */
    function currentTheme() {
        var explicit = document.documentElement.dataset.theme;
        if (explicit) { return explicit; }
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function toggleTheme() {
        var next = currentTheme() === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        document.cookie = 'realtime_dashboard_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }

    /* کمکی */
    function debounce(fn, delay) {
        var timer = null;
        return function () {
            clearTimeout(timer);
            timer = setTimeout(fn, delay);
        };
    }

    /* اتصال رویدادها */
    el.searchForm.addEventListener('submit', function (event) { event.preventDefault(); });

    el.searchInput.addEventListener('input', debounce(function () {
        state.search = el.searchInput.value;
        state.currentPage = 1;
        applyFilters();
    }, 150));

    [el.shipFilter, el.carrierFilter, el.warehouseFilter].forEach(function (select) {
        select.addEventListener('change', function () {
            state.shipFilter = el.shipFilter.value;
            state.carrierFilter = el.carrierFilter.value;
            state.warehouseFilter = el.warehouseFilter.value;
            state.currentPage = 1;
            applyFilters();
        });
    });

    el.refreshBtn.addEventListener('click', function () { poll(true); });

    el.tableHeaderRow.addEventListener('click', function (event) {
        var th = event.target.closest('th[data-sort]');
        if (!th) { return; }

        var field = th.dataset.sort;
        if (state.sortField === field) {
            state.sortDir = state.sortDir === 'asc' ? 'desc' : 'asc';
        } else {
            state.sortField = field;
            state.sortDir = 'asc';
        }

        Array.prototype.forEach.call(el.tableHeaderRow.querySelectorAll('th[data-sort]'), function (header) {
            if (header === th) {
                header.dataset.sortDir = state.sortDir;
            } else {
                delete header.dataset.sortDir;
            }
        });

        applyFilters();
    });

    el.prevPageBtn.addEventListener('click', function () {
        if (state.currentPage > 1) { state.currentPage -= 1; renderTable(); }
    });

    el.nextPageBtn.addEventListener('click', function () {
        state.currentPage += 1;
        renderTable();
    });

    document.getElementById('themeToggle').addEventListener('click', toggleTheme);

    el.settingsBtn.addEventListener('click', function () {
        applySettingsToForm();
        el.settingsDialog.showModal();
    });

    document.querySelectorAll('[data-close]').forEach(function (button) {
        button.addEventListener('click', function () {
            button.closest('dialog').close();
        });
    });

    function persistSettingsFromForm() {
        var previousInterval = state.settings.refreshInterval;
        state.settings.refreshInterval = parseInt(el.refreshIntervalInput.value, 10) || 30000;
        state.settings.pageSize = parseInt(el.pageSizeInput.value, 10) || 25;
        state.settings.notifyToast = el.notifyToastInput.checked;
        state.settings.notifySound = el.notifySoundInput.checked;
        state.settings.notifyBrowser = el.notifyBrowserInput.checked;
        saveSettings();
        state.currentPage = 1;
        renderTable();
        if (state.settings.refreshInterval !== previousInterval) { restartTimers(); }
    }

    [el.refreshIntervalInput, el.pageSizeInput].forEach(function (input) {
        input.addEventListener('change', persistSettingsFromForm);
    });

    el.notifyToastInput.addEventListener('change', persistSettingsFromForm);
    el.notifySoundInput.addEventListener('change', persistSettingsFromForm);
    el.notifyBrowserInput.addEventListener('change', function () {
        if (el.notifyBrowserInput.checked && window.Notification && Notification.permission === 'default') {
            Notification.requestPermission().then(function (permission) {
                if (permission !== 'granted') { el.notifyBrowserInput.checked = false; }
                persistSettingsFromForm();
            });
            return;
        }
        persistSettingsFromForm();
    });

    /* شروع */
    el.settingsDialog && applySettingsToForm();
    setConnectionStatus(true);
    poll(true).then(function () { restartTimers(); });
})();
