package com.pocketcookies.clue.messages.broadcast;

import javax.xml.bind.annotation.XmlType;

import com.pocketcookies.clue.messages.PlayerMessage;

/**
 * Message indicating that the specified player can disprove the last suggestion
 * or accusation.
 * 
 * Note that if the player is null, that indicates that no one can disprove the suggestion.
 * 
 * @author jack
 * 
 */
@XmlType(name = "DisproveMessage")
public class Disprove extends PlayerMessage {
	private static final long serialVersionUID = 1L;

	public Disprove() {
		super();
	}

	public Disprove(String player) {
		super(player);
	}
}
