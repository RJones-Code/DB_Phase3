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

$parent_email = trim($_GET['email'] ?? '', '"');

if (empty($parent_email)) {
    echo json_encode(["success" => false, "error" => "Email is required"]);
    exit();
}


$stmt = $conn->prepare("
    SELECT s.student_id, s.name, s.email
    FROM student s
    INNER JOIN parent p ON s.student_id = p.student_id
    WHERE p.email = ?
");

$stmt->bind_param("s", $parent_email);
$stmt->execute();
$result = $stmt->get_result();

$students = [];

while ($row = $result->fetch_assoc()) {
    $students[] = [
        "student_id" => $row['student_id'],
        "student_name" => $row['name'],
        "student_email" => $row['email']
    ];
}

echo json_encode($students);

$stmt->close();
$conn->close();
?>
