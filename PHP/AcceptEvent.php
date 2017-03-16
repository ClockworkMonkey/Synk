<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	$id = $_POST["eventID"];	
	
	$query = sprintf("UPDATE Invitees SET accepted = '1' WHERE username = '%s' AND eventID = %s", mysqli_real_escape_string($con, $user),mysqli_real_escape_string($con, $id));
	
	echo $query;
	
	$sql = mysqli_query($con, $query);
		
	if (!$sql) {
		echo "somwething went wrong";
	} 

	mysqli_close($con);
?>