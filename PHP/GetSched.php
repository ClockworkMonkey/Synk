<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	//$user = "zxc@cxz.com";
	$user = $_POST["username"];	
	
	
	$query = sprintf("SELECT Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday FROM Schedules WHERE UserName = '%s'", mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
	
	if (!$sql) {
		echo "failure";
	}
	else
	{
		$row =  mysqli_fetch_array($sql);
		echo 'Monday';
		echo ':';
		echo $row['Monday'];
		echo '_';
		echo 'Tuesday';
		echo ':';
		echo $row['Tuesday'];
		echo '_';
		echo 'Wednesday';
		echo ':';
		echo $row['Wednesday'];
		echo '_';
		echo 'Thursday';
		echo ':';
		echo $row['Thursday'];
		echo '_';
		echo 'Friday';
		echo ':';
		echo $row['Friday'];
		echo '_';
		echo 'Saturday';
		echo ':';
		echo $row['Saturday'];
		echo '_';
		echo 'Sunday';
		echo ':';
		echo $row['Sunday'];		
	}
	
	mysqli_close($con);
?>