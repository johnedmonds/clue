package com.pocketcookies.clue.blazeds.remoting;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.blazeds.config.SessionAttributeKeys;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
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
	 * will deliver you messages. In other words it is a rather fancy
	 * authentication method.
	 * 
	 * The association will retrieve your suspect for you. If you are not part
	 * of this game, it will return false.
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
}
