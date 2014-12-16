<?php
	// Contains methods to clean the XML file
	// @author: Tom Jin
	// @date: May 31, 2013
	
	// XML files cannot handle special chars such as & - replace & with 'and'
	function cleanName($name) {
		$name = str_replace("&", "and", $name);
		return $name;
	}
?>