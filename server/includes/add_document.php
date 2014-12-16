<?php

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
if (isset($_POST['document_parent_id'])
	&& isset($_POST['document_name'])
	&& isset($_POST['document_requires'])
	&& isset($_POST['document_desc'])
	&& isset($_POST['document_text'])) {

	$parentid = $_POST['document_parent_id'];
	$name = $_POST['document_name'];
	$name = cleanName($name);
	$requires = $_POST['document_requires'];
	$desc = $_POST['document_desc'];
	
	$is_activity = 'false';
	if (isset($_POST['document_is_activity'])) {
		$is_activity = 'true';
	}
	$id = generateRandomString();
	
	foreach($xpath->query('//menu[@id=\'' . $parentid . '\']') as $node) {
	  $childNode = $doc->createElement("item");
	  $childNode->setAttribute('id', $id);
	  $childNode->setAttribute('type', '2');
	  $childNode->setAttribute('activity', $is_activity);
	  $childNode->setAttribute('path', $id . '.html');
	  $childNode->setAttribute('requires', $requires);
	  $childNode->setAttribute('desc', $desc);
	  $childNode->nodeValue = $name;
	  $node->appendChild($childNode);
	}

	$doc->save("../content/menu.xml");
	
	$fileName = '../content/media/' . $id . '.html';
	$fh = fopen($fileName, 'w') or die("can't open file");
	$data = $_POST['document_text'];
	fwrite($fh, $data);
	fclose($fh);

	header("Location: ../manager.php?success=1&p=$p");
} else {
	$id = $_POST['video_parent_id'];
	header("Location: ../manager_add_richtext.php?id=$id&err=1");
}

?>