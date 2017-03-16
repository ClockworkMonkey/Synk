<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');
	
	//adds a new user to teh database, first checking if the 
	// username/email is alreadty in use
	 
	$con = mysqli_connect(HOST,USER,PASS,DB);	 
	
	$manager = $_POST['username'];
	$description = $_POST['desc'];
	$title = $_POST['title'];
	$time = $_POST['time'];
	$location = $_POST['place'];	 
	
	/*
	$manager = "zxc@cxz.com";
	$description = "an event to test the event stuf";
	$title = "Test event";
	$time = "12-3-17: 10:00pm";
	$location = "here";	 
	*/
	
	$stmt = sprintf("INSERT INTO Events(Manager, Description, Title, Location, Time) VALUES( '%s', '%s', '%s', '%s', '%s')", mysqli_real_escape_string($con, $manager), mysqli_real_escape_string($con, $description), mysqli_real_escape_string($con, $title), mysqli_real_escape_string($con, $location), mysqli_real_escape_string($con, $time));
	
	$sql = mysqli_query($con, $stmt);
	
	
	$stmt3 = sprintf("SELECT * FROM Events WHERE Manager = '%s' AND Description = '%s'", mysqli_real_escape_string($con, $manager), mysqli_real_escape_string($con, $description));
	
	$sql3 = mysqli_query($con, $stmt3);
	
	$row =  mysqli_fetch_array($sql);
	$id = $row['Event ID'];
	
	$stmt2 = sprintf("INSERT INTO Invitees(username, eventID, accepted, manager) VALUES( '%s', %s, 1, 1)", mysqli_real_escape_string($con, $manager), mysqli_real_escape_string($con, $id));		

	if(!$sql)
	{
		echo "false";
	}
	else
	{
		echo "true";		
	}
	 
	mysqli_close($con);
?>