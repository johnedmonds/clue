package com.pocketcookies.clue.messages.broadcast;

import javax.xml.bind.annotation.XmlSeeAlso;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.messages.PlayerMessage;

@XmlSeeAlso({ Accusation.class, Suggestion.class })
public class Proposition extends PlayerMessage {
	private static final long serialVersionUID = 1L;
	private Card room, suspect, weapon;

	public Proposition(String player, Card room, Card suspect, Card weapon) {
		super(player);
		this.setRoom(room);
		this.setSuspect(suspect);
		this.setWeapon(weapon);
	}

	public Proposition() {
		super();
		this.setRoom(null);
		this.setWeapon(null);
		this.setSuspect(null);
	}

	public void setRoom(Card room) {
		this.room = room;
	}

	public Card getRoom() {
		return room;
	}

	public void setSuspect(Card suspect) {
		this.suspect = suspect;
	}

	public Card getSuspect() {
		return suspect;
	}

	public void setWeapon(Card weapon) {
		this.weapon = weapon;
	}

	public Card getWeapon() {
		return weapon;
	}
}
