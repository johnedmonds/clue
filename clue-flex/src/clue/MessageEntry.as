package clue{
	import mx.controls.Image;
	import mx.graphics.SolidColor;
	public class MessageEntry{
		//Message icons
		[Embed(source="../../resources/chat.png")]public static var ICO_CHAT:Class;
		[Embed(source="../../resources/game_over.png")]public static var ICO_GAME_OVER:Class;
		[Embed(source="../../resources/join.svg")]public static var ICO_JOIN:Class;
		[Embed(source="../../resources/leave.svg")]public static var ICO_LEAVE:Class;
		[Embed(source="../../resources/move.png")]public static var ICO_MOVE:Class;
		[Embed(source="../../resources/unknown.svg")]public static var ICO_UNKNOWN:Class;
		
		[Bindable]public var label:String;
		[Bindable]public var iconSource:Object;
		[Bindable]public var playerColor:SolidColor;
		public function MessageEntry(label:String,iconSource:Object,playerColor:SolidColor){this.label=label;this.iconSource=iconSource;this.playerColor=playerColor;}
		public function toString():String{return this.label;}
	}
}