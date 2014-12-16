<?php
include_once '../config.php';
include_once 'functions.php';


$mysqli = new mysqli($db_host, $db_username, $db_password, $db_database);
if (mysqli_connect_errno()) {
	printf("Connect failed: %s\n", mysqli_connect_error());
	exit();
}

sec_session_start();
 
if (!login_check($mysqli)) {
	header("Location: ../login");
	exit();
}

$user_id = $_SESSION['user_id'];
$current_user = $_SESSION['username'];

$error_msg = "";

if (isset($_POST['username'], $_POST['p'])) {

    // Sanitize and validate the data passed in
	$username = mysqli_real_escape_string($mysqli, $_POST['username']);
	$password =  mysqli_real_escape_string($mysqli, $_POST['p']);
	$type = 2; // Trainee type

    if (strlen($password) != 128) {
        // The hashed pwd should be 128 characters long.
        // If it's not, something really odd has happened
        $error_msg .= '<p class="error">Invalid password configuration.</p>';
    }
 
    // Username validity and password validity have been checked client side.
    // This should should be adequate as nobody gains any advantage from
    // breaking these rules.
    //
 
    $prep_stmt = "SELECT id FROM raindrops_members WHERE username = ? LIMIT 1";
    $stmt = $mysqli->prepare($prep_stmt);
 
    if ($stmt) {
        $stmt->bind_param('s', $username);
        $stmt->execute();
        $stmt->store_result();
 
        if ($stmt->num_rows == 1) {
            // A user with this username address already exists
            $error_msg .= '<p class="error">A user with this username address already exists.</p>';
        }
    } else {
        $error_msg .= '<p class="error">Database error</p>';
    }
 
	if ($error_msg == "") {
	
        // Create a random salt
        $random_salt = hash('sha512', uniqid(openssl_random_pseudo_bytes(16), TRUE));
 
        // Create salted password 
        $password = hash('sha512', $password . $random_salt);
 
        // Insert the new user into the database 
        if ($insert_stmt = $mysqli->prepare("INSERT INTO raindrops_members (type, username, password, salt) VALUES (?, ?, ?, ?)")) {
		
            $insert_stmt->bind_param('isss', $type, $username, $password, $random_salt);
            // Execute the prepared query.
            if (! $insert_stmt->execute()) {
				$error_msg .= '<p class="error">Database error</p>';
            }
        }
    }
	
	$id_child = 0;
	$prep_stmt = "SELECT id FROM raindrops_members WHERE username = ? LIMIT 1";
    $stmt = $mysqli->prepare($prep_stmt);
 
    if ($stmt) {
        $stmt->bind_param('s', $username);
        $stmt->execute();
        $stmt->store_result();
 
        if ($stmt->num_rows == 1) {
			$stmt->bind_result($id_new_user);
			$stmt->fetch();
			$id_child = $id_new_user;
        }
    } else {
        $error_msg .= '<p class="error">Database error</p>';
    }
	
	if (!$id_child) {
		$error_msg .= '<p class="error">Database error</p>';
	}
	
    if ($error_msg == "") {
	
		
        if ($insert_stmt = $mysqli->prepare("INSERT INTO raindrops_relations (id_parent, id_child) VALUES (?, ?)")) {
		
            $insert_stmt->bind_param('ii', $user_id, $id_child);
            // Execute the prepared query.
            if (! $insert_stmt->execute()) {
				header('Location: ../register_trainer?err=1');
            }
        }
        header('Location: ../index?success=1');
    }
} else {
	header('Location: ../register_trainer?err=1');
}
?>