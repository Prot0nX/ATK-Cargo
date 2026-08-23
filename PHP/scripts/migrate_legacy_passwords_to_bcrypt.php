<?php
// PHP/scripts/migrate_legacy_passwords_to_bcrypt.php یک‌بارمصرف: تبدیل رمزهای غیر-bcrypt باقی‌مانده در جدول Users به bcrypt .

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