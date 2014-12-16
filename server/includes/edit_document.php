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
if (isset($_POST['document_id'])
	&& isset($_POST['document_name'])
	&& isset($_POST['document_requires'])
	&& isset($_POST['document_desc'])
	&& isset($_POST['document_text'])) {

	$id = $_POST['document_id'];
	$name = $_POST['document_name'];
	$name = cleanName($name);
	$requires = $_POST['document_requires'];
	$desc = $_POST['document_desc'];
	$source = $_POST['document_source'];
	
	$is_activity = 'false';
	if (isset($_POST['document_is_activity'])) {
		$is_activity = 'true';
	}
	
	$isEdited = false;
	foreach($xpath->query('//item[@id=\'' . $id . '\']') as $node) {
	  $origSource = $node->getAttribute('path');
	  $node->setAttribute('type', '2');
	  $node->setAttribute('activity', $is_activity);
	  $node->setAttribute('path', $id . '.html');
	  $node->setAttribute('requires', $requires);
	  $node->setAttribute('desc', $desc);
	  $node->nodeValue = $name;
	}

	$doc->save("../content/menu.xml");
	
	$fileName = '../content/media/' . $id . '.html';
	$fh = fopen($fileName, 'w') or die("can't open file");
	$data = $_POST['document_text'];
	fwrite($fh, $data);
	fclose($fh);

	header("Location: ../manager.php?success=1&p=$p");
} else {
	$id = $_POST['document_id'];
	header("Location: ../manager_edit_richtext.php?id=$id&err=1");
}

?>