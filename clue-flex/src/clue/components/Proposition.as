package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	public class Proposition extends SkinnableComponent{
		[ArrayElementType("String")][Bindable]public var roomCardNames:ArrayCollection;
		[ArrayElementType("String")][Bindable]public var suspectCardNames:ArrayCollection;
		[ArrayElementType("String")][Bindable]public var weaponCardNames:ArrayCollection;
		[SkinPart(required="true")]public var makeProposition:Button;
		[SkinPart(required="true")]public var cancelProposition:Button;
		[Bindable][SkinPart(required="true")]public var room:String;
		[Bindable][SkinPart(required="true")]public var suspect:String;
		[Bindable][SkinPart(required="true")]public var weapon:String;
		public function Proposition(roomCardNames:ArrayCollection=null,suspectCardNames:ArrayCollection=null,weaponCardNames:ArrayCollection=null){
			this.roomCardNames=roomCardNames;
			this.suspectCardNames=suspectCardNames;
			this.weaponCardNames=weaponCardNames;
		}
		public static function isRoom(face:String):Boolean{
			return face == "BALLROOM" || face == "BILLIARD_ROOM"
			|| face == "CONSERVATORY" || face == "DINING_ROOM" || face == "HALL"
			|| face == "KITCHEN" || face == "LIBRARY" || face == "LOUNGE"
			|| face == "STUDY";
		}
		public static function isWeapon(face:String):Boolean {
			return face == "CANDLESTICK" || face == "DAGGER" || face == "LEAD_PIPE"
					|| face == "REVOLVER" || face == "ROPE" || face == "SPANNER";
		}

		public static function isSuspect(face:String):Boolean {
			return face == "GREEN" || face == "SCARLETT" || face == "MUSTARD"
					|| face == "PEACOCK" || face == "PLUM" || face == "WHITE";
		}
	}
}