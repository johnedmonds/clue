package com.pocketcookies.clue.service.server.mud;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.messages.broadcast.NextTurn;
import com.pocketcookies.clue.messages.targeted.Cards;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.commands.CommandProcessor;

public class MudPlayer implements Runnable, MessageListener {

	private Socket client;
	private static final Logger logger = Logger.getLogger(MudPlayer.class);

	private static final TopicConnection topicConnection;
	static {
		try {
			logger.info("Loading connection factory.");
			// TODO: Make this configurable.
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://localhost:61616");
			logger.info("Creating JMS connection.");
			topicConnection = connectionFactory.createTopicConnection();
			logger.info("Starting JMS.");
			topicConnection.start();
		} catch (JMSException e) {
			logger.error(
					"There was a problem starting a connection to the message server.",
					e);
			throw new ExceptionInInitializerError(e);
		}
	}

	private PrintWriter writer;
	private BufferedReader reader;
	private String key = null;
	private ClueServiceAPI service;
	private Point location = new Point();
	// In which game the player is currently.
	private int gameId = -1;
	private TopicSession session;
	private TopicSubscriber subscriber;

	public MudPlayer(Socket client, ClueServiceAPI service) {
		this.client = client;
		this.service = service;
		this.location = new Point();
		try {
			writer = new PrintWriter(client.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
		} catch (IOException e) {
			logger.error(
					"There was an error initializing the writer or reader for the client.",
					e);
			throw new ExceptionInInitializerError(e);
		}
		try {
			this.session = topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error(
					"There was an error creating a topic session or a subscriber to that topic.",
					e);
			writer.println("There was an error connecting to one of our servers.  Sorry.");
			writer.flush();
			try {
				client.close();
			} catch (IOException e1) {
				logger.error(
						"There was an error closing the socket while handling another error.",
						e);
			}
			throw new ExceptionInInitializerError(e);
		}
	}

	@Override
	public void run() {
		try {
			// TODO: Description
			writer.println("Welcome to Clue.  Enter your username (or pick one if you're new).");
			writer.flush();
			while (this.key == null) {
				writer.print("Username: ");
				writer.flush();
				String username = reader.readLine();
				writer.print("Password: ");
				writer.flush();
				String password = reader.readLine();
				this.key = service.login(username, password);
				if (this.key == null)
					writer.println("That username is already taken (or you entered the wrong password).");
				else
					writer.println(this.key);
				writer.flush();
			}
			writer.print(">");
			writer.flush();
			String command = reader.readLine();
			while (command != null) {
				CommandProcessor.process(command, this);
				writer.print(">");
				writer.flush();
				command = reader.readLine();
			}
		} catch (IOException e) {
			logger.error(
					"There was an error relating to the output stream of the client socket.",
					e);
		} finally {
			this.stopConnection();
			writer.flush();
			writer.close();
			try {
				client.close();
				session.close();
			} catch (IOException e) {
				logger.error("There was an error closing the client socket", e);
			} catch (JMSException e) {
				logger.error("There was an error closing the session.", e);
			}
		}
	}

	public ClueServiceAPI getService() {
		return this.service;
	}

	public PrintWriter getWriter() {
		return this.writer;
	}

	public boolean isInGame() {
		return this.gameId >= 0;
	}

	public String getKey() {
		return this.key;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getGameId() {
		return gameId;
	}

	public void leave() {
		this.gameId = -1;
		this.location = new Point();
	}

	@Override
	public void onMessage(Message m) {
		try {
			Object o = ((ObjectMessage) m).getObject();
			assert (o instanceof com.pocketcookies.clue.messages.Message);
			if (o instanceof NextTurn) {
				writer.println("It is now " + ((NextTurn) o).getPlayer()
						+ "'s turn.  That player has "
						+ ((NextTurn) o).getMovementPointsAvailable()
						+ " movement points available.");
			} else if (o instanceof Cards) {
				writer.println("Your cards are: ");
				for (Card c : ((Cards) o).getCards()) {
					writer.println("\t" + c.toString());
				}
			} else {
				logger.warn("You forgot to handle this type of message.");
				writer.println("Unknown message type.");
			}
			writer.flush();
		} catch (JMSException e) {
			logger.error(
					"There was a problem retrieving the object from the ObjectMessage.",
					e);
		}
	}

	public void startConnection() {
		try {
			this.subscriber = this.session.createSubscriber(
					session.createTopic("ClueTopic"), "userKey = '" + this.key
							+ "' and gameId = " + this.gameId, false);
			this.subscriber.setMessageListener(this);
		} catch (JMSException e) {
			logger.error("There was an error working with the message server.",
					e);
			writer.println("There was an error connecting to one of our servers.  Try whatever you just did again later.");
		}
	}

	public void stopConnection() {
		try {
			subscriber.setMessageListener(null);
			subscriber.close();
		} catch (JMSException e) {
			logger.error(
					"There was an error closing the subscriber or session.", e);
		}
	}
}
