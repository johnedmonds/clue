package clue.components{
	import spark.components.supportClasses.*;
	public class Room extends SkinnableComponent{
		[Bindable]public var room:String;
		public function Room(room:String=""){
			this.room=room;
		}
	}
}