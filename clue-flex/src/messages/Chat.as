package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Chat")]
	public class Chat extends PlayerMessage{
		public var message:String;
		public function toString():String{return StringUtil.substitute("{0}:{1}",player,message);}
	}
}