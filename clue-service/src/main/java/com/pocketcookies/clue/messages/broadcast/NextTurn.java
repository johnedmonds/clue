package com.pocketcookies.clue.messages.broadcast;

import com.pocketcookies.clue.messages.PlayerMessage;

public class NextTurn extends PlayerMessage {
	private static final long serialVersionUID = 1L;
	/**
	 * When the server moves to the next player, it will generate a random
	 * number to determine how many spaces this player is allowed to move.
	 */
	private int movementPointsAvailable;

	public NextTurn(String player, int movementPointsAvailable) {
		super(player);
		this.movementPointsAvailable = movementPointsAvailable;
	}

	public NextTurn() {
		super();
		this.movementPointsAvailable = -1;
	}

	public void setMovementPointsAvailable(int movementPointsAvailable) {
		this.movementPointsAvailable = movementPointsAvailable;
	}

	public int getMovementPointsAvailable() {
		return movementPointsAvailable;
	}

}
