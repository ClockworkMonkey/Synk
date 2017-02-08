<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	$friend = $_POST["friend"];

	
	$query = sprintf("UPDATE Friends SET Confirmed = '1' WHERE Assoc_User = '%s' AND Friend = '%s'", mysqli_real_escape_string($con, $friend),mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
	
	if (!$sql) {
		echo "somwething went wrong";
	} 

	mysqli_close($con);
?>