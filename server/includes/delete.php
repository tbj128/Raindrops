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

$doc = new DOMDOcument;
$doc->load("../content/menu.xml");

$xpath = new DOMXpath($doc);

$p = '';
if (isset($_GET['p'])) {
	$p = $_GET['p'];
}

$id = $_GET['id'];

foreach($xpath->query('//menu[@id=\'' . $id . '\']') as $node) {
  if ($node->hasChildNodes()) {
  	// Directory is NOT empty. Disallow delete.
	header("Location: ../manager.php?err=2");
	exit;
  } else {
	$node->parentNode->removeChild($node);
  }
}

$datapath = '';
foreach($xpath->query('//item[@id=\'' . $id . '\']') as $node) {
  $datapath = $node->getAttribute("path");
  $node->parentNode->removeChild($node);
}

$doc->save("../content/menu.xml");

if ($datapath != '') {
	$outputFolder = '../content/media/';
	$fullDataPath = $outputFolder . $datapath;
	unlink($fullDataPath);
}

header("Location: ../manager.php?success=1&p=$p");
?>