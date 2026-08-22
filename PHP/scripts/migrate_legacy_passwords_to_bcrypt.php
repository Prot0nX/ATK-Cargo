<?php
// PHP/scripts/migrate_legacy_passwords_to_bcrypt.php
//
// یک‌بارمصرف: تبدیل رمزهای غیر-bcrypt باقی‌مانده در جدول Users به bcrypt
// (DEEP_CODE_AUDIT.md). پیش‌نیاز حذف کامل fallback
// «حالت ۳» (رمز متن‌خام/SHA-256 مستقیم) در UserService::verifyCredentials —
// تا وقتی این تعداد صفر نشود، حذف آن fallback یعنی قفل‌شدن خاموش حساب
// کاربرانی که مهاجرت نکرده‌اند.
//
// چرا نیازی به تشخیص «رمز متن‌خام است یا SHA-256؟» برای هر کاربر نیست:
// UserService::verifyCredentials() از قبل هر دو حالت را زیر bcrypt پوشش
// می‌دهد — «حالت ۱» ورودی خام کاربر را با bcrypt(stored) مقایسه می‌کند،
// «حالت ۲» SHA-256(ورودی) را. پس هرچه الان در ستون password ذخیره است
// (چه متن خام چه از قبل SHA-256 بوده) را مستقیماً bcrypt می‌کنیم:
//   - اگر stored واقعاً رمز خام بوده: bcrypt(stored) === bcrypt(رمز واقعی)
//     → در ورود بعدی از «حالت ۱» عبور می‌کند.
//   - اگر stored از قبل SHA-256(رمز واقعی) بوده: bcrypt(stored) دقیقاً
//     همان چیزی است که «حالت ۲» انتظار دارد → در ورود بعدی از «حالت ۲»
//     عبور و بی‌صدا به «حالت ۱» ارتقا می‌یابد.
// در هر دو حالت نیازی به دانستن رمز واقعی کاربر نیست و رفتار ورود برای
// کاربر تغییری نمی‌کند.
//
// نصب: یک‌بار دستی روی سرور اجرا شود، نه از طریق cron:
//   php /path/to/PHP/scripts/migrate_legacy_passwords_to_bcrypt.php
//
// بعد از اجرا، این باید صفر برگرداند:
//   SELECT COUNT(*) FROM Users WHERE password NOT LIKE '$2y$%' AND password NOT LIKE '$2a$%';
// وقتی صفر شد، بلوک «حالت ۳» و متغیر ALLOW_LEGACY_PLAINTEXT_LOGIN را از
// UserService.php حذف کنید (به کاربر اطلاع بدهید تا این را به Claude بگوید).

declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit('Forbidden: این اسکریپت فقط از طریق CLI قابل اجراست.');
}

require_once __DIR__ . '/../src/bootstrap.php';

use App\Core\Database;
use App\Repositories\UserRepository;

$pdo = Database::getInstance()->getPdoConnection();
$userRepo = new UserRepository();

$stmt = $pdo->query(
    "SELECT id, username, password FROM Users
     WHERE password NOT LIKE '\$2y\$%' AND password NOT LIKE '\$2a\$%'"
);
$legacyUsers = $stmt->fetchAll();

$total = count($legacyUsers);
if ($total === 0) {
    echo "OK: هیچ رمز غیر-bcrypt ای یافت نشد؛ چیزی برای مهاجرت نیست.\n";
    exit(0);
}

echo "$total کاربر با رمز غیر-bcrypt یافت شد. شروع مهاجرت...\n";

$pdo->beginTransaction();
$migrated = 0;
try {
    foreach ($legacyUsers as $user) {
        $newHash = password_hash((string)$user['password'], PASSWORD_BCRYPT, ['cost' => 12]);
        $userRepo->updatePassword((int)$user['id'], $newHash);
        $migrated++;
        echo "  مهاجرت شد: id={$user['id']} username={$user['username']}\n";
    }
    $pdo->commit();
} catch (\Throwable $e) {
    $pdo->rollBack();
    fwrite(STDERR, "خطا در مهاجرت — همه‌ی تغییرات rollback شدند: " . $e->getMessage() . "\n");
    exit(1);
}

echo "پایان: $migrated از $total کاربر با موفقیت به bcrypt مهاجرت کردند.\n";

// راستی‌آزمایی نهایی — باید صفر باشد.
$remaining = (int)$pdo->query(
    "SELECT COUNT(*) FROM Users WHERE password NOT LIKE '\$2y\$%' AND password NOT LIKE '\$2a\$%'"
)->fetchColumn();

if ($remaining === 0) {
    echo "تأیید شد: دیگر هیچ رمز غیر-bcrypt ای در جدول Users باقی نمانده.\n";
} else {
    fwrite(STDERR, "هشدار: بعد از مهاجرت هنوز $remaining ردیف غیر-bcrypt باقی مانده (رکورد جدید حین اجرا؟). دوباره اجرا کنید.\n");
    exit(1);
}
