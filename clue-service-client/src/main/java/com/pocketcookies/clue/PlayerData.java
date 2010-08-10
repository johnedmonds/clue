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
	/**
	 * The id of this player. Unique to this game.
	 */
	private int playerId;
	private Suspect suspect;

	public PlayerData(int playerId, String playerName, Suspect suspect) {
		this.playerId = playerId;
		this.playerName = playerName;
		this.setSuspect(suspect);
	}

	public PlayerData() {
		this.playerId = -1;
		this.suspect = null;
		this.playerName = null;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setSuspect(Suspect suspect) {
		this.suspect = suspect;
	}

	public Suspect getSuspect() {
		return suspect;
	}
}