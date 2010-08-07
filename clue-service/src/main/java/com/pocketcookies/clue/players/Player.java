package com.pocketcookies.clue.players;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.Grid;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.User;
import com.pocketcookies.clue.hibernate.HibernateMessage;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.messages.Message;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	private User user;
	private boolean lost = false;
	List<HibernateMessage> allMessages = new ArrayList<HibernateMessage>();
	private List<Card> hand = new ArrayList<Card>();
	private Point position;
	private Suspect suspect;
	private int id;
	private static final TopicPublisher publisher;
	private static final TopicSession topicSession;
	private static Logger logger = Logger.getLogger(Player.class);
	static {
		try {
			InitialContext context = new InitialContext();
			TopicConnectionFactory connectionFactory = (TopicConnectionFactory) context
					.lookup("ClueTopicConnectionFactory");
			Topic topic = (Topic) context.lookup("ClueTopic");
			TopicConnection topicConnection = connectionFactory
					.createTopicConnection();
			topicSession = topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			publisher = topicSession.createPublisher(topic);
		} catch (JMSException e) {
			throw new ExceptionInInitializerError(e);
		} catch (NamingException e) {
			throw new ExceptionInInitializerError(
					"There was a problem using JNDI to lookup the connection factory or the topic.");
		}
	}

	public Player(User user, Suspect suspect, int gameId) {
		this.user = user;
		this.suspect = suspect;
		this.gameId = gameId;
		switch (this.suspect) {
		case SCARLETT:
			this.setPosition(Grid.SCARLETT_START);
			break;
		case GREEN:
			this.setPosition(Grid.GREEN_START);
			break;
		case MUSTARD:
			this.setPosition(Grid.MUSTARD_START);
			break;
		case PEACOCK:
			this.setPosition(Grid.PEACOCK_START);
			break;
		case PLUM:
			this.setPosition(Grid.PLUM_START);
			break;
		case WHITE:
			this.setPosition(Grid.WHITE_START);
			break;
		}
	}

	public void setLost(boolean lost) {
		this.lost = lost;
	}

	public boolean isLost() {
		return lost;
	}

	public Suspect getSuspect() {
		return this.suspect;
	}

	public void setSuspect(Suspect suspect) {
		this.suspect = suspect;
	}

	public void publish(Message m) {
		HibernateMessage m2 = new HibernateMessage();
		m2.setPlayerId(this.id);
		m2.setMessage(m);
		this.allMessages.add(m2);
		ObjectMessage objectMessage;
		try {
			objectMessage = topicSession.createObjectMessage();
			objectMessage.setIntProperty("gameId", this.gameId);
			objectMessage.setIntProperty("playerId", this.id);
			objectMessage.setStringProperty("username", this.user.getName());
			objectMessage.setStringProperty("userKey", this.user.getKey());
			objectMessage.setObject(m);
			publisher.publish(objectMessage);
		} catch (JMSException e) {
			logger.error(
					"Attempted to create and publish a message to the message server but encountered an error.",
					e);
		}
	}

	/**
	 * Gets all messages since the parameter. Note that this will not include
	 * messages with a publish time exactly equal to "since."
	 * 
	 * @param since
	 *            All messages published after this date will be returned.
	 * @return All messages published after "since."
	 */
	public Message[] getUpdates(Date since) {
		List<HibernateMessage> messages;
		if (since != null) {
			messages = HibernateUtil
					.getSessionFactory()
					.getCurrentSession()
					.createQuery(
							"from HibernateMessage where published > :published and playerId = :playerId order by published asc")
					.setTimestamp("published", since)
					.setInteger("playerId", this.id).list();
		} else {
			messages = HibernateUtil
					.getSessionFactory()
					.getCurrentSession()
					.createQuery(
							"from HibernateMessage where playerId = :playerId order by published asc")
					.setInteger("playerId", this.id).list();
		}
		Message[] output = new Message[messages.size()];
		int i = 0;
		for (HibernateMessage m : messages) {
			output[i++] = m.getMessage();
		}
		return output;
	}

	public Message[] getAllUpdates() {
		Message[] ret = new Message[this.allMessages.size()];
		int i = 0;
		for (HibernateMessage message : this.allMessages) {
			ret[i++] = message.getMessage();
		}
		return ret;
	}

	public PlayerData getData() {
		PlayerData data = new PlayerData(this.suspect.ordinal(),
				this.user.getName(), this.suspect);
		return data;
	}

	public List<Card> getHand() {
		return this.hand;
	}

	public void setHand(List<Card> hand) {
		this.hand = hand;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public Point getPosition() {
		return position;
	}

	public User getUser() {
		return this.user;
	}

	// Hibernate stuff.

	private int gameId;

	public void setUser(User user) {
		this.user = user;
	}

	public List<HibernateMessage> getAllMessages() {
		return this.allMessages;
	}

	public void setAllMessages(List<HibernateMessage> messages) {
		this.allMessages = messages;
	}

	public int getX() {
		if (this.position == null) {
			return -1;
		}
		return this.position.x;
	}

	public int getY() {
		if (this.position == null) {
			return -1;
		}
		return this.position.y;
	}

	public void setX(int x) {
		if (this.position == null)
			this.position = new Point();
		this.position.x = x;
	}

	public void setY(int y) {
		if (this.position == null)
			this.position = new Point();
		this.position.y = y;
	}

	public Player() {
	}

	public void setId(int id) {
		this.id = id;
		for (HibernateMessage m : this.allMessages) {
			m.setPlayerId(this.id);
		}
	}

	public int getId() {
		return id;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getGameId() {
		return gameId;
	}
}
