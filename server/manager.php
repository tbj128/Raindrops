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
	
if ($username != $admin_user) {
	header("Location: index");
}

// ===============
	
$id = isset($_GET['id']) ? (int)$_GET['id'] : 0;

$num_unread = getNumberUnreadMessages($mysqli, $user_id);
?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Raindrops Content Manager</title>

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
			color:#747474;
			background:#F5FFEC;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-item:hover {
			background:#E5FFCD !important;
		}
		
		.edit-menu {
			color:#747474;
			background:#EAFAFF;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-menu:hover {
			background:#D5F4FD !important;
		}
		
		.edit-add-new {
			color:#747474;
			background:#EEE;
			padding-top:20px;
			padding-bottom:20px;
		}
		
		.edit-add-new:hover {
			background:#E2E2E2 !important;
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
  
	<?php
		if (isset($_GET['success'])) {
	?>
		<div class="alert alert-success alert-dismissable box-width" style="position:absolute; left:50%; top:40px;margin-left:-200px; z-index:10001">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<strong>Success!</strong> Your changes have been successfully saved.
		</div>
	<?php
		}
	?>
  
	<div id="contextMenu" class="dropdown clearfix">
		<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu" style="display:block;position:static;margin-bottom:5px;">
			<li><a tabindex="-1" href="#">View</a></li>
			<li><a tabindex="-1" href="#">Edit</a></li>
			<li class="divider"></li>
			<li><a tabindex="-1" href="#">Delete</a></li>
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
          <div class="col-lg-12">
            <h1>Content Manager <small>Add/Edit/Delete Content</small></h1>
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-12">
			<ol id="breadcrumb-menu" class="breadcrumb">
              <li class="active"><i class="fa fa-list"></i> Main</li>
            </ol>
			<div id="alert-no-items" class="alert alert-info alert-dismissable" style="display:none;">
              <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
			  <strong>This folder has no items yet.</strong> Click "Add New Item" to add a new item into this folder.
            </div>
			<?php 
				if (isset($_GET['p'])) {
					echo renderEditWindow($xml, 1, 'root', 'Main', $_GET['p']);
				} else {
					echo renderEditWindow($xml, 1, 'root', 'Main', '');
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
				var itemType = $rowClicked.data("type");
				var idToDelete = $rowClicked.data("id");
				var dataPath = $rowClicked.data("path");
				
				if ($(this).text() == "View") {
					if (itemType == 0) {
						// Menu Item
						alert("Navigate to this item using the menu on the left");
					} else {
						$contextMenu.hide();
						document.location.href = 'manager_view.php?id=' + idToDelete;
					}
				} else if ($(this).text() == "Edit") {
					if (itemType == 0) {
						$contextMenu.hide();
						document.location.href = 'manager_edit_menu.php?id=' + idToDelete + '&p=' + dataPath;
					} else if (itemType == 1) {
						// Video Item
						$contextMenu.hide();
						document.location.href = 'manager_edit_video.php?id=' + idToDelete + '&p=' + dataPath;
					} else if (itemType == 2) {
						$contextMenu.hide();
						document.location.href = 'manager_edit_richtext.php?id=' + idToDelete + '&p=' + dataPath;
					}
				} else if ($(this).text() == "Delete") {
					$contextMenu.hide();
					var confirmationDelete = confirm('Are you sure you want to delete this item?');
					if (confirmationDelete) {
						document.location.href = 'includes/delete.php?id=' + idToDelete + '&p=' + dataPath;
					}
				}
			});

			$(document).click(function () {
				$contextMenu.hide();
			});
		});

	</script>


  </body>
</html>
