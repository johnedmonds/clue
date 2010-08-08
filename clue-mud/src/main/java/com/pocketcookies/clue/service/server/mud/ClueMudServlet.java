package com.pocketcookies.clue.service.server.mud;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.service.server.ClueServiceBean;

public class ClueMudServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Topic topic;
	private static final TopicConnection topicConnection;
	private static Logger logger;
	private static final ClueServiceBean service;
	private static final ServerSocket serverSocket;
	static {
		logger = Logger.getLogger(ClueMudServlet.class);
		try {
			InitialContext context = new InitialContext();
			service = (ClueServiceBean) context
					.lookup("com/pocketcookies/clue/service/server/ejb/ClueService");
			topic = (Topic) context.lookup("ClueTopic");
			TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) context
					.lookup("ClueTopicConnectionFactory");
			topicConnection = topicConnectionFactory.createTopicConnection();
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

		// Initializing the server socket for telnet clients.
		try {
			serverSocket = new ServerSocket(8081, 10, null);
		} catch (IOException e) {
			logger.fatal("There was an error creating the socket.", e);
			throw new ExceptionInInitializerError(e);
		}
	}

}
