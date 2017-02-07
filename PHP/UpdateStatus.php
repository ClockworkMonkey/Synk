<?php
	define('HOST','synk-app.com');
	define('USER','u813815354_user');
	define('PASS','bhaq2010');
	define('DB','Users');

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