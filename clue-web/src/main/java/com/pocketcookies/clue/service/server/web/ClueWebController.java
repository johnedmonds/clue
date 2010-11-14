package com.pocketcookies.clue.service.server.web;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

/**
 * Servlet implementation class ClueWebServlet
 */
@Controller
public class ClueWebController {
	private static final long serialVersionUID = 1L;
	private final ClueServiceAPI service;
	private static final Logger logger = Logger
			.getLogger(ClueWebController.class);

	public ClueWebController() {
		super();
		try {
			service = (ClueServiceAPI) new HessianProxyFactory().create(
					ClueServiceAPI.class, Config.SERVICE_LOCATION);
		} catch (MalformedURLException e) {
			logger.fatal("There was a problem connecting to the service.", e);
			throw new ExceptionInInitializerError(e);
		}
	}

	@RequestMapping(value = "/")
	public ModelAndView index() {
		return new ModelAndView("index");
	}

	@RequestMapping(value = "/login")
	public @ResponseBody
	String login(@RequestParam("username") String username,
			@RequestParam("password") String password,
			HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");
		final String key = service.login(username, password);
		if (key == null)
			response.setStatus(403);
		else {
			request.getSession().setAttribute("key", key);
			request.getSession().setAttribute("username", username);
			try {
				return new JSONStringer().object().key("key").value(key)
						.endObject().toString();
			} catch (JSONException e) {
				logger.error(
						"There was an error creating the json for logging in.",
						e);
			}
		}
		return null;
	}

	@RequestMapping(value = "/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		request.getSession().invalidate();
	}

	@RequestMapping(value = "/games")
	public @ResponseBody
	String games(HttpServletResponse response) {
		try {
			final GameData[] games = service.getGames();
			final JSONArray jsonGames = new JSONArray();
			for (GameData gd : games) {
				final JSONObject jsonGame = new JSONObject();
				jsonGame.put("name", gd.getGameName());
				final JSONArray jsonPlayers = new JSONArray();
				for (PlayerData pd : gd.getPlayers()) {
					final JSONObject jsonPlayer = new JSONObject();
					jsonPlayer.put("name", pd.getPlayerName());
					jsonPlayer.put("suspect", pd.getSuspect().toString());
					jsonPlayers.put(jsonPlayer);
				}
				jsonGame.put("players", jsonPlayers);
			}
			return new JSONStringer().object().key("games").value(jsonGames)
					.endObject().toString();
		} catch (JSONException e) {
			return null;
		}
	}
}