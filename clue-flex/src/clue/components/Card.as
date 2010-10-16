package clue.components {
	import spark.components.*;
	import spark.components.supportClasses.*;
	import mx.events.FlexEvent;
	import flash.events.*;
	import mx.graphics.*;
	import spark.primitives.Rect;
	import flash.filters.*;
	public class Card extends SkinnableComponent{
		[Bindable] public var face:String;
		public function Card(face:String="")
		{
			super();
			this.face=face;
		}
	}
}