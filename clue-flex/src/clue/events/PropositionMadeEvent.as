package clue.events{
	import flash.events.*;
	public class PropositionMadeEvent extends Event{
		public static const PROPOSITION_MADE:String="propositionMade";
		
		private var _room:String;
		private var _suspect:String;
		private var _weapon:String;
		public function get room():String{return this._room;}
		public function get suspect():String{return this._suspect;}
		public function get weapon():String{return this._weapon;}
		public function PropositionMadeEvent(room:String, suspect:String, weapon:String){
			super(PROPOSITION_MADE);
			this._room=room;
			this._suspect=suspect;
			this._weapon=weapon;
		}
	}
}