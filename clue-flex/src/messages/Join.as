package messages{
	import clue.MessageEntry;
	import mx.controls.Image;
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.Join")]
	public class Join extends PlayerMessage{
		public var suspect:String;
		public override function toString():String{return StringUtil.substitute("{0} joined the game as {1}.",player,suspect);}
		public override function getIcon():Object{return MessageEntry.ICO_JOIN;}
	}
}