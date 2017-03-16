<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');
	
	$user = $_POST["username"];		
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$query = sprintf("SELECT * FROM invitees WHERE username = '%s' AND accepted = '0'", mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);	
	
	
		
	if(mysqli_num_rows($sql)!=0){

		$row = $sql->fetch_array();
		$id = $row['eventID'];
	
		$query2 = sprintf("SELECT * FROM Events WHERE EventID = '%s'", mysqli_real_escape_string($con, $id));
		$sql2 = mysqli_query($con, $query2);
		
		while($row2 = $sql2->fetch_array())
		{
			echo $row2['Title'];
			echo ",";
			echo $row2['Description'];
			echo ",";
			echo $row2['EventID'];
			echo "/";
			
		}		
	}
	else
	{
		//do nothing if there are no pending requests
	}
	
	
	
	mysqli_close($con);
?>