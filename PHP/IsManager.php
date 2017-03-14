<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$id = $_POST["eventid"];	
	$user = $_POST["username"];	
	
	//$id = '2';	
	//$user = "zxc@cxz.com";	
	
	$query = sprintf("SELECT * FROM invitees WHERE eventID = '%s' AND username = '%s' AND Manager = 1", mysqli_real_escape_string($con, $id), mysqli_real_escape_string($con, $user));
	$sql2 = mysqli_query($con, $query);

	echo $query;
	if (mysqli_num_rows($sql2)==0) {
		echo "failure";
	}
	else
	{
		echo "manager";
	}
	
	mysqli_close($con);
?>