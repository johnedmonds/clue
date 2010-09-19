<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript"
	src="<%=request.getContextPath()%>/jquery.js"></script>

<script type="text/javascript">
	function addGameClosure(games,index){
		return function(){
			if (index<games.length){
				var secondGameClass="game-light";
				if (index%2==0)
					secondGameClass="game-dark";
				var currentGame=$("<div class=\"game "+secondGameClass+"\">"+games[index].name+"</div>").hide();
				$("#games").append(currentGame);
				currentGame.slideDown(300,addGameClosure(games,index+1));
			}
		}
	}
	$.get("<%=request.getContextPath()%>/clue/games", function(data,status,r){addGameClosure(data.games,0)()});
	</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/games.css"
	type="text/css" />
<title>Insert title here</title>
</head>
<body>
<div id="games-container">
<h1>Games</h1>
<div id="games"></div>
</div>

</body>
</html>