<?php
// Authentication Here

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


include 'includes/parser_XML_menu.php';
$xml = simplexml_load_file("content/menu.xml");

$id = isset($_GET['id']) ? $_GET['id'] : 0;
$p = isset($_GET['p']) ? $_GET['p'] : 0;

$num_unread = getNumberUnreadMessages($mysqli, $user_id);
?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Raindrops Add Submenu</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
    <!-- Page Specific CSS -->
	
	<style>

		.whiteout {
			position:fixed;
			background:#fff;
			opacity:0.97;
			width:100%;
			height:100%;
			top:0;
			bottom:0;
			left:0;
			right:0;
			z-index:9999;
		}

	</style>
  </head>

  <body>

		
	<div class="whiteout" id="requires-picker" style="display:none;z-index:10001;">
		<div class="well" style="position:absolute;top:50px;left:50%;margin-left:-300px;width:650px;height:400px;overflow-y:scroll;overflow-x:hidden;">
			<h1>Requirements</h1>
			
			<h4>Select the items below which must be completed before this item is accessible.</h4>
			
			<?php echo getRequires($xml, 1, ''); ?>
		  
			<div class="span12" style="margin-top:15px;">
				<button type="button" class="btn btn-success btn-close-requires">
					<span>Okay</span>
				</button>
			</div>
		</div>
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
          <div class="col-lg-6 center">
            <h1>Add Subfolder <small></small></h1>
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-6 center">
			<ol id="breadcrumb-menu" class="breadcrumb">
				<li class="active">Location: </li>
				<?php
					if (isset($_GET['p'])) {
						$doc = new DOMDOcument;
						$doc->load("content/menu.xml");
						$xpath = new DOMXpath($doc);
						$pathItems = explode(',', $_GET['p']);
						foreach($pathItems as $pathItem) {
							echo '<li><i class="fa fa-list"></i> ' . getElementName($xpath, $pathItem) . '</li>';
						}
					}
				?>
            </ol>
			
			<div class="well center">
			  <!-- Check PHP; Add form validation -->
					<form method="post" action="includes/add_submenu.php?p=<?php echo $p;?>">
						<div class="form-group">
							<label>Folder Name</label>
							<input type="hidden" name="menu_parent_id" value="<?php echo $id; ?>">
							<input class="form-control" name="menu_name" placeholder="Folder Name" minlength="4" >
						</div>
						<div class="form-group">
							<label>Description</label>
							<textarea name="menu_desc" class="form-control" rows="3"></textarea>
						</div>
						<div class="form-group">
							<label>Requires <span style="color:#999;" id="requires-num-items">(0 items)</span></label><br />
							<button type="button" id="btn-requires" class="btn btn-warning" text="Requires...">Requires...</button>
							<h4><small>Any required items must be completed before this item is accessible</small></h4>
							<input type="hidden" name="menu_requires" id="requires-list" value="">
							
						</div>
						<hr />
						<p><input type="submit" class="btn btn-primary" role="button" value="Continue"></p>
					</form>
			</div>
			
          </div>
        </div><!-- /.row -->



      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>

	<!-- Custom scripts -->
	<script src="js/main.js"></script>
	<script>
		$(document).ready(function() {
			EpicWheels.Main.init();
		});

	</script>
	


  </body>
</html>
