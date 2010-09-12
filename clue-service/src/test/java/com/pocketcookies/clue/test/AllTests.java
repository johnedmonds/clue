package com.pocketcookies.clue.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(ClueServiceTest.class);
		suite.addTestSuite(HibernateTest.class);
		suite.addTestSuite(TimerTests.class);
		// $JUnit-END$
		return suite;
	}

}
