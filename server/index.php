<?php

	$php_config = 'config.php';
	
	if (!file_exists($php_config)) {
		header("Location: setup.php");
	}
	
	include_once 'config.php';
	include_once 'includes/functions.php';

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		header("Location: setup.php");
	}
	
	sec_session_start();
	 
	if (!login_check($mysqli)) {
		header("Location: login");
	}
	
	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];

	
	if ($username == $admin_user) {
		if (isset($_GET['welcome'])) {
			header("Location: admin?welcome=1");
		} else {
			header("Location: admin");
		}
	}
	
	$user_type = userType($mysqli, $user_id);
	if ($user_type == 2) {
		// This is a trainee, direct them to the statistics page
		header("Location: user.php?id=$user_id");
	}
	
	// ======================
	
	$page = 'Dashboard';
	$num_unread = getNumberUnreadMessages($mysqli, $user_id);
	$children = findTrainees($mysqli, $user_id);
	$users = allUserLookup($mysqli);

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

	<div id="contextMenu" class="dropdown clearfix">
		<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu" style="display:block;position:static;margin-bottom:5px;">
			<li><a id="delete-trainee" tabindex="-1" href="#">Delete Trainee</a></li>
		</ul>
	</div>
	
    <div id="wrapper">

      <!-- Sidebar -->
	  <?php
		include 'component_nav.php';
	  ?>

      <div id="page-wrapper">

        <div class="row">
          <div class="col-lg-12">
            <h1><?php echo $username; ?> <small> Dashboard</small></h1>
          </div>
        </div><!-- /.row -->

        <div class="row">
		  <div class="col-lg-6">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-user"></i>  My Trainees</h3>
              </div>
              <div class="panel-body">
                <div class="list-group">
				  <?php
						foreach ($children as $trainee) {
							$traineeName = userLookup($mysqli, $trainee);
							echo '<a href="user?id=' . $trainee . '" class="list-group-item trainee-item" data-id="' . $trainee . '" data-name="' . $traineeName . '">
									<i class="fa fa-user"></i>&nbsp;&nbsp;' . $traineeName . '
								  </a>';
						}
				  ?>
				  <a href="register?p=<?php echo $user_id; ?>" class="list-group-item">
					<i class="fa fa-plus"></i>&nbsp;&nbsp;New Trainee
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
								echo '<a href="inbox?msg=' . $message['id'] . '" class="list-group-item">
										<span class="badge">' . $message["msg_date"] . '</span>
										<i class="fa fa-comment"></i> ' . $users[$message["id_from"]] . ' sent you a message
									  </a>';
							} else if ($message["msg_type"] == 1) {
								echo '<a href="inbox?msg=' . $message['id'] . '" class="list-group-item">
										<span class="badge">' . $message["msg_date"] . '</span>
										<i class="fa fa-volume-up"></i> ' .$users[$message["id_from"]] . ' sent you a message
									  </a>';
							} else {
								echo '<a href="inbox?msg=' . $message['id'] . '" class="list-group-item">
										<span class="badge">' . $message["msg_date"] . '</span>
										<i class="fa fa-film"></i> ' . $users[$message["id_from"]] . ' sent you a message
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

	<script>
		var clickedTraineeName;
		var clickedTraineeID;
		
		$("body").on("contextmenu", ".trainee-item", function (e) {
			clickedTraineeID = $(this).data('id');
			clickedTraineeName = $(this).data('name');
			$('#contextMenu').css({
				display: "block",
				left: e.pageX,
				top: e.pageY
			});
			return false;
		});
		
		$('#delete-trainee').click(function() {
			_openWarningPopup(clickedTraineeID, clickedTraineeName);
		});
		
		$('body').click(function () {
			$('#contextMenu').hide();
		});
		
		function _openWarningPopup(traineeID, traineeName) {
			var confirmPopupHTML = '<div id="whiteout"></div>\
							<div id="warning-popup" class="thumbnail warning-popup">\
								<div class="caption">\
									<h4>Delete ' + traineeName + '?</h4><br />\
									<p><strong>Warning!</strong> This will delete any data associated with this account</p>\
									<p>';
		
			confirmPopupHTML += '<a id="delete-confirm" href="#" class="btn btn-primary" role="button">Delete ' + traineeName + '</a><br />';
			confirmPopupHTML += '<a id="delete-cancel" href="#" class="btn btn-default" role="button">Cancel</a>\
							</p>\
						</div>\
					</div>';
		
			if ($('#whiteout').length <= 0) {
				$('body').append(confirmPopupHTML);
			}
			
			$('#delete-confirm').click(function() {
				$('#whiteout').remove();
				$('#warning-popup').remove();
				window.location.href = 'delete_trainee.php?id=' + traineeID;
			});
			$('#delete-cancel').click(function() {
				$('#whiteout').remove();
				$('#warning-popup').remove();
			});
		}
	</script>

  </body>
</html>