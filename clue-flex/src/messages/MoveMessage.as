package messages{
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Move")]
	public class MoveMessage extends PlayerMessage{
		public var to:String;
		public var from:String; 
	}
}