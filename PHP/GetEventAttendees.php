<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["eventid"];	
	//$user = 2;	
	
	$query = sprintf("SELECT * FROM invitees WHERE eventID = '%s'", mysqli_real_escape_string($con, $user));
	$sql2 = mysqli_query($con, $query);

	
	if (!$sql2) {
		echo "failure";
	}
	else
	{
		while($row = $sql2->fetch_array())
		{
			$name = $row['username'];
			
			$query2 = sprintf("SELECT * FROM Users WHERE Username = '%s'", mysqli_real_escape_string($con, $name));
			$sql = mysqli_query($con, $query2);
						
			$row2 =  mysqli_fetch_array($sql);			
			
			echo $row2['Name'];
			echo ",";
			echo $row['accepted'];
			echo "/";			
		}
	}
	
	mysqli_close($con);
?>