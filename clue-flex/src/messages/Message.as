package messages{
	import clue.MessageEntry;
	import mx.controls.*;
	public class Message{
		public var published:Date;
		public function getIcon():Object{
			return MessageEntry.ICO_UNKNOWN;
		}
		public function toString():String{return "Message";}
	}
}