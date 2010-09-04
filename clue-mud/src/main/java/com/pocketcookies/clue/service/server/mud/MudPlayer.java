package com.pocketcookies.clue.service.server.mud;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;

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
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.messages.Join;
import com.pocketcookies.clue.messages.broadcast.Accusation;
import com.pocketcookies.clue.messages.broadcast.Chat;
import com.pocketcookies.clue.messages.broadcast.Disprove;
import com.pocketcookies.clue.messages.broadcast.Leave;
import com.pocketcookies.clue.messages.broadcast.Move;
import com.pocketcookies.clue.messages.broadcast.NextTurn;
import com.pocketcookies.clue.messages.broadcast.Suggestion;
import com.pocketcookies.clue.messages.targeted.Cards;
import com.pocketcookies.clue.messages.targeted.DisprovingCard;
import com.pocketcookies.clue.mud.Grid;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.commands.CommandProcessor;

public class MudPlayer implements Runnable, MessageListener {

	private Socket client;
	private static final Logger logger = Logger.getLogger(MudPlayer.class);
	private Map<String, Point> players = new TreeMap<String, Point>();

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
	private String username = null;
	private ClueServiceAPI service;
	public Suspect suspect;
	// In which game the player is currently.
	private int gameId = -1;
	private TopicSession session;
	private TopicSubscriber subscriber;

	public MudPlayer(Socket client, ClueServiceAPI service) {
		this.client = client;
		this.service = service;
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
				username = reader.readLine();
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
			this.stopMessageConnection();
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
		this.suspect = null;
		this.players.clear();
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
				// The game is starting.
				// Position all the players.
				for (PlayerData pd : service.getStatus(this.gameId)
						.getPlayers()) {
					this.players.put(pd.getPlayerName(),
							Grid.getStartingPosition(pd.getSuspect()));
				}
				writer.println("Your cards are: ");
				for (Card c : ((Cards) o).getCards()) {
					writer.println("\t" + c.toString());
				}

			} else if (o instanceof Move) {
				new Formatter(writer).format(
						"Player %s moved from (%d,%d) to (%d,%d)",
						((Move) o).getPlayer(), ((Move) o).getxFrom(),
						((Move) o).getyFrom(), ((Move) o).getxTo(),
						((Move) o).getyTo()).flush();
				// Sanity check that we remember the original position of the
				// player.
				assert (new Point(((Move) o).getxFrom(), ((Move) o).getxTo())
						.equals(this.players.get(((Move) o).getPlayer())));
				// Move where we think that player is.
				this.players.put(((Move) o).getPlayer(),
						new Point(((Move) o).getxTo(), ((Move) o).getyTo()));
			} else if (o instanceof Chat) {
				new Formatter(writer).format("Player %s says \"%s\"",
						((Chat) o).getPlayer(), ((Chat) o).getMessage());
			} else if (o instanceof Suggestion) {
				Suggestion suggestion = (Suggestion) o;
				new Formatter(writer).format(
						"Player %s suggested %s in the %s with the %s.",
						suggestion.getPlayer(), suggestion.getSuspect()
								.toString(), suggestion.getRoom().toString(),
						suggestion.getWeapon().toString());
			} else if (o instanceof Accusation) {
				Accusation accusation = (Accusation) o;
				new Formatter(writer).format(
						"Player %s accused %s in the %s with the %s.",
						accusation.getPlayer(), accusation.getSuspect()
								.toString(), accusation.getRoom().toString(),
						accusation.getWeapon().toString());
			} else if (o instanceof Disprove) {
				Disprove d = (Disprove) o;
				new Formatter(writer).format(
						"Player %s can disprove the proposition.",
						d.getPlayer());
			} else if (o instanceof Join) {
				Join j = (Join) o;
				new Formatter(writer).format("%s joined as %s.", j.getPlayer(),
						j.getSuspect().toString());
			} else if (o instanceof Leave) {
				Leave leave = (Leave) o;
				this.players.remove(leave.getPlayer());
				new Formatter(writer).format("%s left.", leave.getPlayer());
			} else if (o instanceof DisprovingCard) {
				DisprovingCard d = (DisprovingCard) o;
				new Formatter(writer).format("You are shown %s.", d.getCard()
						.toString());
			} else {
				logger.warn("You forgot to handle this type of message.");
				writer.println("Unknown message type.");
			}
			writer.println();
			writer.flush();
		} catch (JMSException e) {
			logger.error(
					"There was a problem retrieving the object from the ObjectMessage.",
					e);
			writer.println("There was some problem with a message that was supposed to be delivered to you.");
		} catch (NoSuchGameException e) {
			writer.println("The game seems to no longer exist.  Try leaving this one and joining another.");
			logger.error("Player " + this.username + " could not find game "
					+ this.gameId, e);
		}
	}

	public void startMessageConnection() {
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

	public void stopMessageConnection() {
		if (subscriber == null)
			return;
		try {
			subscriber.setMessageListener(null);
			subscriber.close();
		} catch (JMSException e) {
			logger.error(
					"There was an error closing the subscriber or session.", e);
		}
	}

	public Map<String, Point> getPlayers() {
		return players;
	}

	public String getUsername() {
		return this.username;
	}

	public Socket getClient() {
		return this.client;
	}
}
