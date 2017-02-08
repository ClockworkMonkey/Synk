<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	
	$query = sprintf("SELECT Status FROM Users WHERE UserName = '%s'", mysql_real_escape_string($user));

	$sql = mysqli_query($mysqli, $query);
	
	if (!$sql) {
		echo "failure";
	}
	else
	{
		$row =  mysqli_fetch_array($sql);
		echo $row['Status'];
	}
	
	mysqli_close($con);
?>