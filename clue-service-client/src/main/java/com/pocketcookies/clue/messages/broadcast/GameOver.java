package com.pocketcookies.clue.messages.broadcast;

import com.pocketcookies.clue.messages.PlayerMessage;

public class GameOver extends PlayerMessage {
	private static final long serialVersionUID = 1L;

	public GameOver() {
	}

	public GameOver(String player) {
		super(player);
	}
}
