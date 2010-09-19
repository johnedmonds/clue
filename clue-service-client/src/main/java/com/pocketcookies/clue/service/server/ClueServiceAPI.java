package com.pocketcookies.clue.service.server;

import java.util.Date;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.Room;
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
import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.players.Suspect;

public interface ClueServiceAPI {

	/**
	 * Does nothing. Though this can be good for testing your connection.
	 */
	public void ping();

	/**
	 * Adds the user to the list of logged in user and gives the user a key.
	 * 
	 * @param username
	 *            The desired or existing username.
	 * @param password
	 *            The desired or existing password.
	 * @return The key to use with the rest of the methods.
	 */
	public String login(String username, String password);

	/**
	 * Assuming the user exists and the key is correct, changes the password and
	 * returns the new key.
	 * 
	 * There is no need to call login again after changing your password.
	 * 
	 * @param username
	 *            The username.
	 * @param key
	 *            The key returned by login.
	 * @param newPassword
	 *            The desired password.
	 * @return The new key.
	 */
	public String changePassword(String username, String key, String newPassword);

	/**
	 * Creates a game room.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameName
	 *            The name of the game to create.
	 * @return The id of the game.
	 */
	public int create(String key, String gameName) throws NotLoggedInException,
			GameAlreadyExistsException;

	/**
	 * Joins a game room by id.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game (returned by create or getGames()).
	 * @param suspect
	 *            The desired suspect to play as.
	 * @throws NotLoggedInException
	 *             Thrown if your key is invalid.
	 * @throws NoSuchGameException
	 *             Thrown if no game by this id exists.
	 * @throws SuspectTakenException
	 *             Thrown if the desired suspect is already taken.
	 * @throws GameStartedException
	 *             Thrown if the game has already started and is not taking any
	 *             more players.
	 * @throws AlreadyJoinedException
	 *             Thrown if you have already joined the game (it's not fair to
	 *             have access to twice (or more) as many cards as the other
	 *             players).
	 */
	public void join(String key, int gameId, Suspect suspect)
			throws NotLoggedInException, NoSuchGameException,
			SuspectTakenException, GameStartedException, AlreadyJoinedException

	;

	/**
	 * Gets a list of updates since the last time you called this method. The
	 * idea here is pretty much that, as events in the game occur, messages will
	 * queue up for each player in that game. Players call this method to get
	 * updated on the list of events that happened since they last called this
	 * method.
	 * 
	 * Since this method will be called repeatedly, we found it to be a good
	 * idea that if we don't have any new updates, to wait until we do.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game for which you desire updates.
	 * @param since
	 *            Used to determine which messages to retrieve. All messages
	 *            with a publish date occurring after "since" will be returned.
	 *            Note that the returned messages will not contain those
	 *            messages with a publish date exactly equal to "since."
	 * @return A list of events that were published after "since."
	 * @throws NotLoggedInException
	 *             Thrown if the given key is not valid.
	 * @throws NoSuchGameException
	 *             Thrown if no games by the given gameId exist.
	 * @throws NotYourTurnException
	 *             Thrown if you are not part of this game.
	 */
	public Message[] getUpdates(String key, int gameId, Date since)
			throws NotLoggedInException, NoSuchGameException,
			NotYourTurnException;

	/**
	 * Like getUpdates except that getAllUpdates returns all updates since the
	 * start of the game, rather than all updates since the last call of the
	 * method.
	 * 
	 * @see getUpdates
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game for which you desire all updates.
	 * @return All events since the start of the game.
	 * @throws NotLoggedInException
	 *             Thrown when the given key is not valid.
	 * @throws NoSuchGameException
	 *             Thrown when no games by gameId exist.
	 * @throws NotYourTurnException
	 *             Thrown if you are not part of this game.
	 */
	public Message[] getAllUpdates(String key, int gameId)
			throws NotLoggedInException, NoSuchGameException,
			NotYourTurnException;

	/**
	 * Suggest a series of cards. Note that there is no parameter for the room
	 * card because you must be in the correct room before making a suggestion
	 * about that room.
	 * 
	 * When the server finds a player who can disprove this, we will post a
	 * message.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game for which to make this suggestion.
	 * @param weapon
	 *            The suggested weapon.
	 * @param suspect
	 *            The suggested suspect.
	 * @throws NotLoggedInException
	 *             Thrown if the given key is not valid.
	 * @throws NotYourTurnException
	 *             Thrown if it is not your turn to make a suggestion.
	 * @throws NoSuchGameException
	 *             Thrown if no game by gameId exists.
	 */
	public void suggest(String key, int gameId, Card weapon, Card suspect)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, NotInRoomException;

	/**
	 * Accuse a series of cards. You do not have to be in the room to suggest a
	 * room. This is the move that will cause the player to either win or lose.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game for which to make this accusation.
	 * @param room
	 *            The accused room.
	 * @param weapon
	 *            The accused weapon.
	 * @param suspect
	 *            The accused suspect.
	 * @throws NotLoggedInException
	 *             Thrown if the key is not valid.
	 * @throws NotYourTurnException
	 *             Thrown if it is not your turn to make an accusation.
	 * @throws NoSuchGameException
	 *             Thrown if gameId is invalid.
	 */
	public void accuse(String key, int gameId, Card room, Card suspect,
			Card weapon) throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException;

	/**
	 * The server has told a certain player to disprove the previous suggestion
	 * or accusation. This should be one of the cards that disproves that
	 * suggestion.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game for which to disprove the previous
	 *            suggestion.
	 * @param disprovingCard
	 *            The card that disproves the previous suggestion.
	 * @throws NotLoggedInException
	 *             Thrown if the key is not valid.
	 * @throws NotYourTurnException
	 *             Thrown if the server has not chosen you to disprove the
	 *             previous suggestion.
	 * @throws NoSuchGameException
	 *             Thrown if no game with gameId exists.
	 */
	public void disprove(String key, int gameId, Card disprovingCard)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException, CheatException;

	/**
	 * Moves your character to the given position.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game in which to move.
	 * @param room
	 *            The room to which you wish to move.
	 * @return Whether you are allowed to move to that room.
	 * @throws NotLoggedInException
	 *             Thrown when the key is not valid.s
	 * @throws NotYourTurnException
	 *             Thrown when it is not your turn.
	 * @throws NoSuchGameException
	 *             Thrown if no game with gameId exists.
	 */
	public boolean move(String key, int gameId, Room room)
			throws NotLoggedInException, NotYourTurnException,
			NoSuchGameException;

	/**
	 * Ends your turn.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game you are playing.
	 * @throws NotLoggedInException
	 *             Thrown when your key is not valid.
	 * @throws NotYourTurnException
	 *             Thrown if it is not your turn to end.
	 * @throws NoSuchGameException
	 *             Thrown if no game with gameId exists.
	 */
	public void endTurn(String key, int gameId) throws NotLoggedInException,
			NotYourTurnException, NoSuchGameException;

	/**
	 * Gets the status of a particular game.
	 * 
	 * @param gameId
	 *            The id of the game for which to get information.
	 * @return The status of the game specified by gameId.
	 * @throws NoSuchGameException
	 *             Thrown if no game with gameId exists.
	 */
	public GameData getStatus(int gameId) throws NoSuchGameException;

	/**
	 * Gets the status of a game using the game's name.
	 * 
	 * Though the game's name could definitely be used as the primary key, I
	 * would rather not have strings be passed around. Also, the database would
	 * perform slightly more slowly for strings than for integers.
	 * 
	 * @param gameName
	 * @return
	 * @throws NoSuchGameException
	 */
	public GameData getStatusByName(String gameName) throws NoSuchGameException;

	/**
	 * Lists all the games being played on the server.
	 * 
	 * @return All the games being played on the server.
	 */
	public GameData[] getGames();

	/**
	 * Starts the game.
	 * 
	 * @param key
	 *            The key given to you when you logged in.
	 * @param gameId
	 *            The id of the game to start.
	 * @throws NotLoggedInException
	 *             Thrown if key is invalid.
	 * @throws NoSuchGameException
	 *             Thrown if no game with gameId exists.
	 * @throws GameStartedException
	 *             Thrown if the game has already started.
	 * @throws NotEnoughPlayersException
	 *             Thrown if there are not enough players to start the game.
	 */
	public void startGame(String key, int gameId) throws NotLoggedInException,
			NoSuchGameException, GameStartedException,
			NotEnoughPlayersException;

	/**
	 * Sends a message to all players in a game.
	 * 
	 * @param key
	 *            The key returned from login.
	 * @param gameId
	 *            The id of the game in which to send the message.
	 * @param message
	 *            The message to send.
	 * @throws NotLoggedInException
	 *             Thrown if your key is invalid.
	 * @throws NoSuchGameException
	 *             Thrown if no game with gameId exists.
	 * @throws NotYourTurnException
	 *             Thrown if you are not part of the game.
	 */
	public void chat(String key, int gameId, String message)
			throws NotLoggedInException, NoSuchGameException,
			NotInGameException;

	/**
	 * 
	 * Allows the user to leave the game.
	 * 
	 * @param key
	 *            The key returned from login.
	 * @param gameId
	 *            The id of the game to leave.
	 * @throws NotLoggedInException
	 * @throws NoSuchGameException
	 * @throws NotInGameException
	 */
	public void leave(String key, int gameId) throws NotLoggedInException,
			NoSuchGameException, NotInGameException;

	/**
	 * Allows users to get their cards. This is mainly for the MUD game where
	 * players can accidentally leave and then rejoin but they will not have
	 * their cards.
	 * 
	 * @param key
	 *            The player's key.
	 * @param gameId
	 *            The game for which the cards should be retrieved.
	 * @return The player's cards. If the game has not yet started, this
	 *         function will return null.
	 * @throws NotLoggedInException
	 * @throws NoSuchGameException
	 * @throws NotInGameException
	 */
	public Card[] getCards(String key, int gameId) throws NoSuchGameException,
			NotInGameException;

	/**
	 * Finds the suspect used by the player identified by key and gameId
	 * 
	 * @param key
	 *            The key of the player.
	 * @param gameId
	 *            The game id of the player.
	 * @return The suspect being used by the player.
	 */
	public Suspect getSuspectForPlayer(String key, int gameId);
}
