<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	$to_add = $POST["to_add"];
	
	$query = sprintf($mysqli, "SELECT * FROM Users WHERE Username = '%s'", mysql_real_escape_string($to_add));

	$sql = mysqli_query($mysqli, $query);
	
	$check = mysqli_fetch_array($sql);
	
	//first we make sure that the user being added exists
	if(isset($check)){
		$query2 = sprintf($mysqli, "SELECT * FROM Friends WHERE Assoc_User = '%s'", mysql_real_escape_string($user));

		$sql2 = mysqli_query($mysqli, $query2);
		
		$check2 = mysqli_fetch_array($sql);
		
		// then we make sure there isnt already a pending request
		if(isset($check2)){
			echo 'false';
		}
		// if we made it this far, we add a new request
		else
		{
			$stmt = $this->conn->prepare("INSERT INTO Friends(Friend, Assoc_User, Confirmed, created_at) VALUES(?, ?, ?,  NOW())");
			$stmt->bind_param("sss", $to_add, $username, "0");
			$result = $stmt->execute();
			$stmt->close();

			echo "true";			
		}
	else{
		echo "Invalid username";
	}

	mysqli_close($con);
?>