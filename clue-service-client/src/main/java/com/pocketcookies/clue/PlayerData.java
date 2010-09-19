package com.pocketcookies.clue;

import java.io.Serializable;

import com.pocketcookies.clue.players.Suspect;

public class PlayerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Obviously don't include this player's key because that would be a
	// huge security flaw.
	private String playerName;
	private Suspect suspect;

	public PlayerData(String playerName, Suspect suspect) {
		this.playerName = playerName;
		this.setSuspect(suspect);
	}

	public PlayerData() {
		this.suspect = null;
		this.playerName = null;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setSuspect(Suspect suspect) {
		this.suspect = suspect;
	}

	public Suspect getSuspect() {
		return suspect;
	}
}