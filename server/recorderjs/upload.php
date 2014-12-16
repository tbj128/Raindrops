<?php    
	session_start(); 

    // Writes recorded audiofile to the server folder
    $audioName = generateRandomString() . '.wav';
    $file = "../upload/audio/" . $audioName;

	if (move_uploaded_file($_FILES["audio"]["tmp_name"], $file)) {
		echo $audioName;
	}
	chmod($file, 0777);

    // ---
    
	function generateRandomString($length = 10) {
		$characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
		$randomString = '';
		for ($i = 0; $i < $length; $i++) {
			$randomString .= $characters[rand(0, strlen($characters) - 1)];
		}
		return $randomString;
	}
?>