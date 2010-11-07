package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.broadcast.Leave")]
	public class Leave extends PlayerMessage{
		public function toString():String{return StringUtil.substitute("{0} left the game.",player);}
	}
}