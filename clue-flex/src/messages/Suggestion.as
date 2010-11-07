package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Suggestion")]
	public class Suggestion extends PropositionMessage{
		public override function toString():String{return StringUtil.substitute("{0} suggests {1} in the {2} with the {3}",player,suspect,room,weapon);}
	}
}