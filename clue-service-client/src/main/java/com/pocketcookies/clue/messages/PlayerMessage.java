package com.pocketcookies.clue.messages;

import javax.xml.bind.annotation.XmlSeeAlso;

import com.pocketcookies.clue.messages.broadcast.Chat;
import com.pocketcookies.clue.messages.broadcast.Disprove;
import com.pocketcookies.clue.messages.broadcast.GameOver;
import com.pocketcookies.clue.messages.broadcast.Leave;
import com.pocketcookies.clue.messages.broadcast.Move;
import com.pocketcookies.clue.messages.broadcast.NextTurn;
import com.pocketcookies.clue.messages.broadcast.Proposition;

/**
 * For messages about a player.
 * 
 * @author jack
 * 
 */
@XmlSeeAlso({ Chat.class, Disprove.class, GameOver.class, NextTurn.class,
		Proposition.class, Move.class, Leave.class, Join.class })
public abstract class PlayerMessage extends Message {
	private static final long serialVersionUID = 1L;
	private String player;

	public String getPlayer() {
		return this.player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public PlayerMessage() {
		this.player = "";
	}

	public PlayerMessage(String player) {
		this.player = player;
	}
}
