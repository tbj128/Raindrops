<?php

	include_once 'config.php';
	include_once 'includes/functions.php';
	
	$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
	if (mysqli_connect_errno()) {
		printf("Connect failed: %s\n", mysqli_connect_error());
		exit();
	}
	
	$json = file_get_contents('php://input');
	$message = json_decode($json, true);
	$username = $message['u'];
	$password = $message['p'];
	if (app_login($mysqli, $username, $password)) {
		$user_id = userIDLookup($mysqli, $username);
		$data = $message['data'];
		foreach ($data as $datum) {
			$date_accessed =  mysqli_real_escape_string($mysqli, $datum['date_accessed']);
			$content_id =  mysqli_real_escape_string($mysqli, $datum['id_content']);
			$viewing_time =  mysqli_real_escape_string($mysqli, $datum['viewing_time']);
			$timed_activity_time =  mysqli_real_escape_string($mysqli, $datum['timed_activity_time']);
		
			$mysqli->query("INSERT INTO raindrops_statistics_access(date_accessed, id_user, id_component, viewing_time, timed_activity_time) 
							VALUES ('$date_accessed', $user_id, '$content_id', $viewing_time, $timed_activity_time)");
	
			// Update summary table
			$summary = getComponentSummary($mysqli, $user_id, $content_id);
			if ($summary == null) {
				$mysqli->query("INSERT INTO raindrops_statistics_summary(id_user, id_component, views, viewing_time, timed_activity_time, num_days_accessed) 
								VALUES ($user_id, '$content_id', 1, $viewing_time, $timed_activity_time, 1)");
			} else {
				$most_recent_entry = getMostRecentComponentEntry($mysqli, $user_id, $content_id);
				if ($most_recent_entry != null) {
					$updated_views = $summary['views'] + 1;
					$updated_viewing_time = $summary['viewing_time'] + $viewing_time;
					$updated_timed_activity_time = $summary['timed_activity_time'] + $timed_activity_time;
					$updated_num_days_accessed = $summary['num_days_accessed'];
					$last_accessed = $most_recent_entry['date_accessed'];
				
					$date_accessed_arr = explode(" ", $date_accessed);
					$last_accessed_arr = explode(" ", $last_accessed);
				
					$date_accessed_day = $date_accessed_arr[0];
					$last_accessed_day = $last_accessed_arr[0];
					if(strtotime($date_accessed_day) - strtotime($last_accessed_day) > 60*60*24) {
						$updated_num_days_accessed++;
					}
				
					$mysqli->query("UPDATE raindrops_statistics_summary SET views = $updated_views, viewing_time = $updated_viewing_time, timed_activity_time = $updated_timed_activity_time, num_days_accessed = $updated_num_days_accessed 
									WHERE id_user = $user_id AND id_component = '$content_id'");
				}
			}
		
			// Update overall users table
			$summary = getOverallUserStatistics($mysqli, $user_id);
			if ($summary == null) {
				$mysqli->query("INSERT INTO raindrops_statistics_users(id_user, viewing_time, timed_activity_time, num_completed) 
								VALUES ($user_id, $viewing_time, $timed_activity_time, 1)");
			} else {
				$updated_viewing_time = $summary['viewing_time'] + $viewing_time;
				$updated_timed_activity_time = $summary['timed_activity_time'] + $timed_activity_time;
				$updated_num_completed = $summary['num_completed'] + 1;

				$mysqli->query("UPDATE raindrops_statistics_users SET viewing_time = $updated_viewing_time, timed_activity_time = $updated_timed_activity_time, num_completed = $updated_num_completed 
								WHERE id_user = $user_id");
			}
			
			// Update permissions
			// TODO: Redundant completed field in permissions
			if ($stmt = $mysqli->prepare("SELECT completed FROM raindrops_permissions 
										  WHERE component = ? AND id_user = ?")) {
				$stmt->bind_param('si', $content_id, $user_id);
				$stmt->execute();   // Execute the prepared query.
				$stmt->store_result();
				$locked = 0;
				$completed = 1;
		
				if ($stmt->num_rows >= 1) {
					$mysqli->query("UPDATE raindrops_permissions SET completed = 1 WHERE id_user = $user_id AND component = '$content_id'");
				} else {
					$mysqli->query("INSERT INTO raindrops_permissions(id_user, component, locked, completed) VALUES ($user_id, '$content_id', $locked, $completed)");
				}
			}
		}
	}
?>