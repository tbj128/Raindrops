<?php
	include_once 'config.php';
	include_once 'includes/functions.php';


	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}

	$php_config = 'config.php';
	$alert_show = false;
	$welcome = false;
	
	if (isset($_GET['error'])) {
		$alert_show = true;
	}
		
	if (isset($_GET['welcome'])) {
		$welcome = true;
	}

	if (!file_exists($php_config)) {
		header("Location: setup.php");
	} else {
		include 'config.php';
	}
	
	 
	sec_session_start();
	 
	if (login_check($mysqli) == true) {
		header("Location: index.php");
	}

?>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $app_name; ?> Login</title>

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
			<strong>Oops!</strong> Username or password combination incorrect.
		</div>
		<?php
			}
		?>
		
		<?php
			if ($welcome) {
		?>
		<div class="alert alert-success alert-dismissable box-width align-center-top">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<strong>Yay!</strong> You've successfully completed setup. Login with the administrator account to continue.
		</div>
		<?php
			}
		?>
		
		
		<div class="jumbotron box-width align-center">
		  <h1>Welcome Back! <small></small></h1>
		  
				<form method="post" action="includes/process_login.php<?php if ($welcome) { echo '?welcome=1'; } ?>">
					<div class="form-group">
						<label>Login</label>
						<input class="form-control" name="username" placeholder="Username" minlength="4" >
					</div>
					<div class="form-group">
						<input class="form-control" name="password" type="password" placeholder="Password" minlength="4" >
					</div>
					<p><input type="submit" class="btn btn-primary btn-lg" role="button" value="Continue" onclick="formhash(this.form, this.form.password);" ></p>
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
