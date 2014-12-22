<?php

	include_once 'config.php';
	include_once 'includes/functions.php';
	
	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	// $json = file_get_contents('message.json');
	$json = file_get_contents('php://input');
	$message = json_decode($json, true);
	
	$username = $message['u'];
	$password = $message['p'];
	if (app_login($mysqli, $username, $password)) {
		$user_id = userIDLookup($mysqli, $username);
		$msg_id_from = $user_id;
		// $msg_username_to = mysqli_real_escape_string($mysqli, $message['id_to']); // Support for multiple mailing
		// $msg_id_to = userIDLookup($mysqli, $msg_username_to);
		
		$msg_id_to_list = findTrainers($mysqli, $msg_id_from);
		foreach ($msg_id_to_list as $msg_id_to_id) {
			$msg_id_to = userIDLookup($mysqli, $msg_id_to_id);
			$msg_title =  mysqli_real_escape_string($mysqli, $message['title']);
			$msg_desc =  mysqli_real_escape_string($mysqli, $message['desc']);
			$msg_link = mysqli_real_escape_string($mysqli, $message['link']);
			$msg_type = mysqli_real_escape_string($mysqli, $message['type']);
			$mysqli->query("INSERT INTO raindrops_messages(id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read) 
							VALUES ($msg_id_from, $msg_id_to, '$msg_title', '$msg_desc', '$msg_link', $msg_type, now(), 0)");
		}
	}
	
?>