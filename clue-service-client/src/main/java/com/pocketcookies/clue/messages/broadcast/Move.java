package com.pocketcookies.clue.messages.broadcast;

import javax.xml.bind.annotation.XmlType;

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
	private int xFrom, xTo, yFrom, yTo;
	/**
	 * The server will calculate the distance between <from> and <to> and tell
	 * all the players this cost.
	 */
	private int cost;

	public Move(String player, int xFrom, int yFrom, int xTo, int yTo, int cost) {
		super(player);
		this.setxFrom(xFrom);
		this.setxTo(xTo);
		this.setyFrom(yFrom);
		this.setyTo(yTo);
		this.setCost(cost);
	}

	public Move() {
		super();
		this.setxFrom(-1);
		this.setyFrom(-1);
		this.setxTo(-1);
		this.setyTo(-1);
		this.setCost(-1);
	}

	public void setxFrom(int xFrom) {
		this.xFrom = xFrom;
	}

	public int getxFrom() {
		return xFrom;
	}

	public void setxTo(int xTo) {
		this.xTo = xTo;
	}

	public int getxTo() {
		return xTo;
	}

	public void setyFrom(int yFrom) {
		this.yFrom = yFrom;
	}

	public int getyFrom() {
		return yFrom;
	}

	public void setyTo(int yTo) {
		this.yTo = yTo;
	}

	public int getyTo() {
		return yTo;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getCost() {
		return cost;
	}
}
