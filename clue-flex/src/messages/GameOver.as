package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.GameOver")]
	public class GameOver extends PlayerMessage{
		public function toString():String{return StringUtil.substitute("The game is over.  It was won by {0}",player);}
	}
}