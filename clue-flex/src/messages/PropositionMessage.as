package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Proposition")]
	public class PropositionMessage extends PlayerMessage{
		public var room:String;
		public var suspect:String;
		public var weapon:String;
		public function toString():String{return StringUtil.substitute("Player {0} proposed {1} in the {2} with the {3}.", player,suspect,room,weapon);}
	}
}