// پنل گزارش آماری کوتاژ — جاوااسکریپت وانیلا بدون وابستگی (به‌جز Chart.js برای نمودار)، جایگزین script.js قدیمی که به api.php از کار افتاده وصل بود.

(function () {
    'use strict';

    var STATUS_LABEL = { 'ورود': 'در انبار', 'خروج': 'خارج شده' };
    var STATUS_BADGE = { 'ورود': 'badge-entered', 'خروج': 'badge-exited' };

    var SORT_TYPES = {
        number: 'number', warehouse: 'text', shippingCompany: 'text', cargoType: 'text',
        totalTonnage: 'number', remainingTonnage: 'number', percentageLoaded: 'number',
        exitVoucherCount: 'number', isActive: 'bool'
    };

    var state = {
        view: 'dashboard',
        quotas: [],
        dashboardSearch: '',
        sortField: null,
        sortDir: 'asc',
        openShip: null,
        currentKotazh: null,
        cargoInfo: [],
        filteredCargo: [],
        statusFilter: 'all',
        dateStart: '',
        dateEnd: '',
        voucherSearch: '',
        chart: null
    };

    var el = {
        toasts: document.getElementById('toasts'),
        dashboardView: document.getElementById('dashboardView'),
        detailView: document.getElementById('detailView'),

        kotazhForm: document.getElementById('kotazhSearchForm'),
        kotazhInput: document.getElementById('kotazhInput'),
        quotasHeaderRow: document.getElementById('quotasHeaderRow'),
        dashboardRefreshBtn: document.getElementById('dashboardRefreshBtn'),
        dashboardCount: document.getElementById('dashboardCount'),
        quotasBody: document.getElementById('quotasBody'),
        dashboardLoading: document.getElementById('dashboardLoading'),
        dashboardEmpty: document.getElementById('dashboardEmpty'),

        advancedSearchBtn: document.getElementById('advancedSearchBtn'),
        advancedSearchDialog: document.getElementById('advancedSearchDialog'),
        advancedSearchForm: document.getElementById('advancedSearchForm'),
        advancedSearchError: document.getElementById('advancedSearchError'),
        advancedSearchSubmit: document.getElementById('advancedSearchSubmit'),
        receiptInput: document.getElementById('receiptInput'),

        backToDashboard: document.getElementById('backToDashboard'),
        detailTitle: document.getElementById('detailTitle'),
        detailSubtitle: document.getElementById('detailSubtitle'),
        detailRefreshBtn: document.getElementById('detailRefreshBtn'),
        detailLoading: document.getElementById('detailLoading'),
        detailEmpty: document.getElementById('detailEmpty'),
        cargoBody: document.getElementById('cargoBody'),
        voucherSearch: document.getElementById('voucherSearch'),

        startDateFilter: document.getElementById('startDateFilter'),
        endDateFilter: document.getElementById('endDateFilter'),
        applyDateFilter: document.getElementById('applyDateFilter'),
        clearDateFilter: document.getElementById('clearDateFilter'),

        exportExcel: document.getElementById('exportExcel'),
        exportHtml: document.getElementById('exportHtml'),

        chartCanvas: document.getElementById('exitChart'),

        stats: {
            total: document.getElementById('statTotal'),
            loaded: document.getElementById('statLoaded'),
            remaining: document.getElementById('statRemaining'),
            percentage: document.getElementById('statPercentage'),
            percentageBar: document.getElementById('statPercentageBar'),
            vouchers: document.getElementById('statVouchers'),
            avgWeight: document.getElementById('statAvgWeight'),
            owner: document.getElementById('statOwner'),
            status: document.getElementById('statStatus')
        }
    };

    /* --- ارتباط با سرور -------------------------------------------------- */
    function request(action, params) {
        var query = new URLSearchParams(params || {});
        query.set('action', action);

        return fetch('api.php?' + query.toString(), { method: 'GET', credentials: 'same-origin' })
            .then(function (response) {
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

    /* --- قالب‌بندی اعداد --------------------------------------------------- */
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

    /* --- نمای داشبورد ------------------------------------------------------ */
    var NEAR_COMPLETION_PERCENTAGE = 90;

    function buildQuotaRow(quota) {
        var isNearCompletion = quota.isActive && quota.totalTonnage > 0 && quota.percentageLoaded >= NEAR_COMPLETION_PERCENTAGE;

        var tr = document.createElement('tr');
        tr.className = 'is-clickable';
        tr.dataset.number = String(quota.number);

        tr.appendChild(cell(quota.number));
        tr.appendChild(cell(quota.warehouse));
        tr.appendChild(cell(quota.shippingCompany));
        tr.appendChild(cell(quota.cargoType));
        tr.appendChild(cell(formatNumber(quota.totalTonnage, 2)));
        tr.appendChild(cell(formatNumber(quota.remainingTonnage, 2)));

        var progressCell = document.createElement('td');
        progressCell.className = 'cell-progress';
        var label = document.createElement('span');
        label.className = 'progress-label';
        label.textContent = formatNumber(quota.percentageLoaded, 1) + '٪';
        var progress = document.createElement('progress');
        progress.className = 'progress-fill' + (isNearCompletion ? ' is-warning' : '');
        progress.max = 100;
        progress.value = Math.min(100, Math.max(0, quota.percentageLoaded));
        progressCell.appendChild(label);
        progressCell.appendChild(progress);
        tr.appendChild(progressCell);

        tr.appendChild(cell(quota.exitVoucherCount));

        var statusCell = document.createElement('td');
        var statusContent = document.createElement('span');
        statusContent.className = 'cell-status-group';
        var badge = document.createElement('span');
        badge.className = 'badge ' + (quota.isActive ? 'badge-active' : 'badge-inactive');
        badge.textContent = quota.isActive ? 'فعال' : 'غیرفعال';
        statusContent.appendChild(badge);
        if (isNearCompletion) {
            var warningBadge = document.createElement('span');
            warningBadge.className = 'badge badge-warning';
            warningBadge.title = 'باقی‌مانده کمتر از ' + (100 - NEAR_COMPLETION_PERCENTAGE) + '٪ است';
            warningBadge.textContent = 'نزدیک اتمام';
            statusContent.appendChild(warningBadge);
        }
        statusCell.appendChild(statusContent);
        tr.appendChild(statusCell);

        return tr;
    }

    var DASHBOARD_COLUMN_COUNT = 9;

    function buildGroupHeaderRow(shipName, count, isOpen) {
        var tr = document.createElement('tr');
        tr.className = 'group-header' + (isOpen ? ' is-open' : '');
        tr.dataset.ship = shipName || '';

        var td = document.createElement('td');
        td.colSpan = DASHBOARD_COLUMN_COUNT;

        var content = document.createElement('span');
        content.className = 'group-header-content';

        var chevron = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        chevron.setAttribute('class', 'icon group-header-chevron');
        chevron.setAttribute('aria-hidden', 'true');
        var use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
        use.setAttribute('href', '#chevron-down');
        chevron.appendChild(use);

        content.appendChild(chevron);
        content.appendChild(document.createTextNode((shipName || 'بدون نام کشتی') + ' — ' + count + ' کوتاژ'));
        td.appendChild(content);
        tr.appendChild(td);
        return tr;
    }

    function compareValues(a, b, type) {
        if (type === 'number') { return (Number(a) || 0) - (Number(b) || 0); }
        if (type === 'bool') { return (a ? 1 : 0) - (b ? 1 : 0); }
        return (a || '').toString().localeCompare((b || '').toString(), 'fa');
    }

    function renderDashboard() {
        var term = state.dashboardSearch.trim().toLowerCase();
        var rows = state.quotas.filter(function (q) {
            if (!term) { return true; }
            return [q.number, q.shipName, q.warehouse, q.shippingCompany, q.cargoType]
                .some(function (value) { return (value || '').toString().toLowerCase().indexOf(term) !== -1; });
        });

        el.quotasBody.replaceChildren();
        el.dashboardCount.textContent = rows.length + ' از ' + state.quotas.length + ' کوتاژ';

        if (rows.length === 0) {
            el.dashboardEmpty.classList.remove('is-hidden');
            return;
        }
        el.dashboardEmpty.classList.add('is-hidden');

        var fragment = document.createDocumentFragment();

        if (state.sortField) {
            var field = state.sortField;
            var type = SORT_TYPES[field];
            var dir = state.sortDir === 'desc' ? -1 : 1;
            rows.slice().sort(function (a, b) { return compareValues(a[field], b[field], type) * dir; })
                .forEach(function (quota) { fragment.appendChild(buildQuotaRow(quota)); });
        } else {
            var shipCounts = {};
            rows.forEach(function (q) { shipCounts[q.shipName] = (shipCounts[q.shipName] || 0) + 1; });

            if (state.openShip !== null && !(state.openShip in shipCounts)) {
                state.openShip = null;
            }

            var currentShip = null;
            rows.forEach(function (quota) {
                if (quota.shipName !== currentShip) {
                    currentShip = quota.shipName;
                    fragment.appendChild(buildGroupHeaderRow(currentShip, shipCounts[currentShip], currentShip === state.openShip));
                }
                if (quota.shipName === state.openShip) {
                    fragment.appendChild(buildQuotaRow(quota));
                }
            });
        }

        el.quotasBody.appendChild(fragment);
    }

    function loadDashboard() {
        el.dashboardLoading.classList.remove('is-hidden');
        el.dashboardEmpty.classList.add('is-hidden');

        return request('summary', null)
            .then(function (data) {
                state.quotas = data.quotas;
                renderDashboard();
            })
            .catch(function (error) {
                if (error.message !== 'unauthenticated') { toast(error.message, 'error'); }
            })
            .finally(function () {
                el.dashboardLoading.classList.add('is-hidden');
            });
    }

    /* --- نمای جزئیات -------------------------------------------------------- */
    function renderStats(info) {
        el.stats.total.textContent = formatNumber(info.totalTonnage, 2);
        el.stats.loaded.textContent = formatNumber(info.loadedTonnage, 2);
        el.stats.remaining.textContent = formatNumber(info.remainingTonnage, 2);
        el.stats.percentage.textContent = formatNumber(info.percentageLoaded, 1) + '٪';
        el.stats.percentageBar.value = Math.min(100, Math.max(0, info.percentageLoaded));
        el.stats.vouchers.textContent = formatNumber(info.exitVoucherCount);
        el.stats.avgWeight.textContent = formatNumber(info.avgVoucherWeight, 2);
        el.stats.owner.textContent = info.cargoOwner || '—';
        el.stats.status.textContent = info.isActive ? 'فعال' : 'غیرفعال';

        el.detailTitle.textContent = 'کوتاژ ' + info.number;
        el.detailSubtitle.textContent = [info.shipName, info.warehouseName, info.shippingCompany, info.cargoType]
            .filter(Boolean).join(' · ');
    }

    function buildCargoRow(cargo) {
        var tr = document.createElement('tr');
        tr.appendChild(cell(cargo.trackingNumber));
        tr.appendChild(cell(cargo.entryTime));
        tr.appendChild(cell(cargo.netWeight !== null ? formatNumber(cargo.netWeight) : null));
        tr.appendChild(cell(cargo.scaleReceiptNumber));
        tr.appendChild(cell(cargo.shortageWeight));
        tr.appendChild(cell(cargo.excessWeight));
        tr.appendChild(cell(cargo.exitTime));
        tr.appendChild(cell(cargo.exitDate));

        var statusCell = document.createElement('td');
        var badge = document.createElement('span');
        badge.className = 'badge ' + (STATUS_BADGE[cargo.status] || '');
        badge.textContent = STATUS_LABEL[cargo.status] || cargo.status || '—';
        statusCell.appendChild(badge);
        tr.appendChild(statusCell);

        return tr;
    }

    function applyDetailFilters() {
        var term = state.voucherSearch.trim().toLowerCase();

        state.filteredCargo = state.cargoInfo.filter(function (c) {
            if (state.statusFilter === 'enter' && c.status !== 'ورود') { return false; }
            if (state.statusFilter === 'exit' && c.status !== 'خروج') { return false; }

            if (state.dateStart && (!c.exitDate || c.exitDate < state.dateStart)) { return false; }
            if (state.dateEnd && (!c.exitDate || c.exitDate > state.dateEnd)) { return false; }

            if (term) {
                var tracking = (c.trackingNumber || '').toString().toLowerCase();
                var receipt = (c.scaleReceiptNumber || '').toString().toLowerCase();
                if (tracking.indexOf(term) === -1 && receipt.indexOf(term) === -1) { return false; }
            }
            return true;
        });

        renderCargoTable();
        renderChart();
    }

    function renderCargoTable() {
        el.cargoBody.replaceChildren();

        if (state.filteredCargo.length === 0) {
            el.detailEmpty.classList.remove('is-hidden');
            return;
        }
        el.detailEmpty.classList.add('is-hidden');

        var fragment = document.createDocumentFragment();
        state.filteredCargo.forEach(function (c) { fragment.appendChild(buildCargoRow(c)); });
        el.cargoBody.appendChild(fragment);
    }

    function populateDateFilters() {
        var dates = Array.from(new Set(
            state.cargoInfo.map(function (c) { return c.exitDate; }).filter(Boolean)
        )).sort();

        [el.startDateFilter, el.endDateFilter].forEach(function (select) {
            var current = select.value;
            select.replaceChildren();
            var placeholder = document.createElement('option');
            placeholder.value = '';
            placeholder.textContent = '—';
            select.appendChild(placeholder);
            dates.forEach(function (d) {
                var option = document.createElement('option');
                option.value = d;
                option.textContent = d;
                select.appendChild(option);
            });
            if (dates.indexOf(current) !== -1) { select.value = current; }
        });
    }

    // نمودار SVG دست‌ساز و بدون وابستگی خارجی — میله‌ی تعداد حواله + خط وزن خالص روزانه؛
    // جایگزین Chart.js از CDN که با CSP سخت‌گیرانه‌ی این پنل (script-src 'self') ناسازگار بود.
    function renderChart() {
        if (!el.chartCanvas) { return; }
        el.chartCanvas.replaceChildren();

        var byDate = {};
        state.filteredCargo.forEach(function (c) {
            if (c.status !== 'خروج' || !c.exitDate) { return; }
            if (!byDate[c.exitDate]) { byDate[c.exitDate] = { count: 0, weight: 0 }; }
            byDate[c.exitDate].count += 1;
            byDate[c.exitDate].weight += Number(c.netWeight) || 0;
        });

        var labels = Object.keys(byDate).sort();
        if (labels.length === 0) {
            var empty = document.createElement('p');
            empty.className = 'muted';
            empty.textContent = 'داده‌ای برای نمایش نمودار در این بازه وجود ندارد.';
            el.chartCanvas.appendChild(empty);
            return;
        }

        var counts = labels.map(function (d) { return byDate[d].count; });
        var weights = labels.map(function (d) { return byDate[d].weight; });
        var maxCount = Math.max.apply(null, counts) || 1;
        var maxWeight = Math.max.apply(null, weights) || 1;

        var width = Math.max(el.chartCanvas.clientWidth || 600, labels.length * 56);
        var height = 240;
        var paddingBottom = 28;
        var paddingTop = 16;
        var plotHeight = height - paddingBottom - paddingTop;
        var colWidth = width / labels.length;
        var barWidth = Math.min(28, colWidth * 0.4);

        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 ' + width + ' ' + height);
        svg.setAttribute('width', '100%');
        svg.setAttribute('height', height);
        svg.setAttribute('preserveAspectRatio', 'none');
        svg.setAttribute('class', 'chart-svg');

        var linePoints = [];

        labels.forEach(function (label, i) {
            var cx = colWidth * i + colWidth / 2;
            var barHeight = (counts[i] / maxCount) * plotHeight;
            var barY = height - paddingBottom - barHeight;

            var bar = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            bar.setAttribute('x', String(cx - barWidth / 2));
            bar.setAttribute('y', String(barY));
            bar.setAttribute('width', String(barWidth));
            bar.setAttribute('height', String(barHeight));
            bar.setAttribute('class', 'chart-bar');
            bar.setAttribute('rx', '2');

            var title = document.createElementNS('http://www.w3.org/2000/svg', 'title');
            title.textContent = label + ' — ' + counts[i] + ' حواله، ' + formatNumber(weights[i], 2) + ' تن';
            bar.appendChild(title);
            svg.appendChild(bar);

            var lineY = height - paddingBottom - (weights[i] / maxWeight) * plotHeight;
            linePoints.push(cx + ',' + lineY);

            var text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.setAttribute('x', String(cx));
            text.setAttribute('y', String(height - 8));
            text.setAttribute('class', 'chart-axis-label');
            text.setAttribute('text-anchor', 'middle');
            text.textContent = label.slice(5);
            svg.appendChild(text);
        });

        var polyline = document.createElementNS('http://www.w3.org/2000/svg', 'polyline');
        polyline.setAttribute('points', linePoints.join(' '));
        polyline.setAttribute('class', 'chart-line');
        svg.appendChild(polyline);

        linePoints.forEach(function (point) {
            var parts = point.split(',');
            var dot = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            dot.setAttribute('cx', parts[0]);
            dot.setAttribute('cy', parts[1]);
            dot.setAttribute('r', '3');
            dot.setAttribute('class', 'chart-dot');
            svg.appendChild(dot);
        });

        var wrap = document.createElement('div');
        wrap.className = 'chart-scroll';
        wrap.appendChild(svg);

        function legendItem(swatchClass, label) {
            var item = document.createElement('span');
            item.className = 'chart-legend-item';
            var swatch = document.createElement('i');
            swatch.className = 'chart-legend-swatch ' + swatchClass;
            item.appendChild(swatch);
            item.appendChild(document.createTextNode(label));
            return item;
        }

        var legend = document.createElement('div');
        legend.className = 'chart-legend';
        legend.appendChild(legendItem('chart-legend-bar', 'تعداد حواله'));
        legend.appendChild(legendItem('chart-legend-line', 'وزن خالص (تن)'));

        el.chartCanvas.appendChild(wrap);
        el.chartCanvas.appendChild(legend);
    }

    function updateExportLinks() {
        if (!state.currentKotazh) { return; }
        el.exportExcel.href = 'export.php?format=excel&kotazh=' + encodeURIComponent(state.currentKotazh);
        el.exportHtml.href = 'export.php?format=html&kotazh=' + encodeURIComponent(state.currentKotazh);
    }

    function loadDetail(kotazh) {
        el.detailLoading.classList.remove('is-hidden');
        el.detailEmpty.classList.add('is-hidden');

        return request('quota', { kotazh: kotazh })
            .then(function (data) {
                state.currentKotazh = kotazh;
                state.cargoInfo = data.cargoInfo;
                renderStats(data.kotazhInfo);
                populateDateFilters();
                applyDetailFilters();
                updateExportLinks();
            })
            .catch(function (error) {
                if (error.message === 'unauthenticated') { return; }
                toast(error.message, 'error');
                showDashboard();
            })
            .finally(function () {
                el.detailLoading.classList.add('is-hidden');
            });
    }

    /* --- ناوبری بین نماها ---------------------------------------------------- */
    function showDashboard(pushState) {
        state.view = 'dashboard';
        el.dashboardView.classList.remove('is-hidden');
        el.detailView.classList.add('is-hidden');
        if (pushState !== false) {
            history.pushState({ view: 'dashboard' }, '', 'index.php');
        }
        loadDashboard();
    }

    function showDetail(kotazh, pushState) {
        state.view = 'detail';
        state.statusFilter = 'all';
        state.dateStart = '';
        state.dateEnd = '';
        state.voucherSearch = '';
        el.voucherSearch.value = '';
        document.querySelectorAll('.chip[data-filter]').forEach(function (chip) {
            chip.classList.toggle('is-selected', chip.dataset.filter === 'all');
        });

        el.dashboardView.classList.add('is-hidden');
        el.detailView.classList.remove('is-hidden');
        if (pushState !== false) {
            history.pushState({ view: 'detail', kotazh: kotazh }, '', 'index.php?kotazh=' + encodeURIComponent(kotazh));
        }
        loadDetail(kotazh);
    }

    /* --- پوسته ------------------------------------------------------------- */
    function currentTheme() {
        var explicit = document.documentElement.dataset.theme;
        if (explicit) { return explicit; }
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function toggleTheme() {
        var next = currentTheme() === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        document.cookie = 'quota_reports_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }

    /* --- اتصال رویدادها ------------------------------------------------------ */
    function debounce(fn, delay) {
        var timer = null;
        return function () {
            clearTimeout(timer);
            timer = setTimeout(fn, delay);
        };
    }

    // جستجوی لحظه‌ای روی همه‌ی ستون‌های جدول؛ فرم فقط برای جلوگیری از reload روی Enter نگه داشته شده.
    el.kotazhForm.addEventListener('submit', function (event) { event.preventDefault(); });

    el.kotazhInput.addEventListener('input', debounce(function () {
        state.dashboardSearch = el.kotazhInput.value;
        renderDashboard();
    }, 150));

    el.dashboardRefreshBtn.addEventListener('click', function () { loadDashboard(); });
    document.getElementById('dashboardPrintBtn').addEventListener('click', function () { window.print(); });

    el.quotasHeaderRow.addEventListener('click', function (event) {
        var th = event.target.closest('th[data-sort]');
        if (!th) { return; }

        var field = th.dataset.sort;
        if (state.sortField === field) {
            state.sortDir = state.sortDir === 'asc' ? 'desc' : 'asc';
        } else {
            state.sortField = field;
            state.sortDir = 'asc';
        }

        Array.prototype.forEach.call(el.quotasHeaderRow.querySelectorAll('th[data-sort]'), function (header) {
            if (header === th) {
                header.dataset.sortDir = state.sortDir;
            } else {
                delete header.dataset.sortDir;
            }
        });

        renderDashboard();
    });

    el.quotasBody.addEventListener('click', function (event) {
        var groupHeader = event.target.closest('tr.group-header');
        if (groupHeader) {
            state.openShip = state.openShip === groupHeader.dataset.ship ? null : groupHeader.dataset.ship;
            renderDashboard();
            return;
        }

        var row = event.target.closest('tr[data-number]');
        if (row) { showDetail(row.dataset.number); }
    });

    el.advancedSearchBtn.addEventListener('click', function () {
        el.advancedSearchError.classList.add('is-hidden');
        el.receiptInput.value = '';
        el.advancedSearchDialog.showModal();
    });

    el.advancedSearchForm.addEventListener('submit', function (event) {
        event.preventDefault();
        var receipt = el.receiptInput.value.trim();
        if (!receipt) { return; }

        el.advancedSearchSubmit.disabled = true;
        request('searchReceipt', { receipt: receipt })
            .then(function (data) {
                el.advancedSearchDialog.close();
                showDetail(data.result.loadingQuotaNumber);
            })
            .catch(function (error) {
                if (error.message === 'unauthenticated') { return; }
                el.advancedSearchError.textContent = error.message;
                el.advancedSearchError.classList.remove('is-hidden');
            })
            .finally(function () {
                el.advancedSearchSubmit.disabled = false;
            });
    });

    el.backToDashboard.addEventListener('click', function () { showDashboard(); });

    el.detailRefreshBtn.addEventListener('click', function () {
        if (state.currentKotazh) { loadDetail(state.currentKotazh); }
    });
    document.getElementById('detailPrintBtn').addEventListener('click', function () { window.print(); });

    document.querySelectorAll('.chip[data-filter]').forEach(function (chip) {
        chip.addEventListener('click', function () {
            document.querySelectorAll('.chip[data-filter]').forEach(function (other) {
                other.classList.toggle('is-selected', other === chip);
            });
            state.statusFilter = chip.dataset.filter;
            applyDetailFilters();
        });
    });

    el.voucherSearch.addEventListener('input', debounce(function () {
        state.voucherSearch = el.voucherSearch.value;
        applyDetailFilters();
    }, 200));

    el.applyDateFilter.addEventListener('click', function () {
        state.dateStart = el.startDateFilter.value;
        state.dateEnd = el.endDateFilter.value;
        applyDetailFilters();
    });

    el.clearDateFilter.addEventListener('click', function () {
        state.dateStart = '';
        state.dateEnd = '';
        el.startDateFilter.value = '';
        el.endDateFilter.value = '';
        applyDetailFilters();
    });

    document.getElementById('themeToggle').addEventListener('click', toggleTheme);

    document.querySelectorAll('[data-close]').forEach(function (button) {
        button.addEventListener('click', function () {
            button.closest('dialog').close();
        });
    });

    window.addEventListener('popstate', function (event) {
        var params = new URLSearchParams(window.location.search);
        var kotazh = params.get('kotazh');
        if (kotazh) { showDetail(kotazh, false); } else { showDashboard(false); }
    });

    /* --- شروع -------------------------------------------------------------- */
    var initialParams = new URLSearchParams(window.location.search);
    var initialKotazh = initialParams.get('kotazh');
    if (initialKotazh && /^\d{8}$/.test(initialKotazh)) {
        showDetail(initialKotazh, false);
    } else {
        showDashboard(false);
    }
})();
