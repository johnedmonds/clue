package clue.components{
	import spark.components.supportClasses.*;
	import mx.collections.*;
	import spark.components.*;
	public class Room extends SkinnableComponent{
		[Bindable]public var room:String;
		[SkinPart(required="true")]
		public var playersContent:Group;
		[ArrayElementType("clue.components.Player")]private var _players:ArrayCollection=new ArrayCollection();
		[ArrayElementType("clue.components.Player")]public function get players():ArrayCollection{return this._players;}
		public function set players(v:ArrayCollection):void{
			if(this.playersContent!=null)for each (var pold:Player in this._players)playersContent.removeElement(pold);
			this._players=v;
			if (this.playersContent!=null)for each (var pnew:Player in this._players){trace("added player");playersContent.addElement(pnew);}
			trace(this.playersContent!=null);
		}
		public function Room(room:String=""){
			this.room=room;
		}
		override protected function partAdded(partName:String, instance:Object):void{
			super.partAdded(partName,instance);
			trace("partadded "+room);
			if (instance==playersContent) for each (var p:Player in this._players){trace("partaddedplayer");playersContent.addElement(p);}
		}
	}
}