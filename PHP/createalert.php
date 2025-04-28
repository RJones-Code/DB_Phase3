<?php
session_start();

$servername = "localhost";
$username = "root"; // Or your actual database username
$password = "";     // Or your actual database password
$dbname = "DB2";    // Your Phase2 database name

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

// Only accept POST requests
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Get POST data
    $student_id = $_POST['student_id'] ?? '';
    $alert_type = $_POST['alert_type'] ?? '';
    $alert = $_POST['alert'] ?? '';

    // Check if any field is empty
    if (empty($student_id) || empty($alert_type) || empty($alert)) {
        echo json_encode(["success" => false, "error" => "All fields are required"]);
        exit();
    }

    // Prepare SQL insert
    $stmt = $conn->prepare("INSERT INTO alerts (student_id, alert_type, alert) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $student_id, $alert_type, $alert);

    // Execute and check if successful
    if ($stmt->execute()) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "error" => "Failed to create alert"]);
    }

    $stmt->close();
}

$conn->close();
?>
