package messages{
	import clue.MessageEntry;
	import mx.controls.Image;
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Chat")]
	public class Chat extends PlayerMessage{
		public var message:String;
		public override function toString():String{return StringUtil.substitute("{0}:{1}",player,message);}
		public override function getIcon():Object{return MessageEntry.ICO_CHAT;}
	}
}