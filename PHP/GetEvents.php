<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	//gets the status of the passed user and return it, if
	// the user does not exist, error
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	//$user = "zxc@cxz.com";	
	
	$query = sprintf("SELECT * FROM invitees WHERE UserName = '%s'", mysqli_real_escape_string($con, $user));
	$sql2 = mysqli_query($con, $query);
	
	$row =  mysqli_fetch_array($sql2);
	$id = $row['eventID'];

	
	$query2 = sprintf("SELECT * FROM Events WHERE EventID = %s", mysqli_real_escape_string($con, $id));
	$sql = mysqli_query($con, $query2);
	
	if (!$sql) {
		echo "failure";
	}
	else
	{
		while($row = $sql->fetch_array())
		{
			echo $row['Title'];
			echo ",";
			echo $row['Description'];
			echo ",";
			echo $row['Manager'];
			echo ",";
			echo $row['Location'];
			echo ",";
			echo $row['Time'];
			echo ",";
			echo $row['EventID'];
			echo "/";			
		}
	}
	
	mysqli_close($con);
?>