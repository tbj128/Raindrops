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
		header("Location: login.php");
	}
	
	$user_id = $_SESSION['user_id'];
	$username = $_SESSION['username'];
	
	if(isset($_GET['file']) && isset($_GET['inbox']) && isset($_GET['id'])) {
		if ($_GET['inbox'] == 1) {
			// Media contents are located within this current user's folder
			$path = 'messages/' . $user_id . '/' . $_GET['file'];
			$orig = $_GET['file'];
			
			$mime_type = "";
			if (false !== strpos($orig,'.3gp')) {
				$mime_type = "audio/3gpp";
			} else if (false !== strpos($orig,'.wav')) {
				$mime_type = "audio/x-wav";
			} else if (false !== strpos($orig,'.mp4')) {
				$mime_type = "video/mp4";
			} else if (false !== strpos($orig,'.mp3')) {
				$mime_type = "audio/mpeg, audio/x-mpeg, audio/x-mpeg-3, audio/mpeg3";
			}
			
			header('Content-Type: ' . $mime_type);
// 			header('Content-disposition: filename="' . $orig);
// 			readfile($path);

				rangeDownload($path);
		} else {
			// Media contents are located in the recipient's folder
			if (checkMessageMediaAccess($mysqli, $user_id, $_GET['id'], $_GET['file'])) {
				$path = 'messages/' . $_GET['id'] . '/' . $_GET['file'];
				$orig = $_GET['file'];
				
				
				
				$mime_type = "";
				if (false !== strpos($orig,'.3gp')) {
					$mime_type = "audio/3gpp";
				} else if (false !== strpos($orig,'.wav')) {
					$mime_type = "audio/x-wav";
				} else if (false !== strpos($orig,'.mp4')) {
					$mime_type = "video/mp4";
				} else if (false !== strpos($orig,'.mp3')) {
					$mime_type = "audio/mpeg3";
				}
			
				header('Content-Type: ' . $mime_type);
// 				header('Content-disposition: filename="' . $orig);
// 				readfile($path);
				rangeDownload($path);
			} else {
				echo 'error 403 - outbox media denied';
			}
		}
	} else {
		echo 'error 401 - URL error';
	}


	// https://bitbucket.org/thomthom/php-resumable-download-server

	function rangeDownload($file) {
 
		$fp = @fopen($file, 'rb');
 
		$size   = filesize($file);
		$length = $size;
		$start  = 0;
		$end    = $size - 1;
		
		header("Accept-Ranges: 0-$length");
		
		if (isset($_SERVER['HTTP_RANGE'])) {
 
			$c_start = $start;
			$c_end   = $end;
			// Extract the range string
			list(, $range) = explode('=', $_SERVER['HTTP_RANGE'], 2);
			// Make sure the client hasn't sent us a multibyte range
			if (strpos($range, ',') !== false) {
 
				// (?) Shoud this be issued here, or should the first
				// range be used? Or should the header be ignored and
				// we output the whole content?
				header('HTTP/1.1 416 Requested Range Not Satisfiable');
				header("Content-Range: bytes $start-$end/$size");
				// (?) Echo some info to the client?
				exit;
			}
			// If the range starts with an '-' we start from the beginning
			// If not, we forward the file pointer
			// And make sure to get the end byte if spesified
			if ($range0 == '-') {
 
				// The n-number of the last bytes is requested
				$c_start = $size - substr($range, 1);
			}
			else {
 
				$range  = explode('-', $range);
				$c_start = $range[0];
				$c_end   = (isset($range[1]) && is_numeric($range[1])) ? $range[1] : $size;
			}
			/* Check the range and make sure it's treated according to the specs.
			 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			 */
			// End bytes can not be larger than $end.
			$c_end = ($c_end > $end) ? $end : $c_end;
			// Validate the requested range and return an error if it's not correct.
			if ($c_start > $c_end || $c_start > $size - 1 || $c_end >= $size) {
 
				header('HTTP/1.1 416 Requested Range Not Satisfiable');
				header("Content-Range: bytes $start-$end/$size");
				// (?) Echo some info to the client?
				exit;
			}
			$start  = $c_start;
			$end    = $c_end;
			$length = $end - $start + 1; // Calculate new content length
			fseek($fp, $start);
			header('HTTP/1.1 206 Partial Content');
		}
		// Notify the client the byte range we'll be outputting
		header("Content-Range: bytes $start-$end/$size");
		header("Content-Length: $length");
 
		// Start buffered download
		$buffer = 1024 * 8;
		while(!feof($fp) && ($p = ftell($fp)) <= $end) {
 
			if ($p + $buffer > $end) {
 
				// In case we're only outputtin a chunk, make sure we don't
				// read past the length
				$buffer = $end - $p + 1;
			}
			set_time_limit(0); // Reset time limit for big files
			echo fread($fp, $buffer);
			flush(); // Free up memory. Otherwise large files will trigger PHP's memory limit.
		}
 
		fclose($fp);
	}
?>