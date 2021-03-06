package com.pocketcookies.clue.service.server.web;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.exceptions.GameAlreadyExistsException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
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

	/**
	 * Responds to an AJAX request to login.
	 * 
	 * @param username
	 *            The user's user name.
	 * @param password
	 *            The user's password.
	 * @param request
	 *            The request made by the user.
	 * @param response
	 *            The response to be returned to the user.
	 * @throws IOException
	 * @throws JSONException
	 */
	@RequestMapping(value = "/login")
	public void login(@RequestParam("username") String username,
			@RequestParam("password") String password,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, JSONException {
		response.setContentType("application/json");
		final String key = service.login(username, password);
		if (key == null)
			response.setStatus(403);
		else {
			request.getSession().setAttribute("key", key);
			request.getSession().setAttribute("username", username);
			response.getWriter().write(
					new JSONStringer().object().key("key").value(key)
							.endObject().toString());
		}
	}

	/**
	 * If the user does not have JavaScript enabled, the form will be submitted
	 * to this function.
	 * 
	 * @param username
	 *            The user's user name.
	 * @param password
	 *            The user's password.
	 * @param request
	 *            The request made by the user.
	 * @param response
	 *            The response given back to the user.
	 * @throws IOException
	 */
	@RequestMapping(value = "/login-form")
	public void loginForm(@RequestParam("username") String username,
			@RequestParam("password") String password,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		final String key = service.login(username, password);
		if (key != null) {
			request.getSession().setAttribute("key", key);
			request.getSession().setAttribute("username", username);
		}
		response.sendRedirect(request.getContextPath() + "/");
	}

	/**
	 * Respond to an AJAX request to logout.
	 * 
	 * @param request
	 */
	@RequestMapping(value = "/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		request.getSession().invalidate();
	}

	/**
	 * If the user does not have JavaScript enabled, the form will be submitted
	 * to this function.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/logout-form")
	public void logoutForm(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		request.getSession().invalidate();
		response.sendRedirect(request.getContextPath() + "/");
	}

	@RequestMapping(value = "/games")
	public void games(HttpServletResponse response) {
		response.setContentType("application/json");
		try {
			final GameData[] games = service.getGames();
			final JSONArray jsonGames = new JSONArray();
			for (GameData gd : games) {
				final JSONObject jsonGame = new JSONObject();
				final JSONArray jsonPlayers = new JSONArray();
				for (PlayerData pd : gd.getPlayers()) {
					final JSONObject jsonPlayer = new JSONObject();
					jsonPlayer.put("name", pd.getPlayerName());
					jsonPlayer.put("suspect", pd.getSuspect().toString());
					jsonPlayers.put(jsonPlayer);
				}
				jsonGame.put("id", gd.getGameId());
				jsonGame.put("name", gd.getGameName());
				jsonGame.put("state", gd.getGameStartedState().toString());
				jsonGame.put("players", jsonPlayers);
				jsonGames.put(jsonGame);
			}
			response.getWriter().write(
					new JSONStringer().object().key("games").value(jsonGames)
							.endObject().toString());
		} catch (JSONException e) {
			logger.error(
					"There was an error generating the JSON for the games currently in play.",
					e);
		} catch (IOException e) {
			logger.error("There was an error writing to the client.", e);
		}
	}

	@RequestMapping(value = "/create")
	public void create(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("gameName") String gameName) {
		response.setContentType("application/json");
		response.setStatus(200);
		try {
			try {
				service.create((String) request.getSession()
						.getAttribute("key"), gameName);
				response.getWriter().write("\"Success\"");
			} catch (GameAlreadyExistsException e) {
				response.setStatus(400);
				try {
					response.getWriter()
							.write(new JSONStringer()
									.object()
									.key("error")
									.value("A game by that name already exists.")
									.endObject().toString());
				} catch (JSONException e2) {
					logger.error(
							"There was an error generating the JSON error string.",
							e2);
				}
			} catch (NotLoggedInException e) {
				response.setStatus(400);
				try {
					response.getWriter()
							.write(new JSONStringer()
									.object()
									.key("error")
									.value("You must log in before you can create a game.")
									.endObject().toString());
				} catch (JSONException e2) {
					logger.error(
							"There was an error generating the JSON error string.",
							e2);
				}
			}
		} catch (IOException e) {
			logger.error("There was an error writing to the client.", e);
		}
	}

	@RequestMapping(value = "/create-form")
	public void createForm(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("gameName") String gameName) throws IOException {
		try {
			service.create((String) request.getSession().getAttribute("key"),
					gameName);
		} catch (NotLoggedInException e) {
		} catch (GameAlreadyExistsException e) {
		}
		response.sendRedirect(request.getContextPath() + "/");
	}
}