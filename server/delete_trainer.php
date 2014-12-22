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
	header("Location: login");
}

$user_id = $_SESSION['user_id'];
$username = $_SESSION['username'];
$user_type = userType($mysqli, $user_id);

if ($user_type != "admin") {
	header("Location: index.php");
}

if (isset($_GET['id'])) {
	$id_user = $_GET['id'];
	
	if (is_int($id_user)) {
		deleteAccount($mysqli, $id_user);
	} else {
		$id_user = mysqli_real_escape_string($mysqli, $id_user);
		deleteAccount($mysqli, $id_user);
	}
}

header("Location: index.php");

?>