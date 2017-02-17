<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_GET["username"];	

	$query = sprintf("SELECT Profile_img FROM Users WHERE UserName = '%s'", mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
	
	$result = mysqli_fetch_array($sql);
	
	header('content-type: image/jpeg');
 
	echo base64_decode($result['image']);

	mysqli_close($con);
?>