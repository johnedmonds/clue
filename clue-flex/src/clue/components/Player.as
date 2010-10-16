package clue.components{
	import spark.components.supportClasses.*;
	import mx.graphics.*;
	[RemoteClass(alias="com.pocketcookies.clue.PlayerData")]
	public class Player extends SkinnableComponent{
		[Bindable]
		public var playerName:String;
		[Bindable]
		public var suspect:String;
		public function Player(playerName:String="",suspect:String=""){
			this.playerName=playerName;
			this.suspect=suspect;
		}
		public function get suspectColor():SolidColor{
			var color:SolidColor=new SolidColor();
			if (this.suspect=="SCARLETT")
				color.color=0xff0000;
			else if (this.suspect=="WHITE")
				color.color=0xffffff;
			else if (this.suspect=="GREEN")
				color.color=0x00ff00;
			else if (this.suspect=="PEACOCK")
				color.color=0x0000ff;
			else if (this.suspect=="MUSTARD")
				color.color=0xffff00;
			else if (this.suspect=="PLUM")
				color.color=0xff00ff;
			return color;
		}
	}
}