package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Move")]
	public class MoveMessage extends PlayerMessage{
		public var to:String;
		public var from:String; 
		public function toString():String{return StringUtil.substitute("{0} moved from {1} to {2}.",player,from,to);}
	}
}