<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	import="java.util.Enumeration,java.net.NetworkInterface,java.net.InetAddress"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript"
	src="<%=getServletContext().getContextPath()%>/jquery.js"></script>
<script type="text/javascript"
	src="<%=getServletContext().getContextPath()%>/swfobject.js"></script>
<script type="text/javascript">
	function logout(){
		$.get("<%=getServletContext().getContextPath()%>/clue/logout",{},
			function(){
				$("#welcome").slideUp();
				$("#login").slideDown();
				clueswfobject.logout();
			});
	}
	function tryLogin(){$.get("<%=getServletContext().getContextPath()%>/clue/login",{'username':$("#username").val(),'password':$("#password").val()},
			function(data){
				loginSuccess();
				clueswfobject.successfulLogin($("#username").val(),data.key);
			});}
	function loginSuccess() {
		$("#login").slideUp();
		$("#welcome>h1:first").text("Welcome "+$("#username").val());
		$("#welcome").slideDown();
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
	function getGamesOnTimer(){getGames();setTimeout("getGamesOnTimer();",4000);}
	function getGames(){$.get("<%=getServletContext().getContextPath()%>/clue/games", function(data,status,r){addAllGames(data.games)});}
	function addAllGames(games){
		$("#games").html("");
		for (var g in games){
			$("#games").append($(makeGameHtml(games[g],g)));
		}
	}
	var clueswfobject;
	$(document).ready(
			function() {
				<%if (request.getSession().getAttribute("key") != null) {%>
				$("#login").hide();
				<%}%>
				swfobject.embedSWF("<%=getServletContext().getContextPath()%>/application.swf","clue-object","500","600","9.0.0","",{},{},{},
						function(e){
							if(e.success){
								clueswfobject=e.ref;
								<%if (request.getSession().getAttribute("key") != null) {%>
								clueswfobject.successfulLogin(<%=request.getAttribute("username")%>,<%=request.getAttribute("key")%>);
								<%}%>
							}
						}
				);
						
				getGamesOnTimer();
			}
	);
	</script>

<link rel="stylesheet"
	href="<%=getServletContext().getContextPath()%>/index.css"
	type="text/css" />
<title>Clue - Games</title>
</head>
<body>
<div id="left-column">
<div id="welcome" class="content-section"
	<%=request.getSession().getAttribute("key") == null ? "style=\"display:none;\""
					: ""%>>
<h1>Welcome <%=request.getSession().getAttribute("username")%></h1>
<input type="submit" value="Logout" style="width: 100%;"
	onclick="logout()" /></div>
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
<div class="content-section">
<h1>Right Column</h1>
</div>
</div>
<div id="mid-column">
<div id="login" class="content-section" style="overflow: auto;"
	<%=request.getSession().getAttribute("key") == null ? ""
					: "style=\"display:none\""%>>
<div id="login-content">
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
</div>
<div id="clue-game"
	style="margin-bottom: 10px; width: 100%; height: 100%;">
<div id="clue-object"></div>
</div>
<div id="games-container" class="content-section">
<h1>Games</h1>
<table width="100%">
	<tr>
		<td><label for="txtCreateGame">Game name</label></td>
		<td><input style="width: 100%;" type="text" id="txtCreateGame" /></td>
		<td><input style="width: 100%;" type="submit" value="Create"
			onclick="$.get('<%=getServletContext().getContextPath()%>/clue/create',{'gameName':$('#txtCreateGame').val()},getGames);$('#txtCreateGame').val('');" /></td>
	</tr>
</table>
<div id="games"></div>
</div>
</div>
</body>
</html>