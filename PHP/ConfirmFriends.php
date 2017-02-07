<?php
	define('HOST','synk-app.com');
	define('USER','u813815354_user');
	define('PASS','bhaq2010');
	define('DB','Users');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	$friend = $_POST["friend"];
	
	$query = sprintf("UPDATE Friends SET Confirmed = '1' WHERE Assoc_User = '%s' AND Friend = '%s'", mysql_real_escape_string($user),mysql_real_escape_string($friend));
	
	$sql = mysqli_query($mysqli, $query);
	
	if (!$sql) {
		echo "somwething went wrong";
	} 

	mysqli_close($con);
?>