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
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';

    if (empty($email) || empty($password)) {
        echo json_encode(["success" => false, "error" => "Email and password are required"]);
        exit();
    }

    $stmt = $conn->prepare("SELECT email, password, type FROM account WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    //Checks if Email Exists
    if ($stmt->num_rows > 0) {
        $stmt->bind_result($dbemail, $dbpassword, $user_type);
        $stmt->fetch();

        //Password Verification
        if ($password == $dbpassword) {
            echo json_encode([
                "success" => true,
                "user_type" => $user_type,
                "email" => $dbemail
            ]);
            exit();
        } else {
            echo json_encode(["success" => false, "error" => "Incorrect password"]);
            exit();
        }
    } else {
        echo json_encode(["success" => false, "error" => "Email not found"]);
        exit();
    }
    $stmt->close();
}

$conn->close();
?>
