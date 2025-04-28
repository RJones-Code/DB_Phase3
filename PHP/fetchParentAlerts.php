<?php
header('Content-Type: application/json');
error_reporting(0);
session_start();

$servername = "localhost";
$username = "root"; // your MySQL username
$password = "";     // your MySQL password
$dbname = "DB2";    // your database name

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

$parent_email = trim($_GET['email'] ?? '', '"');

if (empty($parent_email)) {
    echo json_encode(["success" => false, "error" => "Email is required"]);
    exit();
}

$stmt = $conn->prepare("
    SELECT a.student_id, s.name AS student_name, a.alert_type, a.alert
    FROM alerts a
    INNER JOIN student s ON a.student_id = s.student_id
    INNER JOIN parent p ON s.student_id = p.student_id
    WHERE p.email = ?
");

$stmt->bind_param("s", $parent_email);
$stmt->execute();
$result = $stmt->get_result();

$alerts = [];

while ($row = $result->fetch_assoc()) {
    $alerts[] = [
        "student_id" => $row['student_id'],
        "student_name" => $row['student_name'],
        "alert_type" => $row['alert_type'],
        "alert" => $row['alert']
    ];
}

echo json_encode($alerts);

$stmt->close();
$conn->close();
?>
