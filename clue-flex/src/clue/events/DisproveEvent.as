package clue.events{
	import flash.events.*;
	public class DisproveEvent extends Event{
		[Bindable]public var card:String;
		public static const DISPROVE:String="disprove";
		public function DisproveEvent(card:String){super(DISPROVE);this.card=card;}
	}
}