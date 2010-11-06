package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	import clue.components.*;
	import clue.events.*;
	import flash.events.*;
	import mx.binding.utils.*;
	[Event(name="moveRequested",type="clue.events.MoveRequested")]
	public class Board extends SkinnableComponent{

		[Bindable]public var rooms:Object;
		[SkinPart(required="true")][Bindable]public var kitchen:Room;
		[SkinPart(required="true")][Bindable]public var ballroom:Room;
		[SkinPart(required="true")][Bindable]public var conservatory:Room;
		[SkinPart(required="true")][Bindable]public var billiardRoom:Room;
		[SkinPart(required="true")][Bindable]public var library:Room;
		[SkinPart(required="true")][Bindable]public var study:Room;
		[SkinPart(required="true")][Bindable]public var hall:Room;
		[SkinPart(required="true")][Bindable]public var lounge:Room;
		[SkinPart(required="true")][Bindable]public var diningRoom:Room;
		private function onCardClick(event:MouseEvent):void{dispatchEvent(new MoveRequested(event.currentTarget as Room));}

		protected override function partAdded(partName:String,instance:Object):void{
			if (instance==kitchen)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==ballroom)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==conservatory)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==billiardRoom)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==library)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==study)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==hall)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==lounge)instance.addEventListener(MouseEvent.CLICK,onCardClick);
			if (instance==diningRoom)instance.addEventListener(MouseEvent.CLICK,onCardClick);
		}
		
		public static function getSuspectStartingRoom(suspect:String):String{
			if (suspect=="SCARLETT")return "LOUNGE";
			else if (suspect=="MUSTARD")return "DINING_ROOM";
			else if (suspect=="WHITE")return "KITCHEN";
			else if (suspect=="GREEN")return "BALLROOM";
			else if (suspect=="PEACOCK")return "CONSERVATORY";
			else if (suspect=="PLUM")return "LIBRARY";
			else return null;
		}
	}
}