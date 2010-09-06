package com.pocketcookies.clue.messages.broadcast;

import javax.xml.bind.annotation.XmlType;

import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.messages.PlayerMessage;

/**
 * Indicates that the specified player moved to a new location.
 * 
 * @author jack
 * 
 */
@XmlType(name = "MoveMessage")
public class Move extends PlayerMessage {
	private static final long serialVersionUID = 1L;
	private Room from;
	private Room to;

	public Move(String player, Room from, Room to) {
		super(player);
		this.setFrom(from);
		this.setTo(to);
	}

	public Move() {
	}

	public void setFrom(Room from) {
		this.from = from;
	}

	public Room getFrom() {
		return from;
	}

	public void setTo(Room to) {
		this.to = to;
	}

	public Room getTo() {
		return to;
	}

}
