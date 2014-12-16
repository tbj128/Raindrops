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
// Check permissions later

$p = '';
if (isset($_GET['p'])) {
	$p = $_GET['p'];
}


if (isset($_GET['uid'], $_GET['cid'], $_GET['lock'])) {
	$uid = $_GET['uid'];
	$cid = $_GET['cid'];
	$lock = $_GET['lock'];

	if ($lock) {
		setItemAsLocked($mysqli, $uid, $cid);
	} else {
		setItemAsUnlocked($mysqli, $uid, $cid);
	}
	header("Location: ../user_progress?id=$uid&success=1&p=$p");
} else {
	header("Location: ../user_progress.php?err=1&p=$p");
}

?>