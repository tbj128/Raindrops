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
	
	if ($username != $admin_user) {
		header("Location: index");
	}

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	// ==== Page-specific PHP ========
	include 'includes/parser_XML_menu.php';
	$xml = simplexml_load_file("content/menu.xml");

	$id = 0;
	if (isset($_GET['id'])) {
		$id = $_GET['id'];
	}


?>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $app_name; ?></title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-setup.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">

  </head>

  <body>

    <div id="wrapper">

		<div class="jumbotron box-width align-center">

		  <?php
				$item = getItem($xml, 1, 'root', 'Main', $id);
				if ($item) {
					echo '<h1><span style="float:left;">' . $item['name'] . '</span></h1><div style="clear:both;"></div>';
					echo "<p></p><p style='color:#999;'>" . $item['desc'] . "</p>";
					
					$type = "";
					if ($item['type'] == 1) {
						// Video Item
						if (false !== strpos($item['path'],'3gp')) {
							$type = "audio/3gpp";
						} else if (false !== strpos($item['path'],'wav')) {
							$type = "audio/wav";
						} else if (false !== strpos($item['path'],'mp4')) {
							$type = "video/mp4";
						}
						
						echo "<p style='color:#999;font-size:14px;'>Can't play the video below? Download to your computer <a href='content/media/" . $item['path'] . "'>here</a></p>";
						echo '<video style="width:660px; height: 400px;" controls>
								  <source src="content/media/' . $item['path'] . '" type="' . $type . '">
									Your browser does not support HTML5 video.
							  </video>';
					} else if ($item['type'] == 2) {
						// Rich Text Item
						echo '<iframe class="container well well-small"
									   style="width:660px; height: 400px; background-color: #fff;"
									   src="content/media/' . $item['path'] . '">
							   </iframe>';
					}
					
				}
			?>
		
		</div>

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
	<script src="js/jquery-1.7.2.js"></script>
    <script src="js/bootstrap.js"></script>

  </body>
</html>