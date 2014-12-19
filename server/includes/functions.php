<?php

function sec_session_start() {
    $session_name = 'sec_session_id';   // Set a custom session name
    $secure = false; // TODO
    // This stops JavaScript being able to access the session id.
    $httponly = true;
    // Forces sessions to only use cookies.
    if (ini_set('session.use_only_cookies', 1) === FALSE) {
        header("Location: ../error.php?err=Could not initiate a safe session (ini_set)");
        exit();
    }
    // Gets current cookies params.
    $cookieParams = session_get_cookie_params();
    session_set_cookie_params($cookieParams["lifetime"],
        $cookieParams["path"], 
        $cookieParams["domain"], 
        $secure,
        $httponly);
    // Sets the session name to the one set above.
    session_name($session_name);
    session_start();            // Start the PHP session 
    session_regenerate_id();    // regenerated the session, delete the old one. 
}

function login($username, $password, $mysqli) {

	include_once '../config.php';
	
    // Using prepared statements means that SQL injection is not possible. 
    if ($stmt = $mysqli->prepare("SELECT id, password, salt FROM raindrops_members WHERE username = ? LIMIT 1")) {
        $stmt->bind_param('s', $username);  // Bind "$username" to parameter.
        $stmt->execute();    // Execute the prepared query.
        $stmt->store_result();
 
        // get variables from result.
        $stmt->bind_result($user_id, $check_password, $salt);
        $stmt->fetch();
 
        // hash the password with the unique salt.
        $password = hash('sha512', $password . $salt);
        if ($stmt->num_rows == 1) {
            // If the user exists we check if the account is locked
            // from too many login attempts 
 
            if (checkbrute($user_id, $mysqli) == true) {
                // Account is locked 
                // Send an email to user saying their account is locked
                return false;
            } else {
                // Check if the password in the database matches
                // the password the user submitted.
                if ($check_password == $password) {
                    // Password is correct!
                    // Get the user-agent string of the user.
                    $user_browser = $_SERVER['HTTP_USER_AGENT'];
                    // XSS protection as we might print this value
                    $user_id = preg_replace("/[^0-9]+/", "", $user_id);
                    $_SESSION['user_id'] = $user_id;
					
                    // XSS protection as we might print this value
                    $username = preg_replace("/[^a-zA-Z0-9_\-]+/", 
                                                                "", 
                                                                $username);
                    $_SESSION['username'] = $username;
                    $_SESSION['login_string'] = hash('sha512', 
                              $password . $user_browser);
                    // Login successful.
                    return true;
                } else {
                    // Password is not correct
                    // We record this attempt in the database
                    $now = time();
                    $mysqli->query("INSERT INTO raindrops_login_attempts(user_id, time)
                                    VALUES ('$user_id', '$now')");
                    return false;
                }
            }
        } else {
            // No user exists.
            return false;
        }
    }
}


function app_login($mysqli, $username, $password) {

    // Using prepared statements means that SQL injection is not possible. 
    if ($stmt = $mysqli->prepare("SELECT id, password, salt FROM raindrops_members WHERE username = ? LIMIT 1")) {
        $stmt->bind_param('s', $username);  // Bind "$username" to parameter.
        $stmt->execute();    // Execute the prepared query.
        $stmt->store_result();
 
        // get variables from result.
        $stmt->bind_result($user_id, $check_password, $salt);
        $stmt->fetch();
 
        // hash the password with the unique salt.
        $password = hash('sha512', $password . $salt);
        if ($stmt->num_rows == 1) {
            // If the user exists we check if the account is locked
            // from too many login attempts 
 
            if (checkbrute($user_id, $mysqli) == true) {
                // Account is locked 
                // Send an email to user saying their account is locked
                return false;
            } else {
                // Check if the password in the database matches
                // the password the user submitted.
                if ($check_password == $password) {
                    // Password is correct!
                    // XSS protection as we might print this value
                    $user_id = preg_replace("/[^0-9]+/", "", $user_id);
                    $_SESSION['user_id'] = $user_id;
					
                    // XSS protection as we might print this value
                    $username = preg_replace("/[^a-zA-Z0-9_\-]+/", 
                                                                "", 
                                                                $username);

                    // Login successful.
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            // No user exists.
            return false;
        }
    }
}

function checkbrute($user_id, $mysqli) {
    // Get timestamp of current time 
    $now = time();
 
    // All login attempts are counted from the past 2 hours. 
    $valid_attempts = $now - (2 * 60 * 60);
 
    if ($stmt = $mysqli->prepare("SELECT time FROM raindrops_login_attempts WHERE user_id = ?  AND time > '$valid_attempts'")) {
        $stmt->bind_param('i', $user_id);
 
        // Execute the prepared query. 
        $stmt->execute();
        $stmt->store_result();
 
        // If there have been more than 5 failed logins 
        if ($stmt->num_rows > 5) {
            return true;
        } else {
            return false;
        }
    }
}


function login_check($mysqli) {

    // Check if all session variables are set 
    if (isset($_SESSION['user_id'], 
                        $_SESSION['username'], 
                        $_SESSION['login_string'])) {
 
        $user_id = $_SESSION['user_id'];
        $login_string = $_SESSION['login_string'];
        $username = $_SESSION['username'];
 
        // Get the user-agent string of the user.
        $user_browser = $_SERVER['HTTP_USER_AGENT'];
        if ($stmt = $mysqli->prepare("SELECT password 
                                      FROM raindrops_members 
                                      WHERE id = ? LIMIT 1")) {
            // Bind "$user_id" to parameter. 
            $stmt->bind_param('i', $user_id);
            $stmt->execute();   // Execute the prepared query.
            $stmt->store_result();
            if ($stmt->num_rows == 1) {
                // If the user exists get variables from result.
                $stmt->bind_result($password);
                $stmt->fetch();
                $login_check = hash('sha512', $password . $user_browser);

                if ($login_check == $login_string) {
                    // Logged In!!!! 
                    return true;
                } else {
                    // Not logged in 
                    return false;
                }
            } else {
                // Not logged in 
                return false;
            }
        } else {
            // Not logged in 
            return false;
        }
    } else {
        // Not logged in 
        return false;
    }
}


// ==============================================================================================

/**
* Returns a map of IDs to actual item names
*/
function getItemIdToNameMap($xmlString, $path) {
	$currentArray = array();
	
	if ($xmlString == null) {
		return $currentArray;
	}
	foreach($xmlString->children() as $child) {
		$childType = $child->getName();
		$childId = $child["id"];
		if ($childType == 'item') {
			$currentArray[(string) $childId] = $path . '/' . $child;
		} else {
			$childName = $child["name"];
			$tmpPath = $path . '/' . $childName;
			$currentArray = array_merge($currentArray, getItemIdToNameMap($child, $tmpPath));
		}
	}
	return $currentArray;
}

/**
* Returns a map of IDs to their type
*/
function getItemIdToTypeMap($xmlString) {
	$currentArray = array();
	
	if ($xmlString == null) {
		return $currentArray;
	}
	foreach($xmlString->children() as $child) {
		$childType = $child->getName();
		$childId = $child["id"];
		$itemType = $child["type"];
		if ($childType == 'item') {
			$currentArray[(string) $childId] = $itemType;
		} else {
			$childName = $child["name"];
			$currentArray = array_merge($currentArray, getItemIdToTypeMap($child));
		}
	}
	return $currentArray;
}

/**
* Returns a map of IDs to whether they are activities or not
*/
function getItemIdToIsActivityMap($xmlString) {
	$currentArray = array();
	
	if ($xmlString == null) {
		return $currentArray;
	}
	foreach($xmlString->children() as $child) {
		$childType = $child->getName();
		$childId = $child["id"];
		$isActivity = $child["activity"];
		if ($childType == 'item') {
			$currentArray[(string) $childId] = $isActivity;
		} else {
			$childName = $child["name"];
			$currentArray = array_merge($currentArray, getItemIdToIsActivityMap($child));
		}
	}
	return $currentArray;
}



// ===============================================================================================

function userIDLookup($mysqli, $username) {
	$user_id = -1;
	if ($stmt = $mysqli->prepare("SELECT id
										  FROM raindrops_members 
										  WHERE username = ? LIMIT 1")) {

		$stmt->bind_param('s', $username);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();
		if ($stmt->num_rows >= 1) {
			$stmt->bind_result($user_id);
            $stmt->fetch();
			return $user_id;
		}
	}
	return $user_id;
}

function userLookup($mysqli, $user_id) {
	$user_name = "";
	if ($stmt = $mysqli->prepare("SELECT username
										  FROM raindrops_members 
										  WHERE id = ? LIMIT 1")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();
		if ($stmt->num_rows >= 1) {
			$stmt->bind_result($username);
            $stmt->fetch();
			return $username;
		}
	}
	return $user_name;
}

function userType($mysqli, $user_id) {
	$type = "";
	if ($stmt = $mysqli->prepare("SELECT type
										  FROM raindrops_members 
										  WHERE id = ? LIMIT 1")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();
		if ($stmt->num_rows >= 1) {
			$stmt->bind_result($username);
            $stmt->fetch();
			return $username;
		}
	}
	return $type;
}

function allUserLookup($mysqli) {
	$users = array();
	if ($stmt = $mysqli->prepare("SELECT id, username
										  FROM raindrops_members ORDER BY username ASC")) {
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();
		if ($stmt->num_rows >= 1) {
			$stmt->bind_result($id_child, $username_child);
			while ($stmt->fetch()) {
				$users[$id_child] = $username_child;
			}
		}
	}
	return $users;
}

function findAllTrainees($mysqli) {
	$trainees = array();
	$type = "2";

	if ($stmt = $mysqli->prepare("SELECT id FROM raindrops_members
										  WHERE type = ? ")) {
		$stmt->bind_param('s', $type);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id);
			while ($stmt->fetch()) {
				$trainees[] = $id;
			}
		}
	}
	return $trainees;
}

function findTrainees($mysqli, $parent_id) {
	$trainees = array();

	if ($stmt = $mysqli->prepare("SELECT id_child FROM raindrops_relations 
										  WHERE id_parent = ? ")) {
		$stmt->bind_param('i', $parent_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id_child);
			while ($stmt->fetch()) {
				$trainees[] = $id_child;
			}
		}
	}
	return $trainees;
}


function findTrainer($mysqli, $user_id) {
	$id_parent = -1;
	if ($stmt = $mysqli->prepare("SELECT id_parent FROM raindrops_relations 
										  WHERE id_child = ? LIMIT 1")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();
		if ($stmt->num_rows >= 1) {
			$stmt->bind_result($id_parent);
            $stmt->fetch();
			return $id_parent;
		}
	}
	return $id_parent;
}

function deleteAccount($mysqli, $user_id) {
	$mysqli->query("DELETE FROM raindrops_members WHERE id = $user_id");
	$mysqli->query("DELETE FROM raindrops_relations WHERE id_parent = $user_id OR id_child = $user_id");
}

// ===============================================================================================

// User Progress Functions

function findPermissions($mysqli, $user_id) {
	$permissions = array();

	if ($stmt = $mysqli->prepare("SELECT component, locked, completed FROM raindrops_permissions 
										  WHERE id_user = ? ")) {
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($component, $locked, $completed);
			while ($stmt->fetch()) {
				$permission = array(
					"locked" => $locked,
					"completed" => $completed
				);
				$permissions[$component] = $permission;
			}
		}
	}
	return $permissions;
}

function setItemAsLocked($mysqli, $user_id, $component) {
	if ($stmt = $mysqli->prepare("SELECT completed FROM raindrops_permissions 
										  WHERE component = ? AND id_user = ?")) {
		$stmt->bind_param('si', $component, $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();
		$locked = 1;
		$completed = 0;
		
		if ($stmt->num_rows >= 1) {
			$mysqli->query("UPDATE raindrops_permissions SET locked = 1 WHERE id_user = $user_id AND component = '$component'");
		} else {
			$mysqli->query("INSERT INTO raindrops_permissions(id_user, component, locked, completed) VALUES ($user_id, '$component', $locked, $completed)");
		}
	}
}

function setItemAsUnlocked($mysqli, $user_id, $component) {
	if ($stmt = $mysqli->prepare("SELECT completed FROM raindrops_permissions 
										  WHERE component = ? AND id_user = ?")) {
		$stmt->bind_param('si', $component, $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		$locked = 0;
		$completed = 0;
		
		if ($stmt->num_rows >= 1) {
			$mysqli->query("UPDATE raindrops_permissions SET locked = 0 WHERE id_user = $user_id AND component = '$component'");
		} else {
			$mysqli->query("INSERT INTO raindrops_permissions(id_user, component, locked, completed) VALUES ($user_id, '$component', $locked, $completed)");
		}
	}
}

// ===============================================================================================

// Mail Functions


function checkMessageMediaAccess($mysqli, $curr_user_id, $target_user_id, $file) {
	$allowed = false;

	if ($stmt = $mysqli->prepare("SELECT id FROM raindrops_messages 
										  WHERE id_from = ? AND id_to = ? AND msg_link = ?")) {

		$stmt->bind_param('iis', $curr_user_id, $target_user_id, $file);
		$stmt->execute();
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			$allowed = true;
		}
	}
	return $allowed;
}

function getMessage($mysqli, $user_id, $msg_id, $set_as_read) {
	if ($set_as_read) {
		setMessageAsRead($mysqli, $msg_id);
	}
	if ($stmt = $mysqli->prepare("SELECT id, id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read
									  FROM raindrops_messages 
									  WHERE id = ? LIMIT 1")) {
		$stmt->bind_param('i', $msg_id);
		$stmt->execute();
		$stmt->store_result();
		
		if ($stmt->num_rows == 1) {
			$stmt->bind_result($id, $id_from, $id_to, $msg_title, $msg_content, $msg_link, $msg_type, $msg_date, $msg_read);
			$stmt->fetch();
			if ($id_from == $user_id || $id_to == $user_id) {
				$message = array(
					"id" => $id,
					"id_from" => $id_from,
					"id_to" => $id_to,
					"msg_title" => $msg_title,
					"msg_content" => $msg_content,
					"msg_link" => $msg_link,
					"msg_type" => $msg_type,
					"msg_date" => $msg_date,
					"msg_read" => $msg_read
				);
				return $message;
			}
		}
	}
	return false;
}

function setMessageAsRead($mysqli, $msg_id) {
	mysqli_query($mysqli,"UPDATE raindrops_messages SET msg_read = 1 WHERE id = $msg_id");
}

function setAllMessageAsRead($mysqli, $user_id) {
	mysqli_query($mysqli,"UPDATE raindrops_messages SET msg_read = 1 WHERE id_to = $user_id");
}

function getNumberUnreadMessages($mysqli, $user_id) {
	$num_unread = 0;

	if ($stmt = $mysqli->prepare("SELECT id FROM raindrops_messages 
										  WHERE id_to = ? AND msg_read = 0")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id);
			while ($stmt->fetch()) {
				$num_unread++;
			}
		}
	}
	return $num_unread;
}

function getInbox($mysqli, $user_id) {
	$messages = array();

	if ($stmt = $mysqli->prepare("SELECT id, id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read
										  FROM raindrops_messages 
										  WHERE id_to = ? ORDER BY msg_date DESC")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id, $id_from, $id_to, $msg_title, $msg_content, $msg_link, $msg_type, $msg_date, $msg_read);
			while ($stmt->fetch()) {
				$message = array(
					"id" => $id,
					"id_from" => $id_from,
					"id_to" => $id_to,
					"msg_title" => $msg_title,
					"msg_content" => $msg_content,
					"msg_link" => $msg_link,
					"msg_type" => $msg_type,
					"msg_date" => formatMessageDate($msg_date),
					"msg_read" => $msg_read
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}

function getUnreadMessages($mysqli, $user_id) {
	$messages = array();

	if ($stmt = $mysqli->prepare("SELECT id, id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read
										  FROM raindrops_messages 
										  WHERE id_to = ? AND msg_read = 0 ORDER BY msg_date DESC")) {
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id, $id_from, $id_to, $msg_title, $msg_content, $msg_link, $msg_type, $msg_date, $msg_read);
			while ($stmt->fetch()) {
				$message = array(
					"id" => $id,
					"id_from" => $id_from,
					"id_to" => $id_to,
					"msg_title" => $msg_title,
					"msg_content" => $msg_content,
					"msg_link" => $msg_link,
					"msg_type" => $msg_type,
					"msg_date" => formatMessageDate($msg_date),
					"msg_read" => $msg_read
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}

function getInboxFiltered($mysqli, $user_id, $filter_id) {
	$messages = array();

	if ($stmt = $mysqli->prepare("SELECT id, id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read
										  FROM raindrops_messages 
										  WHERE id_to = ? AND id_from = ? ORDER BY msg_date DESC")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('ii', $user_id, $filter_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id, $id_from, $id_to, $msg_title, $msg_content, $msg_link, $msg_type, $msg_date, $msg_read);
			while ($stmt->fetch()) {
				$message = array(
					"id" => $id,
					"id_from" => $id_from,
					"id_to" => $id_to,
					"msg_title" => $msg_title,
					"msg_content" => $msg_content,
					"msg_link" => $msg_link,
					"msg_type" => $msg_type,
					"msg_date" => $msg_date,
					"msg_read" => $msg_read
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}

function getOutbox($mysqli, $user_id) {
	$messages = array();

	if ($stmt = $mysqli->prepare("SELECT id, id_from, id_to, msg_title, msg_content, msg_link, msg_type, msg_date, msg_read
										  FROM raindrops_messages 
										  WHERE id_from = ? ORDER BY msg_date DESC")) {

		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id, $id_from, $id_to, $msg_title, $msg_content, $msg_link, $msg_type, $msg_date, $msg_read);
			while ($stmt->fetch()) {
				$message = array(
					"id" => $id,
					"id_from" => $id_from,
					"id_to" => $id_to,
					"msg_title" => $msg_title,
					"msg_content" => $msg_content,
					"msg_link" => $msg_link,
					"msg_type" => $msg_type,
					"msg_date" => $msg_date,
					"msg_read" => $msg_read
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}

// ===============================================================================================
// Statistics Functions

function getAllComponentsSummary($mysqli, $user_id) {
	$messages = array();

	if ($stmt = $mysqli->prepare("SELECT id_component, views, viewing_time, timed_activity_time, num_days_accessed
										  FROM raindrops_statistics_summary 
										  WHERE id_user = ?")) {

		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id_component, $views, $viewing_time, $timed_activity_time, $num_days_accessed);
			while ($stmt->fetch()) {
				$message = array(
					"id_component" => $id_component,
					"views" => $views,
					"viewing_time" => $viewing_time,
					"timed_activity_time" => $timed_activity_time,
					"num_days_accessed" => $num_days_accessed
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}


function getComponentSummary($mysqli, $user_id, $content_id) {
	$message = array();

	if ($stmt = $mysqli->prepare("SELECT views, viewing_time, timed_activity_time, num_days_accessed
										  FROM raindrops_statistics_summary 
										  WHERE id_user = ? AND id_component = ? LIMIT 1")) {

		$stmt->bind_param('is', $user_id, $content_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($views, $viewing_time, $timed_activity_time, $num_days_accessed);
			while ($stmt->fetch()) {
				$message = array(
					"views" => $views,
					"viewing_time" => $viewing_time,
					"timed_activity_time" => $timed_activity_time,
					"num_days_accessed" => $num_days_accessed
				);
			}
		}
	}
	return $message;
}

function getAllComponentAccessByDayAndSessions($mysqli, $menuXML, $user_id) {
	$days = array();
	$menuActivityMap = getItemIdToIsActivityMap($menuXML);
	$menuTypeMap = getItemIdToTypeMap($menuXML);
	$entries = getAllComponentAccessAsc($mysqli, $user_id);
	if ($entries != null) {
		$last_processed_datetime = NULL;
		$last_processed_day = NULL;
		$day = array();
		$day_session = array();
		$date_accessed_day = 0;
		foreach ($entries as $entry) {
			$components_completed = array();
			$components_completed[] = $entry["id_component"];
			$is_activity = $menuActivityMap[$entry["id_component"]];
			$component_type = $menuTypeMap[$entry["id_component"]];
			$viewing_time = $entry["viewing_time"];
			
			$video_time = 0;
			$doc_time = 0;
			$activity_time = 0;
			if ($is_activity == 'true') {
				$activity_time = $viewing_time;
			} else {
				if ($component_type == 1) {
					$video_time = $viewing_time;
				} else if ($component_type == 2) {
					$doc_time = $viewing_time;
				}
			}
			
			$date_accessed = $entry['date_accessed'];
			$date_accessed_arr = explode(" ", $date_accessed);
	
			$date_accessed_datetime = strtotime($date_accessed);
			$date_accessed_day = strtotime($date_accessed_arr[0]);
			if ($last_processed_day != NULL) {
				if (($date_accessed_day - $last_processed_day) >= 60*60*24) {
					// New day. Write out previous day.
					$day[] = $day_session;
					$days[(string) $last_processed_day] = $day;
					
					$day = array();
					$day_session = array(
						"components_completed" => $components_completed,
						"video_time" => $video_time,
						"doc_time" => $doc_time,
						"activity_time" => $activity_time,
						"timed_activity_time" => $entry["timed_activity_time"]
					);
					
				} else if (($date_accessed_datetime - $last_processed_datetime) > 60*60*3) {
					// Greater than 3 hours means a new session. Write out previous session
					$day[] = $day_session;
					
					$day_session = array(
						"components_completed" => $components_completed,
						"video_time" => $video_time,
						"doc_time" => $doc_time,
						"activity_time" => $activity_time,
						"timed_activity_time" => $entry["timed_activity_time"]
					);
					
				} else {
					// Add on to existing session
					$updated_components_completed = array_merge($day_session["components_completed"], $components_completed);
					$updated_video_time = $day_session["video_time"] + $video_time;
					$updated_doc_time = $day_session["doc_time"] + $doc_time;
					$updated_activity_time = $day_session["activity_time"] + $activity_time;
					$updated_timed_activity_time = $day_session["timed_activity_time"] + $entry["timed_activity_time"];
					$day_session = array(
						"components_completed" => $updated_components_completed,
						"video_time" => $updated_video_time,
						"doc_time" => $updated_doc_time,
						"activity_time" => $updated_activity_time,
						"timed_activity_time" => $updated_timed_activity_time
					);
					
				}
			} else {
				// First entry processed
				$day_session = array(
					"components_completed" => $components_completed,
					"video_time" => $video_time,
					"doc_time" => $doc_time,
					"activity_time" => $activity_time,
					"timed_activity_time" => $entry["timed_activity_time"]
				);
			}
			
			$last_processed_datetime = $date_accessed_datetime;
			$last_processed_day = $date_accessed_day;
		}
		
		// Write out the remaining session and days
		if ($day_session != null) {
			$day[] = $day_session;
		}
		if ($day != null) {
			$days[(string) $date_accessed_day] = $day;
		}
	}
	
	return $days;
}

function getAllComponentAccessAsc($mysqli, $user_id) {
	$messages = array();

	if ($stmt = $mysqli->prepare("SELECT date_accessed, id_component, viewing_time, timed_activity_time
										  FROM raindrops_statistics_access 
										  WHERE id_user = ? ORDER BY date_accessed ASC")) {

		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($date_accessed, $id_component, $viewing_time, $timed_activity_time);
			while ($stmt->fetch()) {
				$message = array(
					"date_accessed" => $date_accessed,
					"id_component" => $id_component,
					"viewing_time" => $viewing_time,
					"timed_activity_time" => $timed_activity_time
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}

function getMostRecentComponentEntry($mysqli, $user_id, $content_id) {
	$message = array();

	if ($stmt = $mysqli->prepare("SELECT date_accessed, viewing_time, timed_activity_time
										  FROM raindrops_statistics_access 
										  WHERE id_user = ? AND id_component = ? ORDER BY date_accessed DESC LIMIT 1")) {

		$stmt->bind_param('is', $user_id, $content_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($date_accessed, $viewing_time, $timed_activity_time);
			while ($stmt->fetch()) {
				$message = array(
					"date_accessed" => $date_accessed,
					"viewing_time" => $viewing_time,
					"timed_activity_time" => $timed_activity_time
				);
			}
		}
	}
	return $message;
}

function getOverallUserStatistics($mysqli, $user_id) {
	$message = array();

	if ($stmt = $mysqli->prepare("SELECT viewing_time, timed_activity_time, num_completed
										  FROM raindrops_statistics_users 
										  WHERE id_user = ? LIMIT 1")) {

		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($viewing_time, $timed_activity_time, $num_completed);
			while ($stmt->fetch()) {
				$message = array(
					"viewing_time" => $viewing_time,
					"timed_activity_time" => $timed_activity_time,
					"num_completed" => $num_completed
				);
			}
		}
	}
	return $message;
}

// ===============================================================================================
// Completed Leader Functions

function getCompletedLeaders($mysqli) {
	$messages = array();
	if ($stmt = $mysqli->prepare("SELECT id_user, num_completed
										  FROM raindrops_statistics_users ORDER BY num_completed DESC")) {

		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			// If the user exists get variables from result.
			$stmt->bind_result($id_user, $num_completed);
			while ($stmt->fetch()) {
				$message = array(
					"id_user" => $id_user,
					"num_completed" => $num_completed
				);
				$messages[] = $message;
			}
		}
	}
	return $messages;
}

// ===============================================================================================
// Points Leader Functions

function getLifetimePoints($mysqli, $user_id) {
	$points = -1;

	if ($stmt = $mysqli->prepare("SELECT lifetime_points FROM raindrops_points 
										  WHERE id_user = ?")) {
		// Bind "$user_id" to parameter. 
		$stmt->bind_param('i', $user_id);
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			$points = 0;
			// If the user exists get variables from result.
			$stmt->bind_result($lifetime_points);
			while ($stmt->fetch()) {
				$points += $lifetime_points;
			}
		}
	}
	return $points;
}

function getPointLeaders($mysqli) {
	$leaders = array();

	if ($stmt = $mysqli->prepare("SELECT id_user, lifetime_points FROM raindrops_points ORDER BY lifetime_points DESC")) {
		// Bind "$user_id" to parameter. 
		$stmt->execute();   // Execute the prepared query.
		$stmt->store_result();

		if ($stmt->num_rows >= 1) {
			$points = 0;
			// If the user exists get variables from result.
			$stmt->bind_result($user_id, $lifetime_points);
			while ($stmt->fetch()) {
				$leader = array(
					"id_user" => $user_id,
					"lifetime_points" => $lifetime_points
				);
				$leaders[] = $leader;
			}
		}
	}
	return $leaders;
}

// ===

function formatDate($date) {
	$dateTime = strtotime($date);
	return date("M j, Y g:i a", $dateTime);
}

function formatMessageDate($date) {
	$now = strtotime("now");
	$dateTime = strtotime($date);
	
	$nowDay = date("Y-m-d", $now);
	$day = date("Y-m-d", $dateTime);
	if ($nowDay != $day) {
		return date("M d", $dateTime);
	} else {
		return date("g:i a", $dateTime);
	}
}

?>