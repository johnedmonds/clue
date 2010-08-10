package com.pocketcookies.clue.messages.broadcast;

import javax.xml.bind.annotation.XmlType;

import com.pocketcookies.clue.messages.PlayerMessage;

@XmlType(name = "ChatMessage")
public class Chat extends PlayerMessage {
	private static final long serialVersionUID = 1L;
	private String message;

	public Chat() {
	}

	public Chat(String player, String message) {
		super(player);
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
