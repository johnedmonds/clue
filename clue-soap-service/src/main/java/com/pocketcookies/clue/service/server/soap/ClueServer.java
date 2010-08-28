package com.pocketcookies.clue.service.server.soap;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.GameStartedState;
import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.CheatException;
import com.pocketcookies.clue.exceptions.GameAlreadyExistsException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.IllegalMoveException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotInRoomException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

@WebService(serviceName = "ClueServer")
public class ClueServer {

	private static final ClueServiceAPI service;
	private static final TopicConnection topicConnection;
	private static Logger logger;

	private static class PlayerId {
		public String key;
		public int gameId;

		public PlayerId(String key, int gameId) {
			this.key = key;
			this.gameId = gameId;
		}

		@Override
		public boolean equals(Object o) {
			PlayerId p = (PlayerId) o;
			return p.gameId == this.gameId && p.key.equals(this.key);
		}

		@Override
		public int hashCode() {
			return gameId + key.hashCode();
		}
	}

	static {
		logger = Logger.getLogger(ClueServer.class);
		try {
			logger.info("Loading connection factory.");
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					"tcp://localhost:61616");
			logger.info("Creating connection.");
			topicConnection = connectionFactory.createTopicConnection();
			logger.info("Starting connection.");
			topicConnection.start();
			logger.info("Loading service.");
			// TODO: Make this configurable;
			service = (ClueServiceAPI) new HessianProxyFactory().create(
					ClueServiceAPI.class,
					"http://localhost:8080/clue-service/ClueService");
		} catch (JMSException e) {
			logger.fatal(
					"There was a problem starting a connection to the message server.",
					e);
			throw new ExceptionInInitializerError(e);
		} catch (MalformedURLException e) {
			logger.fatal("There was an error retrieving the service.", e);
			throw new ExceptionInInitializerError(e);
		} catch (Exception e) {
			logger.fatal("Something really bad happened.", e);
			throw new ExceptionInInitializerError(e);
		}
	}

	public ClueServer() throws NamingException {
	}

	@WebMethod
	public String login(@WebParam(name = "username") String username,
			@WebParam(name = "password") String password) {
		return service.login(username, password);
	}

	@WebMethod
	public String changePassword(@WebParam(name = "username") String username,
			@WebParam(name = "key") String key,
			@WebParam(name = "newPassword") String newPassword) {
		return service.changePassword(username, key, newPassword);
	}

	@WebMethod
	public int create(@WebParam(name = "key") String key,
			@WebParam(name = "gameName") String gameName)
			throws NotLoggedInException, GameAlreadyExistsException {
		return service.create(key, gameName);
	}

	@WebMethod
	public void join(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId,
			@WebParam(name = "suspect") Suspect suspect)
			throws NotLoggedInException, NoSuchGameException,
			SuspectTakenException, GameStartedException, AlreadyJoinedException {
		service.join(key, gameId, suspect);
	}

	/**
	 * Finds all updates.
	 * 
	 * @param key
	 *            Lets us figure out which player to whom we should deliver
	 *            updates (we allow a single user to play as a single suspect).
	 * @param gameId
	 *            For which game updates should be given.
	 * @param timeout
	 *            Sometimes there are no updates. Instead of having clients spam
	 *            calls to getUpdates, call it once, and the server will wait
	 *            for "timeout" milliseconds for a message to appear. If
	 *            multiple messages appear quickly enough, those will be
	 *            delivered as well.
	 * @param since
	 *            Only messages published after this date/time will be
	 *            retrieved. Note that this does not include messages with a
	 *            publish timestamp exactly equal to "since". The reason for
	 *            this is that clients are expected to remember the publish date
	 *            of the most recent message they received. This can become the
	 *            value of the "since" parameter so that you will not have to
	 *            deal with messages being delivered twice.
	 * @return
	 * @throws NotLoggedInException
	 * @throws NoSuchGameException
	 * @throws NotYourTurnException
	 */
	@WebMethod
	public Message[] getUpdates(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId,
			@WebParam(name = "timeout") long timeout,
			@WebParam(name = "since") Date since) throws NotLoggedInException,
			NoSuchGameException, NotYourTurnException {
		PlayerId pid = new PlayerId(key, gameId);
		TopicSession session = null;
		TopicSubscriber subscriber = null;
		/*
		 * See if we can get a new session with the message server. We'll use
		 * this connection to "hold on" to messages while we check with the
		 * server. We want to handle the following cases:
		 * 
		 * -new messages since the user checked last
		 * 
		 * -no new messages since the user checked last but one will appear
		 * before the timeout expires.
		 */

		try {
			session = topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);
			// We only want to be notified of messages that fit this
			// description.
			subscriber = session.createSubscriber(
					session.createTopic("ClueTopic"), "userKey = '" + key
							+ "' and gameId = " + gameId, true);
		} catch (JMSException e) {
			logger.error(
					"Error creating a session from the connection to the message broker.  Maybe the message broker went down.",
					e);
		}
		Message[] updates = null;
		try {
			// Check for new messages.
			updates = service.getUpdates(key, gameId, since);
			if (updates.length <= 0 && subscriber != null) {
				logger.info("There were no immediate messages.");
				javax.jms.ObjectMessage message;
				message = (ObjectMessage) subscriber.receive(timeout);
				List<Message> messages = new LinkedList<Message>();
				while (message != null) {
					messages.add((Message) message.getObject());
					message = (ObjectMessage) subscriber.receiveNoWait();
				}
				if (messages.size() <= 0)
					updates = null;
				else {
					updates = messages.toArray(new Message[messages.size()]);
				}
			}
		} catch (JMSException e) {
			logger.error(
					"There was an error reading a message from the subscriber (however the subscriber must have been successfully created).",
					e);
		} finally {
			try {
				if (subscriber != null)
					subscriber.close();
				if (session != null)
					session.close();
			} catch (JMSException e) {
				logger.error(
						"There was an error while closing the subscriber or session.",
						e);
			}
		}
		return updates;
	}

	@WebMethod
	public Message[] getAllUpdates(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId) throws NotLoggedInException,
			NoSuchGameException, NotYourTurnException {
		return service.getAllUpdates(key, gameId);
	}

	@WebMethod
	public void suggest(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId,
			@WebParam(name = "suspect") Card suspect,
			@WebParam(name = "weapon") Card weapon)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, NotInRoomException {
		service.suggest(key, gameId, weapon, suspect);
	}

	@WebMethod
	public void accuse(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId,
			@WebParam(name = "room") Card room,
			@WebParam(name = "suspect") Card suspect, Card weapon)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException {
		service.accuse(key, gameId, room, suspect, weapon);
	}

	@WebMethod
	public void disprove(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId,
			@WebParam(name = "disprovingCard") Card disprovingCard)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, CheatException {
		service.disprove(key, gameId, disprovingCard);
	}

	@WebMethod
	public int move(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId, @WebParam(name = "x") int x,
			@WebParam(name = "y") int y) throws NotLoggedInException,
			NotYourTurnException, NoSuchGameException, IllegalMoveException {
		return service.move(key, gameId, x, y);
	}

	@WebMethod
	public void endTurn(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId) throws NotLoggedInException,
			NotYourTurnException, NoSuchGameException {
		service.endTurn(key, gameId);
	}

	@WebMethod
	public GameData getStatus(@WebParam(name = "gameId") int gameId)
			throws NoSuchGameException {
		return service.getStatus(gameId);
	}

	@WebMethod
	public void startGame(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId) throws NotLoggedInException,
			NoSuchGameException, GameStartedException,
			NotEnoughPlayersException {
		service.startGame(key, gameId);
	}

	@WebMethod
	public void chat(@WebParam(name = "key") String key,
			@WebParam(name = "gameId") int gameId,
			@WebParam(name = "message") String message)
			throws NotLoggedInException, NoSuchGameException,
			NotYourTurnException, NotInGameException {
		service.chat(key, gameId, message);
	}

	@WebMethod
	public GameData[] getGames(@WebParam(name = "name") String name,
			@WebParam(name = "state") GameStartedState state) {
		return service.getGames(name, state);
	}

}
