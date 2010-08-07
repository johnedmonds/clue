package com.pocketcookies.clue.messages.broadcast;

import com.pocketcookies.clue.Card;

public class Accusation extends Proposition{
	private static final long serialVersionUID = 1L;

	public Accusation() {
		super();
	}

	public Accusation(String player, Card room, Card suspect, Card weapon) {
		super(player, room, suspect, weapon);
	}
}
