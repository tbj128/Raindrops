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
		header("Location: login.php");
	}
	
	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];
	// ======================
	
	$page = 'Outbox';
	$num_unread = getNumberUnreadMessages($mysqli, $user_id);
	$children = findTrainees($mysqli, $user_id);
	$users = allUserLookup($mysqli);
	
	$msg_id = -1;
	if (isset($_GET['msg'])) {
		if (is_numeric($_GET['msg'])) {
			$msg_id = $_GET['msg'];
		}
	}
	
	$message = 0;
	$messages = getOutbox($mysqli, $user_id);
	$list_messages = '';

	if ($msg_id >= 0) {
		$message = getMessage($mysqli, $user_id, $msg_id, false);
	} else {
		foreach($messages as $message) {
			$is_read = true; // Outbox items will always be considered read 
			// if ($message['msg_read'] != 0) {
				// $is_read = true;
			// }
			if ($message["msg_type"] == 0) {
				// Normal written message
				if ($is_read) {
					$list_messages .= '<a href="outbox?msg=' . $message['id'] . '" class="list-group-item" style="background-color:#F3F3F3;">
						<span class="badge">' . formatDate($message["msg_date"]) . '</span>';
					$list_messages .= '<i class="fa fa-comment"></i>&nbsp;&nbsp;' . $users[$message["id_to"]] . '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' . $message['msg_title'] . ' - <span style="color:#999;">' . substr($message['msg_content'], 0, 30) . '</span>
					  </a>';
				} else {
					$list_messages .= '<a href="outbox?msg=' . $message['id'] . '" class="list-group-item">
						<span class="badge">' . formatDate($message["msg_date"]) . '</span>';
					$list_messages .= '<i class="fa fa-comment"></i>&nbsp;&nbsp;<strong>' . $users[$message["id_to"]] . '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' . $message['msg_title'] . ' - <span style="color:#999;">' . substr($message['msg_content'], 0, 30) . '</span></strong>
					  </a>';
				}
			} else if ($message["msg_type"] == 1) {
				// Audio message
				if ($is_read) {
					$list_messages .= '<a href="outbox?msg=' . $message['id'] . '" class="list-group-item" style="background-color:#F3F3F3;">
						<span class="badge">' . formatDate($message["msg_date"]) . '</span>';
					$list_messages .= '<i class="fa fa-volume-up"></i>&nbsp;&nbsp;' . $users[$message["id_to"]] . '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' . $message['msg_title'] . ' - <span style="color:#999;">' . substr($message['msg_content'], 0, 30) . '</span>
					  </a>';
				} else {
					$list_messages .= '<a href="outbox?msg=' . $message['id'] . '" class="list-group-item">
						<span class="badge">' . formatDate($message["msg_date"]) . '</span>';
					$list_messages .= '<i class="fa fa-volume-up"></i>&nbsp;&nbsp;<strong>' . $users[$message["id_to"]] . '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' . $message['msg_title'] . ' - <span style="color:#999;">' . substr($message['msg_content'], 0, 30) . '</span></strong>
					  </a>';
				}
			} else {
				// Video message
				if ($is_read) {
					$list_messages .= '<a href="outbox?msg=' . $message['id'] . '" class="list-group-item" style="background-color:#F3F3F3;">
						<span class="badge">' . formatDate($message["msg_date"]) . '</span>';
					$list_messages .= '<i class="fa fa-film"></i>&nbsp;&nbsp;' . $users[$message["id_to"]] . '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' . $message['msg_title'] . ' - <span style="color:#999;">' . substr($message['msg_content'], 0, 30) . '</span>
					  </a>';
				} else {
					$list_messages .= '<a href="outbox?msg=' . $message['id'] . '" class="list-group-item">
						<span class="badge">' . formatDate($message["msg_date"]) . '</span>';
					$list_messages .= '<i class="fa fa-film"></i>&nbsp;&nbsp;<strong>' . $users[$message["id_to"]] . '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' . $message['msg_title'] . ' - <span style="color:#999;">' . substr($message['msg_content'], 0, 30) . '</span></strong>
					  </a>';
				}
			}
		}
	}
	

?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $page; ?></title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
  </head>

  <body>

    <div id="wrapper">

      <!-- Sidebar -->
      <?php
		include 'component_nav.php';
	  ?>

      <div id="page-wrapper">

        <div class="row">
			<div class="col-lg-12">
				<?php
					if ($msg_id >= 0) {
						if ($message) {
							$type = "";
							if (false !== strpos($message['msg_link'],'.3gp')) {
								$type = "audio/3gpp";
							} else if (false !== strpos($message['msg_link'],'.wav')) {
								$type = "audio/wav";
							} else if (false !== strpos($message['msg_link'],'.mp4')) {
								$type = "video/mp4";
							}
							
							echo '<h1><span style="float:left;">' . $message['msg_title'] . '</span><small class="pull-right small-date">' . formatDate($message["msg_date"]) . '</small></h1><div style="clear:both;"></div>';
							echo '<h2 style="margin-top:0px;"><small>to ' . $users[$message['id_to']] . '</small></h2><hr />';
							echo '<p>' . $message['msg_content'] . '</p>';

							if ($message['msg_type'] == 1) {
								echo "<p style='color:#999;'>Can't play the message below? Download to your computer <a href='remote_access_messages.php?id=" . $message['id_to'] . "&inbox=0&file=" . $message['msg_link'] . "'>here</a></p>";
								echo '<audio controls>
								  <source src="remote_access_messages.php?id=' . $message['id_to'] . '&inbox=0&file=' . $message['msg_link'] . '" type="' . $type . '">
									Your browser does not support HTML5 audio.
								</audio>';
							} else if ($message['msg_type'] == 2) {
								echo "<p style='color:#999;'>Can't play the video below? Download to your computer <a href='remote_access_messages.php?id=" . $message['id_to'] . "&inbox=0&file=" . $message['msg_link'] . "'>here</a></p>";
								echo '<video width="600" height="400" controls>
								  <source src="remote_access_messages.php?id=' . $message['id_to'] . '&inbox=0&file=' . $message['msg_link'] . '" type="' . $type . '">
									Your browser does not support HTML5 video.
								</video>';
							}
						} else {
							// No found message
							// TODO
						}
					} else {
						echo '<h1>Outbox </h1>
								<div class="list-group">' . $list_messages . '</div>';
					}
				?>
			</div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-12">
				
          </div>
        </div><!-- /.row -->

      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>

    <!-- Page Specific Plugins -->
    <script src="//cdnjs.cloudflare.com/ajax/libs/raphael/2.1.0/raphael-min.js"></script>
    <script src="js/tablesorter/jquery.tablesorter.js"></script>
    <script src="js/tablesorter/tables.js"></script>


  </body>
</html>
