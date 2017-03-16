<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');
	
	//adds a new user to teh database, first checking if the 
	// username/email is alreadty in use
	 
	$con = mysqli_connect(HOST,USER,PASS,DB);	 
	
	$user = $_POST['username'];
	$id = $_POST['eventID'];

	
	$stmt2 = sprintf("DELETE FROM Invitees WHERE username = '%s' AND eventID = %s", mysqli_real_escape_string($con, $user), mysqli_real_escape_string($con, $id));	
	$sql = mysqli_query($con, $stmt2);

	if(!$sql)
	{
		echo "false";
	}
	else
	{
		echo "true";		
	}
	 
	mysqli_close($con);
?>