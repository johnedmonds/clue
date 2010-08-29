package com.pocketcookies.clue.messages;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "JoinMessage")
public class Join extends PlayerMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Join() {
		super();
	}

	public Join(String player) {
		super(player);
	}
}
