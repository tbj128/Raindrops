<?php
	session_start();
	
	$php_config = 'config.php';

	$alert_show = false;
	
	if (file_exists($php_config)) {
		// Redirect to index.php
		header("Location: index.php");
	} else {
		if( isset($_POST['app_name']) && 
			isset($_POST['admin_username']) && 
			isset($_POST['admin_password']) && 
			isset($_POST['mysql_hostname']) && 
			isset($_POST['mysql_username']) && 
			isset($_POST['mysql_password']) &&
			isset($_POST['mysql_database'])) {
			
			$app_name = $_POST['app_name'];
			$admin_username = $_POST['admin_username'];
			$admin_password = $_POST['admin_password']; 
			$mysql_hostname = $_POST['mysql_hostname']; 
			$mysql_username = $_POST['mysql_username'];
			$mysql_password = $_POST['mysql_password'];
			$mysql_database = $_POST['mysql_database'];
			
			$config_output = '<?php 
				$app_name = "' . $app_name . '";
				$admin_user = "'. $admin_username. '";
				$db_host = "'. $mysql_hostname. '";
				$db_username = "'. $mysql_username. '";
				$db_password = "'. $mysql_password. '";
				$db_database = "'. $mysql_database. '";
			?>';
			

			$fp = fopen("config.php", "w");
			fwrite($fp, $config_output);
			fclose($fp);
			
			$_SESSION['admin_username'] = $admin_username;
			$_SESSION['admin_password'] = $admin_password;
			
			header("Location: setup_db.php");
		} else {
			if (count($_POST) > 0) {
				// Form post error
				$alert_show = true;
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

    <title>Raindrops </title>

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
			<strong>Oops!</strong> Please re-enter the information below.
		</div>
		<?php
			}
		?>
		
		<div class="jumbotron box-width align-center">
		  <h1>Hi! <small>Let's get setup</small></h1>
		  
		  <!-- Check PHP; Add form validation -->
				<form method="post" action="">
					<div class="form-group">
						<label>Application</label>
						<input class="form-control" name="app_name" placeholder="Application Name" minlength="4" >
					</div>
					<div class="form-group">
						<label>Admin</label>
						<input class="form-control" name="admin_username" placeholder="Administrator Username" minlength="4" >
					</div>
					<div class="form-group">
						<input class="form-control" name="admin_password" type="password" placeholder="Administrator Password" minlength="4" >
					</div>
					<div class="form-group">
						<label>MySQL</label>
						<input class="form-control" name="mysql_hostname" placeholder="MySQL Hostname" >
						<p class="help-block"><h4>&nbsp;&nbsp;&nbsp;<small></small></h4></p>
					</div>
					<div class="form-group">
						<input class="form-control" name="mysql_username" placeholder="MySQL Username" >
					</div>
					<div class="form-group">
						<input class="form-control" name="mysql_password" type="password" placeholder="MySQL Password" >
					</div>
					<div class="form-group">
						<input class="form-control" name="mysql_database" placeholder="Database Name (will be created)" >
					</div>
					<p><input type="submit" class="btn btn-primary btn-lg" role="button" text="Continue"></p>
				</form>
		</div>

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
	<script src="js/jquery-1.7.2.js"></script>
    <script src="js/bootstrap.js"></script>
	
	<!-- Custom JavaScript -->
	<script src="js/jqBootstrapValidation.js"></script>
	<script>
	  $(function () { $("input,select,textarea").not("[type=submit]").jqBootstrapValidation(); } );
	</script>
  </body>
</html>
