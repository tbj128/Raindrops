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

	// ==== Page-specific PHP ========

$id = isset($_GET['id']) ? $_GET['id'] : 0;
$path = isset($_GET['p']) ? $_GET['p'] : 0;

$num_unread = getNumberUnreadMessages($mysqli, $user_id);
?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Raindrops Dashboard</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
    <!-- Page Specific CSS -->
    <link rel="stylesheet" href="http://cdn.oesmith.co.uk/morris-0.4.3.min.css">
	
	<style>

		.spacer-right {
			margin-right:15px;
		}

		.leftbar-menu {
			color:#f4f4f4;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-item {
			color:#999999;
			background:#F5FFEC;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-menu {
			color:#999999;
			background:#EAFAFF;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-add-new {
			color:#999999;
			background:#EEEAAA;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-window {
			margin: 10px 0;
		}
		
		.edit-window h4 {
			background:#FFF;
			border:1px solid #F4F4F4;
			margin:0px;
			padding:10px 0 10px 10px;
			color:#619BAA;
		}
		
		.selected {
			color:#C3E3FF;
		}
		
		#contextMenu {
			position: absolute;
			display:none;
			z-index:9999;
		}
		
		.wrapper-none;

	</style>
  </head>

  <body>

	  <div id="contextMenu" class="dropdown clearfix">
		<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu" style="display:block;position:static;margin-bottom:5px;">
			<li><a tabindex="-1" href="#">View</a></li>
			<li><a tabindex="-1" href="#">Edit</a></li>
			<li class="divider"></li>
			<li><a tabindex="-1" href="#">Delete</a></li>
		</ul>
	</div>

    <div>

      <!-- Sidebar -->
      <nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="index">Raindrops</a>
        </div>


        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse navbar-ex1-collapse">
          <ul class="nav navbar-nav navbar-right navbar-user">
            <li class="dropdown messages-dropdown">
              <a href="inbox" class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-envelope"></i> Messages <span class="badge"><?php echo $num_unread; ?></span></a>
              
            <li class="dropdown user-dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-user"></i> <?php echo $username; ?> <b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li><a href="inbox"><i class="fa fa-envelope"></i> Inbox <span class="badge"><?php echo $num_unread; ?></span></a></li>
				<li class="divider"></li>
                <li><a href="logout"><i class="fa fa-power-off"></i> Log Out</a></li>
              </ul>
            </li>
          </ul>
        </div><!-- /.navbar-collapse -->
      </nav>

      <div id="page-wrapper">

        <div class="row">
          <div class="col-lg-6 center">
            <h1>Add New Item <small></small></h1>
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-6 center">
			<div id="alert-no-items" class="alert alert-info">
			  <strong>What would you like to add?</strong> Choose an item type below.
            </div>
			
			<ul id="root" class="nav nav-list edit-window">
			<li class="nav-header left table-bordered">
				<a class="edit-menu" href="manager_add_menu.php?id=<?php echo $id; ?>&p=<?php echo $path; ?>">
					<span class="fa fa-list spacer-right"></span> Subfolder
				</a>
			</li>
			<li class="nav-header left table-bordered">
				<a class="edit-item" href="manager_add_video.php?id=<?php echo $id; ?>&p=<?php echo $path; ?>">
					<span class="fa fa-film spacer-right"></span> Video
				</a>
			</li>
			<li class="nav-header left table-bordered">
				<a class="edit-item" href="manager_add_richtext.php?id=<?php echo $id; ?>&p=<?php echo $path; ?>">
					<span class="fa fa-pencil spacer-right"></span> Document
				</a>
			</li>
          </div>
        </div><!-- /.row -->



      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>

    <!-- Page Specific Plugins -->
    <script src="js/morris/chart-data-morris.js"></script>
    <script src="js/tablesorter/jquery.tablesorter.js"></script>
    <script src="js/tablesorter/tables.js"></script>
	
  </body>
</html>

