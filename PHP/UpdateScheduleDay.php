<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);

	$user = $_POST["username"];	
	$day = $_POST["day"];
	$avail = $_POST["sched"];
	
	$query = sprintf("UPDATE Schedule SET '%s'='%s' WHERE UserName = '%s'", mysqli_real_escape_string($con,$day),mysqli_real_escape_string($con, $avail), mysqli_real_escape_string($con, $username));
	
	$sql = mysqli_query($con, $query);
	
	if (!$sql) {
		echo "false";
	} else {
		echo "true";
	}

	mysqli_close($con);
?>