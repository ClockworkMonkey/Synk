<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');
	
	$user = $_POST["username"];	
	
	
	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$query = sprintf("SELECT * FROM Friends WHERE Friend = '%s' AND Confirmed = '0'", mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
		
	if(mysqli_num_rows($sql)!=0){		
		while($row = $sql->fetch_array())
		{
			echo $row['Assoc_User'];
			//echo "<br/>";
		}		
	}
	else
	{
		//do nothing if there are no pending requests
	}
	
	mysqli_close($con);
?>