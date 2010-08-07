package com.pocketcookies.clue;

import java.io.Serializable;

public class GameData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int gameId;
	private String gameName;
	private PlayerData[] players;
	private GameStartedState gameStartedState;

	public GameData(int gameId, String gameName,
			GameStartedState gameStartedState) {
		this.gameId = gameId;
		this.gameName = gameName;
		this.players = null;
		this.gameStartedState = gameStartedState;
	}

	public GameData() {
		this.gameId = -1;
		this.gameName = null;
		this.players = null;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getGameName() {
		return gameName;
	}

	public void setPlayers(PlayerData[] players) {
		this.players = players;
	}

	public PlayerData[] getPlayers() {
		return players;
	}

	public void setGameStartedState(GameStartedState gameStartedState) {
		this.gameStartedState = gameStartedState;
	}

	public GameStartedState getGameStartedState() {
		return gameStartedState;
	}

}
