<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $email = $_POST['email'] ?? '';
    
    if (empty($email)) {
        echo json_encode(["success" => false, "error" => "Email required"]);
        exit();
    }

    // Fetch student profile
    $stmt = $conn->prepare("SELECT student_id, name, dept_name FROM student WHERE email = (SELECT email FROM account WHERE email = ?)");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $studentData = $result->fetch_assoc();
        $student_id = $studentData['student_id'];
        
        // Fetch alerts
        $alertStmt = $conn->prepare("SELECT alert_type, alert FROM alerts WHERE student_id = ?");
        $alertStmt->bind_param("s", $student_id);
        $alertStmt->execute();
        $alertResult = $alertStmt->get_result();
        $alerts = $alertResult->fetch_all(MYSQLI_ASSOC);

        echo json_encode([
            "success" => true,
            "name" => $studentData['name'],
            "student_id" => $student_id,
            "dept_name" => $studentData['dept_name'],
            "email" => $email,
            "alerts" => $alerts
        ]);
    } else {
        echo json_encode(["success" => false, "error" => "Student not found"]);
    }
    exit();
}

echo json_encode(["success" => false, "error" => "Invalid request method"]);
?>