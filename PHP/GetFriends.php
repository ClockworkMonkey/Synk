<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	
	$query = sprintf($mysqli, "SELECT Friend FROM Friends WHERE Assoc_User = '%s' AND Confirmed = '1'", mysql_real_escape_string($user));

	$sql = mysqli_query($mysqli, $query);
	
	if(!$sql)
	{
		//do nothing if the heve no friends :-(		
	}
	else
	{
		while($row = $sql->fetch_assoc())
		{
			$Curr_Friend = $row['Friend'];
			$sql2 = mysqli_query($mysqli, "SELECT Status, Name FROM Users WHERE Username = '$s'", mysql_real_escape_string($Curr_Friend));
			if(!sql2)
			{
			}
			else{
				$row2 = $sql2->fetch_assoc
				echo $row2['Name'];
				echo $row2['Status'];
			}
		}
	}

	mysqli_close($con);
?>