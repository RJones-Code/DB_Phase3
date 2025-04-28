<?php
header('Content-Type: application/json');
error_reporting(0);
session_start();

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

$student_email = trim($_GET['email'] ?? '', '"');

if (empty($student_email)) {
    echo json_encode(["success" => false, "error" => "Email is required"]);
    exit();
}

$stmt = $conn->prepare("
    SELECT a.alert_type, a.alert
    FROM alerts a
    INNER JOIN student s ON a.student_id = s.student_id
    WHERE s.email = ?
");

$stmt->bind_param("s", $student_email);
$stmt->execute();
$result = $stmt->get_result();

$alerts = [];

while ($row = $result->fetch_assoc()) {
    $alerts[] = [
        "alert_type" => $row['alert_type'],
        "alert" => $row['alert']
    ];
}

echo json_encode($alerts);

$stmt->close();
$conn->close();
?>
