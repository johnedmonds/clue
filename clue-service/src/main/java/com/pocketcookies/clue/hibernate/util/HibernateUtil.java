package com.pocketcookies.clue.hibernate.util;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static SessionFactory sessionFactory;
	private static Logger logger;
	static {
		logger = Logger.getLogger(HibernateUtil.class);
		sessionFactory = buildSessionFactory();
	}

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure("hibernate.cfg.xml")
					.buildSessionFactory();
		} catch (Exception e) {
			logger.error("There was an error creating the SessionFactory.", e);
		}
		return null;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Most importantly, read the title.
	 * 
	 * This method should be used for unit testing. It loads a session factory
	 * from a file. We usually configure the files to clear the database. This
	 * can be used to reset the entire database for unit testing. For
	 * production, hopefully we will set the database to be updated rather than
	 * cleared so calling this method won't do anything. Still, this method
	 * should only be called if you really know what you are doing.
	 */
	public static void dangerouslyReloadSessionForUnitTesting() {
		sessionFactory = buildSessionFactory();
	}

}
