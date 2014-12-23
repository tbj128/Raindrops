<?php
	$php_config = '../config.php';
	
	if (!file_exists($php_config)) {
		header("Location: ../setup.php");
	}
	
	include_once '../config.php';
	include_once '../includes/functions.php';

	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}

	sec_session_start();
	 
	if (!login_check($mysqli)) {
		header("Location: ../login");
	}
	
	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];
	$user_type = userType($mysqli, $user_id);

	// ======================
	
	$children = array();
	$id_user = 0;
	$name_user = 'test';
	if (isset($_GET['id'])) {
		$id_user = $_GET['id'];
		$name_user = userLookup($mysqli, $id_user);
	}
	
	if ($user_type != 'admin' && $user_type != '2') {
		$children = findTrainees($mysqli, $user_id);
		// Security check: Is user allowed to access this trainee's data?
		if (!in_array($id_user, $children)) {
			printf("Invalid permissions.\n");
			exit();
		}
	}
	
	$page = $name_user;
	$num_unread = getNumberUnreadMessages($mysqli, $user_id);
	$users = allUserLookup($mysqli);
	
	$xml = simplexml_load_file("../content/menu.xml");
	$itemIdToNameMap = getItemIdToNameMap($xml, '');
	$menuTypeMap = getItemIdToTypeMap($xml);
	$name_user = userLookup($mysqli, $id_user);
		
	$totalVideoTime = 0;
	$totalDocTime = 0;
	$totalActivityTime = 0;
	$totalTimedActivityTime = 0;
	
	if (isset($_GET['type'])) {
		$CSVType = $_GET['type'];
		if ($CSVType == 0) {
			// Daily training data
			$componentAccessByDay = getAllComponentAccessByDayAndSessions($mysqli, $xml, $id_user);
			krsort($componentAccessByDay);
			dailyTrainingDataCSV($name_user, $componentAccessByDay);
		} else if ($CSVType == 1) {
			$itemAccess = getAllComponentAccessAsc($mysqli, $id_user);
			itemAccessCSV($name_user, $itemAccess, $itemIdToNameMap);
		} else if ($CSVType == 2) {
			$componentSummary = getAllComponentsSummary($mysqli, $id_user);
			componentSummaryCSV($name_user, $componentSummary, $itemIdToNameMap);
		}
	}
	
	function dailyTrainingDataCSV($name_user, $componentAccessByDay) {
	
		// Parent array (will be turned into CSV)
		$csvParent = array();
		// Push the ID on to the parent array
		$csvItem = array($name_user);
		array_push($csvParent, $csvItem);
		// Push the headings on to the parent array
		$csvItem = array("Date", "Session Number", "Total Content Time (min)", "Videos Viewed", "Viewing Videos (min)", "Docs Viewed", "Viewing Docs (min)", "Activity Time (min)", "Self-Practiced (min)");
		array_push($csvParent, $csvItem);

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
		
				$csvItem = array(date("F j Y", $date), $session_num, $totalViewingTime, $totalVideosCompleted, $session['video_time'], $totalDocsCompleted, $session['doc_time'], $session['activity_time'], $session['timed_activity_time']);
				array_push($csvParent, $csvItem);
				
				$session_num++;
			}
		}
				
		// ------- Push Totals into CSV --------
		// $csvItem = array("Total", $totalSessions, $totalTraining, $totalVideoTime, $totalActivityTime, $totalTimedActivityTime, $totalAdditionalTraining, $totalExtraWheeling, $totalFalls);
		// array_push($csvParent, $csvItem);
	
		outputCSV('daily_training_' . $name_user, $csvParent);
	}

	
	
	function itemAccessCSV($name_user, $itemAccess, $itemIdToNameMap) {
		
		// Parent array (will be turned into CSV)
		$csvParent = array();
		// Push the ID on to the parent array
		$csvItem = array($name_user);
		array_push($csvParent, $csvItem);
		// Push the headings on to the parent array
		$csvItem = array("Date", "Component ID", "Viewing Time", "Self-Practiced");
		array_push($csvParent, $csvItem);
		
		foreach($itemAccess as $item) {
			$item_id = $item['id_component'];
			if ($item_id != null) {
				$item_id = $itemIdToNameMap[$item_id];
			}
			
			$csvItem = array(date("F j Y h:i:s A", strtotime($item['date_accessed'])), $item_id, $item['viewing_time'], $item['timed_activity_time']);
			array_push($csvParent, $csvItem);
		}
		
		// ------- Push Totals into CSV --------
		// $csvItem = array("Total", $totalSessions, $totalTraining, $totalVideoTime, $totalActivityTime, $totalTimedActivityTime, $totalAdditionalTraining, $totalExtraWheeling, $totalFalls);
		// array_push($csvParent, $csvItem);
		
		outputCSV('item_access_' . $name_user, $csvParent);
	}
	
	function componentSummaryCSV($name_user, $componentSummary, $itemIdToNameMap) {
		
		// Parent array (will be turned into CSV)
		$csvParent = array();
		// Push the ID on to the parent array
		$csvItem = array($name_user);
		array_push($csvParent, $csvItem);
		// Push the headings on to the parent array
		$csvItem = array("Item", "Accessed", "Days Accessed", "Total Time (min)");
		array_push($csvParent, $csvItem);
		
		foreach($componentSummary as $item) {
			$item_id = $item['id_component'];
			if ($item_id != null) {
				$item_id = $itemIdToNameMap[$item_id];
			}
			
			$csvItem = array($item_id, $item['views'], $item['num_days_accessed'], ($item['viewing_time'] + $item['activity_time'] + $item['timed_activity_time']));
			array_push($csvParent, $csvItem);
		}
				
		// ------- Push Totals into CSV --------
		// $csvItem = array("Total", $totalSessions, $totalTraining, $totalVideoTime, $totalActivityTime, $totalTimedActivityTime, $totalAdditionalTraining, $totalExtraWheeling, $totalFalls);
		// array_push($csvParent, $csvItem);
		
		outputCSV('component_access_' . $name_user, $csvParent);
	}

	function outputCSV($title, $data) {
		header("Content-Type: text/csv");
		header("Content-Disposition: attachment; filename=" . $title . ".csv");
		// Disable caching
		header("Cache-Control: no-cache, no-store, must-revalidate"); // HTTP 1.1
		header("Pragma: no-cache"); // HTTP 1.0
		header("Expires: 0"); // Proxies
		
		$out = fopen('php://output', 'w');
		foreach ($data as $fields) {
			fputcsv($out, $fields);
		}
		fclose($out);
	}
?>