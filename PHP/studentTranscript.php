<?php
session_start();

$servername = "localhost";
$username = "root";
$password = "";
$dbname = "DB2";

//Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

//Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Check if student email is passed via POST
if (isset($_POST['email'])) {
    $email = $_POST['email'];

    // Fetch student_id based on the email
    $stmt = $conn->prepare("SELECT student_id, name FROM student WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();
    $stmt->bind_result($student_id, $name);
    $stmt->fetch();
    $stmt->close();

    // Fetch completed courses
    $completed_courses = [];
    $stmt = $conn->prepare("SELECT course_id, section_id, year, semester, grade FROM take WHERE student_id = ? AND grade IS NOT NULL");
    $stmt->bind_param("s", $student_id);
    $stmt->execute();
    $stmt->store_result();
    $stmt->bind_result($course_id, $section_id, $year, $semester, $grade);
    while ($stmt->fetch()) {
        $completed_courses[] = [
            'course_id' => $course_id,
            'section_id' => $section_id,
            'year' => $year,
            'semester' => $semester,
            'grade' => $grade
        ];
    }
    $stmt->close();

    // Fetch current courses
    $current_courses = [];
    $stmt = $conn->prepare("SELECT course_id, section_id, year, semester FROM take WHERE student_id = ? AND grade IS NULL");
    $stmt->bind_param("s", $student_id);
    $stmt->execute();
    $stmt->store_result();
    $stmt->bind_result($course_id, $section_id, $year, $semester);
    while ($stmt->fetch()) {
        $current_courses[] = [
            'course_id' => $course_id,
            'section_id' => $section_id,
            'year' => $year,
            'semester' => $semester
        ];
    }
    $stmt->close();

    // Calculate GPA
    $grade_points = [
        'A' => 4.0,
        'A-' => 3.7,
        'B+' => 3.3,
        'B' => 3.0,
        'B-' => 2.7,
        'C+' => 2.3,
        'C' => 2.0,
        'C-' => 1.7,
        'D+' => 1.3,
        'D' => 1.0,
        'F' => 0.0
    ];

    $total_points = 0;
    $num_grades = 0;
    foreach ($completed_courses as $course) {
        $grade = $course['grade'];
        if (isset($grade_points[$grade])) {
            $total_points += $grade_points[$grade];
            $num_grades++;
        }
    }

    $gpa = $num_grades > 0 ? $total_points / $num_grades : 0;

    // Check if PhD student
    $phd = false;
    $stmt = $conn->prepare("SELECT student_id FROM phd WHERE student_id = ?");
    $stmt->bind_param("s", $student_id);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        $phd = true;
    }
    $stmt->close();

    // Fetch PhD details if applicable
    $phd_details = [];
    if ($phd) {
        $stmt = $conn->prepare("SELECT proposal_defence_date, dissertation_defence_date FROM phd WHERE student_id = ?");
        $stmt->bind_param("s", $student_id);
        $stmt->execute();
        $stmt->store_result();
        $stmt->bind_result($proposal_date, $dissertation_date);
        $stmt->fetch();
        $stmt->close();
        
        $phd_details = [
            'proposal_date' => $proposal_date ? $proposal_date : null,
            'dissertation_date' => $dissertation_date ? $dissertation_date : null
        ];
    }

    // Prepare JSON response
    $response = [
        'success' => true,
        'name' => $name,
        'completed_courses' => $completed_courses,
        'current_courses' => $current_courses,
        'gpa' => number_format($gpa, 2),
        'phd_details' => $phd_details
    ];

    // Output the JSON response
    echo json_encode($response);

} else {
    echo json_encode(['success' => false, 'error' => 'Email not provided']);
}

$conn->close();
?>
