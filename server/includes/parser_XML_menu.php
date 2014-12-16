<?php
	// Overview: Contains methods to parse and generate the menu layout on the page 
	// from a give XML string parameter
	// @author: Tom Jin
	// @date: May 14, 2013
	// @modified: Dec 28, 2013
	
	define("MEDIA_VIDEO", 1);
	define("MEDIA_RICH_TEXT", 2);
	
	// Function: Gets the name of an element based on their ID
	function getElementName($xpath, $id) {
		if (!isset($id)) {
			return '';
		}

		$name = '';
		foreach($xpath->query('//menu[@id=\'' . $id . '\']') as $node) {
		  $name = $node->getAttribute("name");
		}

		foreach($xpath->query('//item[@id=\'' . $id . '\']') as $node) {
			$name = $node;
		}
		
		return $name;
	}
	
	
	// Function: Recursive method to create the main "page-like" representation of each menu layer
	// Each menu layer contains either submenus (blue) or videos/activities (green)
	// $xmlString = the XML to be parsed and rendered as a "page"
	// $level = the current nested level of the page within the menu XML (to determine indentation)
	// $dataPath = the path of the current nested menu XML to the root
	function parseXML($xmlString, $level, $dataPath) {
		if ($xmlString == null) {
			return;
		}
		
		$currType = $xmlString->getName();
		$currID = $xmlString["id"];
		$currIDToDisplay = $xmlString["name"];
		$currRequires = $xmlString["requires"];
		$currText = $xmlString["text"];
		if ($dataPath != '') {
			$dataPath = $dataPath . "," . "\"" . $currID . "\"";
		} else {
			$dataPath = "\"" . $currID . "\"";
		}
		$indentation = -30;
		
		for ($i=0;$i<$level;$i++) {
			$indentation += 30;
		}
	
		echo '<div class="preview" id="'.$currID.'" style="left:'. $indentation .'px;width:'.(832-$indentation).'px;">
					<div class="preview-content">';
							

		// Process video/activity child elements first
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
		
			if ($childType == 'video' || $childType == 'activity') {
				$currName = $child;	
			} else {
				//$currName = ucfirst(strtolower(str_replace("_", " ", $childID)));
				$currName = $child['name'];
			}
			
			
			echo '<div data-id="'.$childID.'" data-path=\'['.$dataPath.']\' class="preview-item preview-' . $childType . '-item transition-all">
						<h6>' . $childType . '</h6>
						<p>' . $currName . '</p>
				  </div>';
			
		}
		
		echo '		<div id="" data-parent=\'' . $currID .'\' data-path=\'['.$dataPath.']\' class="preview-item preview-add-item transition-all">
						<h6>+ Add New Item</h6>
					</div>
				</div>
				<span class="preview-identifier">' . $currIDToDisplay .'</span>
			</div>';
		
		// Process submenu items
		foreach($xmlString->children() as $child) {
			if ($child->getName() == 'menu') {
				parseXML($child, $level + 1, $dataPath);
			}
		}
		
	} // End of function
	
	
	// Function: Generates the left sidebar menu structure that summarizes the "page-like" 
	// representation of the menu XML (recursive)
	// $xmlString = the XML to be parsed and rendered as a "page"
	// $level = the current nested level of the page within the menu XML (to determine indentation)
	// $dataPath = the path of the current nested menu XML to the root
	function renderSidebar($xmlString, $level, $dataPath, $dataTextPath) {
		if ($xmlString == null) {
			return;
		}
		
		$currType = $xmlString->getName();
		$currID = $xmlString["id"];
		$currIDToDisplay = $xmlString["name"];
		$currRequires = $xmlString["requires"];
		$currText = $xmlString["text"];
		
		$indentation = -15;
		for ($i=-1;$i<$level;$i++) {
			$indentation += 15;
		}

		
		if ($dataPath == '') {
			$dataPathTmp = $currID;
			$dataTextPathTmp = $currIDToDisplay;
		}
		
		echo '<ul class="nav nav-list left" id="s-' . $currID . '" >';
		
				
		if ($level == 0) {
			echo '<li class="nav-header left" style="padding-left:' . $indentation . 'px;"><a data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-child="' . $currID . '" class="side-nav-item leftbar-menu" href="#"><span class="fa fa-list spacer-right"></span>' . $dataTextPath . '</a>';
			renderSidebar($xmlString, 1, $dataPath, $dataTextPath);
			echo '</li>';
		} else {
			foreach($xmlString->children() as $child) {
				$childType = $child->getName();
				$childID = $child["id"];
				
				if ($childType == 'menu') {
					$currName = $child["name"];	
					if ($dataPath != '') {
						$dataPathTmp = $dataPath . "," . $childID;
						$dataTextPathTmp = $dataTextPath . "," . $currName;
					} else {
						$dataPathTmp = $childID;
						$dataTextPathTmp =  $currName;
					}
					echo '<li class="nav-header left" style="padding-left:' . $indentation . 'px;"><a data-textpath="' . $dataTextPathTmp . '" data-path="' . $dataPathTmp . '" data-child="' . $childID . '" class="side-nav-item leftbar-menu" href="#"><span class="fa fa-list spacer-right"></span>' . $currName . '</a>';
					
					renderSidebar($child, $level + 1, $dataPathTmp, $dataTextPathTmp);
					
					echo '</li>';
				}
			}
		}

		echo '</ul>';
		
		
	}
	
	
	
	// $xmlString = the XML to be parsed and rendered as a "page"
	// $level = the current nested level of the page within the menu XML (to determine indentation)
	// $dataPath = the path of the current nested menu XML to the root
	function renderEditWindow($xmlString, $level, $dataPath, $dataTextPath, $path) {
		if ($xmlString == null) {
			return;
		}
		
		$currType = $xmlString->getName();
		$currID = $xmlString["id"];
		$currIDToDisplay = $xmlString["name"];
		$currRequires = $xmlString["requires"];
		$currText = $xmlString["text"];

		if ($level == 1 && $path == '') {
			echo '<ul id="' . $currID . '" class="nav nav-list edit-window">';
		} else {
			if (strpos($path, ',') !== false) {
				$navMatch = substr($path, strrpos($path, ",") + 1);
				if ($navMatch == $currID) {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window">';
				} else {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window" style="display:none;">';
				}
			} else {
				$navMatch = $path;
				if ($navMatch == $currID) {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window">';
				} else {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window" style="display:none;">';
				}
			}
		}
		
		echo '<li class=""><h4>' . $currIDToDisplay . '</h4></li>';
			
		foreach($xmlString->children() as $child) {
			$childElement = $child->getName();
			$childID = $child["id"];
			$childType = $child["type"];
			
		
			if ($childElement == 'item' && $childType == MEDIA_VIDEO) {
				$currName = $child;	
				echo '<li class="left table-bordered"><a id="edit" class="edit-item" data-type="' . $child["type"] . '" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-id="'.$childID.'" href="manager_view?id=' . $childID . '" target="_blank"><span class="fa fa-film spacer-right"></span>' . $currName . '</a></li>';
			}
					
			if ($childElement == 'item' && $childType == MEDIA_RICH_TEXT) {
				$currName = $child;	
				echo '<li class="left table-bordered"><a id="edit" class="edit-item" data-type="' . $child["type"] . '" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-id="'.$childID.'" href="manager_view?id=' . $childID . '" target="_blank"><span class="fa fa-pencil spacer-right"></span>' . $currName . '</a></li>';
			}
		}
		
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			if ($childType == 'menu') {
				$currName = $child["name"];	
				echo '<li class="nav-header left table-bordered"><a id="edit" class="edit-menu" href="#" data-type="0" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-id="'.$childID.'"><span class="fa fa-list spacer-right"></span>' . $currName . '</a></li>';
			}
		}
		
		echo '<li class="nav-header left table-bordered"><a id="edit-add-new" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" class="edit-add-new" href="manager_select?id=' . $currID . '&p=' . $dataPath . '"><span class="fa fa-plus spacer-right"></span>Add New Item</a></li>';
		
		echo '</ul>';
		
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			if ($childType == 'menu') {
				$currName = $child["name"];	
				if ($dataPath != '') {
					$dataPathTmp = $dataPath . "," . $childID;
					$dataTextPathTmp = $dataTextPath . "," . $currName;
				}
				renderEditWindow($child, $level + 1, $dataPathTmp, $dataTextPathTmp, $path);
			}
		}
	}
	
	
	
	function renderProgressWindow($xmlString, $level, $dataPath, $dataTextPath, $permissions, $path) {
		if ($xmlString == null) {
			return;
		}
		
		$currType = $xmlString->getName();
		$currID = $xmlString["id"];
		$currIDToDisplay = $xmlString["name"];
		$currRequires = $xmlString["requires"];
		$currText = $xmlString["text"];

		if ($level == 1 && $path == '') {
			echo '<ul id="' . $currID . '" class="nav nav-list edit-window">';
		} else {
			if (strpos($path, ',') !== false) {
				$navMatch = substr($path, strrpos($path, ",") + 1);
				if ($navMatch == $currID) {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window">';
				} else {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window" style="display:none;">';
				}
			} else {
				$navMatch = $path;
				if ($navMatch == $currID) {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window">';
				} else {
					echo '<ul id="' . $currID . '" class="nav nav-list edit-window" style="display:none;">';
				}
			}
		}
		
		echo '<li class=""><h4>' . $currIDToDisplay . '</h4></li>';
			
		foreach($xmlString->children() as $child) {
			$childElement = $child->getName();
			$childID = (string) $child["id"];
			$childType = $child["type"];
			
			$itemLocked = '';
			$itemCompleted = '';
			if (isset($permissions[$childID])) {
				if ($permissions[$childID]['locked'] == 1) {
					$itemLocked = '&nbsp;&nbsp;<i class="fa fa-lock"></i> ';
				}
				if ($permissions[$childID]['completed'] == 1) {
					$itemCompleted = '&nbsp;&nbsp;<i class="fa fa-check"></i> ';
				}
			}
		
			if ($childElement == 'item' && $childType == MEDIA_VIDEO) {
				$currName = $child;	
				echo '<li class="left table-bordered"><a id="edit" class="edit-item" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-id="'.$childID.'" href="#"><span class="fa fa-film spacer-right"></span>' . $currName . $itemLocked . $itemCompleted . '</a></li>';
			}
					
			if ($childElement == 'item' && $childType == MEDIA_RICH_TEXT) {
				$currName = $child;	
				echo '<li class="left table-bordered"><a id="edit" class="edit-item" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-id="'.$childID.'" href="#"><span class="fa fa-pencil spacer-right"></span>' . $currName . $itemLocked . $itemCompleted . '</a></li>';
			}
		}
		
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = (string) $child["id"];
			
			$itemLocked = '';
			$itemCompleted = '';
			if (isset($permissions[$childID])) {
				if ($permissions[$childID]['locked'] == 1) {
					$itemLocked = '&nbsp;&nbsp;<i class="fa fa-lock"></i> ';
				}
				if ($permissions[$childID]['completed'] == 1) {
					$itemCompleted = '&nbsp;&nbsp;<i class="fa fa-check"></i> ';
				}
			}
			
			if ($childType == 'menu') {
				$currName = $child["name"];	
				echo '<li class="nav-header left table-bordered"><a id="edit" class="edit-menu" href="#" data-textpath="' . $dataTextPath . '" data-path="' . $dataPath . '" data-id="'.$childID.'"><span class="fa fa-list spacer-right"></span>' . $currName .  $itemLocked . $itemCompleted . '</a></li>';
			}
		}
		
		echo '</ul>';
		
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			if ($childType == 'menu') {
				$currName = $child["name"];	
				if ($dataPath != '') {
					$dataPathTmp = $dataPath . "," . $childID;
					$dataTextPathTmp = $dataTextPath . "," . $currName;
				}
				renderProgressWindow($child, $level + 1, $dataPathTmp, $dataTextPathTmp, $permissions, $path);
			}
		}
	}
	
	
	function renderSingleEditWindow($xmlString, $parentID, $dataPath) {
		if ($xmlString == null) {
			return;
		}
		
		$currType = $xmlString->getName();
		$currID = $xmlString["id"];
		$currIDToDisplay = $xmlString["name"];
		$currRequires = $xmlString["requires"];
		$currText = $xmlString["text"];
		if ($dataPath != '') {
			$dataPath = $dataPath . "," . "\"" . $currID . "\"";
		} else {
			$dataPath = "\"" . $currID . "\"";
		}

		if ($currID == $parentID || $parentID == 0) {
			
			echo '<ul class="nav nav-list edit-window">';
			echo '<li class=""><h4>' . $currIDToDisplay . '</h4></li>';
				
			foreach($xmlString->children() as $child) {
				$childElement = $child->getName();
				$childID = $child["id"];
				$childType = $child["type"];
				
			
				if ($childElement == 'item' && $childType == MEDIA_VIDEO) {
					$currName = $child;	
					echo '<li class="left table-bordered"><a class="edit-item" data-scroll="'.$currID.'" href="#"><span class="fa fa-film spacer-right"></span>' . $currName . '</a></li>';
				}
						
				if ($childElement == 'item' && $childType == MEDIA_RICH_TEXT) {
					$currName = $child;	
					echo '<li class="left table-bordered"><a class="edit-item" data-scroll="'.$currID.'" href="#"><span class="fa fa-pencil spacer-right"></span>' . $currName . '</a></li>';
				}
			}
			
			foreach($xmlString->children() as $child) {
				$childType = $child->getName();
				$childID = $child["id"];
				if ($childType == 'menu') {
					$currName = $child["name"];	
					echo '<li class="nav-header left table-bordered"><a class="edit-menu" href="#" data-scroll="'.$childID.'"><span class="fa fa-list spacer-right"></span>' . $currName . '</a></li>';
				}
			}
			
			echo '<li class="nav-header left table-bordered"><a id="edit-add-new" class="edit-add-new" href="#"><span class="fa fa-plus spacer-right"></span>Add New Item</a></li>';
			
			echo '</ul>';
			
		} else {
		
			foreach($xmlString->children() as $child) {
				$childType = $child->getName();
				$childID = $child["id"];
				if ($childType == 'menu') {
					renderEditWindow($child, $parentID, $dataPath);
				}
			}
		
		}
	}
	
	// Finds and returns information regarding an item
	// Do not use this if you require knowledge of the full path to this element (use getItem instead)
	function findItem($xpath, $id) {
		foreach($xpath->query('//item[@id=\'' . $id . '\']') as $node) {
			$item = array(
				"id" => $node->getAttribute("id"),
				"type" => $node->getAttribute('type'),
				"name" => $node->nodeValue,
				"desc" => $node->getAttribute('desc'),
				"requires" => $node->getAttribute('requires'),
				"source" => $node->getAttribute('path'),
				"activity" => $node->getAttribute('activity')
			);
			return $item;
		}
		return null;
	}

	// Finds and returns information regarding an menu
	function findMenu($xpath, $id) {
		foreach($xpath->query('//menu[@id=\'' . $id . '\']') as $node) {
			$item = array(
				"id" => $node->getAttribute("id"),
				"name" => $node->getAttribute("name"),
				"desc" => $node->getAttribute('desc'),
				"requires" => $node->getAttribute('requires')
			);
			return $item;
		}
		return null;
	}
	
	function getItem($xmlString, $level, $dataPath, $dataTextPath, $id_to_find) {
		if ($xmlString == null) {
			return false;
		}

		foreach($xmlString->children() as $child) {
			$childElement = $child->getName();
			$childID = $child["id"];
			if ($childID == $id_to_find) {
				if ($childElement == 'item') {
					$childName = $child;	
					$childType = $child["type"];
					$item = array(
						"id" => $child["id"],
						"type" => $childType,
						"name" => $childName,
						"desc" => $child['desc'],
						"requires" => $child['requires'],
						"path" => $child['path'],
						"text_path" => $dataTextPath,
						"activity" => $child['activity']
					);
					return $item;
				} else {
					$childType = 0;
					$item = array(
							"id" => $child["id"],
							"type" => $childType,
							"name" => $child['name'],
							"desc" => $child['desc'],
							"requires" => $child['requires'],
							"path" => "",
							"text_path" => $dataTextPath,
							"activity" => ""
						);
					return $item;
				}
			}
		}

		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			if ($childType == 'menu') {
				$currName = $child["name"];	
				if ($dataPath != '') {
					$dataPathTmp = $dataPath . "," . $childID;
					$dataTextPathTmp = $dataTextPath . "," . $currName;
				}
				$innerItem = getItem($child, $level + 1, $dataPathTmp, $dataTextPathTmp, $id_to_find);
				
				if ($innerItem) {
					return $innerItem;
				}
			}
		}
	}

	// Function: Generates the requires popup box (flat format)
	// Populates with all the current accessible elements of the menu
	// $xmlString = the current menu XML to consider
	function genRequires2($xmlString) {
		if ($xmlString == null) {
			return;
		}
		
		echo '<div class="span3">';
		echo '<label class="checkbox"><input id="cb-'.$xmlString["id"].'" checked="checked" type="checkbox" onClick="checkGroup("cb-'.$xmlString["id"].'", "a,b")"><h3>'.$xmlString["name"].'</h3></label>';
			
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			
			
			if ($childType == 'video' || $childType == 'activity') {
				echo '<label class="checkbox"><input id="cb-'.$childID.'" checked="checked" type="checkbox" >'.$child.'</label>';
			} else {
				$childName = $child["name"];
				echo '<label class="checkbox"><input id="cb-'.$childID.'" checked="checked" type="checkbox" onClick="checkGroup("cb-'.$childID.'", "a,b")">'.$childName.'</label>';
			}
		}
		echo '</div>';
		
		foreach($xmlString->children() as $child) {
			if ($childType == 'menu') {
				genRequires($child);
			}
		}

	} // End of function
						
						
	
	// Function: Generates the left sidebar menu structure that summarizes the "page-like" 
	// representation of the menu XML (recursive)
	// $xmlString = the XML to be parsed and rendered as a "page"
	// $level = the current nested level of the page within the menu XML (to determine indentation)
	// $dataPath = the path of the current nested menu XML to the root
	function getRequires($xmlString, $level, $dataPath) {
		if ($xmlString == null) {
			return;
		}
		
		$currID = $xmlString["id"];
		if ($dataPath != '') {
			$dataPath = $dataPath . "," . "\"" . $currID . "\"";
		} else {
			$dataPath = "\"" . $currID . "\"";
		}
		$indentation = -20;
		
		for ($i=0;$i<$level;$i++) {
			$indentation += 20;
		}
	
		
		echo '<ul class="nav nav-list">';
		
		
		// Process submenus 
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			if ($childType == 'menu') {
				$currName = $child["name"];
				echo '<li class="nav-header" style="padding-left:' . $indentation . 'px;"><label class="checkbox"><input type="checkbox" id="cb-' . $childID . '" onClick="EpicWheels.Main.checkChildren(\'cb-' . $childID . '\');">'.$currName.' ('.$childType.')</label>';
				
				getRequires($child, $level + 1, $dataPath);
				
				echo '</li>';
			} else if ($childType == 'item') {
				$currName = $child;
				echo '<li class="nav-header" style="padding-left:' . $indentation . 'px;"><label class="checkbox"><input type="checkbox" id="cb-' . $childID . '" onClick="EpicWheels.Main.checkChildren(\'cb-' . $childID . '\');">'.$currName.' ('.$childType.')</label>';
				
				echo '</li>';
			}
		}

		echo '</ul>';
		
		
	} // End of function	
	
		
	function getKnownRequires($xmlString, $level, $dataPath, $requires) {
		if ($xmlString == null) {
			return;
		}
		
		$currID = $xmlString["id"];
		if ($dataPath != '') {
			$dataPath = $dataPath . "," . "\"" . $currID . "\"";
		} else {
			$dataPath = "\"" . $currID . "\"";
		}
		$indentation = -20;
		
		for ($i=0;$i<$level;$i++) {
			$indentation += 20;
		}
	
		
		echo '<ul class="nav nav-list">';
		
		
		// Process submenus 
		foreach($xmlString->children() as $child) {
			$childType = $child->getName();
			$childID = $child["id"];
			
			$checked = '';
			if (in_array($childID, $requires)) {
				$checked = 'checked';
			}
			
			if ($childType == 'menu') {
				$currName = $child["name"];
				echo '<li class="nav-header" style="padding-left:' . $indentation . 'px;"><label class="checkbox"><input type="checkbox" id="cb-' . $childID . '" onClick="EpicWheels.Main.checkChildren(\'cb-' . $childID . '\');" ' . $checked . '>'.$currName.' ('.$childType.')</label>';
				
				getKnownRequires($child, $level + 1, $dataPath, $requires);
				
				echo '</li>';
			} else if ($childType == 'item') {
				$currName = $child;
				echo '<li class="nav-header" style="padding-left:' . $indentation . 'px;"><label class="checkbox"><input type="checkbox" id="cb-' . $childID . '" onClick="EpicWheels.Main.checkChildren(\'cb-' . $childID . '\');" ' . $checked . '>'.$currName.' ('.$childType.')</label>';
				
				echo '</li>';
			}
		}

		echo '</ul>';
		
		
	} // End of function	
	
?>