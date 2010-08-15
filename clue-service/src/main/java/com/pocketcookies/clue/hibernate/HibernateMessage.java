package com.pocketcookies.clue.hibernate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Hibernate;

import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.players.Suspect;

/**
 * In the Clue server, we have many types of messages, such as StartGame,
 * GameOver, or NextTurn. These messages all have various fields associated with
 * them and none of them have the same number of fields. Thus it becomes very
 * difficult to put them in a database. We would need a huge number of
 * tables--one for each type of message. We would also need to join those tables
 * together when attempting to retrieve messages. Instead of doing this, we
 * realize (hope) that we will not need to do queries on these different fields.
 * Though users will need to know the fields, we will never be querying for
 * messages with a certain field.
 * 
 * So we're going to do something bad; we're going to combine multiple fields
 * into one field so that the database will not be in 1NF.
 * 
 * We will only need to be querying on certain fields such as the date, whether
 * the message has been retrieved by the user, gameId, and playerId. We will
 * then serialize the message using Java serialization and place that into a
 * binary field.
 * 
 * The main problem with this approach (other than the database no longer being
 * in first normal form) is the crazy versioning scheme for Java's
 * serialization. Hopefully we won't be changing the messages too often because
 * when we do, we'll have to update all the serialized things as well.
 * 
 * @author jack
 * 
 */
public class HibernateMessage {
	private int gameId;
	private Suspect suspect;
	private int id;
	private Message message;

	public HibernateMessage() {
	}

	public void setPublished(Date published) {
		if (this.message != null)
			this.message.setPublished(published);
	}

	public Date getPublished() {
		if (this.message != null)
			return this.message.getPublished();
		return new Date();
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setSerializedMessage(Blob serializedMessage)
			throws IOException, ClassNotFoundException, SQLException {
		this.message = (Message) new ObjectInputStream(
				serializedMessage.getBinaryStream()).readObject();
	}

	public Blob getSerializedMessage() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(this.message);
		oos.close();
		return Hibernate.createBlob(bos.toByteArray());
	}

	public Message getMessage() {
		return this.message;
	}

	public void setMessage(Message m) {
		this.message = m;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getGameId() {
		return gameId;
	}

	public void setSuspect(Suspect suspect) {
		this.suspect = suspect;
	}

	public Suspect getSuspect() {
		return suspect;
	}
}
