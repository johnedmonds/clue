package com.pocketcookies.clue.messages.broadcast;

import javax.xml.bind.annotation.XmlType;

import com.pocketcookies.clue.messages.PlayerMessage;

@XmlType(name = "LeaveMessage")
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
