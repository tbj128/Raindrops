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
	
	$login_json = array();
	if (app_login($mysqli, $username, $password)) {
		$login_json['status'] = 1;
	} else {
		$login_json['status'] = 0;
	}
	
	echo json_encode($login_json);
?>