<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.Enumeration,java.net.NetworkInterface,java.net.InetAddress"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript"
	src="<%=request.getContextPath()%>/jquery.js"></script>

<script type="text/javascript">
	function logout(){
		$.get("<%=request.getContextPath()%>/clue/logout",{},
			function(){
				$("#welcome").slideUp(400,function(){$("#welcome").remove();});
				$("#login").slideDown();
			});
	}
	function tryLogin(){
		$.get("<%=request.getContextPath()%>/clue/login",{"username":$("#username").val(),"password":$("#password").val()},
		function(data){
			if (data.key!=null){
				$("#login").slideUp();
				var logoutButton=$("<input type=\"submit\" value=\"Logout\" style=\"width:100%;\"/>");
				logoutButton.click(logout);
				var welcome=$("<div id=\"welcome\" class=\"content-section\"><h1>Welcome "+$("#username").val()+"</h1></div>").hide();
				welcome.append(logoutButton);
				$("#left-column").prepend(welcome);
				welcome.slideDown();
			}
		});
	}
	function makeGameHtml(game,index){
		var secondGameClass="game-light";
		if (index%2==0)
			secondGameClass="game-dark";
		var ret="<div class=\"game " +secondGameClass+"\">"+game.name+" "+game.state+"<ul class=\"players\">";
		for (var i=0;i<game.players.length;i++){
			ret+="<li>"+game.players[i].name+" "+game.players[i].suspect+"</li>";
		}
		ret +="</ul></div>";
		return ret;
	}
	function addGameClosure(games,index){
		return function(){
			if (index<games.length){
				var currentGame=$(makeGameHtml(games[index],index)).hide();
				$("#games").append(currentGame);
				currentGame.slideDown(300,addGameClosure(games,index+1));
			}
		}
	}
	$.get("<%=request.getContextPath()%>/clue/games", function(data,status,r){addGameClosure(data.games,0)()});
	</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/index.css"
	type="text/css" />
<title>Clue - Games</title>
</head>
<body>
<div id="left-column">
<%
	if (request.getSession().getAttribute("key") != null) {
%>
<div id="welcome" class="content-section">
<h1>Welcome <%=request.getSession().getAttribute("username")%></h1>
<input type="submit" value="Logout" style="width: 100%;"
	onclick="logout()" /></div>
<%
	}
%>
<div id="about" class="content-section">
<h1>About</h1>
<p>Hello world.</p>
</div>
<div id="connect-differently" class="content-section">
<h1>Connect Differently</h1>
<h2>Telnet</h2>
<ul id="telnet-connections">
	<%
		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
				.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			final Enumeration<InetAddress> addresses = networkInterfaces
					.nextElement().getInetAddresses();
			while (addresses.hasMoreElements()) {
	%>
	<li>telnet <%=addresses.nextElement().getHostAddress()%> 8081</li>
	<%
		}
		}
	%>
</ul>
</div>
</div>
<div id="right-column">
<div id="login" class="content-section"
	<%=request.getSession().getAttribute("key") == null ? ""
					: "style=\"display:none\""%>>
<h1>Login</h1>
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
		<td colspan="2"><input type="submit" value="Login"
			style="width: 100%;" onclick="tryLogin()"></td>
	</tr>
</table>
</div>
<div id="games-container" class="content-section">
<h1>Games</h1>
<div id="games"></div>
</div>
</div>

</body>
</html>