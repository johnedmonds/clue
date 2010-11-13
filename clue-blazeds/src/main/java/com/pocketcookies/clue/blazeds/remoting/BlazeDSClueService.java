package com.pocketcookies.clue.blazeds.remoting;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.blazeds.config.SessionAttributeKeys;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.exceptions.CheatException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotInRoomException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

import flex.messaging.FlexContext;
import flex.messaging.client.FlexClient;

public class BlazeDSClueService {
	private static final ClueServiceAPI service;
	private static final Logger logger = Logger
			.getLogger(BlazeDSClueService.class);
	static {
		try {
			service = (ClueServiceAPI) new HessianProxyFactory().create(
					ClueServiceAPI.class, Config.SERVICE_LOCATION);
		} catch (MalformedURLException e) {
			logger.fatal("The URL found in the config file was malformed.", e);
			throw new ExceptionInInitializerError(e);
		}
	}

	public String login(String username, String password) {
		return service.login(username, password);
	}

	public boolean join(String key, int gameId, Suspect suspect) {
		FlexContext.getFlexClient().setAttribute(SessionAttributeKeys.KEY, key);
		FlexContext.getFlexClient().setAttribute(SessionAttributeKeys.GAME_ID,
				gameId);
		FlexContext.getFlexClient().setAttribute(SessionAttributeKeys.SUSPECT,
				suspect.ordinal());
		try {
			service.join(key, gameId, suspect);
			return true;
		} catch (Exception e) {
			logger.error("There was an error", e);
		}
		return false;
	}

	/**
	 * This is a version of join. It associates you with this server so that it
	 * will deliver messages to you. In other words it is a rather fancy
	 * authentication method.
	 * 
	 * If you have already joined the game, the association will retrieve your
	 * suspect for you. Otherwise, it will return null.
	 * 
	 * @param key
	 *            Your key.
	 * @param gameId
	 *            The game to which you wish to associate.
	 * @return The suspect as which you joined if successful. Null if not
	 *         successful.
	 */
	public Suspect associate(String key, int gameId) {
		final Suspect suspect = service.getSuspectForPlayer(key, gameId);
		if (suspect == null)
			return null;
		final FlexClient client = FlexContext.getFlexClient();
		client.setAttribute(SessionAttributeKeys.KEY, key);
		client.setAttribute(SessionAttributeKeys.GAME_ID, gameId);
		client.setAttribute(SessionAttributeKeys.SUSPECT, suspect.ordinal());
		System.out.println("Added");
		return suspect;
	}

	public void chat(String key, int gameId, String message)
			throws NotLoggedInException, NoSuchGameException,
			NotInGameException {
		service.chat(key, gameId, message);
	}

	public GameData getStatus(int gameId) throws NoSuchGameException {
		return service.getStatus(gameId);
	}

	/**
	 * Attempts to start the game. You must have already called join() or
	 * associate() before calling this method. Otherwise, it will throw an
	 * exception.
	 * 
	 * @throws NotEnoughPlayersException
	 * @throws GameStartedException
	 * @throws NoSuchGameException
	 * @throws NotLoggedInException
	 */
	public void startGame() throws NotLoggedInException, NoSuchGameException,
			GameStartedException, NotEnoughPlayersException {
		final FlexClient client = FlexContext.getFlexClient();
		service.startGame(
				(String) client.getAttribute(SessionAttributeKeys.KEY),
				(Integer) client.getAttribute(SessionAttributeKeys.GAME_ID));
	}

	/**
	 * Attempts to make a suggestion. There is no need to provide the room
	 * because suggestions may only be made from the room in which the player is
	 * currently located.
	 * 
	 * @param suspect
	 *            The suspected suspect.
	 * @param weapon
	 *            The suspected weapon.
	 * @throws NotInRoomException
	 * @throws NoSuchGameException
	 * @throws NotYourTurnException
	 * @throws NotLoggedInException
	 */
	public void suggest(Card suspect, Card weapon) throws NotLoggedInException,
			NotYourTurnException, NoSuchGameException, NotInRoomException {
		final FlexClient client = FlexContext.getFlexClient();
		service.suggest((String) client.getAttribute(SessionAttributeKeys.KEY),
				(Integer) client.getAttribute(SessionAttributeKeys.GAME_ID),
				suspect, weapon);
	}

	/**
	 * Accuses a tuple of room, suspect, weapon. The player either wins or loses
	 * after this.
	 * 
	 * @param room
	 *            The suspected room.
	 * @param suspect
	 *            The suspected suspect.
	 * @param weapon
	 *            The suspected weapon.
	 * @throws NoSuchGameException
	 * @throws NotYourTurnException
	 * @throws NotLoggedInException
	 */
	public void accuse(Card room, Card suspect, Card weapon)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException {
		final FlexClient client = FlexContext.getFlexClient();
		service.accuse((String) client.getAttribute(SessionAttributeKeys.KEY),
				(Integer) client.getAttribute(SessionAttributeKeys.GAME_ID),
				room, suspect, weapon);
	}

	/**
	 * Ends the current player's turn.
	 * 
	 * @param key
	 *            The player's key.
	 * @param gameId
	 *            The game id.
	 * @throws NotLoggedInException
	 * @throws NotYourTurnException
	 * @throws NoSuchGameException
	 */
	public void endTurn(String key, int gameId) throws NotLoggedInException,
			NotYourTurnException, NoSuchGameException {
		service.endTurn(key, gameId);
	}

	/**
	 * Disproves a suggestion.
	 * 
	 * @param key
	 *            Your key
	 * @param gameId
	 *            The game for which this should be delivered.
	 * @param card
	 *            The card used for disproving.
	 * @throws NotLoggedInException
	 * @throws NotYourTurnException
	 * @throws NoSuchGameException
	 * @throws CheatException
	 */
	public void disprove(String key, int gameId, Card card)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, CheatException {
		service.disprove(key, gameId, card);
	}

	/**
	 * Moves to a room
	 * 
	 * @param key
	 *            The player's key.
	 * @param gameId
	 *            The game in which the player wants to move.
	 * @param room
	 *            The room to which the player is moving.
	 * @throws NoSuchGameException
	 * @throws NotYourTurnException
	 * @throws NotLoggedInException
	 */
	public void movePlayer(String key, int gameId, Room room)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException {
		service.move(key, gameId, room);
	}

	public Message[] getAllUpdates(String key, int gameId)
			throws NotLoggedInException, NoSuchGameException,
			NotYourTurnException {
		return service.getAllUpdates(key, gameId);
	}

	public void leave(String key, int gameId) throws NotLoggedInException,
			NoSuchGameException, NotInGameException {
		service.leave(key, gameId);
	}
}
