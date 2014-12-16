<?php

	function generateRandomString($length = 8) {
		$characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
		$randomString = '';
		for ($i = 0; $i < $length; $i++) {
			$randomString .= $characters[rand(0, strlen($characters) - 1)];
		}
		return $randomString;
	}

	// XML files cannot handle special chars such as & - replace & with 'and'
	function cleanName($name) {
		$name = str_replace("&", "and", $name);
		return $name;
	}
?>