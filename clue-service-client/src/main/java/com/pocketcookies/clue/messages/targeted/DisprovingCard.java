package com.pocketcookies.clue.messages.targeted;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.messages.Message;

public class DisprovingCard extends Message {
	private static final long serialVersionUID = 1L;
	private Card card;

	public DisprovingCard(Card card) {
		this.card = card;
	}

	public DisprovingCard() {
		this.card = null;
	}

	public Card getCard() {
		return this.card;
	}

	public void setCard(Card card) {
		this.card = card;
	}
}
