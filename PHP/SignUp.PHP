
<?php
	define('HOST','synk-app.com');
	define('USER','u813815354_user');
	define('PASS','bhaq2010');
	define('DB','Users');
	
	//adds a new user to teh database, first checking if the 
	// username/email is alreadty in use
	 
	$con = mysqli_connect(HOST,USER,PASS,DB);
	 
	$username = $_POST['username'];
	$password = $_POST['password'];
	$name = $_PORT['name'];
	 
	 
	$sql = "select * from users where username='$username'";
	 
	$res = mysqli_query($con,$sql);
	 
	$check = mysqli_fetch_array($res);
	 
	if(isset($check)){
		echo 'failure';
	}else{
		$uuid = uniqid('', true);

		$stmt = $this->conn->prepare("INSERT INTO users(name, username, password, created_at) VALUES(?, ?, ?, ?, ?, NOW())");
		$stmt->bind_param("sss", $name, $username, $password);
		$result = $stmt->execute();
		$stmt->close();

		// check for successful store
		if ($result) {
			$stmt = $this->conn->prepare("SELECT * FROM users WHERE username = ?");
			$stmt->bind_param("s", $username);
			$stmt->execute();
			$user = $stmt->get_result()->fetch_assoc();
			$stmt->close();

			return "sucess";
		} else {
			return "failure";
		}
	}
	 
	mysqli_close($con);
?>