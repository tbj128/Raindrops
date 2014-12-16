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
		$completed_leaders = getCompletedLeaders($mysqli);
		$leaders_json = array();
		foreach ($completed_leaders as $completed_leader) {
			$leader_user_id = $completed_leader['id_user'];
			$leader_username = userLookup($mysqli, $leader_user_id);
			$num_completed = $completed_leader['num_completed'];
			$leader_json = array(
					"username" => $leader_username,
					"completed" => $num_completed);
			$leaders_json[] = $leader_json;
		}
		echo json_encode($leaders_json);
	}
	
?>