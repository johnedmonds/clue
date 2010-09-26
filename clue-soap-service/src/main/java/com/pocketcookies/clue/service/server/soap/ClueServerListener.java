package com.pocketcookies.clue.service.server.soap;

import javax.jms.JMSException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ClueServerListener implements ServletContextListener {

	private static final Logger logger = Logger
			.getLogger(ClueServerListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("Destroying servlet context.");
		try {
			ClueServer.topicConnection.close();
		} catch (JMSException e) {
			logger.error("There was a problem closing the topic connection.", e);
		} catch (Exception e) {
			logger.error("An unknown error occurred.", e);
		}
		logger.info("Finished cleaning up.");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
	}

}
