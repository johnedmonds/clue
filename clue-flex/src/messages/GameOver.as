package messages{
	import clue.MessageEntry;
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.GameOver")]
	public class GameOver extends PlayerMessage{
		public override function toString():String{return StringUtil.substitute("The game is over.  It was won by {0}",player);}
		public override function getIcon():Object{return MessageEntry.ICO_GAME_OVER;}
	}
}