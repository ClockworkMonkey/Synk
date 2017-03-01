<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	
	$query = sprintf("SELECT Mon, Tues, Wed, Thur, Fri, Sat, Sun FROM Schedule WHERE UserName = '%s'", mysqli_real_escape_string($con, $user));

	$sql = mysqli_query($con, $query);
	
	if (!$sql) {
		echo "failure";
	}
	else
	{
		$row =  mysqli_fetch_array($sql);
		echo 'MON';
		echo $row['Mon'];
		echo 'TUES';
		echo $row['Tues'];
		echo 'WED';
		echo $row['Wed'];
		echo 'THUR';
		echo $row['Thur'];
		echo 'FRI';
		echo $row['Fri'];
		echo 'SAT';
		echo $row['Sat'];
		echo 'SUN';
		echo $row['Sun'];		
	}
	
	mysqli_close($con);
?>