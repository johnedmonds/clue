package clue.components{
	import spark.components.supportClasses.*;
	import mx.graphics.*;
	[RemoteClass(alias="com.pocketcookies.clue.PlayerData")]
	public class Player extends SkinnableComponent{
		[Bindable]
		public var playerName:String;
		private var _suspect:String;
		[Bindable]
		public function get suspect():String{return this._suspect;}
		public function set suspect(v:String):void{this._suspect=v;dispatchEvent(new Event("suspectColorChange"));}
		public function Player(playerName:String="",suspect:String=""){
			this.playerName=playerName;
			this._suspect=suspect;
		}
		
		[Bindable(event="suspectColorChange")]
		public function get suspectColor():SolidColor{
			var color:SolidColor=new SolidColor();
			if (this._suspect=="SCARLETT")
				color.color=0xff0000;
			else if (this._suspect=="WHITE")
				color.color=0xffffff;
			else if (this._suspect=="GREEN")
				color.color=0x00ff00;
			else if (this._suspect=="PEACOCK")
				color.color=0x0000ff;
			else if (this._suspect=="MUSTARD")
				color.color=0xffff00;
			else if (this._suspect=="PLUM")
				color.color=0xff00ff;
			return color;
		}
	}
}