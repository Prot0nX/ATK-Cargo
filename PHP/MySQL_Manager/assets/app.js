// پنل مدیریت MySQL — فاز ۱: Sidebar/Navigation + Dashboard + Databases + Tables (Read-only) + Storage/Index Analysis.
// همان اسکلت IIFE و الگوی request() سایر پنل‌های خانواده (Lic/Monitoring)؛ بدون کتابخانه‌ی خارجی، بدون innerHTML.
(function () {
    'use strict';

    var CSRF = document.querySelector('meta[name="csrf-token"]').content;

    var FRAG_LABEL = { low: 'کم', medium: 'متوسط', high: 'زیاد' };

    var state = {
        view: 'dashboard',
        loadedViews: {},
        dbName: '',
        tables: { page: 1, perPage: 20, search: '', sort: 'size', dir: 'desc', total: 0 },
        backupHistory: { page: 1, perPage: 20 },
        cleanup: { tableColumnsCache: {} },
        monitoring: { refreshTimer: null },
        logs: { beforeId: null, entityType: '', hasMore: true },
    };

    var el = {
        sidebar: document.getElementById('sidebar'),
        sidebarCollapse: document.getElementById('sidebarCollapse'),
        drawerToggle: document.getElementById('drawerToggle'),
        drawerBackdrop: document.getElementById('drawerBackdrop'),
        themeToggle: document.getElementById('themeToggle'),
        connectionBadge: document.getElementById('connectionBadge'),
        connectionLabel: document.getElementById('connectionLabel'),
        toasts: document.getElementById('toasts'),

        healthLabel: document.getElementById('healthLabel'),
        healthBarFill: document.getElementById('healthBarFill'),
        healthReasons: document.getElementById('healthReasons'),
        dashboardStats: document.getElementById('dashboardStats'),

        databasesBody: document.getElementById('databasesBody'),
        databasesLoading: document.getElementById('databasesLoading'),

        tablesBody: document.getElementById('tablesBody'),
        tablesLoading: document.getElementById('tablesLoading'),
        tablesEmpty: document.getElementById('tablesEmpty'),
        tablesCount: document.getElementById('tablesCount'),
        tablesSearch: document.getElementById('tablesSearch'),
        tablesPagination: document.getElementById('tablesPagination'),

        storageStats: document.getElementById('storageStats'),
        storageChart: document.getElementById('storageChart'),
        topIndexesBody: document.getElementById('topIndexesBody'),

        indexIssues: document.getElementById('indexIssues'),
        indexesBody: document.getElementById('indexesBody'),
        indexesLoading: document.getElementById('indexesLoading'),

        tableDetailDialog: document.getElementById('tableDetailDialog'),
        tableDetailTitle: document.getElementById('tableDetailTitle'),
        tableDetailMeta: document.getElementById('tableDetailMeta'),
        tableDetailColumns: document.getElementById('tableDetailColumns'),
        tableDetailIndexes: document.getElementById('tableDetailIndexes'),

        optimizationRecommendations: document.getElementById('optimizationRecommendations'),
        optimizationRecommendationsEmpty: document.getElementById('optimizationRecommendationsEmpty'),
        optimizationSearch: document.getElementById('optimizationSearch'),
        optimizationSelectedCount: document.getElementById('optimizationSelectedCount'),
        optimizationSelectAll: document.getElementById('optimizationSelectAll'),
        optimizationBody: document.getElementById('optimizationBody'),
        optimizationLoading: document.getElementById('optimizationLoading'),
        optimizationHistoryBody: document.getElementById('optimizationHistoryBody'),
        optimizationHistoryEmpty: document.getElementById('optimizationHistoryEmpty'),
        bulkOptimizeBtn: document.getElementById('bulkOptimizeBtn'),
        bulkAnalyzeBtn: document.getElementById('bulkAnalyzeBtn'),
        bulkCheckBtn: document.getElementById('bulkCheckBtn'),

        optimizeConfirmDialog: document.getElementById('optimizeConfirmDialog'),
        optimizeConfirmTitle: document.getElementById('optimizeConfirmTitle'),
        optimizeConfirmWarning: document.getElementById('optimizeConfirmWarning'),
        optimizeConfirmMeta: document.getElementById('optimizeConfirmMeta'),
        optimizeConfirmOk: document.getElementById('optimizeConfirmOk'),
        optimizeResultDialog: document.getElementById('optimizeResultDialog'),
        optimizeResultMeta: document.getElementById('optimizeResultMeta'),

        backupTablesPicker: document.getElementById('backupTablesPicker'),
        backupTablesList: document.getElementById('backupTablesList'),
        backupCompress: document.getElementById('backupCompress'),
        startBackupBtn: document.getElementById('startBackupBtn'),
        autoBackupEnabled: document.getElementById('autoBackupEnabled'),
        autoBackupFrequency: document.getElementById('autoBackupFrequency'),
        autoBackupTime: document.getElementById('autoBackupTime'),
        autoBackupRetention: document.getElementById('autoBackupRetention'),
        autoBackupCompression: document.getElementById('autoBackupCompression'),
        autoBackupIncludeStructure: document.getElementById('autoBackupIncludeStructure'),
        autoBackupIncludeData: document.getElementById('autoBackupIncludeData'),
        saveBackupSettingsBtn: document.getElementById('saveBackupSettingsBtn'),
        backupHistoryBody: document.getElementById('backupHistoryBody'),
        backupHistoryLoading: document.getElementById('backupHistoryLoading'),
        backupHistoryEmpty: document.getElementById('backupHistoryEmpty'),
        backupHistoryPagination: document.getElementById('backupHistoryPagination'),
        backupResultDialog: document.getElementById('backupResultDialog'),
        backupResultMeta: document.getElementById('backupResultMeta'),

        restoreListBody: document.getElementById('restoreListBody'),
        restoreListLoading: document.getElementById('restoreListLoading'),
        restoreListEmpty: document.getElementById('restoreListEmpty'),
        restoreConfirmDialog: document.getElementById('restoreConfirmDialog'),
        restoreConfirmMeta: document.getElementById('restoreConfirmMeta'),
        restoreConfirmInput: document.getElementById('restoreConfirmInput'),
        restoreConfirmOk: document.getElementById('restoreConfirmOk'),
        restoreResultDialog: document.getElementById('restoreResultDialog'),
        restoreResultMeta: document.getElementById('restoreResultMeta'),

        cleanupRuleTable: document.getElementById('cleanupRuleTable'),
        cleanupRuleColumn: document.getElementById('cleanupRuleColumn'),
        cleanupRuleRetention: document.getElementById('cleanupRuleRetention'),
        cleanupRuleEnabled: document.getElementById('cleanupRuleEnabled'),
        saveCleanupRuleBtn: document.getElementById('saveCleanupRuleBtn'),
        cleanupRulesBody: document.getElementById('cleanupRulesBody'),
        cleanupRulesEmpty: document.getElementById('cleanupRulesEmpty'),
        cleanupPreviewDialog: document.getElementById('cleanupPreviewDialog'),
        cleanupPreviewMeta: document.getElementById('cleanupPreviewMeta'),
        cleanupPreviewOk: document.getElementById('cleanupPreviewOk'),

        monitoringStats: document.getElementById('monitoringStats'),
        processListCount: document.getElementById('processListCount'),
        processListBody: document.getElementById('processListBody'),
        processListLoading: document.getElementById('processListLoading'),
        processListEmpty: document.getElementById('processListEmpty'),
        processListNoAccess: document.getElementById('processListNoAccess'),
        slowQueriesBody: document.getElementById('slowQueriesBody'),
        slowQueriesNoAccess: document.getElementById('slowQueriesNoAccess'),

        schedulerSettingsMeta: document.getElementById('schedulerSettingsMeta'),
        runMaintenanceNowBtn: document.getElementById('runMaintenanceNowBtn'),
        schedulerLastRunMeta: document.getElementById('schedulerLastRunMeta'),
        schedulerLastRunEmpty: document.getElementById('schedulerLastRunEmpty'),

        logsBody: document.getElementById('logsBody'),
        logsLoading: document.getElementById('logsLoading'),
        logsEmpty: document.getElementById('logsEmpty'),
        logsLoadMoreBtn: document.getElementById('logsLoadMoreBtn'),
    };

    /* ارتباط با سرور */
    function request(action, method, payload, params) {
        var query = new URLSearchParams(params || {});
        query.set('action', action);

        var url = 'api.php?' + query.toString();
        var options = { method: method, headers: {}, credentials: 'same-origin' };

        if (method !== 'GET') {
            options.headers['X-CSRF-Token'] = CSRF;
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(payload || {});
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

    function toast(message, type) {
        var node = document.createElement('div');
        node.className = 'toast toast-' + (type || 'success');
        node.textContent = message;
        el.toasts.appendChild(node);
        setTimeout(function () { node.remove(); }, 3500);
    }

    function reportError(error) {
        if (error.message !== 'unauthenticated') {
            toast(error.message, 'error');
        }
    }

    /* قالب‌بندی */
    function formatBytes(bytes) {
        bytes = Number(bytes) || 0;
        var units = ['بایت', 'کیلوبایت', 'مگابایت', 'گیگابایت', 'ترابایت'];
        var i = 0;
        while (bytes >= 1024 && i < units.length - 1) {
            bytes /= 1024;
            i++;
        }
        return bytes.toLocaleString('fa-IR', { maximumFractionDigits: i === 0 ? 0 : 1 }) + ' ' + units[i];
    }

    function formatNumber(n) {
        return (Number(n) || 0).toLocaleString('fa-IR');
    }

    function formatDate(value) {
        if (!value) { return '—'; }
        var d = new Date(String(value).replace(' ', 'T'));
        if (isNaN(d.getTime())) { return '—'; }
        return d.toLocaleDateString('fa-IR') + ' ' + d.toLocaleTimeString('fa-IR', { hour: '2-digit', minute: '2-digit' });
    }

    function formatUptime(seconds) {
        seconds = Number(seconds) || 0;
        var days = Math.floor(seconds / 86400);
        var hours = Math.floor((seconds % 86400) / 3600);
        var minutes = Math.floor((seconds % 3600) / 60);
        var parts = [];
        if (days > 0) { parts.push(formatNumber(days) + ' روز'); }
        if (hours > 0) { parts.push(formatNumber(hours) + ' ساعت'); }
        if (parts.length === 0) { parts.push(formatNumber(minutes) + ' دقیقه'); }
        return parts.join(' و ');
    }

    function svgEl(tag, attrs) {
        var node = document.createElementNS('http://www.w3.org/2000/svg', tag);
        for (var key in attrs) {
            if (Object.prototype.hasOwnProperty.call(attrs, key)) {
                node.setAttribute(key, attrs[key]);
            }
        }
        return node;
    }

    function iconUse(id) {
        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('class', 'icon');
        svg.setAttribute('aria-hidden', 'true');
        var use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
        use.setAttribute('href', '#' + id);
        svg.appendChild(use);
        return svg;
    }

    /* ---------- Sidebar / Navigation ---------- */
    function switchView(view) {
        // پایش زنده‌ی Processlist فقط وقتی نمای Monitoring باز است لازم است؛ خروج از آن باید Interval را متوقف کند
        if (state.view === 'monitoring' && view !== 'monitoring' && state.monitoring.refreshTimer) {
            clearInterval(state.monitoring.refreshTimer);
            state.monitoring.refreshTimer = null;
        }

        state.view = view;

        document.querySelectorAll('.mys-sidebar-item').forEach(function (btn) {
            btn.classList.toggle('is-active', btn.dataset.view === view);
        });
        document.querySelectorAll('.mys-view').forEach(function (section) {
            section.classList.toggle('is-active', section.dataset.view === view);
        });

        closeDrawer();
        loadViewIfNeeded(view);

        if (view === 'monitoring' && !state.monitoring.refreshTimer) {
            state.monitoring.refreshTimer = setInterval(loadProcessList, 10000);
        }
    }

    function loadViewIfNeeded(view) {
        if (state.loadedViews[view]) { return; }

        var loaders = {
            dashboard: loadDashboard,
            databases: loadDatabases,
            tables: loadTables,
            optimization: loadOptimization,
            storage: loadStorage,
            indexes: loadIndexes,
            backup: loadBackup,
            restore: loadRestore,
            cleanup: loadCleanup,
            monitoring: loadMonitoring,
            scheduler: loadScheduler,
            logs: loadLogs,
        };

        if (loaders[view]) {
            state.loadedViews[view] = true;
            loaders[view]();
        }
    }

    function openDrawer() {
        el.sidebar.classList.add('is-open');
        el.drawerBackdrop.classList.add('is-open');
    }

    function closeDrawer() {
        if (window.innerWidth <= 900) {
            el.sidebar.classList.remove('is-open');
            el.drawerBackdrop.classList.remove('is-open');
        }
    }

    function toggleSidebarCollapse() {
        var collapsed = el.sidebar.classList.toggle('is-collapsed');
        try { localStorage.setItem('mys_sidebar_collapsed', collapsed ? '1' : '0'); } catch (e) { /* بی‌اهمیت */ }
    }

    /* ---------- Theme ---------- */
    function currentTheme() {
        var explicit = document.documentElement.dataset.theme;
        if (explicit) { return explicit; }
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function toggleTheme() {
        var next = currentTheme() === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        document.cookie = 'mysql_manager_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }

    /* ---------- Dashboard ---------- */
    function renderHealth(health) {
        el.healthLabel.textContent = health.label + ' (' + formatNumber(health.score) + '٪)';
        // width با setAttribute (ویژگی محتوایی SVG) تنظیم می‌شود، نه با CSSOM، چون CSP این پنل style-src 'self' بدون unsafe-inline دارد
        el.healthBarFill.setAttribute('width', String(Math.max(0, Math.min(100, health.score))));
        el.healthBarFill.classList.toggle('is-warning', health.score < 90 && health.score >= 50);
        el.healthBarFill.classList.toggle('is-danger', health.score < 50);

        el.healthReasons.replaceChildren();
        (health.reasons || []).forEach(function (reason) {
            var li = document.createElement('li');
            li.textContent = reason;
            el.healthReasons.appendChild(li);
        });
    }

    function statCard(label, value, hint) {
        var article = document.createElement('article');
        article.className = 'stat';

        var pLabel = document.createElement('p');
        pLabel.className = 'stat-label';
        pLabel.textContent = label;

        var pValue = document.createElement('p');
        pValue.className = 'stat-value';
        pValue.textContent = value;

        article.appendChild(pLabel);
        article.appendChild(pValue);

        if (hint) {
            var pHint = document.createElement('p');
            pHint.className = 'stat-hint';
            pHint.textContent = hint;
            article.appendChild(pHint);
        }
        return article;
    }

    function loadDashboard() {
        request('dashboard', 'GET')
            .then(function (data) {
                setConnectionStatus(true);
                var d = data.dashboard;
                state.dbName = d.databaseName;
                renderHealth(d.health);

                var frag = document.createDocumentFragment();
                frag.appendChild(statCard('نسخه MySQL', d.mysqlVersion));
                frag.appendChild(statCard('نسخه PHP', d.phpVersion));
                frag.appendChild(statCard('Uptime سرور', formatUptime(d.uptimeSeconds)));
                frag.appendChild(statCard('تعداد جدول', formatNumber(d.tableCount)));
                frag.appendChild(statCard('حجم کل دیتابیس', formatBytes(d.totalSize)));
                frag.appendChild(statCard('حجم ایندکس‌ها', formatBytes(d.totalIndexSize)));
                frag.appendChild(statCard('اتصالات فعال', formatNumber(d.activeConnections)));
                frag.appendChild(statCard('Queryهای در حال اجرا', formatNumber(d.runningQueries)));
                frag.appendChild(statCard('نیازمند بهینه‌سازی', formatNumber(d.tablesRequiringOptimization)));
                el.dashboardStats.replaceChildren(frag);
            })
            .catch(function (error) {
                setConnectionStatus(false);
                reportError(error);
            });
    }

    function setConnectionStatus(ok) {
        el.connectionBadge.classList.toggle('badge-active', ok);
        el.connectionBadge.classList.toggle('badge-danger', !ok);
        el.connectionLabel.textContent = ok ? 'اتصال دیتابیس برقرار است' : 'خطا در اتصال به دیتابیس';
    }

    /* ---------- Databases ---------- */
    function loadDatabases() {
        el.databasesLoading.classList.remove('is-hidden');
        Promise.all([request('dashboard', 'GET')])
            .then(function (results) {
                var d = results[0].dashboard;
                var tr = document.createElement('tr');

                var tdName = document.createElement('td');
                tdName.textContent = d.databaseName;
                tr.appendChild(tdName);

                var tdCharset = document.createElement('td');
                tdCharset.textContent = 'utf8mb4';
                tr.appendChild(tdCharset);

                var tdCollation = document.createElement('td');
                tdCollation.textContent = 'utf8mb4_unicode_ci';
                tr.appendChild(tdCollation);

                var tdTables = document.createElement('td');
                tdTables.textContent = formatNumber(d.tableCount);
                tr.appendChild(tdTables);

                var tdData = document.createElement('td');
                tdData.textContent = formatBytes(d.totalDataSize);
                tr.appendChild(tdData);

                var tdIndex = document.createElement('td');
                tdIndex.textContent = formatBytes(d.totalIndexSize);
                tr.appendChild(tdIndex);

                var tdTotal = document.createElement('td');
                tdTotal.textContent = formatBytes(d.totalSize);
                tr.appendChild(tdTotal);

                var tdActions = document.createElement('td');
                tdActions.className = 'cell-actions';
                var viewBtn = document.createElement('button');
                viewBtn.type = 'button';
                viewBtn.className = 'btn btn-ghost btn-sm';
                viewBtn.appendChild(iconUse('table'));
                var span = document.createElement('span');
                span.textContent = 'مشاهده جدول‌ها';
                viewBtn.appendChild(span);
                viewBtn.addEventListener('click', function () {
                    document.querySelector('.mys-sidebar-item[data-view="tables"]').click();
                });
                tdActions.appendChild(viewBtn);
                tr.appendChild(tdActions);

                el.databasesBody.replaceChildren(tr);
            })
            .catch(reportError)
            .finally(function () {
                el.databasesLoading.classList.add('is-hidden');
            });
    }

    /* ---------- Tables ---------- */
    function fragmentationCell(percent, level) {
        var wrap = document.createElement('div');
        wrap.className = 'mys-fragmentation-bar';

        // SVG به‌جای div+style width تا با CSP سخت‌گیرانه‌ی این پنل (style-src 'self') سازگار باشد
        var track = svgEl('svg', { class: 'mys-fragmentation-track', viewBox: '0 0 100 6', preserveAspectRatio: 'none', 'aria-hidden': 'true' });
        var fillClass = 'mys-fragmentation-fill' + (level === 'high' ? ' is-high' : level === 'medium' ? ' is-medium' : '');
        var fill = svgEl('rect', { class: fillClass, x: 0, y: 0, width: Math.max(0, Math.min(100, percent)), height: 6 });
        track.appendChild(fill);

        var label = document.createElement('span');
        label.className = 'muted';
        label.textContent = percent.toLocaleString('fa-IR', { maximumFractionDigits: 1 }) + '٪';

        wrap.appendChild(track);
        wrap.appendChild(label);
        return wrap;
    }

    function statusBadge(level) {
        var span = document.createElement('span');
        var map = { low: 'badge-healthy', medium: 'badge-warning', high: 'badge-critical' };
        span.className = 'badge ' + (map[level] || 'badge-info');
        span.textContent = FRAG_LABEL[level] || level;
        return span;
    }

    function buildTableRow(table) {
        var tr = document.createElement('tr');
        tr.dataset.name = table.name;

        var tdName = document.createElement('td');
        var nameBtn = document.createElement('button');
        nameBtn.type = 'button';
        nameBtn.className = 'btn-icon mys-table-name-btn';
        nameBtn.textContent = table.name;
        nameBtn.addEventListener('click', function () { openTableDetail(table.name); });
        tdName.appendChild(nameBtn);
        tr.appendChild(tdName);

        var tdEngine = document.createElement('td');
        tdEngine.textContent = table.engine || '—';
        tr.appendChild(tdEngine);

        var tdRows = document.createElement('td');
        tdRows.textContent = formatNumber(table.rows);
        tr.appendChild(tdRows);

        var tdSize = document.createElement('td');
        tdSize.textContent = formatBytes(table.data_length);
        tr.appendChild(tdSize);

        var tdIndexSize = document.createElement('td');
        tdIndexSize.textContent = formatBytes(table.index_length);
        tr.appendChild(tdIndexSize);

        var tdFrag = document.createElement('td');
        tdFrag.appendChild(fragmentationCell(table.fragmentation_percent, table.fragmentation_level));
        tr.appendChild(tdFrag);

        var tdCollation = document.createElement('td');
        tdCollation.textContent = table.collation || '—';
        tr.appendChild(tdCollation);

        var tdUpdated = document.createElement('td');
        tdUpdated.textContent = formatDate(table.updated_at);
        tr.appendChild(tdUpdated);

        var tdStatus = document.createElement('td');
        tdStatus.appendChild(statusBadge(table.fragmentation_level));
        tr.appendChild(tdStatus);

        return tr;
    }

    function renderPagination(total, page, perPage) {
        el.tablesPagination.replaceChildren();
        var totalPages = Math.max(1, Math.ceil(total / perPage));
        if (totalPages <= 1) { return; }

        var prev = document.createElement('button');
        prev.type = 'button';
        prev.className = 'btn btn-ghost btn-sm';
        prev.textContent = 'قبلی';
        prev.disabled = page <= 1;
        prev.addEventListener('click', function () { state.tables.page = page - 1; loadTables(); });

        var label = document.createElement('span');
        label.className = 'muted';
        label.textContent = 'صفحه ' + formatNumber(page) + ' از ' + formatNumber(totalPages);

        var next = document.createElement('button');
        next.type = 'button';
        next.className = 'btn btn-ghost btn-sm';
        next.textContent = 'بعدی';
        next.disabled = page >= totalPages;
        next.addEventListener('click', function () { state.tables.page = page + 1; loadTables(); });

        el.tablesPagination.appendChild(prev);
        el.tablesPagination.appendChild(label);
        el.tablesPagination.appendChild(next);
    }

    // نسخه‌ی عمومی renderPagination — برای Backup History و هر لیست صفحه‌بندی‌شده‌ی دیگر که به آن نیاز باشد
    function renderSimplePagination(container, total, page, perPage, onPageChange) {
        container.replaceChildren();
        var totalPages = Math.max(1, Math.ceil(total / perPage));
        if (totalPages <= 1) { return; }

        var prev = document.createElement('button');
        prev.type = 'button';
        prev.className = 'btn btn-ghost btn-sm';
        prev.textContent = 'قبلی';
        prev.disabled = page <= 1;
        prev.addEventListener('click', function () { onPageChange(page - 1); });

        var label = document.createElement('span');
        label.className = 'muted';
        label.textContent = 'صفحه ' + formatNumber(page) + ' از ' + formatNumber(totalPages);

        var next = document.createElement('button');
        next.type = 'button';
        next.className = 'btn btn-ghost btn-sm';
        next.textContent = 'بعدی';
        next.disabled = page >= totalPages;
        next.addEventListener('click', function () { onPageChange(page + 1); });

        container.appendChild(prev);
        container.appendChild(label);
        container.appendChild(next);
    }

    function loadTables() {
        el.tablesLoading.classList.remove('is-hidden');
        el.tablesEmpty.classList.add('is-hidden');

        var t = state.tables;
        request('tables', 'GET', null, { page: t.page, perPage: t.perPage, search: t.search, sort: t.sort, dir: t.dir })
            .then(function (data) {
                var result = data.tables;
                t.total = result.total;
                el.tablesCount.textContent = formatNumber(result.total) + ' جدول';

                if (result.rows.length === 0) {
                    el.tablesBody.replaceChildren();
                    el.tablesEmpty.classList.remove('is-hidden');
                } else {
                    var frag = document.createDocumentFragment();
                    result.rows.forEach(function (table) { frag.appendChild(buildTableRow(table)); });
                    el.tablesBody.replaceChildren(frag);
                }
                renderPagination(result.total, result.page, result.perPage);
            })
            .catch(reportError)
            .finally(function () {
                el.tablesLoading.classList.add('is-hidden');
            });
    }

    function openTableDetail(name) {
        el.tableDetailTitle.textContent = 'جزئیات جدول: ' + name;
        el.tableDetailMeta.replaceChildren();
        el.tableDetailColumns.replaceChildren();
        el.tableDetailIndexes.replaceChildren();
        el.tableDetailDialog.showModal();

        request('tableDetail', 'GET', null, { table: name })
            .then(function (data) {
                var table = data.table;
                var metaEntries = [
                    ['موتور', table.engine || '—'],
                    ['تعداد ردیف', formatNumber(table.rows)],
                    ['حجم داده', formatBytes(table.data_length)],
                    ['حجم ایندکس', formatBytes(table.index_length)],
                    ['فضای هدررفته', table.fragmentation_percent.toLocaleString('fa-IR', { maximumFractionDigits: 1 }) + '٪'],
                    ['Collation', table.collation || '—'],
                    ['ایجاد', formatDate(table.created_at)],
                    ['بروزرسانی', formatDate(table.updated_at)],
                ];
                var metaFrag = document.createDocumentFragment();
                metaEntries.forEach(function (entry) {
                    var dt = document.createElement('dt');
                    dt.textContent = entry[0];
                    var dd = document.createElement('dd');
                    dd.textContent = entry[1];
                    metaFrag.appendChild(dt);
                    metaFrag.appendChild(dd);
                });
                el.tableDetailMeta.replaceChildren(metaFrag);

                var colsFrag = document.createDocumentFragment();
                (table.columns || []).forEach(function (col) {
                    var tr = document.createElement('tr');
                    [col.name, col.type, col.is_nullable, col.key || '—', col.default_value === null ? 'NULL' : col.default_value, col.extra || '—'].forEach(function (v) {
                        var td = document.createElement('td');
                        td.textContent = v;
                        tr.appendChild(td);
                    });
                    colsFrag.appendChild(tr);
                });
                el.tableDetailColumns.replaceChildren(colsFrag);

                var idxFrag = document.createDocumentFragment();
                (table.indexes || []).forEach(function (idx) {
                    var tr = document.createElement('tr');
                    [idx.index_name, idx.index_type, idx.column_name, idx.non_unique == 0 ? 'بله' : 'خیر', idx.cardinality === null ? '—' : formatNumber(idx.cardinality)].forEach(function (v) {
                        var td = document.createElement('td');
                        td.textContent = v;
                        tr.appendChild(td);
                    });
                    idxFrag.appendChild(tr);
                });
                el.tableDetailIndexes.replaceChildren(idxFrag);
            })
            .catch(reportError);
    }

    /* ---------- Optimization ---------- */
    var OP_LABEL = { optimize: 'Optimize', analyze: 'Analyze', check: 'Check', repair: 'Repair' };
    var OP_ACTION = { optimize: 'optimizeTable', analyze: 'analyzeTable', check: 'checkTable', repair: 'repairTable' };
    var OP_SUCCESS_MESSAGE = {
        optimize: 'عملیات OPTIMIZE با موفقیت انجام شد.',
        analyze: 'عملیات ANALYZE با موفقیت انجام شد.',
        check: 'عملیات CHECK با موفقیت انجام شد.',
        repair: 'عملیات REPAIR با موفقیت انجام شد.',
    };
    // فقط برای عملیات سنگین/خطرناک (OPTIMIZE می‌تواند InnoDB را کامل rebuild کند، REPAIR جدی‌تر است) تأیید صریح گرفته می‌شود
    var OP_REQUIRES_CONFIRM = { optimize: true, analyze: false, check: false, repair: true };

    var optimizationState = { search: '', selected: {}, rows: [] };

    function severityBadgeClass(severity) {
        return { high: 'badge-critical', medium: 'badge-warning', low: 'badge-info' }[severity] || 'badge-info';
    }

    function renderRecommendations(list) {
        el.optimizationRecommendations.replaceChildren();
        el.optimizationRecommendationsEmpty.classList.toggle('is-hidden', list.length > 0);

        var frag = document.createDocumentFragment();
        list.forEach(function (item) {
            var row = document.createElement('div');
            row.className = 'mys-recommend-item is-' + item.severity;

            var badge = document.createElement('span');
            badge.className = 'badge ' + severityBadgeClass(item.severity);
            badge.textContent = { high: 'زیاد', medium: 'متوسط', low: 'کم' }[item.severity] || item.severity;

            var body = document.createElement('div');
            var name = document.createElement('div');
            name.className = 'mys-recommend-table';
            name.textContent = item.tableName;
            var reason = document.createElement('div');
            reason.className = 'mys-recommend-reason';
            reason.textContent = item.reason;
            body.appendChild(name);
            body.appendChild(reason);

            row.appendChild(badge);
            row.appendChild(body);
            frag.appendChild(row);
        });
        el.optimizationRecommendations.appendChild(frag);
    }

    function updateOptimizationSelectionUI() {
        var count = Object.keys(optimizationState.selected).filter(function (k) { return optimizationState.selected[k]; }).length;
        el.optimizationSelectedCount.textContent = count > 0 ? formatNumber(count) + ' جدول انتخاب‌شده' : '';
        el.bulkOptimizeBtn.disabled = count === 0;
        el.bulkAnalyzeBtn.disabled = count === 0;
        el.bulkCheckBtn.disabled = count === 0;
    }

    function buildOptimizationRow(table) {
        var tr = document.createElement('tr');
        tr.dataset.name = table.name;

        var tdCheck = document.createElement('td');
        var checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.setAttribute('aria-label', 'انتخاب ' + table.name);
        checkbox.checked = !!optimizationState.selected[table.name];
        checkbox.addEventListener('change', function () {
            optimizationState.selected[table.name] = checkbox.checked;
            updateOptimizationSelectionUI();
        });
        tdCheck.appendChild(checkbox);
        tr.appendChild(tdCheck);

        var tdName = document.createElement('td');
        tdName.textContent = table.name;
        tr.appendChild(tdName);

        var tdEngine = document.createElement('td');
        tdEngine.textContent = table.engine || '—';
        tr.appendChild(tdEngine);

        var tdFrag = document.createElement('td');
        tdFrag.appendChild(fragmentationCell(table.fragmentation_percent, table.fragmentation_level));
        tr.appendChild(tdFrag);

        var tdSize = document.createElement('td');
        tdSize.textContent = formatBytes(table.total_size);
        tr.appendChild(tdSize);

        var tdActions = document.createElement('td');
        tdActions.className = 'cell-actions';
        ['optimize', 'analyze', 'check'].concat(table.repair_supported ? ['repair'] : []).forEach(function (op) {
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'btn-icon' + (op === 'repair' ? ' is-danger' : '');
            btn.setAttribute('aria-label', OP_LABEL[op] + ' ' + table.name);
            btn.title = OP_LABEL[op];
            btn.appendChild(iconUse(op === 'optimize' ? 'gauge' : op === 'analyze' ? 'play' : op === 'check' ? 'check' : 'alert-triangle'));
            btn.addEventListener('click', function () { requestOperation(table.name, op); });
            tdActions.appendChild(btn);
        });
        tr.appendChild(tdActions);

        return tr;
    }

    function loadOptimizationTables() {
        el.optimizationLoading.classList.remove('is-hidden');
        return request('tables', 'GET', null, { page: 1, perPage: 100, search: optimizationState.search, sort: 'size', dir: 'desc' })
            .then(function (data) {
                optimizationState.rows = data.tables.rows;
                var frag = document.createDocumentFragment();
                data.tables.rows.forEach(function (table) { frag.appendChild(buildOptimizationRow(table)); });
                el.optimizationBody.replaceChildren(frag);
                updateOptimizationSelectionUI();
            })
            .catch(reportError)
            .finally(function () {
                el.optimizationLoading.classList.add('is-hidden');
            });
    }

    function loadOptimizationHistory() {
        return request('optimizationHistory', 'GET', null, { limit: 30 })
            .then(function (data) {
                var rows = data.history || [];
                el.optimizationHistoryEmpty.classList.toggle('is-hidden', rows.length > 0);
                var frag = document.createDocumentFragment();
                rows.forEach(function (row) {
                    var tr = document.createElement('tr');

                    var tdTable = document.createElement('td');
                    tdTable.textContent = row.table_name;
                    tr.appendChild(tdTable);

                    var tdOp = document.createElement('td');
                    tdOp.textContent = OP_LABEL[row.operation] || row.operation;
                    tr.appendChild(tdOp);

                    var tdStatus = document.createElement('td');
                    var badge = document.createElement('span');
                    var statusMap = { ok: 'badge-healthy', warning: 'badge-warning', error: 'badge-critical' };
                    badge.className = 'badge ' + (statusMap[row.status] || 'badge-info');
                    badge.textContent = { ok: 'موفق', warning: 'هشدار', error: 'خطا' }[row.status] || row.status;
                    tdStatus.appendChild(badge);
                    tr.appendChild(tdStatus);

                    var tdBefore = document.createElement('td');
                    tdBefore.textContent = row.size_before_bytes === null ? '—' : formatBytes(row.size_before_bytes);
                    tr.appendChild(tdBefore);

                    var tdAfter = document.createElement('td');
                    tdAfter.textContent = row.size_after_bytes === null ? '—' : formatBytes(row.size_after_bytes);
                    tr.appendChild(tdAfter);

                    var tdReclaimed = document.createElement('td');
                    var reclaimed = (row.size_before_bytes !== null && row.size_after_bytes !== null)
                        ? Math.max(0, row.size_before_bytes - row.size_after_bytes) : null;
                    tdReclaimed.textContent = reclaimed === null ? '—' : formatBytes(reclaimed);
                    tr.appendChild(tdReclaimed);

                    var tdDuration = document.createElement('td');
                    tdDuration.textContent = row.duration_ms === null ? '—' : formatNumber(row.duration_ms) + ' ms';
                    tr.appendChild(tdDuration);

                    var tdTime = document.createElement('td');
                    tdTime.textContent = formatDate(row.created_at);
                    tr.appendChild(tdTime);

                    frag.appendChild(tr);
                });
                el.optimizationHistoryBody.replaceChildren(frag);
            })
            .catch(reportError);
    }

    function loadOptimization() {
        request('optimizationRecommendations', 'GET')
            .then(function (data) { renderRecommendations(data.recommendations || []); })
            .catch(reportError);
        loadOptimizationTables();
        loadOptimizationHistory();
    }

    /* دیالوگ تأیید عمومی — برای عملیات‌های سنگین/خطرناک (Optimize تکی و گروهی، Repair) پیش‌نمایش نشان می‌دهد */
    function confirmOperation(title, warningText, metaEntries) {
        return new Promise(function (resolve) {
            el.optimizeConfirmTitle.textContent = title;
            el.optimizeConfirmWarning.textContent = warningText;
            el.optimizeConfirmWarning.classList.toggle('is-hidden', warningText === '');

            el.optimizeConfirmMeta.replaceChildren();
            metaEntries.forEach(function (entry) {
                var dt = document.createElement('dt');
                dt.textContent = entry[0];
                var dd = document.createElement('dd');
                dd.textContent = entry[1];
                el.optimizeConfirmMeta.appendChild(dt);
                el.optimizeConfirmMeta.appendChild(dd);
            });

            function cleanup(result) {
                el.optimizeConfirmOk.removeEventListener('click', onOk);
                el.optimizeConfirmDialog.removeEventListener('close', onClose);
                el.optimizeConfirmDialog.close();
                resolve(result);
            }
            function onOk() { cleanup(true); }
            function onClose() { cleanup(false); }

            el.optimizeConfirmOk.addEventListener('click', onOk);
            el.optimizeConfirmDialog.addEventListener('close', onClose);
            el.optimizeConfirmDialog.showModal();
        });
    }

    function showOperationResult(result) {
        var statusLabel = { ok: 'موفق', warning: 'هشدار', error: 'خطا' }[result.status] || result.status;
        var entries = [
            ['جدول', result.tableName],
            ['عملیات', OP_LABEL[result.operation] || result.operation],
            ['وضعیت', statusLabel],
            ['زمان اجرا', formatNumber(result.durationMs) + ' ms'],
            ['حجم قبل', formatBytes(result.sizeBefore)],
            ['حجم بعد', result.sizeAfter === null ? '—' : formatBytes(result.sizeAfter)],
            ['فضای آزادشده', result.reclaimedBytes === null ? '—' : formatBytes(result.reclaimedBytes)],
        ];
        if (result.message) {
            entries.push(['پیام MySQL', result.message]);
        }
        el.optimizeResultMeta.replaceChildren();
        entries.forEach(function (entry) {
            var dt = document.createElement('dt');
            dt.textContent = entry[0];
            var dd = document.createElement('dd');
            dd.textContent = entry[1];
            el.optimizeResultMeta.appendChild(dt);
            el.optimizeResultMeta.appendChild(dd);
        });
        el.optimizeResultDialog.showModal();
    }

    function runOperationOnServer(tableName, op) {
        return request(OP_ACTION[op], 'POST', { table: tableName }).then(function (data) { return data.result; });
    }

    // اجرای عملیات روی یک جدول با تأیید (در صورت نیاز) و پیش‌نمایش — از دکمه‌ی هر ردیف صدا زده می‌شود
    function requestOperation(tableName, op) {
        var proceed;
        if (OP_REQUIRES_CONFIRM[op]) {
            proceed = request('optimizationPreview', 'GET', null, { table: tableName }).then(function (data) {
                var p = data.preview;
                var warning = op === 'repair'
                    ? 'REPAIR TABLE می‌تواند داده‌های آسیب‌دیده را بازنویسی کند و در برخی موارد قابل بازگشت نیست. این عملیات ممکن است جدول را موقتاً قفل کند.'
                    : 'این عملیات ممکن است جدول را موقتاً قفل کند و برای جدول‌های بزرگ زمان‌بر باشد.';
                return confirmOperation(OP_LABEL[op] + ' — ' + tableName, warning, [
                    ['موتور', p.engine],
                    ['تعداد ردیف', formatNumber(p.rows)],
                    ['حجم فعلی', formatBytes(p.totalSize)],
                    ['فضای قابل‌بازیابی (تخمینی)', formatBytes(p.reclaimableSpace)],
                    ['ریسک قفل', { low: 'کم', medium: 'متوسط', high: 'زیاد' }[p.lockRisk] || p.lockRisk],
                ]);
            });
        } else {
            proceed = Promise.resolve(true);
        }

        proceed.then(function (confirmed) {
            if (!confirmed) { return; }
            return runOperationOnServer(tableName, op).then(function (result) {
                toast(OP_SUCCESS_MESSAGE[op], 'success');
                showOperationResult(result);
                loadOptimizationTables();
                loadOptimizationHistory();
                if (state.loadedViews.dashboard) { loadDashboard(); }
            });
        }).catch(reportError);
    }

    // اجرای گروهی روی جدول‌های انتخاب‌شده — به‌ترتیب (نه موازی) تا فشار همزمان روی سرور MySQL وارد نشود
    function runBulkOperation(op) {
        var tableNames = Object.keys(optimizationState.selected).filter(function (k) { return optimizationState.selected[k]; });
        if (tableNames.length === 0) { return; }

        confirmOperation(
            OP_LABEL[op] + ' گروهی',
            'این عملیات روی ' + formatNumber(tableNames.length) + ' جدول انتخاب‌شده به‌ترتیب اجرا می‌شود و ممکن است طول بکشد.',
            [['تعداد جدول', formatNumber(tableNames.length)]]
        ).then(function (confirmed) {
            if (!confirmed) { return; }

            var succeeded = 0;
            var failed = 0;
            var chain = Promise.resolve();
            tableNames.forEach(function (tableName) {
                chain = chain.then(function () {
                    return runOperationOnServer(tableName, op)
                        .then(function () { succeeded++; })
                        .catch(function () { failed++; });
                });
            });

            chain.then(function () {
                toast(
                    formatNumber(succeeded) + ' مورد موفق' + (failed > 0 ? '، ' + formatNumber(failed) + ' مورد ناموفق' : '') + '.',
                    failed > 0 ? 'error' : 'success'
                );
                optimizationState.selected = {};
                updateOptimizationSelectionUI();
                loadOptimizationTables();
                loadOptimizationHistory();
                if (state.loadedViews.dashboard) { loadDashboard(); }
            });
        });
    }

    /* ---------- Backup ---------- */
    var BACKUP_TYPE_LABEL = { full: 'کامل', tables: 'جدول‌های منتخب', structure: 'فقط ساختار', data: 'فقط داده' };
    var BACKUP_STATUS_LABEL = { running: 'در حال اجرا', success: 'موفق', verified: 'تأییدشده', failed: 'ناموفق' };
    var BACKUP_STATUS_BADGE = { running: 'badge-info', success: 'badge-healthy', verified: 'badge-healthy', failed: 'badge-critical' };

    function selectedBackupType() {
        var checked = document.querySelector('input[name="backupType"]:checked');
        return checked ? checked.value : 'full';
    }

    function selectedBackupTables() {
        return Array.from(el.backupTablesList.querySelectorAll('input[type="checkbox"]:checked')).map(function (cb) { return cb.value; });
    }

    function loadBackupTablesPicker() {
        return request('tables', 'GET', null, { page: 1, perPage: 200, sort: 'name', dir: 'asc' }).then(function (data) {
            var frag = document.createDocumentFragment();
            data.tables.rows.forEach(function (table) {
                var label = document.createElement('label');
                label.className = 'field-check';
                var cb = document.createElement('input');
                cb.type = 'checkbox';
                cb.value = table.name;
                label.appendChild(cb);
                label.appendChild(document.createTextNode(table.name));
                frag.appendChild(label);
            });
            el.backupTablesList.replaceChildren(frag);
        }).catch(reportError);
    }

    function loadBackupSettings() {
        return request('backupSettings', 'GET').then(function (data) {
            var s = data.settings;
            el.autoBackupEnabled.checked = !!Number(s.auto_backup_enabled);
            el.autoBackupFrequency.value = s.auto_backup_frequency;
            el.autoBackupTime.value = String(s.auto_backup_time).slice(0, 5);
            el.autoBackupRetention.value = String(s.auto_backup_retention);
            el.autoBackupCompression.checked = !!Number(s.auto_backup_compression);
            el.autoBackupIncludeStructure.checked = !!Number(s.auto_backup_include_structure);
            el.autoBackupIncludeData.checked = !!Number(s.auto_backup_include_data);
        }).catch(reportError);
    }

    function saveBackupSettings() {
        var payload = {
            auto_backup_enabled: el.autoBackupEnabled.checked,
            auto_backup_frequency: el.autoBackupFrequency.value,
            auto_backup_time: el.autoBackupTime.value,
            auto_backup_retention: Number(el.autoBackupRetention.value),
            auto_backup_compression: el.autoBackupCompression.checked,
            auto_backup_include_structure: el.autoBackupIncludeStructure.checked,
            auto_backup_include_data: el.autoBackupIncludeData.checked,
        };
        request('saveBackupSettings', 'POST', payload)
            .then(function () { toast('تنظیمات ذخیره شد.', 'success'); })
            .catch(reportError);
    }

    function buildBackupRow(row, mode) {
        var tr = document.createElement('tr');

        var tdDate = document.createElement('td');
        tdDate.textContent = formatDate(row.created_at);
        tr.appendChild(tdDate);

        var tdType = document.createElement('td');
        tdType.textContent = BACKUP_TYPE_LABEL[row.backup_type] || row.backup_type;
        tr.appendChild(tdType);

        var tdSize = document.createElement('td');
        tdSize.textContent = row.file_size_bytes === null ? '—' : formatBytes(row.file_size_bytes);
        tr.appendChild(tdSize);

        if (mode === 'history') {
            var tdDuration = document.createElement('td');
            tdDuration.textContent = row.duration_ms === null ? '—' : formatNumber(row.duration_ms) + ' ms';
            tr.appendChild(tdDuration);
        }

        var tdStatus = document.createElement('td');
        var badge = document.createElement('span');
        badge.className = 'badge ' + (BACKUP_STATUS_BADGE[row.status] || 'badge-info');
        badge.textContent = BACKUP_STATUS_LABEL[row.status] || row.status;
        tdStatus.appendChild(badge);
        tr.appendChild(tdStatus);

        if (mode === 'history') {
            var tdFile = document.createElement('td');
            tdFile.textContent = row.filename;
            tdFile.className = 'cell-key';
            tr.appendChild(tdFile);
        }

        var tdActions = document.createElement('td');
        tdActions.className = 'cell-actions';

        var canDownload = row.status === 'success' || row.status === 'verified';

        if (mode === 'history') {
            var dlLink = document.createElement('a');
            dlLink.className = 'btn-icon' + (canDownload ? '' : ' is-hidden');
            dlLink.href = 'download.php?id=' + row.id;
            dlLink.title = 'دانلود';
            dlLink.setAttribute('aria-label', 'دانلود ' + row.filename);
            dlLink.appendChild(iconUse('download'));
            tdActions.appendChild(dlLink);

            var verifyBtn = document.createElement('button');
            verifyBtn.type = 'button';
            verifyBtn.className = 'btn-icon';
            verifyBtn.title = 'بررسی صحت';
            verifyBtn.setAttribute('aria-label', 'بررسی صحت ' + row.filename);
            verifyBtn.appendChild(iconUse('shield'));
            verifyBtn.addEventListener('click', function () { runVerifyBackup(row.id); });
            tdActions.appendChild(verifyBtn);

            var deleteBtn = document.createElement('button');
            deleteBtn.type = 'button';
            deleteBtn.className = 'btn-icon is-danger';
            deleteBtn.title = 'حذف';
            deleteBtn.setAttribute('aria-label', 'حذف ' + row.filename);
            deleteBtn.appendChild(iconUse('trash'));
            deleteBtn.addEventListener('click', function () { runDeleteBackup(row.id, row.filename); });
            tdActions.appendChild(deleteBtn);
        } else if (mode === 'restore' && canDownload) {
            var restoreBtn = document.createElement('button');
            restoreBtn.type = 'button';
            restoreBtn.className = 'btn btn-danger btn-sm';
            restoreBtn.appendChild(iconUse('upload'));
            var span = document.createElement('span');
            span.textContent = 'بازیابی';
            restoreBtn.appendChild(span);
            restoreBtn.addEventListener('click', function () { openRestoreConfirm(row); });
            tdActions.appendChild(restoreBtn);
        }

        tr.appendChild(tdActions);
        return tr;
    }

    function loadBackupHistory() {
        el.backupHistoryLoading.classList.remove('is-hidden');
        return request('backups', 'GET', null, { page: state.backupHistory.page, perPage: state.backupHistory.perPage })
            .then(function (data) {
                var result = data.backups;
                el.backupHistoryEmpty.classList.toggle('is-hidden', result.rows.length > 0);
                var frag = document.createDocumentFragment();
                result.rows.forEach(function (row) { frag.appendChild(buildBackupRow(row, 'history')); });
                el.backupHistoryBody.replaceChildren(frag);
                renderSimplePagination(el.backupHistoryPagination, result.total, result.page, result.perPage, function (page) {
                    state.backupHistory.page = page;
                    loadBackupHistory();
                });
            })
            .catch(reportError)
            .finally(function () { el.backupHistoryLoading.classList.add('is-hidden'); });
    }

    function runVerifyBackup(id) {
        request('verifyBackup', 'POST', { id: id })
            .then(function (data) {
                toast(data.result.status === 'verified' ? 'پشتیبان معتبر است.' : 'بررسی صحت ناموفق بود.', data.result.status === 'verified' ? 'success' : 'error');
                loadBackupHistory();
            })
            .catch(reportError);
    }

    function runDeleteBackup(id, filename) {
        confirmOperation('حذف پشتیبان', 'این فایل پشتیبان برای همیشه حذف می‌شود و غیرقابل بازگشت است.', [['فایل', filename]])
            .then(function (confirmed) {
                if (!confirmed) { return; }
                return request('deleteBackup', 'POST', { id: id }).then(function () {
                    toast('پشتیبان حذف شد.', 'success');
                    loadBackupHistory();
                });
            })
            .catch(reportError);
    }

    function showBackupResult(backup) {
        var entries = [
            ['فایل', backup.filename],
            ['وضعیت', BACKUP_STATUS_LABEL[backup.status] || backup.status],
            ['حجم', formatBytes(backup.sizeBytes)],
            ['مدت اجرا', formatNumber(backup.durationMs) + ' ms'],
        ];
        if (backup.verifyMessage) {
            entries.push(['بررسی صحت', backup.verifyMessage]);
        }
        el.backupResultMeta.replaceChildren();
        entries.forEach(function (entry) {
            var dt = document.createElement('dt');
            dt.textContent = entry[0];
            var dd = document.createElement('dd');
            dd.textContent = entry[1];
            el.backupResultMeta.appendChild(dt);
            el.backupResultMeta.appendChild(dd);
        });
        el.backupResultDialog.showModal();
    }

    function startBackup() {
        var type = selectedBackupType();
        var tables = type === 'tables' ? selectedBackupTables() : null;
        if (type === 'tables' && (!tables || tables.length === 0)) {
            toast('حداقل یک جدول را برای پشتیبان‌گیری انتخاب کنید.', 'error');
            return;
        }

        el.startBackupBtn.disabled = true;
        request('createBackup', 'POST', { type: type, tables: tables, compress: el.backupCompress.checked })
            .then(function (data) {
                toast('پشتیبان‌گیری با موفقیت انجام شد.', 'success');
                showBackupResult(data.backup);
                loadBackupHistory();
                if (state.loadedViews.dashboard) { loadDashboard(); }
            })
            .catch(reportError)
            .finally(function () { el.startBackupBtn.disabled = false; });
    }

    function loadBackup() {
        loadBackupTablesPicker();
        loadBackupSettings();
        loadBackupHistory();
    }

    /* ---------- Restore ---------- */
    function loadRestore() {
        el.restoreListLoading.classList.remove('is-hidden');
        return request('backups', 'GET', null, { page: 1, perPage: 100 })
            .then(function (data) {
                var rows = data.backups.rows.filter(function (r) { return r.status === 'success' || r.status === 'verified'; });
                el.restoreListEmpty.classList.toggle('is-hidden', rows.length > 0);
                var frag = document.createDocumentFragment();
                rows.forEach(function (row) { frag.appendChild(buildBackupRow(row, 'restore')); });
                el.restoreListBody.replaceChildren(frag);
            })
            .catch(reportError)
            .finally(function () { el.restoreListLoading.classList.add('is-hidden'); });
    }

    function openRestoreConfirm(row) {
        el.restoreConfirmMeta.replaceChildren();
        [
            ['دیتابیس', state.dbName],
            ['فایل پشتیبان', row.filename],
            ['زمان ایجاد', formatDate(row.created_at)],
            ['حجم', row.file_size_bytes === null ? '—' : formatBytes(row.file_size_bytes)],
        ].forEach(function (entry) {
            var dt = document.createElement('dt');
            dt.textContent = entry[0];
            var dd = document.createElement('dd');
            dd.textContent = entry[1];
            el.restoreConfirmMeta.appendChild(dt);
            el.restoreConfirmMeta.appendChild(dd);
        });

        el.restoreConfirmInput.value = '';
        el.restoreConfirmOk.disabled = true;
        el.restoreConfirmDialog.showModal();
        el.restoreConfirmInput.focus();

        function onInput() {
            el.restoreConfirmOk.disabled = el.restoreConfirmInput.value !== state.dbName;
        }
        function onOk() {
            cleanup();
            el.restoreConfirmDialog.close();
            executeRestore(row.id, el.restoreConfirmInput.value);
        }
        function cleanup() {
            el.restoreConfirmInput.removeEventListener('input', onInput);
            el.restoreConfirmOk.removeEventListener('click', onOk);
        }

        el.restoreConfirmInput.addEventListener('input', onInput);
        el.restoreConfirmOk.addEventListener('click', onOk);
    }

    function executeRestore(id, confirmDbName) {
        request('restoreBackup', 'POST', { id: id, confirmDbName: confirmDbName })
            .then(function (data) {
                var r = data.result;
                toast(r.success ? 'بازیابی با موفقیت انجام شد.' : 'بازیابی با خطا متوقف شد.', r.success ? 'success' : 'error');

                var entries = [
                    ['نتیجه', r.success ? 'موفق' : 'ناموفق'],
                    ['کل دستورات', formatNumber(r.totalStatements)],
                    ['دستورات اجراشده', formatNumber(r.executedStatements)],
                    ['مدت اجرا', formatNumber(r.durationMs) + ' ms'],
                ];
                if (!r.success) {
                    entries.push(['متوقف در دستور شماره', formatNumber(r.failedAtStatement)]);
                    entries.push(['پیام', r.errorMessage]);
                }
                el.restoreResultMeta.replaceChildren();
                entries.forEach(function (entry) {
                    var dt = document.createElement('dt');
                    dt.textContent = entry[0];
                    var dd = document.createElement('dd');
                    dd.textContent = entry[1];
                    el.restoreResultMeta.appendChild(dt);
                    el.restoreResultMeta.appendChild(dd);
                });
                el.restoreResultDialog.showModal();

                if (state.loadedViews.dashboard) { loadDashboard(); }
                if (state.loadedViews.tables) { loadTables(); }
            })
            .catch(reportError);
    }

    /* ---------- Cleanup ---------- */
    function loadCleanupRuleTableOptions() {
        return request('tables', 'GET', null, { page: 1, perPage: 200, sort: 'name', dir: 'asc' }).then(function (data) {
            var frag = document.createDocumentFragment();
            data.tables.rows.forEach(function (table) {
                var opt = document.createElement('option');
                opt.value = table.name;
                opt.textContent = table.name;
                frag.appendChild(opt);
            });
            el.cleanupRuleTable.replaceChildren(frag);
            return loadCleanupRuleColumns(el.cleanupRuleTable.value);
        }).catch(reportError);
    }

    function loadCleanupRuleColumns(tableName) {
        if (!tableName) { return Promise.resolve(); }
        if (state.cleanup.tableColumnsCache[tableName]) {
            populateCleanupColumnSelect(state.cleanup.tableColumnsCache[tableName]);
            return Promise.resolve();
        }
        return request('tableDetail', 'GET', null, { table: tableName }).then(function (data) {
            var columns = (data.table.columns || []).map(function (c) { return c.name; });
            state.cleanup.tableColumnsCache[tableName] = columns;
            populateCleanupColumnSelect(columns);
        }).catch(reportError);
    }

    function populateCleanupColumnSelect(columns) {
        var frag = document.createDocumentFragment();
        columns.forEach(function (name) {
            var opt = document.createElement('option');
            opt.value = name;
            opt.textContent = name;
            frag.appendChild(opt);
        });
        el.cleanupRuleColumn.replaceChildren(frag);
    }

    function saveCleanupRule() {
        var tableName = el.cleanupRuleTable.value;
        var dateColumn = el.cleanupRuleColumn.value;
        var retentionDays = Number(el.cleanupRuleRetention.value);
        if (!tableName || !dateColumn || !retentionDays || retentionDays < 1) {
            toast('لطفاً همه‌ی فیلدهای Rule را به‌درستی پر کنید.', 'error');
            return;
        }
        request('saveCleanupRule', 'POST', {
            table: tableName,
            dateColumn: dateColumn,
            retentionDays: retentionDays,
            enabled: el.cleanupRuleEnabled.checked,
        }).then(function () {
            toast('Rule ذخیره شد.', 'success');
            loadCleanupRules();
        }).catch(reportError);
    }

    function buildCleanupRuleRow(rule) {
        var tr = document.createElement('tr');

        [rule.table_name, rule.date_column, formatNumber(rule.retention_days)].forEach(function (v) {
            var td = document.createElement('td');
            td.textContent = v;
            tr.appendChild(td);
        });

        var tdEnabled = document.createElement('td');
        var toggle = document.createElement('input');
        toggle.type = 'checkbox';
        toggle.checked = !!Number(rule.is_enabled);
        toggle.setAttribute('aria-label', 'فعال/غیرفعال ' + rule.table_name);
        toggle.addEventListener('change', function () {
            request('toggleCleanupRule', 'POST', { id: rule.id, enabled: toggle.checked })
                .then(function () { toast('وضعیت Rule بروزرسانی شد.', 'success'); loadCleanupRules(); })
                .catch(reportError);
        });
        tdEnabled.appendChild(toggle);
        tr.appendChild(tdEnabled);

        var tdLastRun = document.createElement('td');
        tdLastRun.textContent = rule.last_run_at ? formatDate(rule.last_run_at) : '—';
        tr.appendChild(tdLastRun);

        var tdLastCount = document.createElement('td');
        tdLastCount.textContent = rule.last_run_deleted_count === null ? '—' : formatNumber(rule.last_run_deleted_count);
        tr.appendChild(tdLastCount);

        var tdActions = document.createElement('td');
        tdActions.className = 'cell-actions';

        var previewBtn = document.createElement('button');
        previewBtn.type = 'button';
        previewBtn.className = 'btn btn-ghost btn-sm';
        previewBtn.appendChild(iconUse('search'));
        var previewSpan = document.createElement('span');
        previewSpan.textContent = 'پیش‌نمایش و اجرا';
        previewBtn.appendChild(previewSpan);
        previewBtn.addEventListener('click', function () { openCleanupPreview(rule); });
        tdActions.appendChild(previewBtn);

        var deleteBtn = document.createElement('button');
        deleteBtn.type = 'button';
        deleteBtn.className = 'btn-icon is-danger';
        deleteBtn.title = 'حذف Rule';
        deleteBtn.setAttribute('aria-label', 'حذف Rule ' + rule.table_name);
        deleteBtn.appendChild(iconUse('trash'));
        deleteBtn.addEventListener('click', function () {
            confirmOperation('حذف Rule', 'این Rule پاکسازی حذف می‌شود (داده‌ای پاک نمی‌شود).', [['جدول', rule.table_name]])
                .then(function (confirmed) {
                    if (!confirmed) { return; }
                    return request('deleteCleanupRule', 'POST', { id: rule.id }).then(function () {
                        toast('Rule حذف شد.', 'success');
                        loadCleanupRules();
                    });
                })
                .catch(reportError);
        });
        tdActions.appendChild(deleteBtn);

        tr.appendChild(tdActions);
        return tr;
    }

    function loadCleanupRules() {
        return request('cleanupRules', 'GET').then(function (data) {
            var rules = data.rules || [];
            el.cleanupRulesEmpty.classList.toggle('is-hidden', rules.length > 0);
            var frag = document.createDocumentFragment();
            rules.forEach(function (rule) { frag.appendChild(buildCleanupRuleRow(rule)); });
            el.cleanupRulesBody.replaceChildren(frag);
        }).catch(reportError);
    }

    function openCleanupPreview(rule) {
        request('cleanupPreview', 'GET', null, { ruleId: rule.id }).then(function (data) {
            var p = data.preview;
            el.cleanupPreviewMeta.replaceChildren();
            [
                ['جدول', p.tableName],
                ['رکوردهای مطابق', formatNumber(p.matchingRecords)],
                ['برآورد حجم', formatBytes(p.estimatedSizeBytes)],
            ].forEach(function (entry) {
                var dt = document.createElement('dt');
                dt.textContent = entry[0];
                var dd = document.createElement('dd');
                dd.textContent = entry[1];
                el.cleanupPreviewMeta.appendChild(dt);
                el.cleanupPreviewMeta.appendChild(dd);
            });

            el.cleanupPreviewOk.disabled = p.matchingRecords === 0;
            el.cleanupPreviewDialog.showModal();

            function onOk() {
                cleanup();
                el.cleanupPreviewDialog.close();
                request('runCleanup', 'POST', { ruleId: rule.id, confirm: true })
                    .then(function (data2) {
                        var r = data2.result;
                        toast(formatNumber(r.deletedCount) + ' رکورد حذف شد.', 'success');
                        loadCleanupRules();
                    })
                    .catch(reportError);
            }
            function cleanup() {
                el.cleanupPreviewOk.removeEventListener('click', onOk);
            }
            el.cleanupPreviewOk.addEventListener('click', onOk);
        }).catch(reportError);
    }

    function loadCleanup() {
        loadCleanupRuleTableOptions();
        loadCleanupRules();
    }

    /* ---------- Monitoring ---------- */
    function loadMonitoringStats() {
        return request('monitoringMetrics', 'GET').then(function (data) {
            var m = data.metrics;
            var frag = document.createDocumentFragment();
            frag.appendChild(statCard('اتصالات فعال', formatNumber(m.threadsConnected) + ' / ' + formatNumber(m.maxConnections)));
            frag.appendChild(statCard('Threadهای در حال اجرا', formatNumber(m.threadsRunning)));
            frag.appendChild(statCard('کل اتصالات از ابتدا', formatNumber(m.totalConnections)));
            frag.appendChild(statCard('کل Queryها', formatNumber(m.questions)));
            frag.appendChild(statCard('Slow Queries', formatNumber(m.slowQueries)));
            frag.appendChild(statCard('Uptime', formatUptime(m.uptimeSeconds)));
            frag.appendChild(statCard('نرخ Cache Hit اینودی‌بی', m.innodbCacheHitRatioPercent === null ? '—' : m.innodbCacheHitRatioPercent.toLocaleString('fa-IR') + '٪'));
            frag.appendChild(statCard('اتصالات ناموفق', formatNumber(m.abortedConnects)));
            el.monitoringStats.replaceChildren(frag);
        }).catch(reportError);
    }

    function buildProcessRow(proc) {
        var tr = document.createElement('tr');

        [proc.id, proc.user, proc.host, proc.db || '—', proc.command, formatNumber(proc.time), proc.state || '—'].forEach(function (v) {
            var td = document.createElement('td');
            td.textContent = v;
            tr.appendChild(td);
        });

        var tdInfo = document.createElement('td');
        var info = proc.info || '';
        tdInfo.textContent = info.length > 60 ? info.slice(0, 60) + '…' : (info || '—');
        if (info) { tdInfo.title = info; }
        tr.appendChild(tdInfo);

        var tdActions = document.createElement('td');
        tdActions.className = 'cell-actions';
        // Kill فقط برای Threadهای واقعیِ کاربر نمایش داده می‌شود؛ Threadهای سیستمی/داخلی MySQL (system user،
        // Daemon مثل InnoDB purge worker) هرگز نباید هدف Kill از این پنل باشند — توقف آن‌ها می‌تواند سرور را بی‌ثبات کند
        var isSystemThread = proc.user === 'system user' || proc.command === 'Daemon';
        if (!isSystemThread && proc.command !== 'Sleep' && proc.id > 0) {
            var killBtn = document.createElement('button');
            killBtn.type = 'button';
            killBtn.className = 'btn-icon is-danger';
            killBtn.title = 'متوقف کردن Query';
            killBtn.setAttribute('aria-label', 'متوقف کردن Query ' + proc.id);
            killBtn.appendChild(iconUse('close'));
            killBtn.addEventListener('click', function () { runKillQuery(proc); });
            tdActions.appendChild(killBtn);
        }
        tr.appendChild(tdActions);

        return tr;
    }

    function runKillQuery(proc) {
        confirmOperation(
            'متوقف کردن Query',
            'این Query بلافاصله و بدون قابلیت بازگشت متوقف می‌شود.',
            [['PID', proc.id], ['کاربر', proc.user], ['Query', proc.info ? proc.info.slice(0, 120) : '—']]
        ).then(function (confirmed) {
            if (!confirmed) { return; }
            return request('killQuery', 'POST', { processId: proc.id }).then(function () {
                toast('Query متوقف شد.', 'success');
                loadProcessList();
            });
        }).catch(reportError);
    }

    function loadProcessList() {
        return request('processList', 'GET').then(function (data) {
            var rows = data.processes || [];
            el.processListLoading.classList.add('is-hidden');
            el.processListEmpty.classList.toggle('is-hidden', rows.length > 0);
            el.processListCount.textContent = formatNumber(rows.length) + ' Query/اتصال فعال';

            var frag = document.createDocumentFragment();
            rows.forEach(function (proc) { frag.appendChild(buildProcessRow(proc)); });
            el.processListBody.replaceChildren(frag);
        }).catch(reportError);
    }

    function loadSlowQueries() {
        return request('slowQueries', 'GET').then(function (data) {
            var sq = data.slowQueries;
            el.slowQueriesNoAccess.classList.toggle('is-hidden', sq.available);
            if (!sq.available) {
                el.slowQueriesBody.replaceChildren();
                return;
            }
            var frag = document.createDocumentFragment();
            sq.rows.forEach(function (row) {
                var tr = document.createElement('tr');
                var tdQuery = document.createElement('td');
                var text = row.query_text || '—';
                tdQuery.textContent = text.length > 80 ? text.slice(0, 80) + '…' : text;
                if (row.query_text) { tdQuery.title = row.query_text; }
                tr.appendChild(tdQuery);
                [row.exec_count, row.avg_ms, row.max_ms, row.rows_examined, row.rows_sent].forEach(function (v) {
                    var td = document.createElement('td');
                    td.textContent = formatNumber(v);
                    tr.appendChild(td);
                });
                frag.appendChild(tr);
            });
            el.slowQueriesBody.replaceChildren(frag);
        }).catch(reportError);
    }

    function loadMonitoring() {
        el.processListLoading.classList.remove('is-hidden');
        loadMonitoringStats();
        loadProcessList();
        loadSlowQueries();
    }

    /* ---------- Scheduler ---------- */
    var FREQUENCY_LABEL = { daily: 'روزانه', weekly: 'هفتگی', monthly: 'ماهانه' };
    var YES_NO = { true: 'بله', false: 'خیر' };

    function loadSchedulerSettings() {
        return request('maintenanceSettings', 'GET').then(function (data) {
            var s = data.settings;
            el.schedulerSettingsMeta.replaceChildren();
            [
                ['پشتیبان‌گیری خودکار', YES_NO[!!Number(s.auto_backup_enabled)]],
                ['دوره‌ی تکرار', FREQUENCY_LABEL[s.auto_backup_frequency] || s.auto_backup_frequency],
                ['ساعت اجرا', String(s.auto_backup_time).slice(0, 5)],
                ['Retention', formatNumber(s.auto_backup_retention) + ' پشتیبان'],
                ['ANALYZE هفتگی همه‌ی جدول‌ها', YES_NO[!!Number(s.auto_analyze_weekly)]],
                ['OPTIMIZE هفتگی همه‌ی جدول‌ها', YES_NO[!!Number(s.auto_optimize_weekly)]],
                ['اجرای خودکار Ruleهای پاکسازی فعال', YES_NO[!!Number(s.auto_cleanup_sessions)]],
            ].forEach(function (entry) {
                var dt = document.createElement('dt');
                dt.textContent = entry[0];
                var dd = document.createElement('dd');
                dd.textContent = entry[1];
                el.schedulerSettingsMeta.appendChild(dt);
                el.schedulerSettingsMeta.appendChild(dd);
            });
        }).catch(reportError);
    }

    function renderSchedulerResult(result) {
        el.schedulerLastRunEmpty.classList.add('is-hidden');
        el.schedulerLastRunMeta.replaceChildren();

        var entries = [['بررسی‌شده در', formatDate(result.checkedAt)]];
        entries.push(['پشتیبان‌گیری', result.backup ? (result.backup.status === 'ok' ? 'اجرا شد' : 'خطا') : 'نوبت نبود / غیرفعال']);
        entries.push(['ANALYZE هفتگی', result.analyze ? (formatNumber(result.analyze.succeeded) + ' از ' + formatNumber(result.analyze.tableCount) + ' جدول موفق') : 'نوبت نبود / غیرفعال']);
        entries.push(['OPTIMIZE هفتگی', result.optimize ? (formatNumber(result.optimize.succeeded) + ' از ' + formatNumber(result.optimize.tableCount) + ' جدول موفق') : 'نوبت نبود / غیرفعال']);
        entries.push(['پاکسازی', result.cleanup ? (formatNumber(result.cleanup.totalDeleted) + ' رکورد از ' + formatNumber(result.cleanup.ruleCount) + ' Rule') : 'غیرفعال']);

        entries.forEach(function (entry) {
            var dt = document.createElement('dt');
            dt.textContent = entry[0];
            var dd = document.createElement('dd');
            dd.textContent = entry[1];
            el.schedulerLastRunMeta.appendChild(dt);
            el.schedulerLastRunMeta.appendChild(dd);
        });
    }

    function runMaintenanceNow() {
        confirmOperation(
            'اجرای نگهداری خودکار',
            'وظایف فعال (پشتیبان‌گیری/ANALYZE/OPTIMIZE/پاکسازی) در صورت رسیدن نوبت، همین الان اجرا می‌شوند.',
            []
        ).then(function (confirmed) {
            if (!confirmed) { return; }
            el.runMaintenanceNowBtn.disabled = true;
            return request('runMaintenanceNow', 'POST', {})
                .then(function (data) {
                    toast('اجرای نگهداری خودکار به پایان رسید.', 'success');
                    renderSchedulerResult(data.result);
                    loadSchedulerSettings();
                    if (state.loadedViews.dashboard) { loadDashboard(); }
                })
                .finally(function () { el.runMaintenanceNowBtn.disabled = false; });
        }).catch(reportError);
    }

    function loadScheduler() {
        loadSchedulerSettings();
    }

    /* ---------- Logs ---------- */
    var AUDIT_ACTION_LABEL = {
        'mysql.optimize.table': 'Optimize جدول', 'mysql.analyze.table': 'Analyze جدول',
        'mysql.check.table': 'Check جدول', 'mysql.repair.table': 'Repair جدول',
        'mysql.backup.create': 'ایجاد پشتیبان', 'mysql.backup.verify': 'بررسی صحت پشتیبان',
        'mysql.backup.delete': 'حذف پشتیبان', 'mysql.backup.download': 'دانلود پشتیبان',
        'mysql.restore.execute': 'بازیابی', 'mysql.settings.update': 'بروزرسانی تنظیمات',
        'mysql.cleanup.rule_save': 'ذخیره‌ی Rule پاکسازی', 'mysql.cleanup.rule_toggle': 'تغییر وضعیت Rule',
        'mysql.cleanup.rule_delete': 'حذف Rule پاکسازی', 'mysql.cleanup.run': 'اجرای پاکسازی',
        'mysql.monitoring.kill_query': 'توقف Query', 'mysql.scheduler.run_now': 'اجرای دستی نگهداری',
    };

    function buildLogRow(row) {
        var tr = document.createElement('tr');

        var tdTime = document.createElement('td');
        tdTime.textContent = formatDate(row.created_at);
        tr.appendChild(tdTime);

        var tdUser = document.createElement('td');
        tdUser.textContent = row.username;
        tr.appendChild(tdUser);

        var tdAction = document.createElement('td');
        tdAction.textContent = AUDIT_ACTION_LABEL[row.action] || row.action;
        tr.appendChild(tdAction);

        var tdType = document.createElement('td');
        tdType.textContent = row.entity_type;
        tr.appendChild(tdType);

        var tdId = document.createElement('td');
        tdId.textContent = row.entity_id;
        tdId.className = 'cell-key';
        tr.appendChild(tdId);

        var tdDetails = document.createElement('td');
        var detailsText = row.details ? (typeof row.details === 'string' ? row.details : JSON.stringify(row.details)) : '—';
        tdDetails.textContent = detailsText.length > 80 ? detailsText.slice(0, 80) + '…' : detailsText;
        if (detailsText !== '—') { tdDetails.title = detailsText; }
        tr.appendChild(tdDetails);

        return tr;
    }

    function loadLogs() {
        state.logs.beforeId = null;
        el.logsLoading.classList.remove('is-hidden');
        return request('auditLogs', 'GET', null, { limit: 50, entityType: state.logs.entityType })
            .then(function (data) {
                var rows = data.logs || [];
                el.logsEmpty.classList.toggle('is-hidden', rows.length > 0);
                var frag = document.createDocumentFragment();
                rows.forEach(function (row) { frag.appendChild(buildLogRow(row)); });
                el.logsBody.replaceChildren(frag);

                state.logs.hasMore = rows.length === 50;
                state.logs.beforeId = rows.length > 0 ? rows[rows.length - 1].id : null;
                el.logsLoadMoreBtn.classList.toggle('is-hidden', !state.logs.hasMore);
            })
            .catch(reportError)
            .finally(function () { el.logsLoading.classList.add('is-hidden'); });
    }

    function loadMoreLogs() {
        if (!state.logs.hasMore) { return; }
        request('auditLogs', 'GET', null, { limit: 50, entityType: state.logs.entityType, beforeId: state.logs.beforeId })
            .then(function (data) {
                var rows = data.logs || [];
                var frag = document.createDocumentFragment();
                rows.forEach(function (row) { frag.appendChild(buildLogRow(row)); });
                el.logsBody.appendChild(frag);

                state.logs.hasMore = rows.length === 50;
                state.logs.beforeId = rows.length > 0 ? rows[rows.length - 1].id : state.logs.beforeId;
                el.logsLoadMoreBtn.classList.toggle('is-hidden', !state.logs.hasMore);
            })
            .catch(reportError);
    }

    /* ---------- Storage ---------- */
    function renderStorageChart(topTables) {
        el.storageChart.replaceChildren();
        if (topTables.length === 0) { return; }

        var width = Math.max(el.storageChart.clientWidth || 600, topTables.length * 64);
        var height = 220;
        var paddingBottom = 40;
        var paddingTop = 16;
        var maxSize = Math.max.apply(null, topTables.map(function (t) { return t.total_size; })) || 1;
        var barWidth = (width / topTables.length) * 0.6;

        var svg = svgEl('svg', { viewBox: '0 0 ' + width + ' ' + height, width: width, height: height, class: 'mys-chart-bar' });

        topTables.forEach(function (table, i) {
            var slot = width / topTables.length;
            var x = i * slot + (slot - barWidth) / 2;
            var barHeight = ((height - paddingTop - paddingBottom) * table.total_size) / maxSize;
            var y = height - paddingBottom - barHeight;

            var rect = svgEl('rect', { x: x, y: y, width: barWidth, height: Math.max(1, barHeight), rx: 2 });
            var title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
            title.textContent = table.name + ' — ' + formatBytes(table.total_size);
            rect.appendChild(title);
            svg.appendChild(rect);

            var label = svgEl('text', { x: x + barWidth / 2, y: height - paddingBottom + 16, 'text-anchor': 'middle' });
            label.textContent = table.name.length > 8 ? table.name.slice(0, 7) + '…' : table.name;
            svg.appendChild(label);
        });

        el.storageChart.appendChild(svg);
    }

    function loadStorage() {
        request('storage', 'GET')
            .then(function (data) {
                var s = data.storage;
                var frag = document.createDocumentFragment();
                frag.appendChild(statCard('حجم داده', formatBytes(s.totalDataSize)));
                frag.appendChild(statCard('حجم ایندکس', formatBytes(s.totalIndexSize)));
                frag.appendChild(statCard('فضای آزاد قابل‌بازیابی', formatBytes(s.totalFreeSpace)));
                el.storageStats.replaceChildren(frag);

                renderStorageChart(s.topTables);

                var idxFrag = document.createDocumentFragment();
                s.topIndexes.forEach(function (row) {
                    var tr = document.createElement('tr');
                    var tdName = document.createElement('td');
                    tdName.textContent = row.tableName;
                    var tdSize = document.createElement('td');
                    tdSize.textContent = formatBytes(row.indexSize);
                    tr.appendChild(tdName);
                    tr.appendChild(tdSize);
                    idxFrag.appendChild(tr);
                });
                el.topIndexesBody.replaceChildren(idxFrag);
            })
            .catch(reportError);
    }

    /* ---------- Index Analysis ---------- */
    function loadIndexes() {
        el.indexesLoading.classList.remove('is-hidden');
        request('indexes', 'GET')
            .then(function (data) {
                el.indexIssues.replaceChildren();
                if ((data.issues || []).length > 0) {
                    var box = document.createElement('div');
                    box.className = 'alert alert-error';
                    var title = document.createElement('strong');
                    title.textContent = 'موارد مشکوک: ';
                    box.appendChild(title);
                    var list = document.createElement('ul');
                    list.className = 'mys-issue-list';
                    data.issues.forEach(function (issue) {
                        var li = document.createElement('li');
                        li.textContent = issue.message;
                        list.appendChild(li);
                    });
                    box.appendChild(list);
                    el.indexIssues.appendChild(box);
                }

                var frag = document.createDocumentFragment();
                (data.rows || []).forEach(function (row) {
                    var tr = document.createElement('tr');
                    [row.table_name, row.index_name, row.index_type, row.column_name,
                        row.cardinality === null ? '—' : formatNumber(row.cardinality),
                        row.non_unique == 0 ? 'بله' : 'خیر'].forEach(function (v) {
                        var td = document.createElement('td');
                        td.textContent = v;
                        tr.appendChild(td);
                    });
                    frag.appendChild(tr);
                });
                el.indexesBody.replaceChildren(frag);
            })
            .catch(reportError)
            .finally(function () {
                el.indexesLoading.classList.add('is-hidden');
            });
    }

    /* اتصال رویدادها */
    function debounce(fn, delay) {
        var timer = null;
        return function () {
            var args = arguments;
            clearTimeout(timer);
            timer = setTimeout(function () { fn.apply(null, args); }, delay);
        };
    }

    document.querySelectorAll('.mys-sidebar-item').forEach(function (btn) {
        btn.addEventListener('click', function () { switchView(btn.dataset.view); });
    });

    el.sidebarCollapse.addEventListener('click', toggleSidebarCollapse);
    el.drawerToggle.addEventListener('click', openDrawer);
    el.drawerBackdrop.addEventListener('click', closeDrawer);
    el.themeToggle.addEventListener('click', toggleTheme);

    document.querySelectorAll('[data-refresh]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var view = btn.dataset.refresh;
            state.loadedViews[view] = false;
            loadViewIfNeeded(view);
        });
    });

    document.querySelectorAll('[data-close]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var dialog = btn.closest('dialog');
            if (dialog) { dialog.close(); }
        });
    });

    el.tablesSearch.addEventListener('input', debounce(function () {
        state.tables.search = el.tablesSearch.value.trim();
        state.tables.page = 1;
        loadTables();
    }, 300));

    el.optimizationSearch.addEventListener('input', debounce(function () {
        optimizationState.search = el.optimizationSearch.value.trim();
        loadOptimizationTables();
    }, 300));

    el.optimizationSelectAll.addEventListener('change', function () {
        var checked = el.optimizationSelectAll.checked;
        optimizationState.rows.forEach(function (t) { optimizationState.selected[t.name] = checked; });
        el.optimizationBody.querySelectorAll('input[type="checkbox"]').forEach(function (cb) { cb.checked = checked; });
        updateOptimizationSelectionUI();
    });

    el.bulkOptimizeBtn.addEventListener('click', function () { runBulkOperation('optimize'); });
    el.bulkAnalyzeBtn.addEventListener('click', function () { runBulkOperation('analyze'); });
    el.bulkCheckBtn.addEventListener('click', function () { runBulkOperation('check'); });

    document.querySelectorAll('input[name="backupType"]').forEach(function (radio) {
        radio.addEventListener('change', function () {
            el.backupTablesPicker.classList.toggle('is-hidden', radio.value !== 'tables' || !radio.checked);
        });
    });
    el.startBackupBtn.addEventListener('click', startBackup);
    el.saveBackupSettingsBtn.addEventListener('click', saveBackupSettings);

    el.cleanupRuleTable.addEventListener('change', function () { loadCleanupRuleColumns(el.cleanupRuleTable.value); });
    el.saveCleanupRuleBtn.addEventListener('click', saveCleanupRule);

    el.runMaintenanceNowBtn.addEventListener('click', runMaintenanceNow);

    el.logsLoadMoreBtn.addEventListener('click', loadMoreLogs);
    document.querySelectorAll('.toolbar-filters [data-entity-type]').forEach(function (chip) {
        chip.addEventListener('click', function () {
            document.querySelectorAll('.toolbar-filters [data-entity-type]').forEach(function (c) {
                c.classList.toggle('is-selected', c === chip);
            });
            state.logs.entityType = chip.dataset.entityType;
            loadLogs();
        });
    });

    var sortHeaders = document.querySelectorAll('[data-sort]');
    function updateSortIndicators() {
        sortHeaders.forEach(function (th) {
            if (th.dataset.sort === state.tables.sort) {
                th.setAttribute('data-sort-dir', state.tables.dir);
            } else {
                th.removeAttribute('data-sort-dir');
            }
        });
    }
    sortHeaders.forEach(function (th) {
        th.addEventListener('click', function () {
            var sort = th.dataset.sort;
            if (state.tables.sort === sort) {
                state.tables.dir = state.tables.dir === 'asc' ? 'desc' : 'asc';
            } else {
                state.tables.sort = sort;
                state.tables.dir = 'desc';
            }
            updateSortIndicators();
            loadTables();
        });
    });
    updateSortIndicators();

    (function initSidebarCollapsed() {
        try {
            if (localStorage.getItem('mys_sidebar_collapsed') === '1' && window.innerWidth > 900) {
                el.sidebar.classList.add('is-collapsed');
            }
        } catch (e) { /* بی‌اهمیت */ }
    })();

    loadDashboard();
    state.loadedViews.dashboard = true;
})();
