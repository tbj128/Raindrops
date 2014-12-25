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

    <title>Raindrops Add Video</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
    <!-- Page Specific CSS -->
	<link rel="stylesheet" href="css/jquery.fileupload-ui.css">
	
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
		
		.bar {
			height: 18px;
			background: green;
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
          <div class="col-lg-6 center">
            <h1>Add Video <small></small></h1>
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
			
			<div id="upload-msg" class="alert alert-success alert-dismissable" style="display:none;">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
				<strong>Success</strong> Video successfully uploaded.
			</div>
			
			<div id="upload-status" class="well" style="display:none;">
				<h3>Uploading Video</h3>
				<h4><small>Please wait, this may take a few minutes.</small></h4>
				<!-- The global progress bar -->
				<div id="progress" class="progress progress-success progress-striped">
					<div class="bar"></div>
				</div>
				<button type="reset" class="btn btn-warning cancel">
                    <i class="icon-ban-circle icon-white"></i>
                    <span>Cancel upload</span>
                </button>
			</div>

			<div class="well center">
			  <!-- Check PHP; Add form validation -->
					<form method="post" action="includes/add_video.php?p=<?php echo $p;?>">
						<div class="form-group">
							<label>Video Name</label>
							<input type="hidden" name="video_parent_id" value="<?php echo $id; ?>">
							<input class="form-control" name="video_name" placeholder="Video Name" minlength="4" >
						</div>
						<div class="form-group">
							<label>
								<input type="checkbox" name="video_is_activity"> <small>Is Activity Item</small>
								<span class="help-block"><h4>&nbsp;&nbsp;&nbsp;<small>Activity items gives users a chance to practice the lessons in the video</small></h4></span>
						
							</label>
							</div>
						<div class="form-group">
							<label>Description</label>
							<textarea name="video_desc" class="form-control" rows="3"></textarea>
						</div>
						<div class="form-group">
							<label>Requires <span style="color:#999;" id="requires-num-items">(0 items)</span></label><br />
							<button type="button" id="btn-requires" class="btn btn-warning" text="Requires...">Requires...</button>
							<h4><small>Any required items must be completed before this item is accessible</small></h4>
							<input type="hidden" name="video_requires" id="requires-list" value="">
							
						</div>
						<div class="form-group">
							<label>Upload Video</label><br />
							<input class="input" type="hidden" id="videoPathEdited" value="0">
							<input class="input" type="hidden" name="video_path" id="videoPathHidden" value="">
							<input type="text" id="videoPath" value="" style="display:none;" disabled><a id="deleteExistingVideo" style="display:none;cursor:pointer;font-size:28px;color:#999999;margin-left:20px;text-decoration:none;vertical-align:middle;">&times;</a>
							<span class="btn btn-success fileinput-button">
								<i class="icon-plus icon-white"></i>
								<span>Select video...</span>
								<!-- The file input field used as target for the file upload widget -->
								<input id="fileupload" type="file" name="files[]">
							</span>
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
	<script src="js/fileuploader/jquery.ui.widget.js"></script>
	<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
	<script src="js/fileuploader/jquery.iframe-transport.js"></script>
	<!-- The basic File Upload plugin -->
	<script src="js/fileuploader/jquery.fileupload.js"></script>
	<script src="js/main.js"></script>
	<script>
		$(document).ready(function() {
			EpicWheels.Main.init();
			
			// File Upload Plugin
			var url = 'upload/';
			$('#fileupload').fileupload({
				url: url,
				dataType: 'json',
				add: function (e, data) {
					$('#videoPath').val(data.files[0].name);
					$('#videoPathHidden').val(data.files[0].name);
					console.log(data.files[0].name);
					data.submit();
				},
				complete: function (e, data) {
					console.log(e);
					console.log(data);
					$('#upload-status').hide();
					$('.fileinput-button').hide();
					$("#videoPath").show();
					$('#deleteExistingVideo').show();
					$('#upload-msg').show();
				},
				progressall: function (e, data) {
					var progress = parseInt(data.loaded / data.total * 100, 10);
					$('#progress .bar').css(
						'width',
						progress + '%'
					);
				}
			});
			$('#fileupload').bind('fileuploadstart', function (e) {
				$('#upload-status').show();
			});
			
			$('button.cancel').click(function () {
				$('#upload-status').hide();
			});
		
		
		});

	</script>
	


  </body>
</html>
