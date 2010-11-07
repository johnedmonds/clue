package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Accusation")]
	public class Accusation extends PropositionMessage {
		public function toString():String{return StringUtil.substitute("Player {0} accused {1} in the {2} with the {3}.", player,suspect,room,weapon);}
	}
}