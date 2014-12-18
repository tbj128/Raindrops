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
	$id_user = 0;
	$name_user = 'test';
	if (isset($_GET['id'])) {
		$id_user = $_GET['id'];
		$name_user = userLookup($mysqli, $id_user);
	}
	$user_type = userType($mysqli, $user_id);
	$target_user_type = userType($mysqli, $id_user);
	if ($user_type != 2) {
		$children = findTrainees($mysqli, $user_id);
		// Security check: Is user allowed to access this trainee's data?
		if ($user_type != "admin" && !in_array($id_user, $children)) {
			printf("Invalid permissions.\n");
			exit();
		}
	}
	
	$page = $name_user;
	$num_unread = getNumberUnreadMessages($mysqli, $user_id);
	$users = allUserLookup($mysqli);
	
	$xml = simplexml_load_file("content/menu.xml");
	$itemIdToNameMap = getItemIdToNameMap($xml, '');
	$componentSummary = getAllComponentsSummary($mysqli, $id_user);
	$componentAccessByDay = getAllComponentAccessByDayAndSessions($mysqli, $xml, $id_user);
	krsort($componentAccessByDay);
	$menuTypeMap = getItemIdToTypeMap($xml);
	$name_user = userLookup($mysqli, $id_user);
		
	$totalVideoTime = 0;
	$totalDocTime = 0;
	$totalActivityTime = 0;
	$totalTimedActivityTime = 0;
?>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <title><?php echo $name_user; ?> Overview</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.css" rel="stylesheet">

    <!-- Add custom CSS here -->
    <link href="css/sb-admin.css" rel="stylesheet">
    <link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
  </head>

  <body>

    <div id="wrapper">

      <!-- Sidebar -->
	  <?php
		include 'component_nav.php';
	  ?>

      <div id="page-wrapper">

		<?php
			// Display trainer info when the admin is viewing trainers
			if ($user_type == "admin" && $target_user_type == 1) {
				$target_children = findTrainees($mysqli, $id_user);
				
		?>
        <div class="row">
          <div class="col-lg-12">
            <h1><?php echo $name_user; ?>  (trainer)</h1>
          </div>
        </div>
        <div class="row">
          <div class="col-lg-6">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><?php echo $name_user; ?>'s trainees</h3>
              </div>
              <div class="panel-body">
                <div class="list-group">
				  <?php
						foreach ($target_children as $trainee) {
							echo '<a href="user?id=' . $trainee . '" class="list-group-item">
									<i class="fa fa-user"></i>&nbsp;&nbsp;' . userLookup($mysqli, $trainee) . '
								  </a>';
						}
				  ?>
				  <a id="newTraineePrompt" href="#" class="list-group-item">
					<i class="fa fa-plus"></i>&nbsp;&nbsp;New Trainee
				  </a>
                </div>
              </div>
            </div>
          </div>
		<?php
			} else {
		?>

        <div class="row">
          <div class="col-lg-12">
            <h1><?php echo $name_user; ?> 
            <?php 
            	if ($user_type != 2) {
            ?>
            		<a href="user_progress?id=<?php echo $id_user; ?>" class="btn btn-primary btn-lg pull-right" target="_blank">View Progress</a> </h1>
          	<?php
          		}
          	?>
          </div>
        </div><!-- /.row -->

        <div class="row">
			<br />
		</div>
        <div class="row">
          <div class="col-lg-12">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-bar-chart-o"></i>  Daily Training Data <a class="pull-right" href="export/export.php?type=0&id=<?php echo $id_user; ?>"><i class="fa fa-download"></i>  Download (CSV)</a></h3>
                   
			</div>
				<?php 
					if ($componentAccessByDay == null) {
				?>
					<div id="error-msg" class="well" style="background:#fff;margin-bottom:0px;">
						<h1>Oops!</h1>
						<h3>No data yet.</h3>
						<h6>This is probably because this is a new account, and that the app has not contacted the server yet - check back later!</h6>
					</div>
				<?php
					} else {
				?>
				<div class="panel-body" id="chart_individual_training">
				</div>

				<div class="block">
				    <table class="table table-bordered table-hover table-striped tablesorter">
						<thead>
						  <tr>
							<th>Date</th>
							<th>Session # </th>
							<th>Total Content Time (min) </th>
							<th>Videos Viewed </th>
							<th>Viewing Videos (min) </th>
							<th>Docs Viewed </th>
							<th>Viewing Docs (min) </th>
							<th>Activities Time (min) </th>
							<th>Self-Practiced (min) </th>
						  </tr>
						</thead>
						<tbody>
							<?php
								$rowCounter = 0;
								$totalRows = count($componentAccessByDay);
								$individualTrainingBehaviourHTML = "";
								
								foreach($componentAccessByDay as $date => $sessions) {
									$session_num = 1;
									foreach($sessions as $session) {
										$totalVideoTime += $session['video_time'];
										$totalDocTime += $session['doc_time'];
										$totalActivityTime += $session['activity_time'];
										$totalTimedActivityTime += $session['timed_activity_time'];
										
										$totalViewingTime = $session['video_time'] + $session['doc_time'];
										$totalVideosCompleted = 0;
										$totalDocsCompleted = 0;
										
										$components_completed = $session['components_completed'];
										foreach ($components_completed as $component) {
											if ($menuTypeMap[$component] == 1) {
												$totalVideosCompleted++;
											} else if ($menuTypeMap[$component] == 2) {
												$totalDocsCompleted++;
											}
										}
										
										$individualTrainingBehaviourHTML = $individualTrainingBehaviourHTML . 
											"<tr>
												<td>" . date("F j Y", $date)  . "</td>
												<td>" . $session_num . "</td>
												<td>" . $totalViewingTime . "</td>
												<td>" . $totalVideosCompleted . "</td>
												<td>" . $session['video_time'] . "</td>
												<td>" . $totalDocsCompleted . "</td>
												<td>" . $session['doc_time'] . "</td>
												<td>" . $session['activity_time'] . "</td>
												<td>" . $session['timed_activity_time'] . "</td>
											</tr>";
										
										$session_num++;
										$rowCounter++;
										if ($rowCounter > 20) {
											break;
										}
									}
									if ($rowCounter > 20) {
										break;
									}
								}
								if ($totalRows > 20) {
									$individualTrainingBehaviourHTML = $individualTrainingBehaviourHTML . "<tr>
											<td colspan='9' style='text-align:center;'><strong>. . .</strong></td>
										</tr>";
								}
								echo $individualTrainingBehaviourHTML;
							?>
							
						</tbody>
					</table>
				</div>
				<?php
					}
				?>
            </div>
          </div>
        </div><!-- /.row -->

        <div class="row">
          <div class="col-lg-6">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-dashboard"></i>  Engagement <a class="pull-right" href="export/export.php?type=1&id=<?php echo $id_user; ?>"><i class="fa fa-download"></i>  Download (CSV)</a></h3>
              </div>
              <div class="panel-body">
                <div id="chart_individual_participant"></div>
				<?php
					if (count($componentAccessByDay) == 0) {
						echo '<h4 style="text-align:center;"><small>No activity yet.</small></h4>';
					}
				?>
              </div>
            </div>
          </div>
          <div class="col-lg-6">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-envelope-o"></i>  Recent Messages</h3>
              </div>
              <div class="panel-body">
                <div class="list-group">
				  <?php
						$messages = getInboxFiltered($mysqli, $user_id, $id_user);
						if (count($messages) > 0) {
							$message_preview_counter = 0;
							foreach($messages as $message) {
								if ($message_preview_counter >= 10) {
									break;
								} else {
									$message_preview_counter++;
								}
								if ($message["msg_type"] == 0) {
									echo '<a href="#" class="list-group-item">
											<span class="badge">' . $message["msg_date"] . '</span>
											<i class="fa fa-comment"></i> ' . userLookup($mysqli, $message["id_from"]) . ' sent you a message
										  </a>';
								} else if ($message["msg_type"] == 1) {
									echo '<a href="#" class="list-group-item">
											<span class="badge">' . $message["msg_date"] . '</span>
											<i class="fa fa-volume-up"></i> ' . userLookup($mysqli, $message["id_from"]) . ' sent you a message
										  </a>';
								} else {
									echo '<a href="#" class="list-group-item">
											<span class="badge">' . $message["msg_date"] . '</span>
											<i class="fa fa-film"></i> ' . userLookup($mysqli, $message["id_from"]) . ' sent you a message
										  </a>';
								}
							}
						} else {
							echo '<h4 style="text-align:center;"><small>No messages from this user.</small></h4>';
						}
				?>
                </div>
                <div class="text-right">
                  <a href="inbox">View All Messages <i class="fa fa-arrow-circle-right"></i></a>
                </div>
              </div>
            </div>
          </div>
        </div><!-- /.row -->
        <div class="row">
          <div class="col-lg-12">
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title"><i class="fa fa-list"></i>  Item Summary <a class="pull-right" href="export/export.php?type=2&id=<?php echo $id_user; ?>"><i class="fa fa-download"></i>  Download (CSV)</a></h3>
              </div>
              <div class="panel-body">
				<?php 
					if (count($componentAccessByDay) == 0) {
						echo '<h4 style="text-align:center;"><small>No activity yet.</small></h4>';
					} else {
				?>
                <div class="table-responsive">
                  <table class="table table-bordered table-hover table-striped tablesorter">
                    <thead>
                      <tr>
                        <th>Item </th>
                        <th># Access </th>
                        <th>Days Accessed </th>
                        <th>Total Time </th>
                      </tr>
                    </thead>
                    <tbody>
						<?php
							foreach($componentSummary as $item) {
								$item_id = $item['id_component'];
								if ($item_id != null) {
									$item_id = $itemIdToNameMap[$item_id];
								}
								echo "
								<tr>
									<td>" . $item_id . "</td>
									<td>" . $item['views'] . "</td>
									<td>" . $item['num_days_accessed'] . "</td>
									<td>" . ($item['viewing_time'] + $item['activity_time'] + $item['timed_activity_time']) . "</td>
								</tr>";
							}
						?>
                    </tbody>
                  </table>
                </div>
				<?php
					}
				?>
              </div>
            </div>
          </div>
        </div><!-- /.row -->

		<?php
			} // End user check
		?>

      </div><!-- /#page-wrapper -->

    </div><!-- /#wrapper -->

    <!-- JavaScript -->
    <script src="js/jquery-1.10.2.js"></script>
    <script src="js/bootstrap.js"></script>

    <!-- Page Specific Plugins -->
   <!-- <script src="js/tablesorter/jquery.tablesorter.js"></script>
    <script src="js/tablesorter/tables.js"></script> -->

	<!-- Custom -->
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script type="text/javascript" src="js/charts/colors.js"></script>
	<script type="text/javascript" src="js/charts/piechart.js"></script>
	<script type="text/javascript">
		<?php
			if ($user_type == "admin" && $target_user_type == 1) {
		?>
			$('#newTraineePrompt').click(function() {
				_openNewTraineeTypePopup();
			});
			
			function _openNewTraineeTypePopup() {
				var confirmPopupHTML = '<div id="whiteout"></div>\
								<div id="warning-popup" class="thumbnail warning-popup">\
									<div class="caption">\
										<h4>Trainee to Create</h4>\
										<p>Create a new trainee account for this trainer or link this trainer with an existing trainee.</p>\
										<p>';
		
				confirmPopupHTML += '<a id="link-new" href="#" class="btn btn-primary" role="button">New Trainee</a><br />';
				confirmPopupHTML += '<a id="link-existing" href="#" class="btn btn-primary" role="button">Link Existing Trainee</a><br />';
				confirmPopupHTML += '<a id="link-cancel" href="#" class="btn btn-default" role="button">Cancel</a>\
								</p>\
							</div>\
						</div>';
		
				if ($('#whiteout').length <= 0) {
					$('body').append(confirmPopupHTML);
				}
			
				$('#link-new').click(function() {
					$('#whiteout').remove();
					$('#warning-popup').remove();
					window.location.href = "register?p=<?php echo $id_user; ?>";
				});
				$('#link-existing').click(function() {
					$('#whiteout').remove();
					$('#warning-popup').remove();
					window.location.href = "register_existing_trainee?p=<?php echo $id_user; ?>";
				});
				$('#link-cancel').click(function() {
					$('#whiteout').remove();
					$('#warning-popup').remove();
				});
			}
		<?php
			}
		?>
	
		google.load("visualization", "1", {packages:["corechart"]});
		google.setOnLoadCallback(drawChart);
		function drawChart() {			
			<?php 
			if (count($componentAccessByDay) > 0) {
			?>
				drawIndividualParticipant();
				drawIndividualTrainingBehaviour();
			<?php
				}
			?>
		}
		
		function drawIndividualParticipant() {
			var individual_participant_data = google.visualization.arrayToDataTable([
			  ['Individual Participant Data', 'Data'],

			  <?php
					echo "['Videos', " . $totalVideoTime . "],";
					echo "['Documents', " . $totalDocTime . "],";
					echo "['Activities', " . $totalActivityTime . "],";
					echo "['Timed Activities', " . $totalTimedActivityTime . "],";
			  ?>
			]);

			var chart = new google.visualization.PieChart(document.getElementById('chart_individual_participant'));
			chart.draw(individual_participant_data, pieChartOptions);
		}
		
		function drawIndividualTrainingBehaviour() {
			
			var data = google.visualization.arrayToDataTable([
			['Date', 'Number of Items Completed'],
			<?php
				$dataOutput = '';
				foreach($componentAccessByDay as $date => $sessions) {
					$date_str = date("F j Y", $date);
					foreach($sessions as $session) {
						$numItems = count($session['components_completed']);
						$dataOutput = "['" . $date_str . "'," . $numItems . "]," . $dataOutput;
					}
				}
				echo $dataOutput;
			?>
			]);
	
			var options = {
			};

			var chart = new google.visualization.LineChart(document.getElementById('chart_individual_training'));
			chart.draw(data, options);
		}
		
		</script>
	
	
  </body>
</html>