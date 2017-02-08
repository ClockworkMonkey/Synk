
<?php
	define('HOST','localhost');
	define('USER','root');
	define('PASS','');
	define('DB','synk-app');
	
	//checks that the passed in username/password is a valid combo
	 
	$con = mysqli_connect(HOST,USER,PASS,DB);
	 
	$username = $_POST['username'];
	$password = $_POST['password'];
	 
	$sql = "select * from users where username='$username' and password='$password'";
	 
	$res = mysqli_query($con,$sql);
	 
	$check = mysqli_fetch_array($res);
	 
	if(isset($check)){
		echo 'true';
	}else{
		echo 'false';
	}
	 
	mysqli_close($con);
?>