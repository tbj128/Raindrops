<?php
	$php_config = '../config.php';
	
	if (!file_exists($php_config)) {
		header("Location: ../setup.php");
	}
	
	include_once '../config.php';
	include_once 'functions.php';

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}

	sec_session_start();
	 
	if (!login_check($mysqli)) {
		header("Location: ../login");
	}
	
	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];

	// ======================

	if (isset($_POST['trainee'], $_POST['parent'])) {

		// Sanitize and validate the data passed in
		$parent = mysqli_real_escape_string($mysqli, $_POST['parent']);
		$trainee = mysqli_real_escape_string($mysqli, $_POST['trainee']);
	
		if ($insert_stmt = $mysqli->prepare("INSERT INTO raindrops_relations (id_parent, id_child) VALUES (?, ?)")) {
			$insert_stmt->bind_param('ii', $parent, $trainee);
			// Execute the prepared query.
			if (! $insert_stmt->execute()) {
				header('Location: ../register_trainer?err=1');
			}
		}
		header('Location: ../index?success=1');
	
	} else {
		header('Location: ../register_trainer?err=1');
	}
?>