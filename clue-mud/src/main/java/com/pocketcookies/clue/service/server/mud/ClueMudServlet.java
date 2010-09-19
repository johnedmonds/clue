package com.pocketcookies.clue.service.server.mud;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

public class ClueMudServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ClueMudServlet.class);
	private final ClueMudServer mudServer;

	public ClueMudServlet() {
		super();
		this.mudServer = new ClueMudServer();
	}

	@Override
	public void init() {
		new Thread(mudServer).start();
	}

	@Override
	public void destroy() {
		mudServer.destroy();
	}
}
