<?php

	$php_config = 'config.php';
	$alert_show = false;
	
	if (!file_exists($php_config)) {
		header("Location: setup.php");
	} else {
		include 'config.php';
	}
	
	include_once 'config.php';
	include_once 'includes/functions.php';


	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}

	if (isset($_GET['error'])) {
		$alert_show = true;
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
?>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $app_name; ?> - Create New Account</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-setup.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">

  </head>

  <body>

    <div id="wrapper">
		<?php
			if ($alert_show) {
		?>
		<div class="alert alert-warning alert-dismissable box-width align-center-top">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<strong>Oops!</strong> 
		</div>
		<?php
			}
		?>
		
		<div class="jumbotron box-width align-center">
		  <h1>Register</h1>
		  <h3>Create a new trainer account.</h3><h4>Trainers communicate and monitor the progress of trainees.</h4><br />
		  
				<form method="post" action="includes/process_trainer.php">
					<div class="form-group">
						<input class="form-control" name="username" placeholder="Username" minlength="4" >
					</div>
					<div class="form-group">
						<input class="form-control" id="password" name="password" type="password" placeholder="Password" minlength="4" >
					</div>
					<div class="form-group">
						<input class="form-control" id="confirmpwd" name="confirmpwd" type="password" placeholder="Confirm Password" minlength="4" >
					</div>
					<p> <input type="button" 
						class="btn btn-primary btn-lg" 
						value="Create" 
						onclick="return regformhash(this.form,
                                   this.form.username,
                                   this.form.password,
                                   this.form.confirmpwd);" /> </p>
				</form>
		</div>

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
	<script src="js/jquery-1.7.2.js"></script>
    <script src="js/bootstrap.js"></script>
	
	<script type="text/JavaScript" src="js/sha512.js"></script> 
	<script type="text/JavaScript" src="js/forms.js"></script> 

  </body>
</html>