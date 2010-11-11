package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Disprove")]
	public class Disprove extends PlayerMessage{
		public override function toString():String{return player==null?"No one can disprove the proposition.":StringUtil.substitute("{0} can disprove the proposition.",player);}
	}
}