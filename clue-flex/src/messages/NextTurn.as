package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.NextTurn")]
	public class NextTurn extends PlayerMessage{
		public var movementPointsAvailable:int;
		public function toString():String{return StringUtil.substitute("It is now {0}'s turn. {0} has {1} movement points available.",player,movementPointsAvailable);}
	}
}