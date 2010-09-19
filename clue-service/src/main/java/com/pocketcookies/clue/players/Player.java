package com.pocketcookies.clue.players;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.EnumType;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.Board;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.User;
import com.pocketcookies.clue.hibernate.HibernateMessage;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.messages.Message;

public class Player implements Serializable {
	public static class PlayerKey implements Serializable {
		private static final long serialVersionUID = 1L;
		private int gameId;
		private Suspect suspect;

		public PlayerKey() {
		}

		public PlayerKey(int gameId, Suspect suspect) {
			this.gameId = gameId;
			this.suspect = suspect;
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

		@Override
		public boolean equals(Object o) {
			return ((PlayerKey) o).gameId == this.gameId
					&& ((PlayerKey) o).suspect == this.suspect;
		}

		@Override
		public int hashCode() {
			return this.gameId + this.suspect.ordinal();
		}
	}

	private static final long serialVersionUID = 1L;
	private User user;
	private boolean lost = false;
	List<HibernateMessage> allMessages = new ArrayList<HibernateMessage>();
	private List<Card> hand = new ArrayList<Card>();
	private Room room;
	private static final TopicPublisher publisher;
	private static final TopicSession topicSession;
	private static Logger logger = Logger.getLogger(Player.class);
	private PlayerKey id;
	static {
		try {
			logger.info("Starting connection to ActiveMQ.");
			// TODO: Make this configurable.
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"vm://clue-broker");
			logger.info("Creating connection.");
			TopicConnection topicConnection = connectionFactory
					.createTopicConnection();
			logger.info("Creating session.");
			topicSession = topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);

			logger.info("Loading topic.");
			// TODO: Make this configurable.
			publisher = topicSession.createPublisher(topicSession
					.createTopic("ClueTopic"));
		} catch (JMSException e) {
			logger.fatal("There was an error initializing the JMS system.", e);
			throw new ExceptionInInitializerError(e);
		} catch (Exception e) {
			logger.fatal(
					"There was some unknown error (probably with BrokerService).",
					e);
			throw new ExceptionInInitializerError(e);
		}
	}

	public Player(User user, Suspect suspect, int gameId) {
		this.id = new PlayerKey(gameId, suspect);
		this.user = user;
		this.room = Board.getStartingPosition(suspect);
	}

	public void setLost(boolean lost) {
		this.lost = lost;
	}

	public boolean isLost() {
		return lost;
	}

	public void publish(Message m) {
		HibernateMessage m2 = new HibernateMessage();
		m2.setGameId(this.id.getGameId());
		m2.setSuspect(this.id.getSuspect());
		m2.setMessage(m);
		this.allMessages.add(m2);
		ObjectMessage objectMessage;
		try {
			objectMessage = topicSession.createObjectMessage();
			objectMessage.setIntProperty("gameId", this.id.getGameId());
			objectMessage.setIntProperty("suspect", this.id.getSuspect()
					.ordinal());
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
	@SuppressWarnings("unchecked")
	public Message[] getUpdates(Date since) {
		List<HibernateMessage> messages;
		if (since != null) {
			messages = HibernateUtil
					.getSessionFactory()
					.getCurrentSession()
					.createQuery(
							"from HibernateMessage where published > :published and gameId = :gameId and suspect = :suspect order by published asc")
					.setTimestamp("published", since)
					.setInteger("gameId", this.id.getGameId())
					.setParameter(
							"suspect",
							this.id.getSuspect(),
							Hibernate.custom(EnumType.class,
									new String[] { "enumClass" },
									new String[] { Suspect.class.getName() }))
					.list();
		} else {
			messages = HibernateUtil
					.getSessionFactory()
					.getCurrentSession()
					.createQuery(
							"from HibernateMessage where gameId = :gameId and suspect = :suspect order by published asc")
					.setInteger("gameId", this.id.getGameId())
					.setParameter(
							"suspect",
							this.id.getSuspect(),
							Hibernate.custom(EnumType.class,
									new String[] { "enumClass" },
									new String[] { Suspect.class.getName() }))
					.list();
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
		PlayerData data = new PlayerData(this.user.getName(),
				this.id.getSuspect());
		return data;
	}

	public List<Card> getHand() {
		return this.hand;
	}

	public void setHand(List<Card> hand) {
		this.hand = hand;
	}

	public Room getRoom() {
		return this.room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public User getUser() {
		return this.user;
	}

	// Hibernate stuff.

	public void setUser(User user) {
		this.user = user;
	}

	public List<HibernateMessage> getAllMessages() {
		return this.allMessages;
	}

	public void setAllMessages(List<HibernateMessage> messages) {
		this.allMessages = messages;
	}

	public Player() {
	}

	public PlayerKey getId() {
		return this.id;
	}

	public void setId(PlayerKey id) {
		this.id = id;
	}
}
