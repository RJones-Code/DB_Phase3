<?php
header('Content-Type: application/json'); // Tell Android that we're sending JSON

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Connection failed: " . $conn->connect_error]);
    exit();
}

// Check if email was sent in POST
if (!isset($_POST['email'])) {
    echo json_encode(["success" => false, "error" => "No email provided"]);
    exit();
}

$email = $_POST['email'];

// Prepare and execute the query
$stmt = $conn->prepare("SELECT instructor_id, instructor_name FROM instructor WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    // Instructor found
    echo json_encode([
        "success" => true,
        "instructor_id" => $row['instructor_id'],
        "name" => $row['instructor_name']
    ]);
} else {
    // Instructor not found
    echo json_encode([
        "success" => false,
        "error" => "Instructor not found"
    ]);
}

$stmt->close();
$conn->close();
?>