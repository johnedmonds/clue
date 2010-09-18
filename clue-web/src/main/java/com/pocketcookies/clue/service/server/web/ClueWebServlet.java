package com.pocketcookies.clue.service.server.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ClueWebServlet
 */
public class ClueWebServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
		else if (dispatchSection.length()<=1)
			getServletContext().getRequestDispatcher("/index.jsp").include(
					request, response);
		else
			response.sendRedirect(request.getContextPath());
	}

	private void login(HttpServletRequest request, HttpServletResponse response) {
	}

	private void games(HttpServletRequest request, HttpServletResponse response) {
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
