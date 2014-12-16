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

$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
if (mysqli_connect_errno()) {
	printf("Connect failed: %s\n", mysqli_connect_error());
	exit();
}

// ==== Page-specific PHP ========
	
$error_msg = "";

if (isset($_POST['msg_id_to'], $_POST['msg_title'], $_POST['msg_desc'])) {
	$msg_id_from = $user_id;
	$msg_id_to = mysqli_real_escape_string($mysqli, $_POST['msg_id_to']);
	$msg_title =  mysqli_real_escape_string($mysqli, $_POST['msg_title']);
	$msg_desc =  mysqli_real_escape_string($mysqli, $_POST['msg_desc']);
	$msg_link = '';
	$msg_type = 0;
	if (isset($_POST['audio_location'])) {
		if ($_POST['audio_location'] != "") {
			$msg_link = $_POST['audio_location'];
			if ($msg_link != "") {
				$msg_type = 1;
			}
			if (!file_exists('../messages/' . $msg_id_to)) {
				mkdir('../messages/' . $msg_id_to, 0777, true);
			}
			rename('../upload/audio/' . $msg_link, '../messages/' . $msg_id_to . '/' . $msg_link);
		}
	}
	$mysqli->query("INSERT INTO raindrops_messages(id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read) 
					VALUES ($msg_id_from, $msg_id_to, '$msg_title', '$msg_desc', '$msg_link', $msg_type, now(), 0)");
					
	header("Location: ../inbox?success=3");
} else {
	header("Location: ../compose?err=3");
}
?>