# طراحی I-05 — Access Token کوتاه‌مدت + Refresh Token

**وضعیت:** فقط طراحی — به‌درخواست کاربر پیاده‌سازی نشده است.
**پیش‌نیاز مرتبط:** S-05 (انقضای نشست بر اساس `last_activity`) از قبل پیاده‌سازی شده و مبنای این طراحی است.

---

## ۱. مشکلی که حل می‌کند

الان `session_token` هم نقش access token و هم refresh token را هم‌زمان دارد:

- در هر درخواست API فرستاده می‌شود (`X-Session-Token`) — یعنی هر endpoint‌ای که آن را می‌بیند، عملاً می‌تواند لاگ شود/افشا شود/سرقت شود.
- بعد از S-05 اگر ۲۴ ساعت بی‌فعالیتی رخ دهد منقضی می‌شود؛ کاربر باید دوباره **رمز عبور کامل** را وارد کند — چون هیچ توکن سبک‌تری برای «فقط تمدید نشست» وجود ندارد.
- یک توکن سرقت‌شده (از لاگ سرور، بکاپ دیتابیس، یا دستگاه گم‌شده) تا وقتی کاربر واقعی logout نکند یا ۲۴ ساعت بی‌فعالیتی رخ ندهد، کاملاً معتبر است و دسترسی کامل به همه‌ی endpointها می‌دهد.

مدل access+refresh این‌ها را جدا می‌کند: **access token** کوتاه‌مدت (مثلاً ۱۵ دقیقه) و فقط برای درخواست‌های واقعی API استفاده می‌شود؛ **refresh token** بلندمدت‌تر (مثلاً هم‌ان ۲۴ ساعت فعلی) و **فقط** برای گرفتن access token جدید از یک endpoint اختصاصی فرستاده می‌شود — سطح افشای آن در لاگ‌های عادی API بسیار کمتر است.

---

## ۲. تغییرات پایگاه‌داده

جدول `user_sessions` فعلی حفظ می‌شود (شناسه‌ی دستگاه، last_activity، غیره)؛ فقط ستون‌های توکن جایگزین/اضافه می‌شوند:

```sql
ALTER TABLE user_sessions
    ADD COLUMN refresh_token VARCHAR(128) NOT NULL AFTER session_token,
    ADD COLUMN refresh_token_expires_at DATETIME NOT NULL AFTER refresh_token,
    ADD COLUMN access_token_expires_at DATETIME NOT NULL AFTER session_token,
    ADD UNIQUE KEY idx_refresh_token (refresh_token);
```

- `session_token` (ستون فعلی) به‌عنوان **access token** باقی می‌ماند — هیچ تغییری در نام/نوع ستون لازم نیست، فقط طول عمرش کوتاه‌تر می‌شود.
- `refresh_token` جدید، تصادفی و مستقل از access token (نه مشتق‌شده از آن) تولید می‌شود.
- چرخش (rotation): با هر بار refresh موفق، هم `session_token` و هم `refresh_token` مقدار جدید می‌گیرند؛ `refresh_token` قبلی فوراً باطل می‌شود. اگر یک refresh token قبلاً استفاده‌شده دوباره دیده شود (یعنی دزدیده و توسط دو طرف هم‌زمان استفاده شده)، **کل نشست باید بلافاصله باطل شود** (نشانه‌ی سرقت توکن — الگوی استاندارد refresh token rotation).

---

## ۳. جریان سمت سرور

### ۳.۱ ورود (`login`)
بدون تغییر در ورودی/منطق احراز هویت رمز؛ فقط خروجی تغییر می‌کند:

```json
{
  "success": true,
  "accessToken": "...",
  "accessTokenExpiresIn": 900,
  "refreshToken": "...",
  "refreshTokenExpiresIn": 86400
}
```

### ۳.۲ Endpoint جدید: `POST /auth/refresh`
- ورودی: `username`, `deviceId`, `refreshToken` (در بدنه، نه هدر — تا با الگوی هدر access token قاطی نشود)
- منطق:
  1. `SELECT ... WHERE username=? AND device_id=? AND refresh_token=? AND refresh_token_expires_at > NOW()`
  2. اگر یافت نشد → 401 (کاربر باید دوباره login کند)
  3. اگر یافت شد → access token جدید + refresh token جدید تولید و در همان ردیف UPDATE می‌شود (rotation)؛ `refresh_token_expires_at` هم می‌تواند تمدید شود (sliding) یا ثابت بماند (absolute) — تصمیم محصولی در بخش ۶.
- این endpoint **خودش با access token محافظت نمی‌شود** (چون دقیقاً زمانی صدا زده می‌شود که access token منقضی شده)؛ فقط با rate-limit سخت‌گیرانه‌تر از `login` (چون رمز عبور در آن نیست، حمله‌ی brute-force روی refresh token دیگر «حدس رمز» نیست بلکه «حدس یک توکن تصادفی ۱۲۸ بیتی» است — عملاً غیرممکن — اما rate-limit همچنان به‌عنوان لایه‌ی دوم لازم است).

### ۳.۳ `AuthenticatesRequests::requireAuthenticatedSession()`
تغییر کوچک: `validateTokenAndGetUserType` باید علاوه بر `is_active`، `access_token_expires_at > NOW()` را هم چک کند (نه فقط `last_activity` را). اگر منقضی بود، پاسخ باید کد وضعیت **متمایز** از «نشست کاملاً نامعتبر» برگرداند (پیشنهاد: `401` با `code: "access_token_expired"` در بدنه) تا کلاینت بداند باید silent refresh کند، نه کاربر را مستقیم به صفحه‌ی login بفرستد.

### ۳.۴ Logout
باید `refresh_token` را هم پاک/باطل کند، نه فقط `is_active=0` روی access token فعلی — در غیر این صورت یک refresh token هنوز معتبر می‌تواند نشست را «زنده» نگه دارد.

---

## ۴. جریان سمت کلاینت (اندروید)

### ۴.۱ ذخیره‌سازی
`UserPreferencesManager`/`CryptoManager` (رمزنگاری‌شده در DataStore، از قبل موجود) هم `accessToken` و هم `refreshToken` را نگه می‌دارد. `AuthSession` (نگه‌دارنده‌ی درون‌حافظه‌ای فعلی) یک فیلد `refreshToken` هم اضافه می‌کند.

### ۴.۲ تمدید خودکار (Silent Refresh)
به‌جای دستکاری دستی در هر Repository، از مکانیزم `Authenticator` خودِ OkHttp استفاده شود (نه `Interceptor`) — دقیقاً برای همین سناریو طراحی شده:

```kotlin
class TokenAuthenticator(
    private val refreshApi: () -> ApiService, // یک نمونه‌ی جدا بدون این Authenticator، تا حلقه‌ی بی‌نهایت رخ ندهد
    private val onRefreshFailed: () -> Unit    // → logout و بازگشت به صفحه‌ی ورود
) : Authenticator {
    private val mutex = Mutex() // چند درخواست هم‌زمان که هر دو 401 گرفته‌اند، نباید هر کدام جدا refresh بزنند

    override fun authenticate(route: Route?, response: Response): Request? {
        // فقط وقتی بدنه‌ی پاسخ صراحتاً code=access_token_expired دارد وارد این مسیر شود،
        // نه هر 401 دلخواه (که ممکن است «نشست کاملاً نامعتبر» باشد و refresh هم بی‌فایده باشد)
        ...
        // یک‌بار refresh را واقعاً صدا بزند (double-checked locking داخل mutex)،
        // access token جدید را در AuthSession/DataStore ذخیره کند،
        // و درخواست اصلی را با هدر جدید دوباره بسازد (response.request.newBuilder()...)
    }
}
```

نکات حیاتی برای جلوگیری از باگ‌های رایج این الگو:
- **جلوگیری از حلقه‌ی بی‌نهایت**: اگر خودِ درخواست refresh هم 401 بگیرد، نباید دوباره از این Authenticator عبور کند (باید با یک OkHttpClient جدا بدون این Authenticator صدا زده شود).
- **جلوگیری از refresh موازی**: اگر ۵ درخواست هم‌زمان با access token منقضی برخورد کنند، نباید ۵ بار refresh صدا زده شود (هر کدام refresh token را rotate می‌کند و ۴ تای دیگر با refresh token باطل‌شده مواجه می‌شوند) — قفل (mutex) + بررسی «آیا از وقتی این درخواست شروع شد access token عوض شده؟» قبل از صدازدن مجدد refresh لازم است.
- **شکست نهایی**: اگر refresh token هم منقضی/نامعتبر بود، باید کاربر را واقعاً logout کند (پاک‌کردن AuthSession + بازگشت به صفحه‌ی ورود)، نه فقط خطا نشان دهد.

---

## ۵. طرح Rollout (سازگاری با نصب‌های قدیمی)

چون این اپ از طریق Play Store توزیع نمی‌شود، همیشه ترکیبی از نسخه‌های قدیمی/جدید اپ هم‌زمان روی سرور فعال خواهند بود. پیشنهاد:

1. **فاز ۱ (سرور، بدون شکستن کلاینت‌های فعلی):** endpoint `login` هم فرمت قدیمی (`sessionToken`) و هم فرمت جدید (`accessToken`+`refreshToken`) را برگرداند؛ کلاینت‌های قدیمی فقط `sessionToken` را می‌خوانند و طول عمر آن همچنان همان S-05 فعلی (۲۴ ساعت با last_activity) باقی می‌ماند — یعنی رفتار فعلی برای نصب‌های قدیمی دست‌نخورده می‌ماند.
2. **فاز ۲ (کلاینت):** نسخه‌ی جدید اپ منتشر شود که `TokenAuthenticator` را پیاده می‌کند و از فیلدهای جدید استفاده می‌کند.
3. **فاز ۳ (اجباری، بعد از گذشت زمان کافی برای آپدیت اکثر کاربران):** access token واقعاً کوتاه‌مدت شود (۱۵ دقیقه)؛ قبل از این فاز، حتی اگر ستون‌های DB اضافه شده باشند، `access_token_expires_at` می‌تواند همان مقدار طولانی فعلی (۲۴ ساعت) بماند تا نصب‌های قدیمی که هنوز منتظر رفرش نیستند، ناگهان قطع نشوند.

این چارچوب همان الگوی `min_allowed_version`/`enforceMinAppVersion` موجود در پروژه را می‌تواند برای اجباری‌کردن فاز ۳ به کار بگیرد.

---

## ۶. تصمیم‌های محصولی که قبل از پیاده‌سازی لازم است

| # | سوال | گزینه‌ها |
|---|---|---|
| ۱ | طول عمر access token | پیشنهاد: ۱۵ دقیقه (استاندارد رایج) — عدد دیگری هم ممکن است |
| ۲ | طول عمر refresh token | همان ۲۴ ساعت S-05 فعلی، یا طولانی‌تر (مثلاً ۷–۳۰ روز) چون دیگر مستقیماً در هر درخواست فرستاده نمی‌شود و ریسک افشای آن کمتر است |
| ۳ | Sliding یا Absolute expiration برای refresh token | Sliding (هر refresh موفق، انقضا را دوباره از صفر می‌شمارد → کاربر فعال هرگز logout نمی‌شود) در مقابل Absolute (بعد از N روز، فارغ از فعالیت، حتماً باید دوباره login کند) |
| ۴ | رفتار در تشخیص سرقت (استفاده‌ی دوباره از refresh token باطل‌شده) | فقط همان نشست باطل شود، یا همه‌ی نشست‌های آن کاربر (احتیاط بیشتر) |
| ۵ | زمان‌بندی فاز ۳ (اجباری‌کردن) | بستگی به سرعت آپدیت واقعی کاربران دارد — نیاز به داده‌ی میدانی از `app_version` نصب‌شده‌ها (که همین الان هم در `user_sessions.app_version` ثبت می‌شود) |

---

## ۷. برآورد حجم کار (در صورت تصمیم به پیاده‌سازی)

- سرور: تغییر `login`، endpoint جدید `refresh`، تغییر `logout`، تغییر `AuthenticatesRequests`، migration ستون‌ها — تقریباً هم‌اندازه‌ی کاری که برای C-05 (شکستن AppApiController) انجام شد.
- کلاینت: `TokenAuthenticator` جدید (پیچیده‌ترین بخش، به‌خاطر race condition)، تغییر `AuthSession`/`UserPreferencesManager`، تغییر مسیر لاگین/لاگ‌اوت.
- تست: این بخش برخلاف بقیه‌ی کارهای این پروژه، **حتماً نیاز به تست دستی روی دستگاه واقعی با سناریوهای هم‌زمانی (چند درخواست موازی هنگام انقضا)** دارد — چیزی که با PHPStan/PHPUnit به‌تنهایی قابل تأیید نیست.
