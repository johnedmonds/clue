package com.pocketcookies.clue.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class Config {
	public static final String TOPIC_JNDI;
	public static final String CONNECTION_FACTORY_JNDI;
	public static final String SERVICE_LOCATION;
	static {
		final Logger logger = Logger.getLogger(Config.class);
		try {
			final Properties properties = new Properties();
			properties.load(Config.class.getClassLoader().getResourceAsStream("service-locations.properties"));
			TOPIC_JNDI = properties.getProperty("topic");
			CONNECTION_FACTORY_JNDI = properties.getProperty("broker");
			SERVICE_LOCATION = properties.getProperty("service");
		} catch (IOException e) {
			logger.fatal("There was an error loading the service locations.", e);
			throw new ExceptionInInitializerError(e);
		}
	}
}
