package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	import clue.components.*;
	public class Board extends SkinnableComponent{

		[ArrayElementType("clue.components.Room")]
		private var _rooms:Array=new Array();
		
		private static function addRooms(rooms:ArrayCollection,roomsContent:Group):void{
			if (roomsContent!=null)
				for each (var r:Room in rooms)
					roomsContent.addElement(r);
		}
		[ArrayElementType("clue.components.Room")]
		public function get rooms():Array{return this._rooms;}
		
		public function Board(){
			this._rooms["BALLROOM"]=new Room("Ballroom");
			this._rooms["HALL"]=new Room("Hall");
			this._rooms["CONSERVATORY"]=new Room("Conservatory");
			this._rooms["BILLIARD_ROOM"]=new Room("Billiard Room");
			this._rooms["LOUNGE"]=new Room("Lounge");
			this._rooms["DINING_ROOM"]=new Room("Dining Room");
			this._rooms["STUDY"]=new Room("Study");
			this._rooms["KITCHEN"]=new Room("Kitchen");
			this._rooms["LIBRARY"]=new Room("Library");
			for each (var r:Room in this._rooms)r.setStyle("skinClass",RoomSkin);
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
		public function set suspects(suspects:Array):void{
			for each (var suspect:Player in suspects)
				this._rooms[getSuspectStartingRoom(suspect.suspect)].addElement(suspect);
		}
	}
}