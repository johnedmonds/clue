package com.pocketcookies.clue.blazeds.remoting;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

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
		try {
			service.join(key, gameId, suspect);
			return true;
		} catch (Exception e) {
			logger.error("There was an error", e);
		}
		return false;
	}
}
