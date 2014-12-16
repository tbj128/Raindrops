<?php
include_once '../config.php';
include_once 'functions.php';


$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
if (mysqli_connect_errno()) {
	printf("Connect failed: %s\n", mysqli_connect_error());
	exit();
}

sec_session_start(); // Our custom secure way of starting a PHP session.

$welcome = false;
if (isset($_GET['welcome'])) {
	$welcome = true;
}

if (isset($_POST['username'], $_POST['p'])) {
    $username = $_POST['username'];
    $password = $_POST['p']; // The hashed password.
 
    if (login($username, $password, $mysqli) == true) {
        // Login success 
		if ($welcome) {
			header('Location: ../index.php?welcome=1');
		} else {
			header('Location: ../index.php');
		}
    } else {
        // Login failed 
        header('Location: ../login.php?error=1');
    }
} else {
    // The correct POST variables were not sent to this page. 
    echo 'Invalid Request';
}
?>