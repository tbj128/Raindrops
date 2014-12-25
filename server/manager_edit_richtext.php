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

$doc = new DOMDOcument;
$doc->load("content/menu.xml");
$xpath = new DOMXpath($doc);

$id = isset($_GET['id']) ? $_GET['id'] : 0;
$p = isset($_GET['p']) ? $_GET['p'] : 0;

$num_unread = getNumberUnreadMessages($mysqli, $user_id);
$item = findItem($xpath, $id);

$numRequiredItems = 0;
$requiredItems = array();
if ($item['requires'] != '') {
	$requiredItems = explode(',', $item['requires']);
	foreach($requiredItems as $requiredItem) {
		$numRequiredItems++;
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

    <title>Raindrops Edit Document</title>

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
		
		#editor {
			max-height: 250px;
			height: 350px;
			background-color: white;
			border-collapse: separate; 
			border: 1px solid rgb(204, 204, 204); 
			padding: 4px; 
			box-sizing: content-box; 
			-webkit-box-shadow: rgba(0, 0, 0, 0.0745098) 0px 1px 1px 0px inset; 
			box-shadow: rgba(0, 0, 0, 0.0745098) 0px 1px 1px 0px inset;
			border-top-right-radius: 3px; border-bottom-right-radius: 3px;
			border-bottom-left-radius: 3px; border-top-left-radius: 3px;
			overflow: scroll;
			outline: none;
		}

	</style>
  </head>

  <body>

		
	<div class="whiteout" id="requires-picker" style="display:none;z-index:10001;">
		<div class="well" style="position:absolute;top:50px;left:50%;margin-left:-300px;width:650px;height:400px;overflow-y:scroll;overflow-x:hidden;">
			<h1>Requirements</h1>
			
			<h4>Select the items below which must be completed before this item is accessible.</h4>
			
			<?php echo getKnownRequires($xml, 1, '', $requiredItems); ?>
		  
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
          <div class="col-lg-8 center">
            <h1>Add Document <small></small></h1>
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-8 center">
			<ol id="breadcrumb-menu" class="breadcrumb">
				<li class="active">Location: </li>
				<?php
					if (isset($_GET['p'])) {
						$pathItems = explode(',', $_GET['p']);
						foreach($pathItems as $pathItem) {
							echo '<li><i class="fa fa-list"></i> ' . getElementName($xpath, $pathItem) . '</li>';
						}
					}
				?>
            </ol>
			
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
					<form id="document-form" method="post" action="includes/edit_document.php?p=<?php echo $p;?>">
						<div class="form-group">
							<label>Document Name</label>
							<input type="hidden" name="document_id" value="<?php echo $id; ?>">
							<input type="hidden" name="document_source" value="<?php echo $item['source']; ?>">
							<input class="form-control" name="document_name" placeholder="Document Name" minlength="4" value="<?php echo $item['name']; ?>">
						</div>
						<div class="form-group">
							<label>
								<input type="checkbox" name="document_is_activity" <?php if ($item['activity'] == "true") { echo 'checked'; } ?>> <small>Is Activity Item</small>
								<span class="help-block"><h4>&nbsp;&nbsp;&nbsp;<small>Activity items gives users a chance to practice the lessons in the video</small></h4></span>
						
							</label>
						</div>
						<div class="form-group">
							<label>Description</label>
							<textarea name="document_desc" class="form-control" rows="3"><?php echo $item['desc']; ?></textarea>
						</div>
						<div class="form-group">
							<label>Requires <span style="color:#999;" id="requires-num-items">(<?php echo $numRequiredItems; ?> items)</span></label><br />
							<button type="button" id="btn-requires" class="btn btn-warning" text="Requires...">Requires...</button>
							<h4><small>Any required items must be completed before this item is accessible</small></h4>
							<input type="hidden" name="document_requires" id="requires-list" value="<?php echo implode(",", $requiredItems); ?>">
							
						</div>
						
						<div id="alerts"></div>
						<div class="btn-toolbar" data-role="editor-toolbar" data-target="#editor">
						  <div class="btn-group">
							<a class="btn dropdown-toggle" data-toggle="dropdown" title="Font"><i class="fa fa-font"></i><b class="caret"></b></a>
							  <ul class="dropdown-menu">
							  </ul>
							</div>
						  <div class="btn-group">
							<a class="btn dropdown-toggle" data-toggle="dropdown" title="Font Size"><i class="fa fa-text-height"></i>&nbsp;<b class="caret"></b></a>
							  <ul class="dropdown-menu">
							  <li><a data-edit="fontSize 5"><font size="5">Huge</font></a></li>
							  <li><a data-edit="fontSize 3"><font size="3">Normal</font></a></li>
							  <li><a data-edit="fontSize 1"><font size="1">Small</font></a></li>
							  </ul>
						  </div>
						  <div class="btn-group">
							<a class="btn" data-edit="bold" title="Bold (Ctrl/Cmd+B)"><i class="fa fa-bold"></i></a>
							<a class="btn" data-edit="italic" title="Italic (Ctrl/Cmd+I)"><i class="fa fa-italic"></i></a>
							<a class="btn" data-edit="strikethrough" title="Strikethrough"><i class="fa fa-strikethrough"></i></a>
							<a class="btn" data-edit="underline" title="Underline (Ctrl/Cmd+U)"><i class="fa fa-underline"></i></a>
						  </div>
						  <div class="btn-group">
							<a class="btn" data-edit="insertunorderedlist" title="Bullet list"><i class="fa fa-list-ul"></i></a>
							<a class="btn" data-edit="insertorderedlist" title="Number list"><i class="fa fa-list-ol"></i></a>
							<a class="btn" data-edit="outdent" title="Reduce indent (Shift+Tab)"><i class="fa fa-arrow-left"></i></a>
							<a class="btn" data-edit="indent" title="Indent (Tab)"><i class="fa fa-indent"></i></a>
						  </div>
						  <div class="btn-group">
							<a class="btn" data-edit="justifyleft" title="Align Left (Ctrl/Cmd+L)"><i class="fa fa-align-left"></i></a>
							<a class="btn" data-edit="justifycenter" title="Center (Ctrl/Cmd+E)"><i class="fa fa-align-center"></i></a>
							<a class="btn" data-edit="justifyright" title="Align Right (Ctrl/Cmd+R)"><i class="fa fa-align-right"></i></a>
							<a class="btn" data-edit="justifyfull" title="Justify (Ctrl/Cmd+J)"><i class="fa fa-align-justify"></i></a>
						  </div>
						  <div class="btn-group">
							  <a class="btn dropdown-toggle" data-toggle="dropdown" title="Hyperlink"><i class="fa fa-link"></i></a>
								<div class="dropdown-menu input-append">
									<input class="span2" placeholder="URL" type="text" data-edit="createLink"/>
									<button class="btn" type="button">Add</button>
							</div>
							<a class="btn" data-edit="unlink" title="Remove Hyperlink"><i class="fa fa-cut"></i></a>

						  </div>
						  
						  <div class="btn-group">
							<a class="btn" title="Insert picture (or just drag & drop)" id="pictureBtn"><i class="fa fa-picture-o"></i></a>
							<input type="file" data-role="magic-overlay" data-target="#pictureBtn" data-edit="insertImage" />
						  </div>
						  <div class="btn-group">
							<a class="btn" data-edit="undo" title="Undo (Ctrl/Cmd+Z)"><i class="fa fa-undo"></i></a>
							<a class="btn" data-edit="redo" title="Redo (Ctrl/Cmd+Y)"><i class="fa fa-repeat"></i></a>
						  </div>
						</div>

						<div id="editor">
							<?php echo file_get_contents('content/media/' . $item['source']); ?>
						</div>
						<input type="hidden" value="" name="document_text" id="document-text">
						
						<hr />
						<p><button type="button" id="btn-submit-continue" class="btn btn-primary">Save</button></p>
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
	<script src="js/wysiwyg/jquery.hotkeys.js"></script>
    <script src="js/wysiwyg/bootstrap-wysiwyg.js"></script>
	<script src="js/main.js"></script>
	
	<script>
	  $(function(){
	  
			EpicWheels.Main.init();
			<?php 
				foreach ($requiredItems as $requiredItem) {
					echo 'EpicWheels.Main.addToRequiresArray(\'' . $requiredItem . '\');';
				}
			?>
			
			$('#btn-submit-continue').click(function(){
				$('#document-text').val($('#editor').html());
				$('#document-form').submit();
				
			});
			
			function initToolbarBootstrapBindings() {
			  var fonts = ['Serif', 'Sans', 'Arial', 'Arial Black', 'Courier', 
					'Courier New', 'Comic Sans MS', 'Helvetica', 'Impact', 'Lucida Grande', 'Lucida Sans', 'Tahoma', 'Times',
					'Times New Roman', 'Verdana'],
					fontTarget = $('[title=Font]').siblings('.dropdown-menu');
			  $.each(fonts, function (idx, fontName) {
				  fontTarget.append($('<li><a data-edit="fontName ' + fontName +'" style="font-family:\''+ fontName +'\'">'+fontName + '</a></li>'));
			  });
			  $('a[title]').tooltip({container:'body'});
				$('.dropdown-menu input').click(function() {return false;})
					.change(function () {$(this).parent('.dropdown-menu').siblings('.dropdown-toggle').dropdown('toggle');})
				.keydown('esc', function () {this.value='';$(this).change();});

			  $('[data-role=magic-overlay]').each(function () { 
				var overlay = $(this), target = $(overlay.data('target')); 
				overlay.css('opacity', 0).css('position', 'absolute').offset(target.offset()).width(target.outerWidth()).height(target.outerHeight());
			  });
			  if ("onwebkitspeechchange"  in document.createElement("input")) {
				var editorOffset = $('#editor').offset();
				$('#voiceBtn').css('position','absolute').offset({top: editorOffset.top, left: editorOffset.left+$('#editor').innerWidth()-35});
			  } else {
				$('#voiceBtn').hide();
			  }
			};
			function showErrorAlert (reason, detail) {
				var msg='';
				if (reason==='unsupported-file-type') { msg = "Unsupported format " +detail; }
				else {
					console.log("error uploading file", reason, detail);
				}
				$('<div class="alert"> <button type="button" class="close" data-dismiss="alert">&times;</button>'+ 
				 '<strong>File upload error</strong> '+msg+' </div>').prependTo('#alerts');
			};
			initToolbarBootstrapBindings();  
			$('#editor').wysiwyg({ fileUploadError: showErrorAlert} );
			window.prettyPrint && prettyPrint();
		  });
	</script>

  </body>
</html>
