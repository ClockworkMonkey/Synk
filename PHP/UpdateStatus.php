<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	//sets the status of the passed in user to the passed in status

	$status = $_POST["status"];
	$user = $_POST["username"];	
	
	$query = sprintf("UPDATE Users SET Status='%s' WHERE UserName = '%s'", mysql_real_escape_string($status),mysql_real_escape_string($user));
	
	$sql = mysqli_query($mysqli, $query);
	
	if (!$sql) {
		echo "false";
	} else {
		echo "true";
	}

	mysqli_close($con);
?>