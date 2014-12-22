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

	// ======================
	
	$children = array();
	$user_type = userType($mysqli, $user_id);
	
	if ($user_type != 2) {
		$children = findTrainees($mysqli, $user_id);
		// Security check: Is user allowed to access this trainee's data?
		if ($user_type != "admin" && !in_array($id_user, $children)) {
			printf("Invalid permissions.\n");
			exit();
		}
	}
	
	$num_unread = getNumberUnreadMessages($mysqli, $user_id);
	$users = allUserLookup($mysqli);
?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $username; ?> Settings</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
  </head>

  <body>
	<?php
		if (isset($_GET['success'])) {
	?>
		<div class="alert alert-success alert-dismissable box-width" style="position:fixed; width:400px; left:50%; top:-4px;margin-left:-200px; z-index:10001">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<strong>Success!</strong> Your password has been changed.
		</div>
	<?php
		}
		
		if (isset($_GET['err'])) {
			$decoded = urldecode($_GET['err']);
	?>
		<div class="alert alert-danger alert-dismissable box-width" style="position:fixed; width:300px; left:50%; top:-4px;margin-left:-150px; z-index:10001">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<strong>Error</strong> <?php echo $decoded; ?>
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
            <h1>Settings</h1>
          </div>
        </div>
        <div class="row">
          <div class="col-lg-6">
            <div class="panel panel-default">
              <div class="panel-heading">
                <h3 class="panel-title">Change Password</h3>
              </div>
              <div class="panel-body">
                <form method="post" action="includes/change_password.php">
					<div class="form-group">
    					<label for="oldPassword">Old Password</label>
						<input id="oldPassword" class="form-control" name="oldPassword" type="password" placeholder="" minlength="4" >
					</div>
					<div class="form-group">
    					<label for="password">New Password</label>
						<input class="form-control" id="password" name="password" type="password" placeholder="" minlength="4" >
					</div>
					<div class="form-group">
    					<label for="confirmpwd">Confirm New Password</label>
						<input class="form-control" id="confirmpwd" name="confirmpwd" type="password" placeholder="" minlength="4" >
					</div>
					<p> <input type="button" 
						class="btn btn-primary" 
						value="Update" 
						onclick="return hashFormChangePassword(this.form,
                                   this.form.oldPassword,
                                   this.form.password,
                                   this.form.confirmpwd);" /> </p>
				</form>
              </div>
            </div>
          </div>
      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>
    
	<script src="js/sha512.js"></script> 
	<script src="js/forms.js"></script> 
	
  </body>
</html>