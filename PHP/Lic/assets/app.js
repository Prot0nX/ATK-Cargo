/* =============================================================================
   پنل مدیریت لایسنس — جاوااسکریپت داشبورد
   =============================================================================
   وانیلا، بدون وابستگی. جایگزین license-manager.js قبلی که به SweetAlert2 و
   Tailwind CDN وابسته بود.

   دو تفاوت مهم با نسخه‌ی قبلی:

   ۱. هیچ ردیفی با innerHTML ساخته نمی‌شود. نسخه‌ی قبلی داده‌ی کاربر را
      مستقیم درون رشته‌ی HTML و حتی داخل onclick="...('${key}')" می‌گذاشت —
      یعنی یک نام شرکت با کاراکتر ' یا < می‌توانست کد اجرا کند. اینجا همه‌چیز
      با createElement/textContent ساخته می‌شود و رویدادها با delegation روی
      <tbody> بسته می‌شوند.

   ۲. بازخوانی خودکار هر ۶۰ ثانیه حذف شده. آن setInterval نشست را برای همیشه
      زنده نگه می‌داشت و انقضای بی‌کاری سمت سرور را عملاً بی‌اثر می‌کرد.
   ========================================================================== */

(function () {
    'use strict';

    var CSRF = document.querySelector('meta[name="csrf-token"]').content;

    var STATUS_LABEL = { active: 'فعال', expired: 'منقضی', inactive: 'غیرفعال' };

    var state = {
        licenses: [],
        search: '',
        status: '',
        editingId: null
    };

    var el = {
        body: document.getElementById('licensesBody'),
        loading: document.getElementById('loadingState'),
        empty: document.getElementById('emptyState'),
        search: document.getElementById('searchInput'),
        exportLink: document.getElementById('exportLink'),
        toasts: document.getElementById('toasts'),
        form: document.getElementById('licenseForm'),
        formError: document.getElementById('formError'),
        dialog: document.getElementById('licenseDialog'),
        dialogTitle: document.getElementById('dialogTitle'),
        saveBtn: document.getElementById('saveBtn'),
        details: document.getElementById('detailsDialog'),
        detailsContent: document.getElementById('detailsContent'),
        confirm: document.getElementById('confirmDialog'),
        confirmTitle: document.getElementById('confirmTitle'),
        confirmMessage: document.getElementById('confirmMessage'),
        confirmOk: document.getElementById('confirmOk'),
        stats: {
            active: document.getElementById('statActive'),
            expired: document.getElementById('statExpired'),
            inactive: document.getElementById('statInactive'),
            total: document.getElementById('statTotal'),
            monthly: document.getElementById('statMonthly')
        }
    };

    /* --- ارتباط با سرور -------------------------------------------------- */

    /**
     * هر پاسخ ۴۰۱ یعنی نشست منقضی شده — کاربر به صفحه‌ی ورود برمی‌گردد
     * به‌جای اینکه با خطاهای مبهم روبه‌رو شود.
     */
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

    function formatDate(value) {
        if (!value) { return '—'; }
        var date = new Date(value.replace(' ', 'T'));
        return isNaN(date.getTime()) ? value : date.toLocaleDateString('fa-IR');
    }

    function formatRelative(value) {
        if (!value) { return 'هرگز'; }
        var date = new Date(value.replace(' ', 'T'));
        if (isNaN(date.getTime())) { return value; }

        var minutes = Math.floor((Date.now() - date.getTime()) / 60000);
        if (minutes < 1) { return 'لحظاتی پیش'; }
        if (minutes < 60) { return minutes + ' دقیقه پیش'; }
        if (minutes < 1440) { return Math.floor(minutes / 60) + ' ساعت پیش'; }
        return formatDate(value);
    }

    /* --- ساخت جدول ------------------------------------------------------- */
    function iconButton(symbolId, label, action, id, extraClass) {
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn-icon' + (extraClass ? ' ' + extraClass : '');
        button.title = label;
        button.setAttribute('aria-label', label);
        button.dataset.action = action;
        button.dataset.id = id;

        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('class', 'icon');
        svg.setAttribute('aria-hidden', 'true');
        var use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
        use.setAttribute('href', '#' + symbolId);
        svg.appendChild(use);
        button.appendChild(svg);

        return button;
    }

    function cell(text, className) {
        var td = document.createElement('td');
        if (className) { td.className = className; }
        if (text !== undefined) { td.textContent = text; }
        return td;
    }

    function buildRow(license) {
        var tr = document.createElement('tr');
        tr.dataset.id = String(license.id);

        // کلید + دکمه‌ی کپی
        var keyCell = document.createElement('td');
        var keyWrap = document.createElement('span');
        keyWrap.className = 'cell-key';
        var code = document.createElement('code');
        code.textContent = license.license_key;
        keyWrap.appendChild(code);
        keyWrap.appendChild(iconButton('copy', 'کپی کلید', 'copy', license.id));
        keyCell.appendChild(keyWrap);
        tr.appendChild(keyCell);

        tr.appendChild(cell(license.company_name, 'cell-company'));
        tr.appendChild(cell(license.plan_label));

        var statusCell = document.createElement('td');
        var badge = document.createElement('span');
        badge.className = 'badge badge-' + license.status;
        badge.textContent = STATUS_LABEL[license.status] || license.status;
        statusCell.appendChild(badge);
        tr.appendChild(statusCell);

        tr.appendChild(cell(license.expires_at ? formatDate(license.expires_at) : 'نامحدود'));
        tr.appendChild(cell(formatRelative(license.last_check)));

        var actions = document.createElement('td');
        var group = document.createElement('div');
        group.className = 'cell-actions';
        group.appendChild(iconButton('edit', 'ویرایش', 'edit', license.id));
        group.appendChild(iconButton(
            'power',
            license.is_active ? 'غیرفعال کردن' : 'فعال کردن',
            'toggle',
            license.id
        ));
        group.appendChild(iconButton('trash', 'حذف', 'delete', license.id, 'is-danger'));
        actions.appendChild(group);
        tr.appendChild(actions);

        return tr;
    }

    function render() {
        el.body.replaceChildren();

        if (state.licenses.length === 0) {
            el.empty.classList.remove('is-hidden');
            return;
        }

        el.empty.classList.add('is-hidden');
        var fragment = document.createDocumentFragment();
        state.licenses.forEach(function (license) {
            fragment.appendChild(buildRow(license));
        });
        el.body.appendChild(fragment);
    }

    function renderStats(stats) {
        el.stats.active.textContent = stats.active;
        el.stats.expired.textContent = stats.expired;
        el.stats.inactive.textContent = stats.inactive;
        el.stats.total.textContent = stats.total;
        el.stats.monthly.textContent =
            'این ماه ' + stats.this_month + ' · ماه قبل ' + stats.last_month;
    }

    /* --- بارگذاری داده --------------------------------------------------- */
    function filterParams() {
        var params = {};
        if (state.search) { params.search = state.search; }
        if (state.status) { params.status = state.status; }
        return params;
    }

    function load() {
        el.loading.classList.remove('is-hidden');
        el.empty.classList.add('is-hidden');

        var params = filterParams();
        var qs = new URLSearchParams(params).toString();
        el.exportLink.href = 'export.php' + (qs ? '?' + qs : '');

        return request('list', 'GET', null, params)
            .then(function (data) {
                state.licenses = data.licenses;
                renderStats(data.stats);
                render();
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

    /* --- دیالوگ تأیید ---------------------------------------------------- */
    function confirmAction(title, message, confirmLabel) {
        return new Promise(function (resolve) {
            el.confirmTitle.textContent = title;
            el.confirmMessage.textContent = message;
            el.confirmOk.textContent = confirmLabel;

            function cleanup(result) {
                el.confirmOk.removeEventListener('click', onOk);
                el.confirm.removeEventListener('close', onClose);
                el.confirm.close();
                resolve(result);
            }
            function onOk() { cleanup(true); }
            function onClose() { cleanup(false); }

            el.confirmOk.addEventListener('click', onOk);
            el.confirm.addEventListener('close', onClose);
            el.confirm.showModal();
        });
    }

    /* --- فرم ایجاد/ویرایش ------------------------------------------------ */

    /**
     * ورودی datetime-local فقط قالب `YYYY-MM-DDTHH:MM` را می‌پذیرد، اما
     * دیتابیس `YYYY-MM-DD HH:MM:SS` برمی‌گرداند.
     */
    function toDatetimeLocal(value) {
        return value ? value.replace(' ', 'T').slice(0, 16) : '';
    }

    function openForm(license) {
        el.formError.classList.add('is-hidden');
        el.form.reset();

        if (license) {
            state.editingId = license.id;
            el.dialogTitle.textContent = 'ویرایش لایسنس';
            el.form.elements.company_name.value = license.company_name;
            el.form.elements.plan.value = license.plan;
            el.form.elements.expires_at.value = toDatetimeLocal(license.expires_at);
            el.form.elements.contact_name.value = license.contact_name || '';
            el.form.elements.contact_phone.value = license.contact_phone || '';
            el.form.elements.contact_email.value = license.contact_email || '';
            el.form.elements.notes.value = license.notes || '';
        } else {
            state.editingId = null;
            el.dialogTitle.textContent = 'لایسنس جدید';
        }

        el.dialog.showModal();
    }

    function submitForm(event) {
        event.preventDefault();
        el.formError.classList.add('is-hidden');

        var payload = {
            company_name: el.form.elements.company_name.value,
            plan: el.form.elements.plan.value,
            expires_at: el.form.elements.expires_at.value,
            contact_name: el.form.elements.contact_name.value,
            contact_phone: el.form.elements.contact_phone.value,
            contact_email: el.form.elements.contact_email.value,
            notes: el.form.elements.notes.value
        };

        var isEdit = state.editingId !== null;

        el.saveBtn.disabled = true;
        request(
            isEdit ? 'update' : 'create',
            'POST',
            payload,
            isEdit ? { id: state.editingId } : null
        )
            .then(function (data) {
                el.dialog.close();
                toast(data.message, 'success');
                if (!isEdit) {
                    // کلید تازه فقط همین یک‌بار به‌راحتی در دسترس است.
                    showDetails(data.license);
                }
                return load();
            })
            .catch(function (error) {
                if (error.message === 'unauthenticated') { return; }
                el.formError.textContent = error.message;
                el.formError.classList.remove('is-hidden');
            })
            .finally(function () {
                el.saveBtn.disabled = false;
            });
    }

    /* --- جزئیات ---------------------------------------------------------- */
    function showDetails(license) {
        var rows = [
            ['نام شرکت', license.company_name],
            ['کلید لایسنس', license.license_key, true],
            ['پلن', license.plan_label],
            ['وضعیت', STATUS_LABEL[license.status] || license.status],
            ['تاریخ انقضا', license.expires_at ? formatDateTime(license.expires_at) : 'نامحدود'],
            ['نام رابط', license.contact_name || '—'],
            ['شماره تماس', license.contact_phone || '—'],
            ['ایمیل', license.contact_email || '—'],
            ['تاریخ ایجاد', formatDateTime(license.created_at)],
            ['آخرین بررسی', license.last_check ? formatDateTime(license.last_check) : 'هرگز'],
            ['یادداشت', license.notes || '—']
        ];

        var list = document.createElement('dl');
        list.className = 'detail-list';

        rows.forEach(function (row) {
            var dt = document.createElement('dt');
            dt.textContent = row[0];
            var dd = document.createElement('dd');
            if (row[2]) {
                var code = document.createElement('code');
                code.textContent = row[1];
                dd.appendChild(code);
            } else {
                dd.textContent = row[1];
            }
            list.appendChild(dt);
            list.appendChild(dd);
        });

        el.detailsContent.replaceChildren(list);
        el.details.showModal();
    }

    /* --- عملیات ردیف ----------------------------------------------------- */
    function findLicense(id) {
        return state.licenses.filter(function (item) {
            return String(item.id) === String(id);
        })[0];
    }

    function copyKey(license) {
        if (!navigator.clipboard) {
            toast('مرورگر شما از کپی خودکار پشتیبانی نمی‌کند.', 'error');
            return;
        }
        navigator.clipboard.writeText(license.license_key).then(function () {
            toast('کلید کپی شد.', 'success');
        }, function () {
            toast('کپی کلید ناموفق بود.', 'error');
        });
    }

    function toggleLicense(license) {
        var activating = !license.is_active;
        confirmAction(
            activating ? 'فعال‌سازی لایسنس' : 'غیرفعال‌سازی لایسنس',
            activating
                ? 'لایسنس «' + license.company_name + '» دوباره فعال شود؟'
                : 'لایسنس «' + license.company_name + '» غیرفعال شود؟ اپ این مشتری بلافاصله از کار می‌افتد.',
            activating ? 'فعال کن' : 'غیرفعال کن'
        ).then(function (confirmed) {
            if (!confirmed) { return; }
            request('toggle', 'POST', null, { id: license.id })
                .then(function (data) {
                    toast(data.message, 'success');
                    return load();
                })
                .catch(function (error) {
                    if (error.message !== 'unauthenticated') { toast(error.message, 'error'); }
                });
        });
    }

    function deleteLicense(license) {
        confirmAction(
            'حذف لایسنس',
            'لایسنس «' + license.company_name + '» برای همیشه حذف شود؟ این عملیات بازگشت‌پذیر نیست.',
            'حذف کن'
        ).then(function (confirmed) {
            if (!confirmed) { return; }
            request('delete', 'POST', null, { id: license.id })
                .then(function (data) {
                    toast(data.message, 'success');
                    return load();
                })
                .catch(function (error) {
                    if (error.message !== 'unauthenticated') { toast(error.message, 'error'); }
                });
        });
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
        // کوکی (نه localStorage) چون PHP باید مقدار را هنگام رندر بخواند و
        // data-theme را در همان اولین بایت خروجی بگذارد؛ وگرنه صفحه لحظه‌ای
        // با پوسته‌ی اشتباه نمایش داده می‌شود.
        document.cookie = 'lic_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }

    /* --- اتصال رویدادها -------------------------------------------------- */
    function debounce(fn, delay) {
        var timer = null;
        return function () {
            clearTimeout(timer);
            timer = setTimeout(fn, delay);
        };
    }

    el.search.addEventListener('input', debounce(function () {
        state.search = el.search.value.trim();
        load();
    }, 300));

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
    document.getElementById('newLicenseBtn').addEventListener('click', function () { openForm(null); });
    document.getElementById('themeToggle').addEventListener('click', toggleTheme);
    el.form.addEventListener('submit', submitForm);

    // Event delegation: یک شنونده برای کل جدول به‌جای هندلر درون‌خطی روی هر
    // دکمه — همان چیزی که الگوی تزریق نسخه‌ی قبلی را ممکن می‌کرد.
    el.body.addEventListener('click', function (event) {
        var button = event.target.closest('button[data-action]');
        var license;

        if (button) {
            license = findLicense(button.dataset.id);
            if (!license) { return; }

            if (button.dataset.action === 'copy') { copyKey(license); }
            else if (button.dataset.action === 'edit') { openForm(license); }
            else if (button.dataset.action === 'toggle') { toggleLicense(license); }
            else if (button.dataset.action === 'delete') { deleteLicense(license); }
            return;
        }

        var row = event.target.closest('tr[data-id]');
        if (row) {
            license = findLicense(row.dataset.id);
            if (license) { showDetails(license); }
        }
    });

    document.querySelectorAll('[data-close]').forEach(function (button) {
        button.addEventListener('click', function () {
            button.closest('dialog').close();
        });
    });

    load();
})();
