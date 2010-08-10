package com.pocketcookies.clue.messages.broadcast;

import com.pocketcookies.clue.Card;

public class Suggestion extends Proposition {
	private static final long serialVersionUID = 1L;

	public Suggestion(String player, Card room, Card suspect, Card weapon) {
		super(player, room, suspect, weapon);
	}

	public Suggestion() {
		super();
	}
}
