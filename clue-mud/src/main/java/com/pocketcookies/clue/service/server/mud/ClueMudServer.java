package com.pocketcookies.clue.service.server.mud;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.util.LinkedList;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.caucho.hessian.client.HessianProxyFactory;
import com.pocketcookies.clue.config.Config;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

public class ClueMudServer implements Runnable {

	private static final Logger logger = Logger.getLogger(ClueMudServer.class);
	private ClueServiceAPI service;
	private ServerSocket serverSocket;
	private Thread myThread;
	// We need to keep track of players so we can gracefully stop them.
	private LinkedList<MudPlayer> players = new LinkedList<MudPlayer>();
	private final TopicConnection topicConnection;

	public ClueMudServer() {
		logger.info("Starting to load clue service and message service objects.");
		try {
			final InitialContext initialContext = new InitialContext();
			logger.info("Loading clue service.");
			service = (ClueServiceAPI) new HessianProxyFactory().create(
					ClueServiceAPI.class, Config.SERVICE_LOCATION);
			logger.info("Loading connection factory.");
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) initialContext
					.lookup(Config.CONNECTION_FACTORY_JNDI);
			logger.info("Creating JMS connection.");
			topicConnection = topicConnectionFactory.createTopicConnection();
			logger.info("Starting JMS.");
			topicConnection.start();
			logger.info("JMS successfully started.");
		} catch (MalformedURLException e) {
			logger.error(
					"There was a problem contacting the server (malformed URL).",
					e);
			throw new ExceptionInInitializerError(e);
		} catch (JMSException e) {
			logger.error(
					"There was a problem starting a connection to the message server.",
					e);
			throw new ExceptionInInitializerError(e);
		} catch (NamingException e) {
			logger.error("There was an error with the InitialContext", e);
			throw new ExceptionInInitializerError(e);
		}
		logger.info("MUD server has loaded the message service and clue service objects successfully.");

		// Initializing the server socket for telnet clients.
		logger.info("Creating socket to accept connections.");
		try {
			serverSocket = new ServerSocket(9001, 10, null);
		} catch (IOException e) {
			logger.fatal("There was an error creating the socket.", e);
			throw new ExceptionInInitializerError(e);
		}
		logger.info("MUD server socket has been successfully created.");

	}

	@Override
	public void run() {
		myThread = Thread.currentThread();
		logger.info("Thread to accept connections has started.");
		// This thread will exit when the servlet is shut down because in the
		// servlet's "destroy" method, the socket is closed. Accept() will throw
		// an exception, we will catch it, and finally return from the thread.
		try {
			while (true) {
				try {
					// Add the player.
					this.players.add(new MudPlayer(serverSocket.accept(),
							service, this.topicConnection));
					new Thread(this.players.getLast()).start();
				} catch (IOException e) {
					logger.error(
							"There was an error accepting a connection from a client.  Note: if this is a SocketException about a closed connection, check for messages about the server exiting.  If the server is exiting, then this exception is expected and can be safely ignored.",
							e);
				}
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} catch (InterruptedException e) {
			logger.info("MUD server is exiting.");
		}
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void destroy() {
		logger.info("Closing connections.");
		for (MudPlayer player : this.players) {
			try {
				player.getWriter()
						.println(
								"The server is being shut down.  You may try re-connecting later.");
				player.getWriter().flush();
				player.stopMessageConnection();
				player.getClient().close();
			} catch (IOException e) {
				logger.error("There was an error closing the socket for "
						+ player.getUsername() + ".");
			}
		}
		logger.info("Closing topic connection.");
		try {
			this.topicConnection.close();
		} catch (JMSException e) {
			logger.error("There was an error closing the topic connection.", e);
		}
		myThread.interrupt();
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("There was an error closing the server socket.");
		}
		logger.info("Successfully closed the server's socket.");
	}
}