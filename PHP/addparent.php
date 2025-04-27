<?php
session_start();

$servername = "localhost";
$username = "root"; 
$password = "";     
$dbname = "DB2";    

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

// Only accept POST requests
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $parent_email = $_POST['parent_email'] ?? '';
    $parent_phone = $_POST['parent_phone'] ?? '';
    $parent_password = $_POST['parent_password'] ?? '';
    $student_email = $_POST['student_email'] ?? '';

    if (empty($parent_email) || empty($parent_phone) || empty($parent_password) || empty($student_email)) {
        echo json_encode(["success" => false, "error" => "All fields are required"]);
        exit();
    }

    $stmt = $conn->prepare("Select student_id FROM student WHERE email = ?");
    $stmt->bind_param("s", $student_email);
    $stmt->execute();
    $stmt->store_result();
    $stmt->bind_result($student_id);
    $stmt->fetch();
    $stmt->close();

    $stmt = $conn->prepare("INSERT INTO parent (email, phone, student_id) VALUES (?, ?, ?)");
    $stmt->bind_param("ssi", $parent_email, $parent_phone, $student_id);
    
    if ($stmt->execute()) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "error" => "Failed to add parent"]);
    }

    $stmt->close();

    $stmt = $conn->prepare("INSERT INTO account (email, password, type) VALUES (?, ?, ?)");
    $user_type = "parent";
    $stmt->bind_param("sss", $parent_email, $parent_password, $user_type);
    
    if ($stmt->execute()) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "error" => "Failed to add account"]);
    }
    $stmt->close();

}

$conn->close();
?>
