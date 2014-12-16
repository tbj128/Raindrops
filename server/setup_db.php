<?php
	session_start();
		
	include 'config.php';

	if( isset($_SESSION['admin_username']) && 
		isset($_SESSION['admin_password'])) {
		
		$admin_username = $_SESSION['admin_username'];
		$admin_password = $_SESSION['admin_password'];
		$connection = new mysqli($db_host, $db_username, $db_password);
		if (mysqli_connect_errno()) {
			printf("Connect failed: %s\n", mysqli_connect_error());
			exit();
		}
		
		$sql_create_db="CREATE DATABASE $db_database";
		if (mysqli_query($connection,$sql_create_db)) {
			mysqli_select_db($connection, $db_database);
			
			$sql_create_table = array();
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_content` (`content_id` int(10) NOT NULL AUTO_INCREMENT,`name` varchar(255) NOT NULL,`type` int(10) NOT NULL,`location` varchar(255) NOT NULL,`requries` int(10) NOT NULL,`is_activity` int(10) NOT NULL,`desc` varchar(255) NOT NULL, PRIMARY KEY (`content_id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_login_attempts` (`user_id` int(11) NOT NULL,`time` varchar(30) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_members` (`id` int(11) NOT NULL AUTO_INCREMENT,`type` varchar(30) NOT NULL,`username` varchar(30) NOT NULL,`password` char(128) NOT NULL,`salt` char(128) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=18 ;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_messages` (`id` int(10) NOT NULL AUTO_INCREMENT,`id_from` int(10) NOT NULL,`id_to` int(10) NOT NULL,`msg_title` varchar(128) NOT NULL,`msg_content` text NOT NULL,`msg_link` varchar(128) NOT NULL,`msg_type` int(10) NOT NULL,`msg_date` datetime NOT NULL,`msg_read` int(4) NOT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=79 ;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_permissions` (`id_user` int(10) NOT NULL,`component` varchar(128) NOT NULL,`locked` int(4) NOT NULL,`completed` int(4) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_relations` (`id_parent` int(10) NOT NULL,`id_child` int(10) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_points` (`id_user` int(10) NOT NULL,`lifetime_points` int(10) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_statistics_access` (`date_accessed` DATETIME NOT NULL ,`id_user` INT( 10 ) NOT NULL ,`id_component` varchar(128) NOT NULL,`viewing_time` INT( 10 ) NOT NULL ,`timed_activity_time` INT( 10 ) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_statistics_summary` (`id_user` INT( 10 ) NOT NULL ,`id_component` varchar(128) NOT NULL,`views` INT( 10 ) NOT NULL ,`viewing_time` INT( 10 ) NOT NULL ,`timed_activity_time` INT( 10 ) NOT NULL ,`num_days_accessed` INT( 10 ) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
			$sql_create_table[] = "CREATE TABLE IF NOT EXISTS `raindrops_statistics_users` (`id_user` INT( 10 ) NOT NULL ,`viewing_time` INT( 10 ) NOT NULL ,`timed_activity_time` INT( 10 ) NOT NULL ,`num_completed` INT( 10 ) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=latin1;";

			
			foreach($sql_create_table as $sql_table) {
				if (!mysqli_query($connection,$sql_table)) {
					// Error executing SQL create table
					header("Location: setup.php?err=1");
				}
			}
			// Create a random salt
			$random_salt = hash('sha512', uniqid(openssl_random_pseudo_bytes(16), TRUE));
	 
			// Create salted password 
			$password_first_hash = hash('sha512', $admin_password);
			$password = hash('sha512', $password_first_hash . $random_salt);
			
			$type = "admin";
	 
			// Insert the new user into the database 
			if ($insert_stmt = $connection->prepare("INSERT INTO raindrops_members (username, type, password, salt) VALUES (?, ?, ?, ?)")) {
				$insert_stmt->bind_param('ssss', $admin_username, $type, $password, $random_salt);
				// Execute the prepared query.
				if (! $insert_stmt->execute()) {
					header('Location: register.php?err=1');
				}
			}
			header('Location: login.php?welcome=1');
		}
		else {
			echo "Error creating database: " . mysqli_error($connection);
		}

	} else {
		// Error
		header("Location: setup.php");
	}
		
?>