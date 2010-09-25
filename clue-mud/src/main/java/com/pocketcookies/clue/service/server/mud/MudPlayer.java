package com.pocketcookies.clue.service.server.mud;

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
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Board;
import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.messages.Join;
import com.pocketcookies.clue.messages.broadcast.Accusation;
import com.pocketcookies.clue.messages.broadcast.Chat;
import com.pocketcookies.clue.messages.broadcast.Disprove;
import com.pocketcookies.clue.messages.broadcast.GameOver;
import com.pocketcookies.clue.messages.broadcast.Leave;
import com.pocketcookies.clue.messages.broadcast.Move;
import com.pocketcookies.clue.messages.broadcast.NextTurn;
import com.pocketcookies.clue.messages.broadcast.Suggestion;
import com.pocketcookies.clue.messages.targeted.Cards;
import com.pocketcookies.clue.messages.targeted.DisprovingCard;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.commands.CommandProcessor;

public class MudPlayer implements Runnable, MessageListener {

	private Socket client;
	private static final Logger logger = Logger.getLogger(MudPlayer.class);
	private Map<String, Room> players = new TreeMap<String, Room>();

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

	public MudPlayer(Socket client, ClueServiceAPI service,
			TopicConnection topicConnection) {
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
			if (o instanceof NextTurn)
				processNextTurn((NextTurn) o);
			// We use this message to know that the game is starting.
			else if (o instanceof Cards)
				processCardsMessage((Cards) o);
			else if (o instanceof Move)
				processMove((Move) o);
			else if (o instanceof Chat)
				processChat((Chat) o);
			else if (o instanceof Suggestion)
				processSuggestion((Suggestion) o);
			else if (o instanceof Accusation)
				processAccusation((Accusation) o);
			else if (o instanceof Disprove)
				processDisprove((Disprove) o);
			else if (o instanceof Join)
				processJoin((Join) o);
			else if (o instanceof Leave)
				processLeave((Leave) o);
			else if (o instanceof DisprovingCard)
				processDisprovingCard((DisprovingCard) o);
			else if (o instanceof GameOver)
				processGameOver((GameOver) o);
			else {
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

	private void processGameOver(GameOver go) {
		new Formatter(writer).format("%s won the game.", go.getPlayer());
		try {
			service.leave(this.key, this.gameId);
		} catch (Exception e) {
			logger.error("There was an error leaving.", e);
		}
		this.leave();
		this.stopMessageConnection();
	}

	public void processNextTurn(NextTurn nextTurn) {
		new Formatter(writer)
				.format("It is now %s's turn.  That player has %d movement points available.",
						nextTurn.getPlayer(),
						nextTurn.getMovementPointsAvailable());
	}

	public void processDisprovingCard(DisprovingCard d) {
		new Formatter(writer).format("You are shown %s.", d.getCard()
				.toString());
	}

	public void processLeave(Leave leave) {
		this.players.remove(leave.getPlayer());
		new Formatter(writer).format("%s left.", leave.getPlayer());
	}

	public void processJoin(Join j) {
		new Formatter(writer).format("%s joined as %s.", j.getPlayer(), j
				.getSuspect().toString());
	}

	public void processDisprove(Disprove d) {
		new Formatter(writer).format("Player %s can disprove the proposition.",
				d.getPlayer());
	}

	public void processAccusation(Accusation accusation) {
		new Formatter(writer).format(
				"Player %s accused %s in the %s with the %s.", accusation
						.getPlayer(), accusation.getSuspect().toString(),
				accusation.getRoom().toString(), accusation.getWeapon()
						.toString());
	}

	public void processSuggestion(Suggestion suggestion) {
		new Formatter(writer).format(
				"Player %s suggested %s in the %s with the %s.", suggestion
						.getPlayer(), suggestion.getSuspect().toString(),
				suggestion.getRoom().toString(), suggestion.getWeapon()
						.toString());
	}

	public void processChat(Chat c) {
		new Formatter(writer).format("Player %s says \"%s\"", c.getPlayer(),
				c.getMessage());
		System.out.println();
	}

	public void processMove(Move m) {
		new Formatter(writer).format("Player %s moved from %s to %s.",
				m.getPlayer(), m.getFrom().toString(), m.getTo().toString())
				.flush();
		// Sanity check that we remember the original position of the
		// player.
		assert (m.getFrom().equals(this.players.get(m.getPlayer())));
		// Move where we think that player is.
		this.players.put(m.getPlayer(), m.getTo());
	}

	public void processCardsMessage(Cards cards) throws NoSuchGameException {
		// Position all the players.
		loadPlayerPositions();
		writer.println("Your cards are: ");
		for (Card c : cards.getCards()) {
			writer.println("\t" + c.toString());
		}
	}

	public void loadPlayerPositions() throws NoSuchGameException {
		for (PlayerData pd : service.getStatus(this.gameId).getPlayers()) {
			this.players.put(pd.getPlayerName(),
					Board.getStartingPosition(pd.getSuspect()));
		}
	}

	public void startMessageConnection() {
		try {
			this.subscriber = this.session.createSubscriber(
					(Topic) new InitialContext()
							.lookup("java:comp/env/clue/jms/clue-topic"),
					"gameId = " + this.gameId + " and suspect = "
							+ this.suspect.ordinal(), false);
			this.subscriber.setMessageListener(this);
		} catch (JMSException e) {
			logger.error("There was an error working with the message server.",
					e);
			writer.println("There was an error connecting to one of our servers.  Try whatever you just did again later.");
		} catch (NamingException e) {
			logger.error("There was an error getting the topic.", e);
			writer.println("There was an error looking up a resource.  Try logging out and back in again.");
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

	public Map<String, Room> getPlayers() {
		return players;
	}

	public String getUsername() {
		return this.username;
	}

	public Socket getClient() {
		return this.client;
	}

}
