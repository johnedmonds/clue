package com.pocketcookies.clue.service.server.mud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.service.server.ClueServiceBean;

public class ClueMudServer implements Runnable {

	private static final Topic topic;
	private static final TopicConnection topicConnection;
	private static Logger logger;
	private static final ClueServiceBean service;
	private static final ServerSocket serverSocket;
	static {
		logger = Logger.getLogger(ClueMudServlet.class);
		logger.info("Starting to load clue service and message service objects.");
		try {
			logger.info("Creating InitialContext.");
			InitialContext context = new InitialContext();
			logger.info("Loading clue service.");
			service = (ClueServiceBean) context
					.lookup("com/pocketcookies/clue/service/server/ejb/ClueService");
			logger.info("Loading topic.");
			topic = (Topic) context.lookup("ClueTopic");
			logger.info("Loading connection factory.");
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) context
					.lookup("ClueTopicConnectionFactory");
			logger.info("Creating JMS connection.");
			topicConnection = topicConnectionFactory.createTopicConnection();
			logger.info("Starting JMS.");
			topicConnection.start();
		} catch (NamingException e) {
			logger.error(
					"There was a problem looking up the topic, connection factory, and/or EJB service.",
					e);
			throw new ExceptionInInitializerError(e);
		} catch (JMSException e) {
			logger.error(
					"There was a problem starting a connection to the message server.",
					e);
			throw new ExceptionInInitializerError(e);
		}
		logger.info("MUD server has loaded the message service and clue service objects successfully.");

		// Initializing the server socket for telnet clients.
		logger.info("Creating socket to accept connections.");
		try {
			serverSocket = new ServerSocket(8081, 10, null);
		} catch (IOException e) {
			logger.fatal("There was an error creating the socket.", e);
			throw new ExceptionInInitializerError(e);
		}
		logger.info("MUD server socket has been successfully created.");

	}

	@Override
	public void run() {
		logger.info("Thread to accept connections has started.");
		while (true) {
			try {
				Socket client = serverSocket.accept();
				new Thread(new MudPlayer(client)).start();
			} catch (IOException e) {
				logger.error(
						"There was an error accepting a connection from a client.",
						e);
			}

		}
	}

}
