package com.pocketcookies.clue.messages;

import javax.xml.bind.annotation.XmlType;

import com.pocketcookies.clue.players.Suspect;

@XmlType(name = "JoinMessage")
public class Join extends PlayerMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Suspect suspect;

	public Suspect getSuspect() {
		return this.suspect;
	}

	public void setSuspect(Suspect suspect) {
		this.suspect = suspect;
	}

	public Join() {
		super();
		this.suspect = null;
	}

	public Join(String player, Suspect suspect) {
		super(player);
		this.suspect = suspect;
	}
}
