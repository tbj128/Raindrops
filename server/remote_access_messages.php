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
		header("Location: login.php");
	}
	
	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];
	
	if(isset($_GET['file']) && isset($_GET['inbox']) && isset($_GET['id'])) {
		if ($_GET['inbox'] == 1) {
			// Media contents are located within this current user's folder
			$path = 'messages/' . $user_id . '/' . $_GET['file'];
			$orig = $_GET['file'];
			header("Content-disposition: attachment; filename=$orig");
			readfile($path);
			echo $path;
		} else {
			// Media contents are located in the recipient's folder
			if (checkMessageMediaAccess($mysqli, $user_id, $_GET['id'], $_GET['file'])) {
				$path = 'messages/' . $_GET['id'] . '/' . $_GET['file'];
				$orig = $_GET['file'];
				header("Content-disposition: attachment; filename=$orig");
				readfile($path);
				echo $path;
			} else {
				echo 'error 403 - outbox media denied';
			}
		}
	} else {
		echo 'error 401 - URL error';
	}
?>