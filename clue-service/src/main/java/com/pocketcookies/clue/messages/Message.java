package com.pocketcookies.clue.messages;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlSeeAlso;

import com.pocketcookies.clue.messages.targeted.Cards;
import com.pocketcookies.clue.messages.targeted.DisprovingCard;

@XmlSeeAlso({ PlayerMessage.class, Cards.class, DisprovingCard.class })
public abstract class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private Date published = new Date();

	public Date getPublished() {
		return this.published;
	}

	public void setPublished(Date published) {
		this.published = published;
	}
}
