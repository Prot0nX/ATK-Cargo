# حذف/محدودسازی phpMyAdmin روی سرور production (Phase 1, آیتم #۲)

این مورد کاملاً سمت سرور است — هیچ کد داخل این ریپازیتوری دخیل نیست، پس
اینجا فقط چک‌لیست اجرا آورده شده. جزئیات کامل: [DEEP_CODE_REVIEW.md —
phpMyAdmin روی سرور production نصب است](../DEEP_CODE_REVIEW.md).

## گزینه‌ی ۱ — حذف کامل (توصیه‌شده)

```bash
# روی سرور، با SSH:
sudo apt purge phpmyadmin
sudo apt autoremove
```

بعد از حذف، بررسی کنید مسیر دیگر پاسخ نمی‌دهد:
```bash
curl -sI https://atk-nk.ir/phpmyadmin/   # باید 404 باشد، نه 200/302
```

مدیریت دیتابیس از این پس از طریق تونل SSH:
```bash
ssh -L 3306:localhost:3306 user@atk-nk.ir
# سپس با mysql/DBeaver/... به localhost:3306 وصل شوید
```

## گزینه‌ی ۲ — اگر حذف فوری ممکن نیست

محدودسازی به IP خاص در Apache (`/etc/apache2/...` یا `.htaccess` داخل
مسیر phpMyAdmin — **نه** ریشه‌ی `PHP/`):

```apache
<Directory "/usr/share/phpmyadmin">
    Require ip 1.2.3.4/32   # فقط IP شما
</Directory>
```

به‌علاوه HTTP Basic Auth مستقل:
```bash
sudo htpasswd -c /etc/phpmyadmin/.htpasswd youruser
```
```apache
<Directory "/usr/share/phpmyadmin">
    AuthType Basic
    AuthName "Restricted"
    AuthUserFile /etc/phpmyadmin/.htpasswd
    Require valid-user
</Directory>
```

سپس: `sudo systemctl reload apache2`

## راستی‌آزمایی نهایی

```bash
curl -sI https://atk-nk.ir/phpmyadmin/
```
انتظار: `404` (گزینه‌ی ۱) یا `401`/اتصال رد‌شده از IPهای غیرمجاز (گزینه‌ی ۲).
