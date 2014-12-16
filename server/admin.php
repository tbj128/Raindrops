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
	// ======================
	
	$page = 'Dashboard';
	$num_unread = getNumberUnreadMessages($mysqli, $user_id);
	$children = findTrainees($mysqli, $user_id);
	$users = allUserLookup($mysqli);
	$welcome = false;
	if (isset($_GET['welcome'])) {
		$welcome = true;
	}
?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $app_name; ?> Dashboard</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
  </head>

  <body>

	<?php
		if ($welcome) {
	?>
		<div class="alert alert-success alert-dismissable box-width" style="position:absolute; left:50%; top:40px;margin-left:-400px; z-index:10001">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<strong>Welcome!</strong> Now that you're all setup, it's time to start building some content for your users to interact with. <a href="manager">Go to your content manager.</a>
		</div>
	<?php
		}
	?>
	
    <div id="wrapper">

      <!-- Sidebar -->
	  <?php
		include 'component_nav.php';
	  ?>

      <div id="page-wrapper">

        <div class="row">
          <div class="col-lg-12">
            <h1><?php echo $username; ?> <small> Admin Dashboard</small></h1>
          </div>
        </div><!-- /.row -->

        <div class="row">
		  <div class="col-lg-6">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-user"></i>&nbsp;&nbsp;All Trainers</h3>
              </div>
              <div class="panel-body">
				<div class="alert alert-info">
				  <strong>Note </strong> Trainers interact with trainees and monitor their progress over time.
				</div>
                <div class="list-group">
				  <?php
						foreach ($children as $trainer) {
							echo '<a href="user?id=' . $trainer . '" class="list-group-item">
									<i class="fa fa-user"></i>&nbsp;&nbsp;' . userLookup($mysqli, $trainer) . '
								  </a>';
						}
				  ?>
				  <a href="register_trainer" class="list-group-item">
					<i class="fa fa-plus"></i>&nbsp;&nbsp;New Trainer
				  </a>
                </div>
              </div>
            </div>
          </div>
          <div class="col-lg-6">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-envelope-o"></i>  Recent Messages</h3>
              </div>
              <div class="panel-body">
                <div class="list-group">
				  <?php
						$messages = getInbox($mysqli, $user_id);
						$message_preview_counter = 0;
						foreach($messages as $message) {
							if ($message_preview_counter >= 10) {
								break;
							} else {
								$message_preview_counter++;
							}
							if ($message["msg_type"] == 0) {
								echo '<a href="#" class="list-group-item">
										<span class="badge">' . $message["msg_date"] . '</span>
										<i class="fa fa-comment"></i> ' . userLookup($mysqli, $message["id_from"]) . ' sent you a message
									  </a>';
							} else if ($message["msg_type"] == 1) {
								echo '<a href="#" class="list-group-item">
										<span class="badge">' . $message["msg_date"] . '</span>
										<i class="fa fa-volume-up"></i> ' . userLookup($mysqli, $message["id_from"]) . ' sent you a message
									  </a>';
							} else {
								echo '<a href="#" class="list-group-item">
										<span class="badge">' . $message["msg_date"] . '</span>
										<i class="fa fa-film"></i> ' . userLookup($mysqli, $message["id_from"]) . ' sent you a message
									  </a>';
							}
						}
				  ?>
                </div>
                <div class="text-right">
                  <a href="inbox">View All Messages <i class="fa fa-arrow-circle-right"></i></a>
                </div>
              </div>
            </div>
          </div>
        </div><!-- /.row -->

      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>

    <!-- Page Specific Plugins -->
    <script src="js/tablesorter/jquery.tablesorter.js"></script>
    <script src="js/tablesorter/tables.js"></script>

  </body>
</html>