package com.pocketcookies.clue.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(BoardTest.class);
		suite.addTestSuite(ClueServiceTest.class);
		suite.addTestSuite(HibernateTest.class);
		// $JUnit-END$
		return suite;
	}

}
