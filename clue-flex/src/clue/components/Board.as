package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	import clue.components.*;
	public class Board extends SkinnableComponent{

		[ArrayElementType("clue.components.Room")]
		private var _rooms:ArrayCollection=new ArrayCollection();
		
		[ArrayElementType("clue.components.Room")]
		public function get rooms():ArrayCollection{return _rooms;}
		public function set rooms(v:ArrayCollection):void{
			if (roomsContent!=null)
				for each (var rold:Room in this._rooms)
					this.roomsContent.removeElement(rold);
			this._rooms=v;
			if (roomsContent!=null)
				for each (var rnew:Room in this._rooms)
					this.roomsContent.addElement(rnew);
		}
		
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