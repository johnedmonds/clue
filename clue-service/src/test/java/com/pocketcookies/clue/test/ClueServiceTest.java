package com.pocketcookies.clue.test;

import java.util.Date;
import java.util.Random;

import junit.framework.TestCase;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.GameStartedState;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.CheatException;
import com.pocketcookies.clue.exceptions.GameAlreadyExistsException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.IllegalMoveException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotInRoomException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.messages.Join;
import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.messages.targeted.Cards;
import com.pocketcookies.clue.messages.targeted.DisprovingCard;
import com.pocketcookies.clue.messages.broadcast.Chat;
import com.pocketcookies.clue.messages.broadcast.Disprove;
import com.pocketcookies.clue.messages.broadcast.GameOver;
import com.pocketcookies.clue.messages.broadcast.Leave;
import com.pocketcookies.clue.messages.broadcast.NextTurn;
import com.pocketcookies.clue.messages.broadcast.Move;
import com.pocketcookies.clue.messages.broadcast.Suggestion;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueService;

public class ClueServiceTest extends TestCase {

	public void setUp() {
		HibernateUtil.dangerouslyReloadSessionForUnitTesting();
	}

	public void testCreate() throws GameAlreadyExistsException,
			NotLoggedInException, NoSuchGameException, AlreadyJoinedException,
			SuspectTakenException, GameStartedException,
			NotEnoughPlayersException, NotYourTurnException,
			IllegalMoveException, NotInRoomException, CheatException,
			NotInGameException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("clue", "pass");
		String key2 = service.login("clue2", "pass2");
		String key3 = service.login("clue3", "pass3");
		String key4 = service.login("clue4", "pass4");
		// Create the game.
		int gameId = service.create(key1, "test");
		GameData gameData = service.getStatus(gameId);
		assertEquals(gameId, gameData.getGameId());
		assertEquals("test", gameData.getGameName());
		assertEquals(GameStartedState.NOT_STARTED,
				gameData.getGameStartedState());

		// join.
		service.join(key1, gameId, Suspect.SCARLETT);
		try {
			service.join(key1, gameId, Suspect.GREEN);
			fail("User successfully joined twice.");
		} catch (AlreadyJoinedException e) {
			// We want this to happen
		}

		try {
			service.join(key2, gameId, Suspect.SCARLETT);
			fail("User successfully took SCARLETT even though that suspect was already taken.");
		} catch (SuspectTakenException e) {
			// We want this to happen.
		}
		service.join(key2, gameId, Suspect.GREEN);
		try {
			service.startGame(key1, gameId);
			fail("Started game with not enough players.");
		} catch (NotEnoughPlayersException e) {
			// We want this to happen.
		}

		service.join(key3, gameId, Suspect.PEACOCK);
		gameData = service.getStatus(gameId);
		PlayerData[] players = gameData.getPlayers();
		assertEquals(3, players.length);
		service.startGame(key1, gameId);
		try {
			service.join(key4, gameId, Suspect.PLUM);
			fail("Player joined after game had started.");
		} catch (GameStartedException e) {
			// We want this to happen.
		}
		Date p1Since;
		Message[] p1Messages = service.getUpdates(key1, gameId, null);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		assertEquals(5, p1Messages.length);
		assertTrue(p1Messages[0] instanceof Join);
		assertEquals("clue", ((Join) p1Messages[0]).getPlayer());
		assertTrue(p1Messages[1] instanceof Join);
		assertEquals("clue2", ((Join) p1Messages[1]).getPlayer());
		assertTrue(p1Messages[2] instanceof Join);
		assertEquals("clue3", ((Join) p1Messages[2]).getPlayer());
		assertTrue(p1Messages[3] instanceof Cards);
		assertFalse(p1Messages[3] instanceof NextTurn);
		assertTrue(p1Messages[4] instanceof NextTurn);
		assertFalse(p1Messages[4] instanceof Cards);
		NextTurn p1NextTurn = (NextTurn) p1Messages[4];
		assertEquals("clue", p1NextTurn.getPlayer());
		assertEquals(9, p1NextTurn.getMovementPointsAvailable());
		// Make sure we actually do return from this function as it is
		// implemented as a blocking queue.
		assertEquals(0, service.getUpdates(key1, gameId, p1Since).length);
		// Have everyone else attempt to move to make sure we prevent players
		// from moving when it is not their turn.
		try {
			service.move(key2, gameId, 0, 0);
			fail("Player 2 should not be allowed to move.");
		} catch (NotYourTurnException e) {
		}
		try {
			service.move(key3, gameId, 1, 1);
			fail("Player 3 should not be allowed to move.");
		} catch (NotYourTurnException e) {
		}
		try {
			service.move(key1, gameId, 7, 24);
			fail("Can't move into yourself.");
		} catch (IllegalMoveException e) {
		}
		// Try to move somewhere. Note that for the purposes of this test, we
		// will always pick something one square away because we don't know how
		// many movement points were assigned but we do know that a minimum of
		// one movement point was given.
		assertEquals(1, service.move(key1, gameId, 7, 23));
		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		assertEquals(1, p1Messages.length);
		assertEquals(1, ((Move) p1Messages[0]).getCost());
		// Attempt to make a suggestion while not in a room.
		try {
			service.suggest(key1, gameId, Card.SCARLETT, Card.ROPE);
			fail("We were able to suggest while not in a room.");
		} catch (NotInRoomException e) {
		}
		assertEquals(3, service.move(key1, gameId, 11, 22));
		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		assertEquals(3, ((Move) p1Messages[0]).getCost());
		// Attempt to suggest something players 1, 2, and 3 can disprove.
		// However, only player 2 should disprove it.
		service.suggest(key1, gameId, Card.SCARLETT, Card.ROPE);
		// Make sure all the things are correct.
		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		Suggestion p1Suggestion = (Suggestion) p1Messages[0];
		assertEquals("clue", p1Suggestion.getPlayer());
		assertEquals(Card.HALL, p1Suggestion.getRoom());
		assertEquals(Card.SCARLETT, p1Suggestion.getSuspect());
		assertEquals(Card.ROPE, p1Suggestion.getWeapon());
		Disprove p1SuggestionDisprove = (Disprove) p1Messages[1];
		assertEquals("clue2", p1SuggestionDisprove.getPlayer());
		assertEquals(2, p1Messages.length);
		try {
			service.endTurn(key2, gameId);
			fail("Wrong player ending turn.");
		} catch (NotYourTurnException e) {
		}
		try {
			service.endTurn(key1, gameId);
			fail("Suggestion has not yet been disproved.");
		} catch (NotYourTurnException e) {
		}
		try {
			service.disprove(key1, gameId, Card.HALL);
			fail("Player 1 was able to disprove while not being the disproving player.");
		} catch (NotYourTurnException e) {
		}
		try {
			service.disprove(key3, gameId, Card.HALL);
			fail("Player 3 was able to disprove without being the disproving player.");
		} catch (NotYourTurnException e) {
		}
		try {
			service.disprove(key2, gameId, Card.BALLROOM);
			fail("Player was allowed to disprove with the wrong card.");
		} catch (CheatException e) {
		}
		service.disprove(key2, gameId, Card.HALL);

		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		assertEquals(1, p1Messages.length);
		assertEquals(Card.HALL, ((DisprovingCard) p1Messages[0]).getCard());

		// Cause player 1 to lose.
		service.accuse(key1, gameId, Card.SCARLETT, Card.STUDY,
				Card.CANDLESTICK);
		service.disprove(key3, gameId, Card.SCARLETT);
		service.endTurn(key1, gameId);
		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		assertEquals(4, p1Messages.length);

		Date p2Since = null;
		Message[] p2Messages = service.getUpdates(key2, gameId, p2Since);
		p2Since = p2Messages[p2Messages.length - 1].getPublished();
		// One might think this should be 12 because of the 3 join messages but
		// it is actually 11 because player 2 only receives messages from the
		// time it joins and player 1 joined before player 2 received the
		// message.
		assertEquals(11, p2Messages.length);
		assertEquals(5, service.move(key2, gameId, 12, 5));

		service.endTurn(key2, gameId);
		Date p3Since = null;
		Message[] p3Messages = service.getUpdates(key3, gameId, p3Since);
		p3Since = p3Messages[p3Messages.length - 1].getPublished();
		assertEquals(12, p3Messages.length);

		assertEquals(9,
				((NextTurn) p3Messages[11]).getMovementPointsAvailable());
		// Make sure player 1 is skipped because that player lost.
		service.endTurn(key3, gameId);
		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		assertEquals(3, p1Messages.length);
		assertEquals("clue2", ((NextTurn) p1Messages[2]).getPlayer());
		p2Messages = service.getUpdates(key2, gameId, p2Since);
		p2Since = p2Messages[p2Messages.length - 1].getPublished();
		assertEquals(3, p2Messages.length);
		// Send a chat message.

		service.chat(key1, gameId, "Hello world from player 1.");
		service.chat(key2, gameId, "Hello world from player 2.");
		p1Messages = service.getUpdates(key1, gameId, p1Since);
		p1Since = p1Messages[p1Messages.length - 1].getPublished();
		p2Messages = service.getUpdates(key2, gameId, p2Since);
		p2Since = p2Messages[p2Messages.length - 1].getPublished();
		assertEquals(2, p1Messages.length);
		assertEquals(2, p2Messages.length);
		// Player 1's message queue.
		assertEquals("Hello world from player 1.",
				((Chat) p1Messages[0]).getMessage());
		assertEquals("clue", ((Chat) p1Messages[0]).getPlayer());
		assertEquals("Hello world from player 2.",
				((Chat) p1Messages[1]).getMessage());
		assertEquals("clue2", ((Chat) p1Messages[1]).getPlayer());
		// Player 2's message queue.
		assertEquals("Hello world from player 1.",
				((Chat) p2Messages[0]).getMessage());
		assertEquals("clue", ((Chat) p2Messages[0]).getPlayer());
		assertEquals("Hello world from player 2.",
				((Chat) p2Messages[1]).getMessage());
		assertEquals("clue2", ((Chat) p2Messages[1]).getPlayer());

		service.accuse(key2, gameId, Card.DINING_ROOM, Card.MUSTARD,
				Card.SPANNER);
		p2Messages = service.getUpdates(key2, gameId, p2Since);
		p2Since = p2Messages[p2Messages.length - 1].getPublished();
		assertEquals(2, p2Messages.length);
		assertTrue(p2Messages[1] instanceof GameOver);
		assertEquals("clue2", ((GameOver) p2Messages[1]).getPlayer());
	}

	private static boolean gameDataIncludesGameWithId(GameData[] data, int id) {
		for (GameData gd : data) {
			if (gd.getGameId() == id)
				return true;
		}
		return false;
	}

	public void testGetGames() throws NotLoggedInException,
			GameAlreadyExistsException, NoSuchGameException,
			SuspectTakenException, GameStartedException,
			AlreadyJoinedException, NotEnoughPlayersException,
			NotYourTurnException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("clue1", "pass1");
		String key2 = service.login("clue2", "pass2");
		String key3 = service.login("clue3", "pass3");
		int gameId1 = service.create(key1, "test");
		service.join(key1, gameId1, Suspect.SCARLETT);
		service.join(key2, gameId1, Suspect.GREEN);
		service.join(key3, gameId1, Suspect.WHITE);
		service.startGame(key1, gameId1);
		service.accuse(key1, gameId1, Card.DINING_ROOM, Card.MUSTARD,
				Card.SPANNER);

		int gameId2 = service.create(key1, "test");
		service.join(key1, gameId2, Suspect.SCARLETT);
		service.join(key2, gameId2, Suspect.GREEN);
		service.join(key3, gameId2, Suspect.WHITE);
		service.startGame(key1, gameId2);

		int gameId3 = service.create(key1, "test");
		int gameId4 = service.create(key1, "test2");

		GameData[] data = service
				.getGames("test", GameStartedState.NOT_STARTED);
		assertEquals(1, data.length);
		assertEquals(gameId3, data[0].getGameId());
		data = service.getGames("test", GameStartedState.STARTED);
		assertEquals(1, data.length);
		assertEquals(gameId2, data[0].getGameId());
		data = service.getGames("test", GameStartedState.ENDED);
		assertEquals(1, data.length);
		assertEquals(gameId1, data[0].getGameId());
		data = service.getGames("test", null);
		assertEquals(3, data.length);
		assertTrue(gameDataIncludesGameWithId(data, gameId1));
		assertTrue(gameDataIncludesGameWithId(data, gameId2));
		assertTrue(gameDataIncludesGameWithId(data, gameId3));

		data = service.getGames(null, GameStartedState.NOT_STARTED);
		assertEquals(2, data.length);
		assertTrue(gameDataIncludesGameWithId(data, gameId3));
		assertTrue(gameDataIncludesGameWithId(data, gameId4));

		data = service.getGames(null, null);
		assertEquals(4, data.length);
		assertTrue(gameDataIncludesGameWithId(data, gameId1));
		assertTrue(gameDataIncludesGameWithId(data, gameId2));
		assertTrue(gameDataIncludesGameWithId(data, gameId3));
		assertTrue(gameDataIncludesGameWithId(data, gameId4));

	}

	public void testLeave() throws NotLoggedInException,
			GameAlreadyExistsException, NoSuchGameException,
			GameStartedException, NotEnoughPlayersException,
			SuspectTakenException, AlreadyJoinedException, NotInGameException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("user1", "pass1");
		int gameId = service.create(key1, "test");
		service.join(key1, gameId, Suspect.SCARLETT);
		assertEquals(1, service.getStatus(gameId).getPlayers().length);
		service.leave(key1, gameId);
		assertEquals(0, service.getStatus(gameId).getPlayers().length);
		service.join(key1, gameId, Suspect.SCARLETT);
		assertEquals(1, service.getStatus(gameId).getPlayers().length);
		service.leave(key1, gameId);
		assertEquals(0, service.getStatus(gameId).getPlayers().length);
		service.join(key1, gameId, Suspect.GREEN);
		assertEquals(1, service.getStatus(gameId).getPlayers().length);
	}

	public void testCurrentPlayerLeave() throws NotLoggedInException,
			GameAlreadyExistsException, NoSuchGameException,
			SuspectTakenException, GameStartedException,
			AlreadyJoinedException, NotEnoughPlayersException,
			NotYourTurnException, NotInGameException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("user1", "pass1");
		String key2 = service.login("user2", "pass2");
		String key3 = service.login("user3", "pass3");
		int gameId = service.create(key1, "test");
		service.join(key1, gameId, Suspect.SCARLETT);
		service.join(key2, gameId, Suspect.GREEN);
		service.join(key3, gameId, Suspect.WHITE);
		service.startGame(key1, gameId);
		Message updates[] = service.getUpdates(key1, gameId, null);
		assertEquals("user1", ((Join) updates[0]).getPlayer());
		assertEquals("user2", ((Join) updates[1]).getPlayer());
		assertEquals("user3", ((Join) updates[2]).getPlayer());
		assertEquals("user1", ((NextTurn) updates[4]).getPlayer());
		// Make sure this does not throw an exception.
		service.leave(key1, gameId);
		updates = service.getUpdates(key2, gameId, null);
		assertEquals("user1", ((Leave) updates[5]).getPlayer());
		assertEquals("user3",
				((NextTurn) service.getUpdates(key2, gameId, null)[4])
						.getPlayer());
	}

	public void testCurrentPlayerLeaveDuringDisprove()
			throws NotLoggedInException, GameAlreadyExistsException,
			NoSuchGameException, SuspectTakenException, GameStartedException,
			AlreadyJoinedException, NotEnoughPlayersException,
			NotYourTurnException, IllegalMoveException, NotInRoomException,
			NotInGameException, CheatException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("user1", "pass1");
		String key2 = service.login("user2", "pass2");
		String key3 = service.login("user3", "pass3");
		int gameId = service.create(key1, "test");
		service.join(key1, gameId, Suspect.SCARLETT);
		service.join(key2, gameId, Suspect.GREEN);
		service.join(key3, gameId, Suspect.PEACOCK);
		service.startGame(key1, gameId);
		service.move(key1, gameId, 11, 22);
		service.suggest(key1, gameId, Card.SCARLETT, Card.ROPE);
		service.leave(key1, gameId);
		assertEquals("user2",
				((NextTurn) service.getUpdates(key2, gameId, null)[7])
						.getPlayer());
		assertEquals("user1",
				((Leave) service.getUpdates(key2, gameId, null)[8]).getPlayer());
		try {
			service.disprove(key2, gameId, Card.HALL);
			fail("The user should not be able to disprove after the current player has left.");
		} catch (NotYourTurnException e) {
		}
		service.endTurn(key2, gameId);
		assertEquals("user3",
				((NextTurn) service.getUpdates(key2, gameId, null)[9])
						.getPlayer());
	}

	public void testLeavingPlayerAutoDisprovesSuggestion()
			throws NotLoggedInException, GameAlreadyExistsException,
			NoSuchGameException, SuspectTakenException, GameStartedException,
			AlreadyJoinedException, NotEnoughPlayersException,
			NotYourTurnException, IllegalMoveException, NotInRoomException,
			NotInGameException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("user1", "pass1");
		String key2 = service.login("user2", "pass2");
		String key3 = service.login("user3", "pass3");
		int gameId = service.create(key1, "test");
		service.join(key1, gameId, Suspect.SCARLETT);
		service.join(key2, gameId, Suspect.GREEN);
		service.join(key3, gameId, Suspect.PEACOCK);
		service.startGame(key1, gameId);
		service.leave(key2, gameId);
		service.move(key1, gameId, 11, 22);
		service.suggest(key1, gameId, Card.SCARLETT, Card.ROPE);
		assertEquals(Card.HALL, ((DisprovingCard) service.getUpdates(key1,
				gameId, null)[9]).getCard());
		service.endTurn(key1, gameId);
		try {
			service.endTurn(key2, gameId);
			fail("user2 should not be able to end turn because it has already lost and this should be happening automatically.");
		} catch (NotYourTurnException e) {
		}
		assertEquals("user3",
				((NextTurn) service.getUpdates(key1, gameId, null)[10])
						.getPlayer());
	}

	public void testDisprovingPlayerLeave() throws NotLoggedInException,
			GameAlreadyExistsException, NoSuchGameException,
			SuspectTakenException, GameStartedException,
			AlreadyJoinedException, NotYourTurnException,
			NotEnoughPlayersException, IllegalMoveException,
			NotInRoomException, NotInGameException {
		ClueService service = new ClueService(new Random(3));
		String key1 = service.login("user1", "pass1");
		String key2 = service.login("user2", "pass2");
		String key3 = service.login("user3", "pass3");
		int gameId = service.create(key1, "test");
		service.join(key1, gameId, Suspect.SCARLETT);
		service.join(key2, gameId, Suspect.GREEN);
		service.join(key3, gameId, Suspect.PEACOCK);
		service.startGame(key1, gameId);
		assertEquals("user1",
				((NextTurn) service.getUpdates(key1, gameId, null)[4])
						.getPlayer());
		service.move(key1, gameId, 11, 22);
		service.suggest(key1, gameId, Card.SCARLETT, Card.ROPE);
		service.leave(key2, gameId);
		assertEquals("user2",
				((Disprove) service.getUpdates(key1, gameId, null)[7])
						.getPlayer());
		assertEquals(Card.HALL, ((DisprovingCard) service.getUpdates(key1,
				gameId, null)[8]).getCard());
		assertEquals("user2",
				((Leave) service.getUpdates(key1, gameId, null)[9]).getPlayer());
	}
}
