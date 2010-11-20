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
			$("#games-container").slideUp();
			username=null;
			clearTimeout(currentTimeout);
			clueswfobject.logout();
		}
	);
}
function playerJoined(){
	clearTimeout(currentTimeout);
	$("#games-container").slideUp();
}
//Used to cancel the timeout object once the player has joined a game.
var currentTimeout;
//Sends an AJAX request to login.
function tryLogin(){
	$.get("<%=getServletContext().getContextPath()%>/clue/login",{'username':$("#username").val(),'password':$("#password").val()},loginSuccess);
}
//Called when the AJAX request returns successfully.
function loginSuccess(data) {
	//Keep track of the username.  We will need this when determining whether to allow the user to join or rejoin a game.
	username=$("#username").val();
	//Hide the login div and show the welcome div.
	$("#login").slideUp();
	$("#welcome>h1:first").text("Welcome "+username);
	$("#welcome").slideDown();
	$("#games-container").slideDown();
	getGamesOnTimer();
	//Pass this data onto the plugin.
	clueswfobject.successfulLogin(username,data.key);
}
function leave(){
	$("#games-container").slideDown();
	getGamesOnTimer();
}
//This section provides the controls for joining the game.
function makeJoinContainer(gameId,takenSuspects){
	var joinContainer=$("<ul class='joinControls'></ul>");
	var suspects=['SCARLETT','MUSTARD','WHITE','GREEN','PEACOCK','PLUM'];
	for (var suspect in suspects){
		//If the suspect has already been taken, do not present the option to the user.
		if(takenSuspects[suspects[suspect]]!=true){
			joinContainer.append($("<li><input type='submit' value='"+suspects[suspect]+"'/></li>").click(
					function (suspect){
						return function(){
							clueswfobject.join(suspect,gameId);
							playerJoined();
						};
					}(suspects[suspect])
				)
			);
		}
	}
	return joinContainer;
}
//Used to make a <div> entry in the list of games.
//game is one of the entries in the data returned from the AJAX call to get the list of games.
//index tells us what index this is in the list of games (this is not the id of the game).  It's primary purpose (and only purpose so far) is to let us know whether to apply a dark or light css class (since we are alternating colors in the divs).
function makeGameHtml(game,index){
	//Holds an entry in the list of games.
	var gameContainer=$("<div>"+game.name+" "+game.state+"</div>").addClass(index%2==0?"game-dark":"game-light");
	//Holds the list of players in this game.
	playersContainer=$("<ul class=\"players\"></ul>");
	//Whether the currently logged in user is part of this game.
	var containsPlayer=false;
	var takenSuspects=new Object();
	for (var i=0;i<game.players.length;i++){
		//Check if this user is part of the game
		containsPlayer=containsPlayer||game.players[i].name==username;
		playersContainer.append($("<li>"+game.players[i].name+" "+game.players[i].suspect+"</li>"));
		takenSuspects[game.players[i].suspect]=true;
	}
	var joinContainer;
	//If the current player is already in this game, we only allow that player to rejoin.
	if (containsPlayer)
		joinContainer=$("<div class='joinContainer'></div>").append($("<input type='submit' value='Rejoin'/>")
			.click(function(){
				clueswfobject.rejoin(game.id);
				playerJoined();
				}
			)
		);
	else joinContainer=makeJoinContainer(game.id,takenSuspects);
	//Add the players and join lists to the game list.
	gameContainer.append(playersContainer).append(joinContainer);		
	return gameContainer;
}
//Calls getGames and then arranges for this function to be called again.
function getGamesOnTimer(){getGames();currentTimeout=setTimeout("getGamesOnTimer();",10000);}
//Sends an AJAX request to the server for the list of games.  Upon successfully retrieving the list of games, calls addAllGames.
function getGames(){$.get("<%=getServletContext().getContextPath()%>/clue/games", function(data,status,r){addAllGames(data.games)});}
//Clears the list of games, then goes through each game retrieved from the server and adds it to the list of games.
function addAllGames(games){
	$("#games").html(""); //Clear the list of games.
	//Go through each game and add it to the list of games.
	for (var g in games){
		$("#games").append($(makeGameHtml(games[g],g)));
	}
}
//Used to retrieve all necessary information from clue.
function clueFinishedLoading(){
	<%if (request.getSession().getAttribute("key") != null) {%>
	clueswfobject.successfulLogin('<%=request.getSession().getAttribute("username")%>','<%=request.getSession().getAttribute("key")%>');
<%}%>
	}
	//The actualy swf object upon which we make calls.
	var clueswfobject;
	//The player's current username.
	var username =
<%=request.getSession().getAttribute("username") == null ? "null"
					: "\"" + request.getSession().getAttribute("username")
							+ "\""%>
	swfobject.registerObject("clue-object", "9.0.0", null, function(e) {
		if (e.success)
			clueswfobject = e.ref;
		else
			alert("There was an error registering the swf object.");
	});
	$(document).ready(function() {
<%if (request.getSession().getAttribute("key") != null) {%>
	$("#login").hide();
		$("#games-container").show();
		getGamesOnTimer();
<%}%>
	});
</script>
<link rel="stylesheet"
	href="<%=getServletContext().getContextPath()%>/index.css"
	type="text/css" />
<title>Clue - Games</title>
</head>
<body>
<div id="left-column" style="width:25%;">
<div id="welcome" class="content-section"
	<%=request.getSession().getAttribute("key") == null ? "style=\"display:none;\""
					: ""%>>
<h1>Welcome <%=request.getSession().getAttribute("username")%></h1>
<input type="submit" value="Logout" style="width: 100%;"
	onclick="logout();" /></div>
<div id="instructions" class="content-section">
<h1>Instructions</h1>
<p>Someone has been murdered.  There are six people, six weapons, and nine rooms in the mansion.  Your job is to figure out who committed the murder, with what, and where.</p>
</div>
</div>
<div id="right-column">
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
<div id="mid-column">
<div id="login" class="content-section"
	<%=request.getSession().getAttribute("key") == null ? ""
					: "style=\"display:none\""%>>
<div id="login-content">
<h1>Login</h1>
<form method="post" action="<%=request.getContextPath()%>/clue/login">
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
</form>
</div>
</div>
<div id="games-container" class="content-section" style="display: none;">
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
<div id="clue-game" style="margin-bottom: 10px;"><object
	classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="600"
	height="600" id="clue-object">
	<param name="movie"
		value="<%=getServletContext().getContextPath()%>/application.swf" />
	<!--[if !IE]>--> <object type="application/x-shockwave-flash"
		data="<%=getServletContext().getContextPath()%>/application.swf"
		width="600" height="600"> <!--<![endif]--> <a
			href="http://www.adobe.com/go/getflashplayer"> <img
			src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif"
			alt="Get Adobe Flash player" /> </a> <!--[if !IE]>--> </object> <!--<![endif]-->
</object></div>
</div>
</body>
</html>