<?php

	include_once 'config.php';
	include_once 'includes/functions.php';
	
	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	if (isset($_GET['u'], $_GET['p'])) {
		$username = $_GET['u'];
		$password = $_GET['p'];

		if (app_login($mysqli, $username, $password)) {
			$user_id = userIDLookup($mysqli, $username);
			
			$recipient = -1;
			if (isset($_GET['r'])) {
				$recipient = userIDLookup($mysqli, $_GET['r']);
			} else {
				$recipient = findTrainer($mysqli, $user_id);
			}
			
			$user_dir = "messages/$recipient";
			if (!file_exists($user_dir)) {
				mkdir($user_dir, 0777, true);
			}
			
			$file_path = $user_dir . '/' . basename( $_FILES['uploaded_file']['name']);
			if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
				// echo "success";
			} else{
				// echo "fail";
			}
		}
	}
	
?>