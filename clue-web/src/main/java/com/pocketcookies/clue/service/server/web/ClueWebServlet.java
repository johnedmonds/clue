package com.pocketcookies.clue.service.server.web;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

/**
 * Servlet implementation class ClueWebServlet
 */
public class ClueWebServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ClueServiceAPI service = null;
	private static final Logger logger = Logger.getLogger(ClueWebServlet.class);

	@Override
	public void init() {
		try {
			service = (ClueServiceAPI) new HessianProxyFactory().create(
					ClueServiceAPI.class,
					"http://localhost:8080/clue-service/ClueService");
		} catch (MalformedURLException e) {
			logger.fatal("There was a problem connecting to the service.", e);
			throw new ExceptionInInitializerError(e);
		}
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ClueWebServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		final String requestString = request.getRequestURI().substring(
				request.getContextPath().length());
		final String dispatchSection = requestString.substring(0,
				requestString.indexOf('?') == -1 ? requestString.length()
						: requestString.indexOf('?'));
		if (dispatchSection.equals("/clue/login"))
			login(request, response);
		else if (dispatchSection.equals("/clue/games"))
			games(request, response);
		else
			response.sendRedirect(request.getContextPath());
	}

	private void login(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (!request.getParameter("username").equals("")
				&& !request.getParameter("password").equals("")) {
			request.getSession().setAttribute(
					"key",
					service.login(request.getParameter("username"),
							request.getParameter("password")));
		}
		if (request.getSession().getAttribute("key") == null)
			response.sendRedirect(request.getContextPath() + "/login.jsp");
		else
			response.sendRedirect(request.getContextPath() + "/games.jsp");
	}

	private static String gameDataToString(GameData gd) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"id\":");
		sb.append(gd.getGameId());
		sb.append(",\"name\":\"");
		sb.append(gd.getGameName());
		sb.append("\",\"players\":[");
		if (gd.getPlayers().length > 0) {
			sb.append(playerDataToString(gd.getPlayers()[0]));
			for (int i = 1; i < gd.getPlayers().length; i++) {
				sb.append(",").append(playerDataToString(gd.getPlayers()[i]));
			}
		}
		sb.append("]}");
		return sb.toString();
	}

	private static Object playerDataToString(PlayerData pd) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"suspect\":\"");
		sb.append(pd.getSuspect().toString());
		sb.append("\",\"name\":\"");
		sb.append(pd.getPlayerName());
		sb.append("\"}");
		return sb;
	}

	private void games(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");
		response.getOutputStream().println("{\"games\":[");
		GameData[] data = service.getGames();
		if (data.length > 0) {
			response.getOutputStream().print(gameDataToString(data[0]));
			for (int i = 1; i < data.length; i++) {
				response.getOutputStream().print(
						"," + gameDataToString(data[i]));
			}
		}
		response.getOutputStream().print("]}");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
