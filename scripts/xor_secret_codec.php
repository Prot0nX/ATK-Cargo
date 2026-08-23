<?php
// scripts/xor_secret_codec.php — ابزار کمکی برای چرخش API_KEY/LICENSE_KEY (و بقیه‌ی مقادیر) در app/src/main/cpp/secrets.cpp.

declare(strict_types=1);

const XOR_KEY = 0x5A;

function encode(string $plaintext): void {
    $bytes = [];
    foreach (str_split($plaintext) as $ch) {
        $bytes[] = ord($ch) ^ XOR_KEY;
    }
    $hex = array_map(fn($b) => sprintf('0x%02X', $b), $bytes);

    echo "طول رشته: " . count($bytes) . "\n";
    echo "آرایه‌ی C++ (جایگزین بلوک `bytes[]` در تابع مربوطه‌ی secrets.cpp):\n\n";

    $chunks = array_chunk($hex, 16);
    foreach ($chunks as $chunk) {
        echo "    " . implode(", ", $chunk) . ",\n";
    }
    echo "\nطول را هم در فراخوانی decryptXor(bytes, <LEN>) به‌روزرسانی کنید: " . count($bytes) . "\n";
}

function decode(string $csvHex): void {
    $parts = array_filter(array_map('trim', explode(',', $csvHex)));
    $plaintext = '';
    foreach ($parts as $hex) {
        $byte = (int)hexdec(str_replace('0x', '', $hex));
        $plaintext .= chr($byte ^ XOR_KEY);
    }
    echo "متن اصلی: {$plaintext}\n";
}

$mode = $argv[1] ?? null;
$arg = $argv[2] ?? null;

if ($mode === 'encode' && $arg !== null) {
    encode($arg);
} elseif ($mode === 'decode' && $arg !== null) {
    decode($arg);
} else {
    fwrite(STDERR, "استفاده:\n  php xor_secret_codec.php encode \"متن-جدید\"\n  php xor_secret_codec.php decode \"0x32,0x2E,...\"\n");
    exit(1);
}