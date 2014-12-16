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
	$user_id = userIDLookup($mysqli, $username);
	if (app_login($mysqli, $username, $password)) {
		$messages = getUnreadMessages($mysqli, $user_id);
		setAllMessageAsRead($mysqli, $user_id);
		$messages_json = array();
		foreach ($messages as $message) {
			$msg_from = userLookup($mysqli, $message['id_from']);
			$msg_title = $message['msg_title'];
			$msg_content = $message['msg_content'];
			$msg_link = $message['msg_link'];
			$msg_type = $message['msg_type'];
			$msg_date = $message['msg_date'];
			$message_json = array(
					"from" => $msg_from,
					"title" => $msg_title,
					"content" => $msg_content,
					"link" => $msg_link,
					"type" => $msg_type,
					"date" => $msg_date);
			$messages_json[] = $message_json;
		}
		echo json_encode($messages_json);
	}
	
?>