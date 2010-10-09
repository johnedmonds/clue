package messages{
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.Join")]
	public class Join extends PlayerMessage{
		public var suspect:String;
	}
}