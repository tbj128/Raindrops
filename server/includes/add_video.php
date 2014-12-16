<?php

// Adds a video item to the menu XML and menu folder structure
// @author: Tom Jin
// @date: May 30, 2013

$php_config = '../config.php';

if (!file_exists($php_config)) {
	header("Location: ../setup.php");
}

include_once '../config.php';
include_once '../includes/functions.php';

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

if ($username != $admin_user) {
	header("Location: ../index");
}

// ==== Page-specific PHP ========


include_once 'content_functions.php';

$doc = new DOMDOcument;
$doc->load("../content/menu.xml");

$xpath = new DOMXpath($doc);
$p = '';
if (isset($_GET['p'])) {
	$p = $_GET['p'];
}
if (isset($_POST['video_parent_id'])
	&& isset($_POST['video_name'])
	&& isset($_POST['video_requires'])
	&& isset($_POST['video_desc'])
	&& isset($_POST['video_path'])) {

	$parentid = $_POST['video_parent_id'];
	$name = $_POST['video_name'];
	$name = cleanName($name);
	$requires = $_POST['video_requires'];
	$desc = $_POST['video_desc'];
	$fileName = $_POST['video_path'];
	$extension = substr(strrchr($fileName,'.'), 1);
	$is_activity = 'false';
	if (isset($_POST['video_is_activity'])) {
		$is_activity = 'true';
	}
	$id = generateRandomString();
	
	foreach($xpath->query('//menu[@id=\'' . $parentid . '\']') as $node) {
	  $childNode = $doc->createElement("item");
	  $childNode->setAttribute('id', $id);
	  $childNode->setAttribute('type', '1');
	  $childNode->setAttribute('activity', $is_activity);
	  $childNode->setAttribute('path', $id . '.' . $extension);
	  $childNode->setAttribute('requires', $requires);
	  $childNode->setAttribute('desc', $desc);
	  $childNode->nodeValue = $name;
	  $node->appendChild($childNode);
	}

	$doc->save("../content/menu.xml");

	$outputFolder = '../content/media/';
	rename('../upload/files/' . $fileName, $outputFolder . $id . '.' . $extension);
	header("Location: ../manager.php?success=1&p=$p");
} else {
	$id = $_POST['video_parent_id'];
	header("Location: ../manager_add_video.php?id=$id&err=1");
}

?>