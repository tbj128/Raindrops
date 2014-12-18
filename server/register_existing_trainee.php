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

	$parent = -1;
	if (isset($_GET['p'])) {
		$parent = $_GET['p'];
	} else {
		$alert_show = true;
	}

	$user_type = userType($mysqli, $user_id);
	if ($user_type != "admin") {
		header("Location: index");
	}

	$parent_name = userLookup($mysqli, $parent);

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
		  <h2>Link an Existing Trainee</h2>
		  <p>Link an existing trainee to the trainer <strong><?php echo $parent_name; ?></strong>.</p>
		  
				<form method="post" action="includes/process_existing_trainee.php">
					<input type="hidden" name="parent" value="<?php echo $parent; ?>" >
					<select name="trainee">
					<?php
						$trainees = findAllTrainees($mysqli);
						foreach ($trainees as $trainee) {
							$trainee_name = userLookup($mysqli, $trainee);
							echo '<option value="' . $trainee . '">' . $trainee_name . '</option>';
						}
					?>
					</select><br />
					<input type="submit" class="btn btn-primary btn-lg" />
				</form>
		</div>

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
	<script src="js/jquery-1.7.2.js"></script>
    <script src="js/bootstrap.js"></script>
	
  </body>
</html>