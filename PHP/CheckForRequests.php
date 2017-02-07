<?php
	define('HOST','synk-app.com');
	define('USER','u813815354_user');
	define('PASS','bhaq2010');
	define('DB','Users');
	
	$user = $_POST["username"];	

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	
	$query = sprintf($mysqli, "SELECT * FROM Friends WHERE Assoc_User = '%s' AND Confirmed = '0'", mysql_real_escape_string($user));

	$sql = mysqli_query($mysqli, $query);
	
	$check = mysqli_fetch_array($sql);
	
	if(isset($check)){
		while($row = $sql->fetch_assoc())
		{
			echo $row['Friend'];
		}		
	}
	else
	{
		//do nothing if there are no pending requests
	}
	
	mysqli_close($mysqli);
?>