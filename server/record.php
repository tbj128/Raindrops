<?php

	$php_config = 'config.php';

	if (!file_exists($php_config)) {
		header("Location: setup.php");
	}

	include_once 'config.php';
	include_once 'includes/functions.php';

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}

	sec_session_start();
	 
	if (!login_check($mysqli)) {
		header("Location: login");
	}

	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	// ==== Page-specific PHP ========

	$audioName = 'temp.wav';
	if (isset($_GET['a'])) {
		$audioName = $_GET['a'];
	}
    $file = "upload/audio/" . $audioName;
	
    // Writes recorded audiofile to the server folder
    parse_str($_SERVER['QUERY_STRING'], $params);
	
    // save the recorded audio to that file
    $content = file_get_contents('php://input');
    $fh = fopen($file, 'w') or die("can't open file");
    fwrite($fh, $content);
    fclose($fh);
?>