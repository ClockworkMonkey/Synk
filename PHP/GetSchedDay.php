<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	//$user = "zxc@cxz.com";
	//$day = "Friday";
	$user = $_POST["username"];	
	$day = $_POST["day"];
	
	
	$query = sprintf("SELECT %s FROM Schedules WHERE UserName = '%s'", mysqli_real_escape_string($con, $day), mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
	
	if (!$sql) {
		echo "failure";
	}
	else
	{
		$row =  mysqli_fetch_array($sql);
		echo $day;
		echo ':';
		echo $row[$day];
			
	}
	
	mysqli_close($con);
?>