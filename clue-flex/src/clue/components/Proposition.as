package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	import flash.events.*;
	import clue.events.*;
	import clue.components.*;
	[Event(name=PropositionMadeEvent.PROPOSITION_MADE,type="clue.events.PropositionMadeEvent")]
	[Event(name="propositionCanceled",type="flash.events.Event")]
	public class Proposition extends SkinnableComponent{
		public static const PROPOSITION_CANCELED:String="propositionCanceled";
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
		public static function makeProposition(roomCardNames:ArrayCollection,suspectCardNames:ArrayCollection,weaponCardNames:ArrayCollection,onMakeProposition:Function,onCancelProposition:Function):Proposition{
			var proposition:Proposition=new Proposition(roomCardNames,suspectCardNames,weaponCardNames);
			proposition.verticalCenter=0;
			proposition.horizontalCenter=0;
			proposition.addEventListener(Proposition.PROPOSITION_CANCELED,onCancelProposition);
			proposition.addEventListener(PropositionMadeEvent.PROPOSITION_MADE,onMakeProposition);
			proposition.setStyle("skinClass",PropositionSkin);
			return proposition;
		}
		public static function isRoom(o:Object):Boolean{
			var face:String=o as String;
			return face == "BALLROOM" || face == "BILLIARD_ROOM"
			|| face == "CONSERVATORY" || face == "DINING_ROOM" || face == "HALL"
			|| face == "KITCHEN" || face == "LIBRARY" || face == "LOUNGE"
			|| face == "STUDY";
		}
		public static function isWeapon(o:Object):Boolean {
			var face:String=o as String;
			return face == "CANDLESTICK" || face == "DAGGER" || face == "LEAD_PIPE"
					|| face == "REVOLVER" || face == "ROPE" || face == "SPANNER";
		}

		public static function isSuspect(o:Object):Boolean {
			var face:String=o as String;
			return face == "GREEN" || face == "SCARLETT" || face == "MUSTARD"
					|| face == "PEACOCK" || face == "PLUM" || face == "WHITE";
		}
		protected override function partAdded(partName:String, instance:Object):void{
			if (instance == this.makeProposition)this.makeProposition.addEventListener(MouseEvent.CLICK,function(e:MouseEvent):void{dispatchEvent(new PropositionMadeEvent(this.room,this.suspect,this.weapon));});
			else if (instance == this.cancelProposition)this.cancelProposition.addEventListener(MouseEvent.CLICK,function(e:MouseEvent):void{dispatchEvent(new Event(PROPOSITION_CANCELED));});
		}
	}
}