// داشبورد مانیتورینگ — جاوااسکریپت (وانیلا، بدون وابستگی، هم‌الگو با Lic/assets/app.js) برخلاف Lic (که بازخوانی خودکار را عمداً حذف کرده بود)، این داشبورد هر ۳۰ ثانیه خودکار...

(function () {
    'use strict';

    var CSRF = document.querySelector('meta[name="csrf-token"]').content;
    var REFRESH_INTERVAL_MS = 30000;

    var SEVERITY_LABEL = { critical: 'بحرانی', warning: 'هشدار', info: 'اطلاعاتی' };
    var SEVERITY_BADGE_CLASS = { critical: 'badge-critical', warning: 'badge-warning', info: 'badge-info' };

    var state = {
        events: [],
        status: 'open',
        selectedId: null,
        refreshTimer: null
    };

    var el = {
        body: document.getElementById('eventsBody'),
        loading: document.getElementById('loadingState'),
        empty: document.getElementById('emptyState'),
        toasts: document.getElementById('toasts'),
        lastUpdated: document.getElementById('lastUpdated'),
        details: document.getElementById('detailsDialog'),
        detailsContent: document.getElementById('detailsContent'),
        ackFromDetails: document.getElementById('ackFromDetails'),
        stats: {
            health: document.getElementById('statHealth'),
            healthHint: document.getElementById('statHealthHint'),
            critical: document.getElementById('statCritical'),
            warning: document.getElementById('statWarning'),
            info: document.getElementById('statInfo'),
            total: document.getElementById('statTotal')
        }
    };

 /* --- ارتباط با سرور -------------------------------------------------- */
    function request(action, method, params) {
        var query = new URLSearchParams(params || {});
        query.set('action', action);

        var url = 'api.php?' + query.toString();
        var options = { method: method, headers: {}, credentials: 'same-origin' };

        if (method !== 'GET') {
            options.headers['X-CSRF-Token'] = CSRF;
        }

        return fetch(url, options).then(function (response) {
            if (response.status === 401) {
                window.location.href = 'login.php';
                return Promise.reject(new Error('unauthenticated'));
            }
            return response.json().then(function (data) {
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'خطای ناشناخته رخ داد.');
                }
                return data;
            });
        });
    }

 /* --- توست ------------------------------------------------------------ */
    function toast(message, type) {
        var node = document.createElement('div');
        node.className = 'toast toast-' + (type || 'success');
        node.textContent = message;
        el.toasts.appendChild(node);
        setTimeout(function () { node.remove(); }, 3500);
    }

 /* --- قالب‌بندی ------------------------------------------------------- */
    function formatDateTime(value) {
        if (!value) { return '—'; }
        var date = new Date(value.replace(' ', 'T'));
        if (isNaN(date.getTime())) { return value; }
        return date.toLocaleDateString('fa-IR') + ' ' +
            date.toLocaleTimeString('fa-IR', { hour: '2-digit', minute: '2-digit' });
    }

    function formatRelative(value) {
        if (!value) { return '—'; }
        var date = new Date(value.replace(' ', 'T'));
        if (isNaN(date.getTime())) { return value; }

        var minutes = Math.floor((Date.now() - date.getTime()) / 60000);
        if (minutes < 1) { return 'لحظاتی پیش'; }
        if (minutes < 60) { return minutes + ' دقیقه پیش'; }
        if (minutes < 1440) { return Math.floor(minutes / 60) + ' ساعت پیش'; }
        return formatDateTime(value);
    }

 /* --- ساخت جدول ------------------------------------------------------- */
    function cell(text, className) {
        var td = document.createElement('td');
        if (className) { td.className = className; }
        if (text !== undefined) { td.textContent = text; }
        return td;
    }

    function severityBadge(severity) {
        var span = document.createElement('span');
        span.className = 'badge ' + (SEVERITY_BADGE_CLASS[severity] || 'badge-info');
        span.textContent = SEVERITY_LABEL[severity] || severity;
        return span;
    }

    function statusBadge(event) {
        var span = document.createElement('span');
        if (event.acknowledgedAt) {
            span.className = 'badge badge-acknowledged';
            span.textContent = 'تأییدشده';
        } else {
            span.className = 'badge badge-critical';
            span.textContent = 'باز';
        }
        return span;
    }

    function ackButton(event) {
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn-icon';
        button.title = 'تأیید رویداد';
        button.setAttribute('aria-label', 'تأیید رویداد');
        button.dataset.action = 'acknowledge';
        button.dataset.id = event.id;

        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('class', 'icon');
        svg.setAttribute('aria-hidden', 'true');
        var use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
        use.setAttribute('href', '#check');
        svg.appendChild(use);
        button.appendChild(svg);

        return button;
    }

    function buildRow(event) {
        var tr = document.createElement('tr');
        tr.dataset.id = String(event.id);

        var severityCell = document.createElement('td');
        severityCell.appendChild(severityBadge(event.severity));
        tr.appendChild(severityCell);

        tr.appendChild(cell(event.eventType));
        tr.appendChild(cell(event.message, 'cell-message'));
        tr.appendChild(cell(event.source, 'source-tag'));
        tr.appendChild(cell(formatRelative(event.createdAt), 'cell-time'));

        var statusCell = document.createElement('td');
        statusCell.appendChild(statusBadge(event));
        tr.appendChild(statusCell);

        var actions = document.createElement('td');
        actions.className = 'cell-actions';
        if (!event.acknowledgedAt) {
            actions.appendChild(ackButton(event));
        }
        tr.appendChild(actions);

        return tr;
    }

    function render() {
        el.body.replaceChildren();

        if (state.events.length === 0) {
            el.empty.classList.remove('is-hidden');
            return;
        }

        el.empty.classList.add('is-hidden');
        var fragment = document.createDocumentFragment();
        state.events.forEach(function (event) {
            fragment.appendChild(buildRow(event));
        });
        el.body.appendChild(fragment);
    }

    function renderSummary(summary) {
        var counts = summary.openAlerts;
        el.stats.critical.textContent = counts.open_critical;
        el.stats.warning.textContent = counts.open_warning;
        el.stats.info.textContent = counts.open_info;
        el.stats.total.textContent = counts.open_total;

        var health = summary.health;
        if (health.healthy) {
            el.stats.health.textContent = 'سالم';
            el.stats.health.className = 'stat-value';
        } else {
            el.stats.health.textContent = 'ناسالم';
            el.stats.health.className = 'stat-value';
        }
        el.stats.healthHint.textContent = 'بررسی: ' + formatDateTime(health.checkedAt);
    }

 /* --- بارگذاری داده --------------------------------------------------- */
    function findEvent(id) {
        return state.events.filter(function (item) {
            return String(item.id) === String(id);
        })[0];
    }

    function load() {
        el.loading.classList.remove('is-hidden');
        el.empty.classList.add('is-hidden');

        return Promise.all([
            request('summary', 'GET'),
            request('events', 'GET', { status: state.status, limit: 100 })
        ])
            .then(function (results) {
                renderSummary(results[0]);
                state.events = results[1].events;
                render();
                el.lastUpdated.textContent = 'آخرین بروزرسانی: ' + new Date().toLocaleTimeString('fa-IR');
            })
            .catch(function (error) {
                if (error.message !== 'unauthenticated') {
                    toast(error.message, 'error');
                }
            })
            .finally(function () {
                el.loading.classList.add('is-hidden');
            });
    }

    function scheduleRefresh() {
        if (state.refreshTimer) { clearInterval(state.refreshTimer); }
        state.refreshTimer = setInterval(load, REFRESH_INTERVAL_MS);
    }

 /* --- تأیید رویداد ------------------------------------------------------ */
    function acknowledgeEvent(id) {
        return request('acknowledge', 'POST', { id: id })
            .then(function (data) {
                toast(data.message, 'success');
                return load();
            })
            .catch(function (error) {
                if (error.message !== 'unauthenticated') { toast(error.message, 'error'); }
            });
    }

 /* --- جزئیات ---------------------------------------------------------- */
    function showDetails(event) {
        state.selectedId = event.id;

        var rows = [
            ['شناسه', String(event.id)],
            ['شدت', SEVERITY_LABEL[event.severity] || event.severity],
            ['نوع رویداد', event.eventType],
            ['پیام', event.message],
            ['منبع', event.source],
            ['کلید dedupe', event.dedupeKey || '—'],
            ['زمان ثبت', formatDateTime(event.createdAt)],
            ['تأییدشده در', event.acknowledgedAt ? formatDateTime(event.acknowledgedAt) : 'هنوز تأیید نشده'],
            ['تأییدکننده', event.acknowledgedBy || '—']
        ];

        var list = document.createElement('dl');
        list.className = 'detail-list';

        rows.forEach(function (row) {
            var dt = document.createElement('dt');
            dt.textContent = row[0];
            var dd = document.createElement('dd');
            dd.textContent = row[1];
            list.appendChild(dt);
            list.appendChild(dd);
        });

        el.detailsContent.replaceChildren(list);
        el.ackFromDetails.classList.toggle('is-hidden', !!event.acknowledgedAt);
        el.details.showModal();
    }

 /* --- پوسته ----------------------------------------------------------- */
    function currentTheme() {
        var explicit = document.documentElement.dataset.theme;
        if (explicit) { return explicit; }
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function toggleTheme() {
        var next = currentTheme() === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        document.cookie = 'monitoring_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }

 /* --- اتصال رویدادها -------------------------------------------------- */
    document.querySelectorAll('.chip[data-status]').forEach(function (chip) {
        chip.addEventListener('click', function () {
            document.querySelectorAll('.chip[data-status]').forEach(function (other) {
                other.classList.toggle('is-selected', other === chip);
            });
            state.status = chip.dataset.status;
            load();
        });
    });

    document.getElementById('refreshBtn').addEventListener('click', function () { load(); });
    document.getElementById('themeToggle').addEventListener('click', toggleTheme);

    el.ackFromDetails.addEventListener('click', function () {
        if (state.selectedId === null) { return; }
        el.details.close();
        acknowledgeEvent(state.selectedId);
    });

    el.body.addEventListener('click', function (event) {
        var button = event.target.closest('button[data-action="acknowledge"]');
        if (button) {
            acknowledgeEvent(button.dataset.id);
            return;
        }

        var row = event.target.closest('tr[data-id]');
        if (row) {
            var found = findEvent(row.dataset.id);
            if (found) { showDetails(found); }
        }
    });

    document.querySelectorAll('[data-close]').forEach(function (button) {
        button.addEventListener('click', function () {
            button.closest('dialog').close();
        });
    });

    load();
    scheduleRefresh();
})();