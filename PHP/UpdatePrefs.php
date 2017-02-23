<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	//sets the status of the passed in user to the passed in status

	$status = $_POST["prefs"];
	$user = $_POST["username"];	
	
	$query = sprintf("UPDATE Users SET Prefernces='%s' WHERE UserName = '%s'", mysqli_real_escape_string($con,$status),mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
	
	if (!$sql) {
		echo "false";
	} else {
		echo "true";
	}

	mysqli_close($con);
?>