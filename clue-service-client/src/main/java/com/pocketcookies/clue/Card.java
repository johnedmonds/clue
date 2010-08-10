package com.pocketcookies.clue;

public enum Card {
	DAGGER, CANDLESTICK, REVOLVER, ROPE, LEAD_PIPE, SPANNER, SCARLETT, MUSTARD, WHITE, GREEN, PEACOCK, PLUM, KITCHEN, BALLROOM, CONSERVATORY, DINING_ROOM, BILLIARD_ROOM, LIBRARY, LOUNGE, HALL, STUDY;
	public boolean isRoom() {
		return this == BALLROOM || this == BILLIARD_ROOM
				|| this == CONSERVATORY || this == DINING_ROOM || this == HALL
				|| this == KITCHEN || this == LIBRARY || this == LOUNGE
				|| this == STUDY;
	}

	public boolean isWeapon() {
		return this == CANDLESTICK || this == DAGGER || this == LEAD_PIPE
				|| this == REVOLVER || this == ROPE || this == SPANNER;
	}

	public boolean isSuspect() {
		return this == GREEN || this == SCARLETT || this == MUSTARD
				|| this == PEACOCK || this == PLUM || this == WHITE;
	}
}
