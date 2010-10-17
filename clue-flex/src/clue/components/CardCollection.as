package clue.components{
	import spark.components.*;
	import spark.components.supportClasses.*;
	import mx.collections.*;
	import mx.events.*;
	import mx.binding.utils.BindingUtils;
	public class CardCollection extends SkinnableComponent{
		[ArrayElementType("clue.components.Card")]
		private var _cards:ArrayCollection;
		[ArrayElementType("clue.components.Card")] public function get cards():ArrayCollection{return _cards;}
		public function set cards(v:ArrayCollection):void{
			var c:Card;
			if (this.cardsContent!=null) for each (c in _cards)this.cardsContent.removeElement(c);
			this._cards=v;
			if (this.cardsContent!=null) for each (c in _cards)this.cardsContent.addElement(c);
			this._cards.addEventListener("collectionChange",onCardsChanged);
		}
		private function onCardsChanged(event:CollectionEvent):void{
			var c:Card;
			if (cardsContent!=null){
				if (event.kind==CollectionEventKind.ADD) for each (c in event.items)this.cardsContent.addElement(c);
				else if (event.kind==CollectionEventKind.REMOVE) for each (c in event.items)this.cardsContent.removeElement(c);
			}
		}
		override protected function partAdded(partName:String, instance:Object):void{
			super.partAdded(partName,instance);
			if (instance==this.cardsContent)
				for each (var c:Card in this._cards)this.cardsContent.addElement(c);
		}
		public static function makeCards(cardNames:ArrayCollection):ArrayCollection{
			var out:ArrayCollection=new ArrayCollection();
			for each (var s:String in cardNames){
				var c:Card=new Card(s);
				c.setStyle("skinClass",CardSkin);
				out.addItem(c);
			}
			return out;
		}
		[SkinPart(required="true")]
		public var cardsContent:Group;
	}
}