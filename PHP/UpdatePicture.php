<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');

	$con = mysqli_connect(HOST,USER,PASS,DB);
	
	$user = $_POST["username"];	
	$image = $_PORT["image"];
	
	$query = sprintf("UPDATE Users SET Profile_img='%s' WHERE UserName = '%s'", mysqli_real_escape_string($con,$image),mysqli_real_escape_string($con, $user));
	
	$sql = mysqli_query($con, $query);
	 
	$check = mysqli_stmt_affected_rows($sql);
	 
	if($check == 1){
		 echo "Image Uploaded Successfully";
	}else{
		 echo "Error Uploading Image";
	}	

	mysqli_close($con);
?>