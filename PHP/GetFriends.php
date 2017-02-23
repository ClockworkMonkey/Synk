<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	
	$query = sprintf("SELECT Friend FROM Friends WHERE Assoc_User = '%s' AND Confirmed = '1'", mysqli_real_escape_string($con, $user));

	$sql = mysqli_query($con, $query);
	
	if(!$sql)
	{
		echo "no_friends";
	}
	else
	{
		while($row = $sql->fetch_array())
		{
			$Curr_Friend = $row['Friend'];
			$query2 = sprintf( "SELECT * FROM Users WHERE Username = '%s'", mysqli_real_escape_string($con, $Curr_Friend));
			
			$sql2 = mysqli_query($con, $query2);
			if(!$sql2)
			{
			}
			else{
				$row2 = $sql2->fetch_array();
				echo $row2['Name'];
				echo ",";
				echo $row2['Status'];
				echo ",";
				echo $row2['Username'];
				echo ",";
				echo $row2['Prefernces'];
				echo "/";
			}
		}
	}

	mysqli_close($con);
?>