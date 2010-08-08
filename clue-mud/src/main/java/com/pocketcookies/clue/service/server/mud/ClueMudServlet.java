package com.pocketcookies.clue.service.server.mud;

import javax.servlet.http.HttpServlet;

public class ClueMudServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void init() {
		new Thread(new ClueMudServer()).start();
	}

}
