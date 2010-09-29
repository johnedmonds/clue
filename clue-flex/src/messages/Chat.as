package messages{
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Chat")]
	public class Chat extends PlayerMessage{
		public var message:String;
	}
}