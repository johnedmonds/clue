package com.pocketcookies.clue.messages.targeted;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.messages.Message;

public class Cards extends Message {
	private static final long serialVersionUID = 1L;
	private Card[] cards;

	public Cards(Card[] cards) {
		this.cards = cards;
	}

	public Cards() {
		this.cards = null;
	}

	public Card[] getCards() {
		return this.cards;
	}

	public void setCards(Card[] cards) {
		this.cards = cards;
	}
}
