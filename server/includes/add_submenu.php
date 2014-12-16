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

if (isset($_POST['menu_parent_id'])
	&& isset($_POST['menu_name'])
	&& isset($_POST['menu_requires'])
	&& isset($_POST['menu_desc'])) {

	$parentid = $_POST['menu_parent_id'];
	$name = $_POST['menu_name'];
	$name = cleanName($name);
	$requires = $_POST['menu_requires'];
	$desc = $_POST['menu_desc'];

	$id = generateRandomString();
	
	foreach($xpath->query('//menu[@id=\'' . $parentid . '\']') as $node) {
	  $childNode = $doc->createElement("menu");
	  $childNode->setAttribute('id', $id);
	  $childNode->setAttribute('name', $name);
	  $childNode->setAttribute('requires', $requires);
	  $childNode->setAttribute('desc', $desc);
	  $node->appendChild($childNode);
	}

	$doc->save("../content/menu.xml");
	header("Location: ../manager.php?success=1&p=$p");
} else {
	$id = $_POST['menu_parent_id'];
	header("Location: ../manager_add_menu.php?id=$id&err=1");
}

?>