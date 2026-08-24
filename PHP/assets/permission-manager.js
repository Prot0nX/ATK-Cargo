// PHP/assets/permission-manager.js — پنل مدیریت دسترسی، جاوااسکریپت وانیلا بدون وابستگی.

(function () {
    'use strict';

    /* توست */
    function toast(message, type) {
        var container = document.getElementById('toasts');
        if (!container || !message) { return; }
        var node = document.createElement('div');
        node.className = 'toast toast-' + (type || 'success');
        node.textContent = message;
        container.appendChild(node);
        setTimeout(function () { node.remove(); }, 3500);
    }

    var appData = document.getElementById('permApp');
    var permToast = appData ? JSON.parse(appData.dataset.permToast || 'null') : null;
    if (permToast) {
        toast(permToast.message, permToast.type);
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
        // کوکی (نه localStorage) چون PHP باید مقدار را هنگام رندر بخواند و data-theme را در همان اولین بایت خروجی بگذارد.
        document.cookie = 'permission_manager_theme=' + next + '; path=/; max-age=31536000; samesite=Lax';
    }

    var themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', toggleTheme);
    }

    /* نمایش/مخفی‌کردن رمز عبور در صفحه‌ی ورود */
    var togglePassword = document.getElementById('togglePassword');
    if (togglePassword) {
        togglePassword.addEventListener('click', function () {
            var input = document.getElementById('password');
            var showing = input.type === 'text';
            input.type = showing ? 'password' : 'text';
            togglePassword.querySelector('use').setAttribute('href', showing ? '#eye' : '#eye-off');
            togglePassword.setAttribute('aria-label', showing ? 'نمایش رمز عبور' : 'پنهان‌کردن رمز عبور');
        });
    }

    /* بخش زیر فقط در حالت احراز‌شده (پس از ورود) روی صفحه وجود دارد. */
    var form = document.getElementById('permissionsForm');
    if (!form) { return; }

    var allData = appData ? JSON.parse(appData.dataset.permData || '{}') : { roles: {}, users: {} };
    var currentMode = 'role';

    var FEATURES = [
        'initial_info', 'select_info', 'cargo_counter', 'manage_ships',
        'manage_users', 'admin_chat', 'edit_cargo', 'delete_cargo',
        'view_reports', 'tonnage_warning', 'manage_quotas', 'view_monitoring'
    ];

    var el = {
        targetType: document.getElementById('target_type'),
        targetName: document.getElementById('target_name'),
        targetUser: document.getElementById('target_user'),
        roleGroup: document.getElementById('roleSelectorGroup'),
        userGroup: document.getElementById('userSelectorGroup'),
        userActions: document.getElementById('userActions'),
        targetRole: document.getElementById('target_role'),
        hint: document.getElementById('inheritedHint'),
        roleNameText: document.getElementById('roleNameText')
    };

    function setMode(mode, button) {
        currentMode = mode;
        el.targetType.value = mode;

        document.querySelectorAll('.chip[data-mode]').forEach(function (chip) {
            chip.classList.toggle('is-selected', chip === button);
        });

        el.roleGroup.classList.toggle('is-hidden', mode !== 'role');
        el.userGroup.classList.toggle('is-hidden', mode !== 'user');
        el.userActions.classList.toggle('is-hidden', mode !== 'user');

        loadPermissions();
    }

    document.querySelectorAll('.chip[data-mode]').forEach(function (chip) {
        chip.addEventListener('click', function () { setMode(chip.dataset.mode, chip); });
    });

    el.targetRole.addEventListener('change', loadPermissions);

    /* دراپ‌داون جستجوپذیر کاربر ------------------------------------------- */
    var sd = {
        wrapper: document.getElementById('userDropdown'),
        input: document.getElementById('userSearchInput'),
        list: document.getElementById('sdDropdownList'),
        empty: document.getElementById('sdEmpty'),
        label: document.getElementById('sdSelectedLabel'),
        clearBtn: document.getElementById('sdClearBtn')
    };

    var sdSelectedValue = '';
    var sdSelectedRole = '';
    var sdFocusedIndex = -1;

    function visibleOptions() {
        return Array.prototype.slice.call(sd.list.querySelectorAll('.sd-option'))
            .filter(function (opt) { return opt.style.display !== 'none'; });
    }

    function openDropdown() {
        sd.wrapper.classList.add('is-open');
        sd.input.value = '';
        sd.input.focus();
        sdFocusedIndex = -1;
        filterUsers('');
    }

    function closeDropdown() {
        sd.wrapper.classList.remove('is-open');
        sdFocusedIndex = -1;
        highlightOption(-1);
    }

    function filterUsers(query) {
        var q = query.trim().toLowerCase();
        var visible = 0;
        sd.list.querySelectorAll('.sd-option').forEach(function (opt) {
            var match = !q || (opt.dataset.search || '').indexOf(q) !== -1;
            opt.style.display = match ? '' : 'none';
            if (match) { visible++; }
        });
        sd.empty.classList.toggle('is-hidden', visible !== 0);
        sdFocusedIndex = -1;
        highlightOption(-1);
    }

    function highlightOption(index) {
        var opts = visibleOptions();
        opts.forEach(function (opt, i) { opt.classList.toggle('focused', i === index); });
        if (index >= 0 && opts[index]) { opts[index].scrollIntoView({ block: 'nearest' }); }
    }

    function selectUser(optionEl) {
        sdSelectedValue = optionEl.dataset.value;
        sdSelectedRole = optionEl.dataset.role;

        el.targetUser.value = sdSelectedValue;
        el.targetName.value = sdSelectedValue;

        sd.label.textContent = optionEl.dataset.label;
        sd.label.classList.remove('sd-placeholder');
        sd.clearBtn.classList.remove('is-hidden');

        closeDropdown();
        loadPermissions();
    }

    function clearUserSelection() {
        sdSelectedValue = '';
        sdSelectedRole = '';
        el.targetUser.value = '';
        el.targetName.value = '';
        sd.label.textContent = 'انتخاب کاربر';
        sd.label.classList.add('sd-placeholder');
        sd.clearBtn.classList.add('is-hidden');
        loadPermissions();
    }

    sd.wrapper.addEventListener('click', function (event) {
        if (event.target.closest('.sd-clear-btn')) { return; }
        if (!sd.wrapper.classList.contains('is-open')) { openDropdown(); }
    });

    sd.input.addEventListener('input', function () { filterUsers(sd.input.value); });

    sd.input.addEventListener('keydown', function (event) {
        var opts = visibleOptions();
        if (event.key === 'ArrowDown') {
            event.preventDefault();
            sdFocusedIndex = Math.min(sdFocusedIndex + 1, opts.length - 1);
            highlightOption(sdFocusedIndex);
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            sdFocusedIndex = Math.max(sdFocusedIndex - 1, 0);
            highlightOption(sdFocusedIndex);
        } else if (event.key === 'Enter') {
            event.preventDefault();
            if (sdFocusedIndex >= 0 && opts[sdFocusedIndex]) { selectUser(opts[sdFocusedIndex]); }
        } else if (event.key === 'Escape') {
            closeDropdown();
        }
    });

    sd.list.addEventListener('click', function (event) {
        var option = event.target.closest('.sd-option');
        if (option) { selectUser(option); }
    });

    sd.clearBtn.addEventListener('click', function (event) {
        event.stopPropagation();
        clearUserSelection();
    });

    document.addEventListener('click', function (event) {
        if (!sd.wrapper.contains(event.target) && sd.wrapper.classList.contains('is-open')) {
            closeDropdown();
        }
    });

    /* بارگذاری/اعمال دسترسی‌ها -------------------------------------------- */
    function applyPermissions(perms) {
        FEATURES.forEach(function (feature) {
            var checkbox = document.getElementById('perm_' + feature);
            if (checkbox) { checkbox.checked = !!perms[feature]; }
        });
    }

    function resetCheckboxes(value) {
        form.querySelectorAll('.permissions-grid input[type="checkbox"]').forEach(function (checkbox) {
            checkbox.checked = value;
        });
    }

    function loadPermissions() {
        var perms;

        if (currentMode === 'role') {
            var role = el.targetRole.value;
            el.targetName.value = role;
            perms = (allData.roles && allData.roles[role]) || {};
            el.hint.classList.add('is-hidden');
        } else {
            el.targetName.value = sdSelectedValue;

            if (!sdSelectedValue) {
                resetCheckboxes(false);
                el.hint.classList.add('is-hidden');
                return;
            }

            if (allData.users && allData.users[sdSelectedValue]) {
                perms = allData.users[sdSelectedValue];
                el.hint.classList.add('is-hidden');
            } else {
                perms = (allData.roles && allData.roles[sdSelectedRole]) || {};
                el.roleNameText.textContent = (sdSelectedRole || '').toUpperCase();
                el.hint.classList.remove('is-hidden');
            }
        }

        applyPermissions(perms);
    }

    loadPermissions();
})();
