<?php
header('Content-Type: application/json');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "error" => "Connection failed"]);
    exit();
}


try {
    $input = json_decode(file_get_contents('php://input'), true);
    $email = $conn->real_escape_string($input['email'] ?? '');
    $stmt = $conn->prepare("SELECT student_id FROM student WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        echo json_encode(["success" => false, "error" => "Student not found"]);
        exit();
    }

    $studentData = $result->fetch_assoc();
    $studentId = $studentData['student_id'];
    $stmt->close();

    $stmt = $conn->prepare("SELECT course_id, course_name, credits FROM course");
    $stmt->execute();
    $result = $stmt->get_result();

    $courses = [];
    while ($row = $result->fetch_assoc()) {
        $courses[] = [
            'course_id' => $row['course_id'],
            'course_name' => $row['course_name'], // Fixed typo
            'credits' => $row['credits']
        ];
    }
    $stmt ->close();


    echo json_encode([
        "success" => true,
        "student_id" => $studentId, // Optional: echo back for verification
        "courses" => $courses
    ]);
    
} catch (Exception $e) {
    echo json_encode(["success" => false, "error" => "Database error"]);
} finally {
    $conn->close();
}
?>