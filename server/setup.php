<?php
	session_start();
	
	include 'config.php';

	$php_config = 'config.php';
	$is_password_different = false;
	$is_config_empty = false;
	
	if (!file_exists($php_config)) {
		printf("Error: No config.php file found");
		exit();
	}
	
	$connection = new mysqli($db_host, $db_username, $db_password);
	if (mysqli_connect_errno()) {
		$db_selected = mysqli_select_db($connection, $db_database);
		if ($db_selected) {
			// Successful database link established
			header("Location: index.php");
		}
	}
	
	if ($app_name == "" || 
		$admin_user == "" ||
		$db_host == "" ||
		$db_username == "" ||
		$db_password == "" ||
		$db_database == "") {
		$is_config_empty = true;
	} else {
		if( isset($_POST['admin_password']) &&
			isset($_POST['admin_password_confirm'])) {
			
			$admin_password = $_POST['admin_password'];
			$admin_password_confirm = $_POST['admin_password_confirm'];
			
			if ($admin_password == "" 
				|| $admin_password_confirm == "" 
				|| $admin_password != $admin_password_confirm) {
				$is_password_different = true;
			} else {
				$_SESSION['admin_username'] = $admin_user;
				$_SESSION['admin_password'] = $admin_password;
			
				header("Location: setup_db.php");
			}
		} else {
			if (count($_POST) > 0) {
				// Form post error
				$is_password_different = true;
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

    <title>Setup</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-setup.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">

  </head>

  <body>

    <div id="wrapper">
		<?php
			if ($is_password_different) {
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
	  
	  		<?php
	  			if ($is_config_empty) {
	  		?>
	  		
	  		You will need to first manually configure the config.php file on the server. An example is shown below:
	  		<pre>
&lt;?php 
	$app_name = &quot;Raindrops&quot;;
	$admin_user = &quot;administrator&quot;;
	$db_host = &quot;localhost&quot;;
	$db_username = &quot;root&quot;;
	$db_password = &quot;Password1&quot;;
	$db_database = &quot;raindrops&quot;;
?&gt;
	  		</pre>
	  		
	  		<?php
	  			} else {
	  		?>
	  		
	  		<p>
	  		Before we configure the database for the first time, choose the password for the <?php echo $admin_user; ?> account.
	  		</p>
	  		
			<form method="post" action="">
				<div class="form-group">
					<input class="form-control" name="admin_password" type="password" placeholder="Administrator Password" minlength="4" >
				</div>
				<div class="form-group">
					<input class="form-control" name="admin_password_confirm" type="password" placeholder="Confirm Administrator Password" minlength="4" >
				</div>
				<p><input type="submit" class="btn btn-primary btn-lg" role="button" text="Continue"></p>
			</form>
			
			<?php
				}
			?>
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
