<?php

	include_once 'config.php';
	include_once 'includes/functions.php';
	
	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	$username = $_GET['u'];
	$password = $_GET['p'];
	$file = $_GET['file'];
	$user_id = userIDLookup($mysqli, $username);
	if (app_login($mysqli, $username, $password)) {
		// Media contents are located within this current user's folder
		$path = 'messages/' . $user_id . '/' . $file;
		$orig = $file;
		header("Content-disposition: attachment; filename=$orig");
		readfile($path);
	}
	
?>