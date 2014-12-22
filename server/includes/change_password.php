<?php
include_once '../config.php';
include_once 'functions.php';

$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
if (mysqli_connect_errno()) {
	printf("Connect failed: %s\n", mysqli_connect_error());
	exit();
}

sec_session_start();
 
if (!login_check($mysqli)) {
	header("Location: ../login");
	exit();
}

$user_id = $_SESSION['user_id'];
$username = $_SESSION['username'];

$error_msg = "";

if (isset($_POST['password'], $_POST['oldPassword'])) {

    // Sanitize and validate the data passed in
	$oldPassword = mysqli_real_escape_string($mysqli, $_POST['oldPassword']);
	$password =  mysqli_real_escape_string($mysqli, $_POST['password']);

    if (strlen($password) != 128) {
        // The hashed pwd should be 128 characters long.
        // If it's not, something really odd has happened
        $error_msg .= 'Invalid password configuration';
    }

    if ($stmt = $mysqli->prepare("SELECT password, salt FROM raindrops_members WHERE username = ? LIMIT 1")) {
        $stmt->bind_param('s', $username);  // Bind "$username" to parameter.
        $stmt->execute();    // Execute the prepared query.
        $stmt->store_result();
 
        // get variables from result.
        $stmt->bind_result($check_password, $salt);
        $stmt->fetch();
 
        // hash the password with the unique salt.
        $oldPassword = hash('sha512', $oldPassword . $salt);
        if ($stmt->num_rows == 1) {
			// Check if the password in the database matches
			// the password the user submitted.
			if ($check_password == $oldPassword) {
        		// Create salted password 
        		$newPassword = hash('sha512', $password . $salt);
				$mysqli->query("UPDATE raindrops_members SET password = '$newPassword' WHERE id = $user_id");
			} else {
				$error_msg = "Incorrect old password";
			}
        } else {
            // No user exists.
            $error_msg = "No user exists";
        }
    }
 
	if ($error_msg == "") {
        header('Location: ../settings?success=1');
    } else {
    	$error_msg = urlencode($error_msg);
		header('Location: ../settings?err=' . $error_msg);
    }
} else {
    $error_msg = urlencode("Internal Error (POST)");
	header('Location: ../settings?err=' . $error_msg);
}
?>