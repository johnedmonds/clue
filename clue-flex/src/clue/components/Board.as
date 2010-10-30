package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	import clue.components.*;
	import clue.events.*;
	import flash.events.*;
	[Event(name="moveRequested",type="clue.events.MoveRequested")]
	public class Board extends SkinnableComponent{

		[ArrayElementType("clue.components.Room")]
		private var _rooms:ArrayCollection=new ArrayCollection();
		
		[ArrayElementType("clue.components.Room")]
		public function get rooms():ArrayCollection{return _rooms;}
		public function set rooms(v:ArrayCollection):void{
			if (this._rooms!=null){
				if (roomsContent!=null)
					for each (var rold:Room in this._rooms)
						this.roomsContent.removeElement(rold);
				//Remove event listeners.
				for each (var erold:Room in this._rooms)erold.removeEventListener(MouseEvent.CLICK,onCardClick);
			}
			this._rooms=v;
			if (this._rooms!=null){
				if (roomsContent!=null)
					for each (var rnew:Room in this._rooms)
						this.roomsContent.addElement(rnew);
				//Add event listeners.
				for each (var ernew:Room in this._rooms)ernew.addEventListener(MouseEvent.CLICK,onCardClick);
			}
		}
		private function onCardClick(event:MouseEvent):void{dispatchEvent(new MoveRequested(event.currentTarget as Room));}
		
		[SkinPart(required="true")]
		public var roomsContent:Group;
		override protected function partAdded(partName:String, instance:Object):void{
			if (instance==roomsContent)
				for each (var r:Room in this._rooms)
					this.roomsContent.addElement(r);
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