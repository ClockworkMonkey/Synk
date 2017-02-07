
<?php
	define('HOST','synk-app.com');
	define('USER','u813815354_user');
	define('PASS','bhaq2010');
	define('DB','Users');
	
	//checks that the passed in username/password is a valid combo
	 
	$con = mysqli_connect(HOST,USER,PASS,DB);
	 
	$username = $_POST['username'];
	$password = $_POST['password'];
	 
	$sql = "select * from users where username='$username' and password='$password'";
	 
	$res = mysqli_query($con,$sql);
	 
	$check = mysqli_fetch_array($res);
	 
	if(isset($check)){
		echo 'success';
	}else{
		echo 'failure';
	}
	 
	mysqli_close($con);
?>