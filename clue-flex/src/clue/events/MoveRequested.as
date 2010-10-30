package clue.events{
	import flash.events.*;
	import clue.components.*;
	public class MoveRequested extends Event{
		[Bindable]public var toRoom:Room;
		public static const MOVE:String="moveRequested";
		public function MoveRequested(toRoom:Room){super(MOVE);this.toRoom=toRoom;}
	}
	
}