package com.pocketcookies.clue.service.server;

/**
 * ClueServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.1  Built on : Oct 19, 2009 (10:59:00 EDT)
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;

import com.caucho.hessian.server.HessianServlet;
import com.pocketcookies.clue.BootTimerTask;
import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.DeleteEmptyGameTimerTask;
import com.pocketcookies.clue.Game;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.GameStartedState;
import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.User;
import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.CheatException;
import com.pocketcookies.clue.exceptions.GameAlreadyExistsException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotInRoomException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.players.Player;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

/**
 * ClueServiceSkeleton java skeleton for the axisService
 */
public class ClueService extends HessianServlet implements ClueServiceAPI {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ClueService.class);
	private final Random random;
	// !How long games will last until they are deleted.
	private final long CREATE_EMPTY_GAME_LIFE_TIME;
	private final long LEAVE_EMPTY_GAME_LIFE_TIME;
	// After a certain amount of time, players will be booted from a game that
	// has ended in case they forgot to leave (or their client does not leave
	// for them).
	private final long PLAYER_BOOT_TIME;
	private final Timer timer;

	public void ping() {
	}

	public void destroy() {
		super.destroy();
		logger.info("Stopping timer.");
		if (this.timer != null)
			this.timer.cancel();
	}

	public ClueService() {
		this.CREATE_EMPTY_GAME_LIFE_TIME = 100000;
		this.LEAVE_EMPTY_GAME_LIFE_TIME = 100000;
		this.PLAYER_BOOT_TIME = 100000;
		this.timer = new Timer();
		this.random = new Random();
	}

	public ClueService(Random random) {
		this.CREATE_EMPTY_GAME_LIFE_TIME = 100000;
		this.LEAVE_EMPTY_GAME_LIFE_TIME = 100000;
		this.PLAYER_BOOT_TIME = 100000;
		this.random = random;
		this.timer = null;
	}

	public ClueService(Random random, Timer timer, int createTime,
			int leaveTime, int bootTime) {
		this.CREATE_EMPTY_GAME_LIFE_TIME = createTime;
		this.LEAVE_EMPTY_GAME_LIFE_TIME = leaveTime;
		this.PLAYER_BOOT_TIME = bootTime;
		this.timer = timer;
		this.random = random;
	}

	private static final byte[] SALT = new String(
			"cai9Eethiek8ziqueih5reij9cei\\lae5quei:f1").getBytes();

	private void validateUser(String key) throws NotLoggedInException {
		if (HibernateUtil.getSessionFactory().getCurrentSession()
				.createQuery("from User where key=:key").setString("key", key)
				.uniqueResult() == null)
			throw new NotLoggedInException();
	}

	/**
	 * Checks to see if the user specified by key is really the current user in
	 * the game.
	 * 
	 * @param key
	 * @param gameId
	 * @throws NotYourTurnExceptionException
	 * @throws NotLoggedInExceptionException
	 * @throws NoSuchGameExceptionException
	 */
	private void validateCurrentUser(String key, Game g)
			throws NotYourTurnException, NotLoggedInException,
			NoSuchGameException {
		if (g == null)
			throw new NoSuchGameException();
		if (g.getCurrentPlayer() == null
				|| !g.getCurrentPlayer().getUser().getKey().equals(key)) {
			// Is the player even logged in?
			validateUser(key);
			throw new NotYourTurnException();
		}
	}

	private String generateKey(String username, String password) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		digest.update(username.getBytes());
		digest.update(password.getBytes());
		digest.update(SALT);
		return UUID.nameUUIDFromBytes(digest.digest()).toString();
	}

	@Override
	public String login(String username, String password) {
		String uuidToReturn = generateKey(username, password);
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		User u = (User) session.get(User.class, username);
		if (u == null)
			session.save(new User(username, uuidToReturn));
		else if (!u.getKey().equals(uuidToReturn))
			// Though there is no real harm in returning a fake uuid, it's
			// probably better to notify the user that their password was
			// incorrect.
			uuidToReturn = null;
		session.getTransaction().commit();
		logger.info("User " + username + " logged in and has UUID: "
				+ uuidToReturn);
		return uuidToReturn;
	}

	@Override
	public int create(String key, String gameName) throws NotLoggedInException,
			GameAlreadyExistsException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		validateUser(key);
		try {
			Game g = new Game(gameName);
			session.save(g);
			session.flush();
			int ret = g.getId();
			session.getTransaction().commit();
			// If no one joins the game, it will be deleted.
			if (this.timer != null)
				this.timer.schedule(new DeleteEmptyGameTimerTask(ret),
						CREATE_EMPTY_GAME_LIFE_TIME);
			return ret;
		} catch (ConstraintViolationException e) {
			session.getTransaction().rollback();
			throw new GameAlreadyExistsException();
		}
	}

	@Override
	public void join(String key, int gameId, Suspect suspect)
			throws NotLoggedInException, NoSuchGameException,
			SuspectTakenException, GameStartedException, AlreadyJoinedException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		User u = (User) session.createQuery("from User where key = :key")
				.setString("key", key).uniqueResult();
		validateUser(key);
		g.join(u, suspect);
		session.getTransaction().commit();
	}

	@Override
	public Message[] getUpdates(String key, int gameId, Date since)
			throws NotLoggedInException, NoSuchGameException,
			NotYourTurnException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		validateUser(key);
		Player currentPlayer = g.getPlayerWithKey(key);
		if (currentPlayer == null)
			throw new NotYourTurnException();
		Message[] ret = currentPlayer.getUpdates(since);
		session.getTransaction().commit();
		return ret;
	}

	@Override
	public Message[] getAllUpdates(String key, int gameId)
			throws NotLoggedInException, NoSuchGameException,
			NotYourTurnException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		Player currentPlayer = g.getPlayerWithKey(key);
		if (currentPlayer == null) // This player has not joined this game.
			throw new NotYourTurnException();
		Message[] ret = currentPlayer.getAllUpdates();
		session.getTransaction().commit();
		return ret;
	}

	@Override
	public void suggest(String key, int gameId, Card suspect, Card weapon)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, NotInRoomException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		validateCurrentUser(key, g);
		g.suggest(suspect, weapon);
		session.getTransaction().commit();
	}

	@Override
	public void accuse(String key, int gameId, Card room, Card suspect,
			Card weapon) throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Game g = (Game) session.load(Game.class, gameId);

		validateCurrentUser(key, g);

		boolean gameHasEnded = g.accuse(room, suspect, weapon);
		session.getTransaction().commit();
		if (gameHasEnded && this.timer != null)
			this.timer.schedule(new BootTimerTask(gameId),
					this.PLAYER_BOOT_TIME);
	}

	@Override
	public void disprove(String key, int gameId, Card disprovingCard)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, CheatException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		// Make sure the user has logged in.
		validateUser(key);
		Game g = (Game) session.load(Game.class, gameId);
		// Make sure the game exists.
		if (g == null)
			throw new NoSuchGameException();
		// Make sure the user is the disproving user.
		if (g.getDisprovingPlayer() == null
				|| !g.getDisprovingPlayer().getUser().getKey().equals(key))
			throw new NotYourTurnException();
		// Attempt to disprove.
		g.disprove(disprovingCard);
		session.getTransaction().commit();
	}

	@Override
	public boolean move(String key, int gameId, Room room)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		validateCurrentUser(key, g);
		boolean ret = g.move(room);
		session.getTransaction().commit();
		return ret;
	}

	@Override
	public void endTurn(String key, int gameId) throws NotLoggedInException,
			NotYourTurnException, NoSuchGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		validateCurrentUser(key, g);
		g.endTurn();
		session.getTransaction().commit();
	}

	@Override
	public com.pocketcookies.clue.GameData getStatus(int gameId)
			throws NoSuchGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		GameData data = g.getData();
		data.setGameId(gameId);
		session.getTransaction().commit();
		return data;
	}

	@Override
	public com.pocketcookies.clue.GameData getStatusByName(String gameName)
			throws NoSuchGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		final Game g = (Game) session
				.createQuery("from Game where name = :name")
				.setString("name", gameName).uniqueResult();
		if (g == null)
			throw new NoSuchGameException();
		return g.getData();
	}

	@Override
	public void startGame(String key, int gameId) throws NotLoggedInException,
			NoSuchGameException, GameStartedException,
			NotEnoughPlayersException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		validateUser(key);
		g.start(this.random);
		session.getTransaction().commit();
	}

	@Override
	public void chat(String key, int gameId, String message)
			throws NotLoggedInException, NoSuchGameException,
			NotInGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		validateUser(key);
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		g.chat(key, message);
		session.getTransaction().commit();
	}

	@Override
	public String changePassword(String username, String key, String newPassword) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		User u = (User) session.load(User.class, username);
		if (u == null)
			return null;
		if (!u.getKey().equals(key))
			return null;
		String outKey = generateKey(username, newPassword);
		u.setKey(outKey);
		session.getTransaction().commit();
		return outKey;
	}

	@Override
	public void leave(String key, int gameId) throws NotLoggedInException,
			NoSuchGameException, NotInGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		validateUser(key);
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		g.leave(key);
		// If, by leaving, the game becomes empty, we schedule a task to delete
		// the game.
		if (this.timer != null && g.getJoinedPlayersCount() == 0)
			this.timer.schedule(new DeleteEmptyGameTimerTask(g.getId()),
					LEAVE_EMPTY_GAME_LIFE_TIME);
		session.getTransaction().commit();
	}

	@Override
	public Card[] getCards(String key, int gameId) throws NoSuchGameException,
			NotInGameException {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null)
			throw new NoSuchGameException();
		if (g.getGameStartedState() == GameStartedState.NOT_STARTED)
			return null;
		Player p = g.getPlayerWithKey(key);
		if (p == null)
			throw new NotInGameException();

		Card[] ret = p.getHand().toArray(new Card[p.getHand().size()]);
		session.getTransaction().commit();
		return ret;
	}

	@Override
	public GameData[] getGames() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<Game> games = session.createQuery("from Game").list();
		GameData data[] = new GameData[games.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = games.get(i).getData();
		}
		session.getTransaction().commit();
		return data;
	}

	@Override
	public Suspect getSuspectForPlayer(String key, int gameId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Player player = (Player) session
				.createQuery(
						"from Player p where p.id.gameId = :gameId and p.user.key = :key")
				.setInteger("gameId", gameId).setString("key", key)
				.uniqueResult();
		if (player == null)
			return null;
		Suspect s = player.getId().getSuspect();
		session.getTransaction().commit();
		return s;
	}
}
