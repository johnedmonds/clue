package messages{
	import clue.MessageEntry;
	import mx.controls.Image;
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Move")]
	public class MoveMessage extends PlayerMessage{
		public var to:String;
		public var from:String; 
		public override function toString():String{return StringUtil.substitute("{0} moved from {1} to {2}.",player,from,to);}
		public override function getIcon():Object{return MessageEntry.ICO_MOVE;}
	}
}