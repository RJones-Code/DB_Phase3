<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

$response = ['success' => false, 'error' => ''];
$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    $response['error'] = "Database connection failed";
    echo json_encode($response);
    exit();
}

try {
    $input = json_decode(file_get_contents('php://input'), true);
    
    // Validate required parameters
    if (!isset($input['student_id']) || !isset($input['course_id'])) {
        $response['error'] = "Missing student_id or course_id";
        echo json_encode($response);
        exit();
    }

    $student_id = $conn->real_escape_string($input['student_id']);
    $course_id = $conn->real_escape_string($input['course_id']);

    // Handle enrollment request
    if (isset($input['section_id'])) {
        $section_id = $conn->real_escape_string($input['section_id']);
        $semester = $conn->real_escape_string($input['semester'] ?? '');
        $year = $conn->real_escape_string($input['year'] ?? '');

        // Check prerequisites
        $prereqCheck = $conn->prepare("SELECT p.prereq_id 
                                     FROM prereq p
                                     WHERE p.course_id = ?
                                     AND NOT EXISTS (
                                         SELECT 1 
                                         FROM take t 
                                         WHERE t.student_id = ? 
                                         AND t.course_id = p.prereq_id
                                         AND t.grade IS NOT NULL
                                     )");
        $prereqCheck->bind_param("ss", $course_id, $student_id);
        $prereqCheck->execute();
        
        if ($prereqCheck->get_result()->num_rows > 0) {
            $response['error'] = "Prerequisites not met";
            echo json_encode($response);
            exit();
        }

        // Check existing enrollment
        $enrollmentCheck = $conn->prepare("SELECT 1 FROM take 
                                          WHERE student_id = ? 
                                          AND course_id = ? 
                                          AND section_id = ? 
                                          AND semester = ? 
                                          AND year = ?");
        $enrollmentCheck->bind_param("sssss", $student_id, $course_id, $section_id, $semester, $year);
        $enrollmentCheck->execute();
        
        if ($enrollmentCheck->get_result()->num_rows > 0) {
            $response['error'] = "Already enrolled in this section";
            echo json_encode($response);
            exit();
        }

        // Check capacity
        $capacityCheck = $conn->prepare("SELECT s.current_enrollment, c.capacity 
                                        FROM section s
                                        JOIN classroom c ON s.classroom_id = c.classroom_id
                                        WHERE s.course_id = ? 
                                        AND s.section_id = ? 
                                        AND s.semester = ? 
                                        AND s.year = ?");
        $capacityCheck->bind_param("ssss", $course_id, $section_id, $semester, $year);
        $capacityCheck->execute();
        $capacityResult = $capacityCheck->get_result()->fetch_assoc();

        if (!$capacityResult) {
            $response['error'] = "Section not found";
            echo json_encode($response);
            exit();
        }

        if ($capacityResult['current_enrollment'] >= $capacityResult['capacity']) {
            $response['error'] = "Section is full";
            echo json_encode($response);
            exit();
        }

        // Enroll student
        $enrollStmt = $conn->prepare("INSERT INTO take 
                                     (student_id, course_id, section_id, semester, year, grade) 
                                     VALUES (?, ?, ?, ?, ?, NULL)");
        $enrollStmt->bind_param("sssss", $student_id, $course_id, $section_id, $semester, $year);
        
        if ($enrollStmt->execute()) {
            // Update enrollment count
            $conn->query("UPDATE section 
                         SET current_enrollment = current_enrollment + 1 
                         WHERE course_id = '$course_id' 
                         AND section_id = '$section_id' 
                         AND semester = '$semester' 
                         AND year = '$year'");
            
            $response['success'] = true;
            $response['message'] = "Enrollment successful";
        } else {
            $response['error'] = "Enrollment failed";
        }
    } else {
        // Get available sections (existing code remains the same)
        $stmt = $conn->prepare("SELECT s.section_id, s.semester, s.year, 
                               s.current_enrollment, c.capacity 
                               FROM section s
                               JOIN classroom c ON s.classroom_id = c.classroom_id
                               WHERE s.course_id = ?");
        $stmt->bind_param("s", $course_id);
        $stmt->execute();
        $result = $stmt->get_result();

        $sections = [];
        while ($row = $result->fetch_assoc()) {
            $sections[] = [
                'section_id' => $row['section_id'],
                'semester' => $row['semester'],
                'year' => $row['year'],
                'current_enrollment' => $row['current_enrollment'],
                'capacity' => $row['capacity']
            ];
        }

        $response['success'] = true;
        $response['sections'] = $sections;
    }
} catch (Exception $e) {
    $response['error'] = "Server error: " . $e->getMessage();
} finally {
    $conn->close();
    echo json_encode($response);
}
?>