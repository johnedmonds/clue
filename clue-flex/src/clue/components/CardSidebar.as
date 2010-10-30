package clue.components{
	import spark.components.supportClasses.*;
	import spark.components.*;
	import mx.collections.*;
	import flash.events.*;
	import clue.events.*;
	[SkinState("normal")]
	[SkinState("disprove")]
	[Event(name="disprove",type="clue.events.DisproveEvent")]
	public class CardSidebar extends SkinnableComponent{
		protected var _isDisproving:Boolean;
		[Bindable]public function get isDisproving():Boolean{return this._isDisproving;}
		public function set isDisproving(b:Boolean):void{this._isDisproving=b; this.invalidateSkinState();}
		override protected function getCurrentSkinState():String{return this._isDisproving==true?"disprove":"normal";}
		[SkinPart(required="true")]public var btnDisprove:Button;
		[SkinPart(required="true")]public var lstCards:List;
		[ArrayElementType("String")][Bindable]public var cardNames:ArrayCollection
		override protected function partAdded(partName:String, instance:Object):void{
			if (instance==this.btnDisprove)this.btnDisprove.addEventListener(MouseEvent.CLICK,function(e:MouseEvent):void{dispatchEvent(new DisproveEvent(lstCards.selectedItem));});
		}
	}
}
