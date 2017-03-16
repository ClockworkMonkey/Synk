<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$id = $_POST["id"];	
	
	$query = sprintf("DELETE FROM Events WHERE EventID = %s", mysqli_real_escape_string($con, $id));
		
	$sql = mysqli_query($con, $query);	

	$query2 = sprintf("DELETE FROM Invitees WHERE eventID = %s", mysqli_real_escape_string($con, $id));
	
	$sql2 = mysqli_query($con, $query2);	
	
	mysqli_close($con);
?>