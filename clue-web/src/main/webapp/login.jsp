<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="main.css" type="text/css" />
<title>Insert title here</title>
</head>
<body>
<div id="login-container">
<div id="login">
<h1>Login</h1>
<form action="<%=request.getContextPath() + "/clue/login"%>"
	method="get">
<table>
	<tr>
		<td><label for="username">Username</label></td>
		<td><input type="text" id="username" name="username" /></td>
	</tr>
	<tr>
		<td><label for="password">Password</label></td>
		<td><input type="text" id="password" name="password" /></td>
	</tr>
	<tr>
		<td colspan="2"><input type="submit" name="submit" value="Login" style="width:100%;" /></td>
	</tr>
</table>
</form>
</div>
</div>
</body>
</html>