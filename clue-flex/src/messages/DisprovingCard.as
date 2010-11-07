package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.targeted.DisprovingCard")]
	public class DisprovingCard extends PlayerMessage{
		public var card:String;
		public function toString():String{return StringUtil.substitute("{0} shows you the card {1}.",player,card);}
	}
}
