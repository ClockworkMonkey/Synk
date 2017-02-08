<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	$to_add = $_POST["to_add"];
	
	
	$query = sprintf( "SELECT * FROM Users WHERE Username = '%s'", mysqli_real_escape_string($con, $to_add));
	
	$sql = mysqli_query($con, $query);
		
	$check = mysqli_fetch_array($sql);
	
	//first we make sure that the user being added exists
	if(isset($check)){
		$query2 = sprintf( "SELECT * FROM Friends WHERE Assoc_User = '%s' AND Friend = '%s'", mysqli_real_escape_string($con, $user), mysqli_real_escape_string($con, $to_add));
		
		$sql2 = mysqli_query($con, $query2);
		
		$check2 = mysqli_fetch_array($sql);

		
		// then we make sure there isnt already a pending request
		if(mysqli_num_rows($sql2)!=0){
			echo 'false';
		}
		// if we made it this far, we add a new request
		else
		{
			$stmt = "INSERT INTO Friends(Friend, Assoc_User, Confirmed) VALUES('$to_add', '$user', 0)";
			$sql3 = mysqli_query($con, $stmt);

			echo "true";			
		}
	}
	else{
		echo "Invalid username";
	}

	mysqli_close($con);
?>