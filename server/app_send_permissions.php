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
		$permissions = findPermissions($mysqli, $user_id);
		$permissions_json = array();
		foreach ($permissions as $permission_key => $permission) {
			$cid = $permission_key;
			$locked = $permission['locked'];
			$permission_json = array(
					"cid" => $cid,
					"l" => $locked);
			$permissions_json[] = $permission_json;
		}
		echo json_encode($permissions_json);
	}
	
?>