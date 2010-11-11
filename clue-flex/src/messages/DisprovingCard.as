package messages{
	import mx.utils.StringUtil;
	[Bindable]
	[RemoteClass(alias="com.pocketcookies.clue.messages.targeted.DisprovingCard")]
	public class DisprovingCard extends Message{
		public var card:String;
		public override function toString():String{return StringUtil.substitute("You are shown the card {0}.",card);}
	}
}
