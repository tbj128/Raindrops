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

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	// ==== Page-specific PHP ========
	
	$page = 'Compose';
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

    <title>Compose</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
	
  </head>

  <body>
  	<div id="sending-alert" class="alert alert-info" role="alert" style="display:none;width:120px;position:fixed;top:-4px;left:50%;margin-left:-60px;text-align:center;z-index:9999;padding:10px;">Sending...</div>
						
    <div id="wrapper">

      <!-- Sidebar -->
	  <?php
		include 'component_nav.php';
	  ?>

      <div id="page-wrapper">

        <div class="row">
			<div class="col-lg-12">
				<form id="send-form" method="post" action="includes/process_send_message.php">
					<div class="form-group input-group">
					</div>
					<div class="form-group input-group">
						<span class="input-group-addon">To</span>
						<select class="form-control" name="msg_id_to">
							<?php
								foreach ($users as $uid => $uname) {
									if ($uname == $admin_user) {
										echo '<option value="' . $uid . '">' . $uname . ' (admin)</option>'; 
									} else {
										echo '<option value="' . $uid . '">' . $uname . '</option>';
									}
								}
							?>
						</select>
					</div>
					<div class="form-group">
						<input class="form-control" name="msg_title" placeholder="Subject" >
					</div>
					<div class="form-group">
						<label>Description</label>
						<textarea class="form-control" name="msg_desc" rows="3"></textarea>
					</div>
					<div class="form-group">
						<label>Attach Audio Component (Optional)</label><br />
						<input id="audio_location" type="hidden" name="audio_location" value="" />
						<button type="button" class="btn btn-info" id="add-recording"><i class="icon-edit"></i> Add Recording</button>
						
						<a class="btn btn-danger" href="javascript:record()" id="record" style="display:none;">Record</a>
						<a class="btn btn-success" href="javascript:play()" id="play" style="display:none;">Play</a> 
						<a class="btn btn-danger" href="javascript:stop()" id="stop" style="display:none;">Stop</a>
						<br /><br /> 
						<span id="time" style="display:none;">0:00</span>
					</div>
					<hr />
					<p> <button id="send" type="button" class="btn btn-primary btn-lg" value="Send">Send</button> </p>
				</form>
			</div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-12">
				
          </div>
        </div><!-- /.row -->

      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>
    <script src="recorderjs/recorder.js"></script>

    <!-- Page Specific Plugins -->
    <script>
		function timecode(ms) {
			var hms = {
			  h: Math.floor(ms/(60*60*1000)),
			  m: Math.floor((ms/60000) % 60),
			  s: Math.floor((ms/1000) % 60)
			};
			var tc = []; // Timecode array to be joined with '.'
			if (hms.h > 0) {
			  tc.push(hms.h);
			}
			tc.push((hms.m < 10 && hms.h > 0 ? "0" + hms.m : hms.m));
			tc.push((hms.s < 10  ? "0" + hms.s : hms.s));
			return tc.join(':');
		}
		
		function initRecorder() {
			Recorder.initialize({
				swfSrc: "recorderjs/recorder.swf"
			});
		}

		function record(){
			Recorder.record({
				start: function(){
					$('#record').hide();
					$('#stop').show();
				},
				progress: function(milliseconds){
					document.getElementById("time").innerHTML = timecode(milliseconds);
				}
			});
		}
	  
		function play(){
			Recorder.stop();
			Recorder.play({
				progress: function(milliseconds){
					document.getElementById("time").innerHTML = timecode(milliseconds);
				}
			});
		}
	  
		function stop(){
			Recorder.stop();
			$('#record').html("Record Again");
			$('#record').show();
			$('#stop').hide();
			$('#play').show();
		}
	  
		function send(){
			$('#sending-alert').show();
			Recorder.upload({
				method: "POST",                             // (not implemented) (optional, defaults to POST) HTTP Method can be either POST or PUT 
				url: "recorderjs/upload.php",   // URL to upload to (needs to have a suitable crossdomain.xml for Adobe Flash)
				success: function(responseText) {
					if (responseText != '') {
						$("#audio_location").val(responseText);
						$("#send-form").submit();
					}
				}
			});
		}
		
		$("#add-recording").click(function() {
			initRecorder();
			$("#add-recording").hide();
			$("#record").show();
		});
		
		$("#send").click(function() {
			if ($('#add-recording').is(":visible")) {
				// No audio attachment
				$( "#send-form" ).submit();
			} else {
				send();
			}
		});
	</script>
  </body>
</html>
