/* پنل مدیریت لایسنس — اسکریپت صفحه‌ی ورود.
 *
 * فرم خودش با POST معمولی ارسال می‌شود (رندر و اعتبارسنجی سمت سرور)، پس
 * تنها کار این فایل نمایش/مخفی‌کردن رمز است. نسخه‌ی قبلی به‌جای این، فرم را
 * با fetch می‌فرستاد و نتیجه‌ی احراز هویت را در localStorage نگه می‌داشت —
 * که همان چیزی بود که با یک خط در کنسول مرورگر دور زده می‌شد.
 */
(function () {
    'use strict';

    var toggle = document.getElementById('togglePassword');
    var input = document.getElementById('password');
    if (!toggle || !input) { return; }

    toggle.addEventListener('click', function () {
        var show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        toggle.setAttribute('aria-label', show ? 'مخفی کردن رمز عبور' : 'نمایش رمز عبور');
        toggle.querySelector('use').setAttribute('href', show ? '#eye-off' : '#eye');
        input.focus();
    });
})();
