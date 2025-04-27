<?php
header('Content-Type: application/json');
session_start();

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode(["error" => "Connection failed"]);
    exit();
}

// Get course info from HTTP headers
$course_id = $_SERVER['HTTP_COURSE_ID'] ?? null;
$section_id = $_SERVER['HTTP_SECTION_ID'] ?? null;
$semester = $_SERVER['HTTP_SEMESTER'] ?? null;
$year = $_SERVER['HTTP_YEAR'] ?? null;

// Validate all fields
if (!$course_id || !$section_id || !$semester || !$year) {
    echo json_encode(["error" => "Missing course parameters"]);
    exit();
}

// Prepare and execute the query
$stmt = $conn->prepare(
    "SELECT s.student_id, s.name, t.grade
     FROM student s
     JOIN take t ON s.student_id = t.student_id
     WHERE t.course_id = ? AND t.section_id = ? AND t.semester = ? AND t.year = ?"
);
$stmt->bind_param("sssi", $course_id, $section_id, $semester, $year);
$stmt->execute();
$result = $stmt->get_result();

// Initialize students array
$students = [];

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $students[] = [
            'student_id' => $row['student_id'],
            'name' => $row['name'],
            'grade' => $row['grade']
        ];
    }
}

// Return JSON
echo json_encode($students);

// Close connection
$conn->close();
?>