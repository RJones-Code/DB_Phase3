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
    die("Connection failed: " . $conn->connect_error);
}

// Get instructor_id from HTTP headers
$instructor_id = $_SERVER['HTTP_INSTRUCTOR_ID']; // Custom header

// Check if instructor_id is provided
if (!$instructor_id) {
    echo json_encode(['error' => 'Instructor ID is missing']);
    exit();
}

// Prepare and execute the query
$stmt = $conn->prepare("SELECT course_id, section_id, semester, year FROM section WHERE instructor_id = ?;");
$stmt->bind_param("s", $instructor_id); // Use instructor_id from the header
$stmt->execute();
$result = $stmt->get_result(); // Use get_result() to fetch data

// Initialize array to hold course data
$courses = [];

if ($result->num_rows > 0) {
    // Fetch each row and add it to the courses array
    while ($row = $result->fetch_assoc()) {
        $courses[] = [
            'course_id' => $row['course_id'],
            'section_id' => $row['section_id'],
            'semester' => $row['semester'],
            'year' => $row['year']
        ];
    }
} else {
    echo json_encode(['error' => 'No courses found']);
    exit();
}

// Output the results as a JSON array
echo json_encode($courses);

// Close the database connection
$conn->close();
?>