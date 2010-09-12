package com.pocketcookies.clue;

public enum Room {
	KITCHEN, BALLROOM, CONSERVATORY, BILLIARD_ROOM, LIBRARY, STUDY, HALL, LOUNGE, DINING_ROOM;
	public Card toCard() {
		switch (this) {
		case BALLROOM:
			return Card.BALLROOM;
		case BILLIARD_ROOM:
			return Card.BILLIARD_ROOM;
		case CONSERVATORY:
			return Card.CONSERVATORY;
		case DINING_ROOM:
			return Card.DINING_ROOM;
		case HALL:
			return Card.HALL;
		case KITCHEN:
			return Card.KITCHEN;
		case LIBRARY:
			return Card.LIBRARY;
		case LOUNGE:
			return Card.LOUNGE;
		case STUDY:
			return Card.STUDY;
		default:
			return null;
		}
	}
}
