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
		$points =  mysqli_real_escape_string($mysqli, $message['points']);
		$current_points = getLifetimePoints($mysqli, $user_id);
		
		if ($current_points == -1) {
			// User does not exist yet in points DB
			$mysqli->query("INSERT INTO raindrops_points(id_user, lifetime_points) 
						VALUES ($user_id, $points)");
		} else {
			$mysqli->query("UPDATE raindrops_points SET lifetime_points = $points WHERE id_user = $user_id");
		}
	}
	
?>