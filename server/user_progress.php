<?php

include 'includes/parser_XML_menu.php';
$xml = simplexml_load_file("content/menu.xml");

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

$id_user = isset($_GET['id']) ? (int)$_GET['id'] : 0;
$name_user = userLookup($mysqli, $id_user);
$permissions = findPermissions($mysqli, $id_user);
$num_unread = getNumberUnreadMessages($mysqli, $user_id);

$user_type = userType($mysqli, $user_id);
$target_user_type = userType($mysqli, $id_user);
if ($user_type != 2) {
	$children = findTrainees($mysqli, $user_id);
	// Security check: Is user allowed to access this trainee's data?
	if ($user_type != "admin" && !in_array($id_user, $children)) {
		printf("Invalid permissions.\n");
		exit();
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

    <title>User Progress</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
	
	<style>

		.spacer-right {
			margin-right:15px;
		}

		.leftbar-menu {
			color:#333333;
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
			color:#4AABFF;
		}
		
		#contextMenu {
			position: absolute;
			display:none;
			z-index:9999;
		}
		

	</style>
  </head>

  <body>

	<div id="contextMenu" class="dropdown clearfix">
		<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu" style="display:block;position:static;margin-bottom:5px;">
			<!--<li class="divider"></li>-->
			<li><a tabindex="0" href="#">Unlock Item</a></li>
			<li><a tabindex="1" href="#">Lock Item</a></li>
		</ul>
	</div>

    <div id="wrapper-large">

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
		  
		  <ul class="nav navbar-nav side-nav-large" style="top:51px;background-color:#fff;">
			<div class="alert alert-warning" style="padding:10px 15px;margin:10px;"><strong>Navigation</strong> Expand the items below</div>
			<?php echo renderSidebar($xml, 0, 'root', 'Main'); ?>
		  </ul>

          <ul class="nav navbar-nav navbar-right navbar-user">
            <li class="dropdown messages-dropdown">
              <a href="inbox"><i class="fa fa-envelope"></i> Messages <span class="badge"><?php echo $num_unread; ?></span></a>
              
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
          <div class="col-lg-12">
            <h1><?php echo $name_user; ?>'s progress <small></small></h1>
			<div class="alert alert-info"><strong>Permissions</strong> In addition to seeing which items have been completed, you can also lock and unlock items for this user</div>
			
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-12">
			<ol id="breadcrumb-menu" class="breadcrumb">
              <li class="active"><i class="fa fa-list"></i> Main</li>
            </ol>
			<div id="alert-no-items" class="alert alert-info alert-dismissable" style="display:none;">
              <button type="button" class="close" data-dismiss="alert" aria-hidden="true">�</button>
			  <strong>This folder has no items yet.</strong> Click "Add New Item" to add a new item into this folder.
            </div>
			<?php 
				if (isset($_GET['p'])) {
					echo renderProgressWindow($xml, 1, 'root', 'Main', $permissions, $_GET['p']);
				} else {
					echo renderProgressWindow($xml, 1, 'root', 'Main', $permissions, '');
				}
			?>
          </div>
        </div><!-- /.row -->



      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>

    <script src="js/bootstrap.js"></script>

    <!-- Page Specific Plugins -->
	
	<!-- Custom scripts -->
	<script src="js/main.js"></script>
	<script>
		$(document).ready(function() {
			EpicWheels.Main.init();
					
			// Show the specified elements.
			<?php 
				if (isset($_GET['p'])) {
					$pathItems = explode(',', $_GET['p']);
					echo 'var pathToOpen = [';
					$i=0;
					foreach ($pathItems as $pathItem) {
						if (++$i != count($pathItems)) {
							echo '"' . $pathItem . '",';
						} else {
							echo '"' . $pathItem . '"';
						}
					}
					echo '];';
					echo '
						if (pathToOpen.length > 0) {
							for (var i = 0; i < pathToOpen.length; i++) {
								if (i < pathToOpen.length) {
									$("ul.left ul#s-" + pathToOpen[i]).show();
								}
								
								if (i == (pathToOpen.length - 1)) {
									$(\'ul.left ul#s-\' + pathToOpen[i]).parent().find(\'> a\').addClass("selected");
								}
							}
						}
					';
				}
			?>
			
			var $contextMenu = $("#contextMenu");
			var $rowClicked;

			$("body").on("contextmenu", ".edit-menu, .edit-item", function (e) {
				$rowClicked = $(this);
				$contextMenu.css({
					display: "block",
					left: e.pageX,
					top: e.pageY
				});
				return false;
			});

			$contextMenu.on("click", "a", function () {
				if ($(this).text() == "Unlock Item") {
					$contextMenu.hide();
					var cid = $rowClicked.data("id");
					var dataPath = $rowClicked.data("path");
					document.location.href = 'includes/set_permission.php?uid=<?php echo $id_user ?>&cid=' + cid + '&lock=0' + '&p=' + dataPath;
				} else {
					$contextMenu.hide();
					var cid = $rowClicked.data("id");
					var dataPath = $rowClicked.data("path");
					document.location.href = 'includes/set_permission.php?uid=<?php echo $id_user ?>&cid=' + cid + '&lock=1' + '&p=' + dataPath;
				}
			});

			$(document).click(function () {
				$contextMenu.hide();
			});
		});

	</script>


  </body>
</html>
