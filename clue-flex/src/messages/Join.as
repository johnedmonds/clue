package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.Join")]
	public class Join extends PlayerMessage{
		public var suspect:String;
		public function toString():String{return StringUtil.substitute("{0} joined the game as {1}.",player,suspect);}
	}
}