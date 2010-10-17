package clue.components{
	import spark.components.supportClasses.*;
	import mx.collections.*;
	import mx.events.*;
	import spark.components.*;
	public class Room extends SkinnableComponent{
		[Bindable]public var room:String;
		[SkinPart(required="true")]
		public var playersContent:Group;
		[ArrayElementType("clue.components.Player")]private var _players:ArrayCollection=new ArrayCollection();
		[ArrayElementType("clue.components.Player")]public function get players():ArrayCollection{return this._players;}
		[Bindable]
		public function set players(v:ArrayCollection):void{
			var p:Player;
			if (playersContent!=null)for each (p in _players) this.playersContent.removeElement(p);
			this._players=v;
			if (playersContent!=null)for each (p in _players) this.playersContent.addElement(p);
			this._players.addEventListener("collectionChange",onCollectionChanged);
		}
		private function onCollectionChanged(event:CollectionEvent):void{
			if (event.kind==CollectionEventKind.ADD)for each (var padd:Player in event.items)this.playersContent.addElement(padd);
			else if (event.kind==CollectionEventKind.REMOVE)for each (var premove:Player in event.items)this.playersContent.removeElement(premove);
		}
		public function Room(room:String=""){
			this.room=room;
		}
		override protected function partAdded(partName:String, instance:Object):void{
			super.partAdded(partName,instance);
			if (instance==playersContent) for each (var p:Player in this._players){playersContent.addElement(p);}
		}
	}
}