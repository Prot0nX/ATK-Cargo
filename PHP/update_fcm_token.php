<?php
header('Content-Type: application/json');

require_once 'config/config.php';

$userId = $_GET['user_id'] ?? 0;
$token = $_GET['token'] ?? '';

if (!$userId || !$token) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing user_id or token']);
    exit;
}

$conn = getDbConnection();

$stmt = $conn->prepare("UPDATE users SET fcm_token = ? WHERE id = ?");
$stmt->bind_param("si", $token, $userId);

if ($stmt->execute()) {
    echo json_encode(['message' => 'FCM token updated successfully']);
} else {
    http_response_code(500);
    echo json_encode(['error' => 'Failed to update FCM token']);
}

$stmt->close();
$conn->close();