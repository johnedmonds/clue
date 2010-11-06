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

		private var _rooms:Object;
		[Bindable]public var kitchen:Room;
		[Bindable]public var ballroom:Room;
		[Bindable]public var conservatory:Room;
		[Bindable]public var billiardRoom:Room;
		[Bindable]public var library:Room;
		[Bindable]public var study:Room;
		[Bindable]public var hall:Room;
		[Bindable]public var lounge:Room;
		[Bindable]public var diningRoom:Room;
		public function get rooms():Object{return _rooms;}
		public function set rooms(v:Object):void{
			this._rooms=v;
			this.kitchen=makeRoom('KITCHEN',v,onCardClick);
			this.ballroom=makeRoom('BALLROOM',v,onCardClick);
			this.conservatory=makeRoom('CONSERVATORY',v,onCardClick);
			this.billiardRoom=makeRoom('BILLIARD_ROOM',v,onCardClick);
			this.library=makeRoom('LIBRARY',v,onCardClick);
			this.study=makeRoom('STUDY',v,onCardClick);
			this.hall=makeRoom('HALL',v,onCardClick);
			this.lounge=makeRoom('LOUNGE',v,onCardClick);
			this.diningRoom=makeRoom('DINING_ROOM',v,onCardClick);
		}
		private static function makeRoom(roomName:String,rooms:Object,callback:Function):Room{
			const room:Room=new Room(roomName);
			BindingUtils.bindProperty(room,'players',rooms,roomName);
			room.addEventListener(MouseEvent.CLICK,callback);
			room.setStyle("skinClass",RoomSkin);
			return room;
		}
		private function onCardClick(event:MouseEvent):void{dispatchEvent(new MoveRequested(event.currentTarget as Room));}
		
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