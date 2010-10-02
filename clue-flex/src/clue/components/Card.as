package clue.components {
	import spark.components.*;
	import mx.events.FlexEvent;
	import flash.events.*;
	import mx.graphics.*;
	import spark.primitives.Rect;
	import flash.filters.*;
	public class Card extends Group{
		private var label:Label;
		public function Card(cardName:String="")
		{
			super();
			this.label=new Label();
			this.label.horizontalCenter=0;
			this.label.verticalCenter=0;
			this.label.text=cardName;
			this.addEventListener("initialize", initializeHandler);
		}
		public function get cardName():String{return this.label.text;}
		public function set cardName(value:String):void{this.label.text=value;}
		private function initializeHandler(event:FlexEvent):void{
			this.filters=[new DropShadowFilter()];
			this.width=80;this.height=150;
			var r:Rect=new Rect();
			r.percentWidth=100;r.percentHeight=100;
			r.fill=new SolidColor(0x00ff00);
			this.addElement(r);
			this.addElement(label);
		}
	}
}