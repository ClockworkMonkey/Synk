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
		echo 'MON';
		echo $row['Monday'];
		echo 'TUES';
		echo $row['Tuesday'];
		echo 'WED';
		echo $row['Wednesday'];
		echo 'THUR';
		echo $row['Thursday'];
		echo 'FRI';
		echo $row['Friday'];
		echo 'SAT';
		echo $row['Saturday'];
		echo 'SUN';
		echo $row['Sunday'];		
	}
	
	mysqli_close($con);
?>