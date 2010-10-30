package messages{
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.NextTurn")]
	public class NextTurn extends PlayerMessage{
		public var movementPointsAvailable:int;
	}
}