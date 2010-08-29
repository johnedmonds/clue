package com.pocketcookies.clue.messages.broadcast;

import com.pocketcookies.clue.messages.PlayerMessage;

public class Leave extends PlayerMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Leave() {
		super();
	}

	public Leave(String player) {
		super(player);
	}

}
