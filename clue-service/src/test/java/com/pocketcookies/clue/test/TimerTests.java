package com.pocketcookies.clue.test;

import java.util.Random;
import java.util.Timer;

import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.GameAlreadyExistsException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueService;

import junit.framework.TestCase;

public class TimerTests extends TestCase {
	@Override
	public void setUp() {
		HibernateUtil.dangerouslyReloadSessionForUnitTesting();
	}

	public void testCreate() throws NotLoggedInException,
			GameAlreadyExistsException, InterruptedException {
		ClueService service = new ClueService(new Random(3), new Timer(), 100,
				100, 100);
		String key = service.login("user", "");
		service.create(key, "test");
		Thread.sleep(200);
		assertEquals(0, service.getGames().length);
	}

	public void testJoinedGameNotDeleted() throws NotLoggedInException,
			GameAlreadyExistsException, NoSuchGameException,
			SuspectTakenException, GameStartedException,
			AlreadyJoinedException, InterruptedException {
		ClueService service = new ClueService(new Random(3), new Timer(), 100,
				100, 100);
		String key = service.login("user", "");
		int gameId = service.create(key, "test");
		service.join(key, gameId, Suspect.SCARLETT);
		Thread.sleep(200);
		assertEquals(gameId, service.getGames()[0].getGameId());
	}

	public void testLeftGameDeleted() throws NotLoggedInException,
			GameAlreadyExistsException, NoSuchGameException,
			SuspectTakenException, GameStartedException,
			AlreadyJoinedException, InterruptedException, NotInGameException {
		ClueService service = new ClueService(new Random(3), new Timer(), 100,
				100, 100);
		String key = service.login("user", "");
		int gameId = service.create(key, "test");
		service.join(key, gameId, Suspect.SCARLETT);
		Thread.sleep(200); // Make sure the first timer fires.
		assertEquals(gameId, service.getGames()[0].getGameId());
		service.leave(key, gameId);
		Thread.sleep(200);
		assertEquals(0, service.getGames().length);
	}
}
