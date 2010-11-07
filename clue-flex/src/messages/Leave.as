package messages{
	import mx.controls.Image;
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Leave")]
	public class Leave extends PlayerMessage{
		public override function toString():String{return StringUtil.substitute("{0} left the game.",player);}
		public override function getIcon():Object{return MessageEntry.ICO_LEAVE;}
	}
}