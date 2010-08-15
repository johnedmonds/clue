package com.pocketcookies.clue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.CheatException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.IllegalMoveException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.NotInRoomException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.messages.Message;
import com.pocketcookies.clue.messages.broadcast.Accusation;
import com.pocketcookies.clue.messages.broadcast.Chat;
import com.pocketcookies.clue.messages.broadcast.Disprove;
import com.pocketcookies.clue.messages.broadcast.GameOver;
import com.pocketcookies.clue.messages.broadcast.Move;
import com.pocketcookies.clue.messages.broadcast.NextTurn;
import com.pocketcookies.clue.messages.broadcast.Proposition;
import com.pocketcookies.clue.messages.broadcast.Suggestion;
import com.pocketcookies.clue.messages.targeted.Cards;
import com.pocketcookies.clue.messages.targeted.DisprovingCard;
import com.pocketcookies.clue.players.Player;
import com.pocketcookies.clue.players.Suspect;

public class Game {
	private static Logger logger = Logger.getLogger(Game.class);

	private String name;
	// A list of players in order of the suspects (SCARLETT, MUSTARD, WHITE,
	// GREEN, PEACOCK, PLUM).
	private List<Player> players = new ArrayList<Player>();
	private Player currentPlayer;
	private GameStartedState gameStartedState = GameStartedState.NOT_STARTED;
	private boolean suggestionMade = false;
	private int movementAllowed = -1;
	private Card suspect, room, weapon;
	private Proposition proposition;
	private Player disprovingPlayer = null;
	private Integer id;

	public String getName() {
		return this.name;
	}

	public Game(String name) {
		this.name = name;
	}

	public Player getCurrentPlayer() {
		return this.currentPlayer;
	}

	public void setCurrentPlayer(Player p) {
		this.currentPlayer = p;
	}

	public GameStartedState getStartedState() {
		return this.gameStartedState;
	}

	public void endTurn(Random random) throws NotYourTurnException {
		if (this.gameStartedState != GameStartedState.STARTED
				|| this.proposition != null) {
			throw new NotYourTurnException();
		}
		// Used to make sure every player hasn't lost and the server doesn't
		// just keep going in an infinite loop.
		Player currentCurrentPlayer = this.currentPlayer;
		int i;
		for (i = this.currentPlayer.getId().getSuspect().ordinal() + 1; (players
				.get(i % this.players.size()) == null || players.get(
				i % this.players.size()).isLost())
				&& i % this.players.size() != currentCurrentPlayer.getId()
						.getSuspect().ordinal(); i++) {
		}
		this.currentPlayer = players.get(i % this.players.size());
		if (this.currentPlayer == null
				|| this.currentPlayer == currentCurrentPlayer
				|| this.currentPlayer.isLost()) {
			logger.info("Everybody lost.");
			GameOver gameOver = new GameOver(null);
			publish(gameOver);
			this.gameStartedState = GameStartedState.ENDED;
			return;
		}

		this.suggestionMade = false;
		this.proposition = null;
		this.disprovingPlayer = null;
		this.movementAllowed = 9;
		NextTurn nextTurn = new NextTurn(
				this.currentPlayer.getUser().getName(), this.movementAllowed);
		this.publish(nextTurn);
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
		while (this.players.size() < Suspect.values().length) {
			this.players.add(null);
		}
	}

	public synchronized GameData getData() {
		GameData data = new GameData();
		ArrayList<PlayerData> playerData = new ArrayList<PlayerData>(
				this.players.size());
		for (int i = 0; i < this.players.size(); i++) {
			if (this.players.get(i) != null) {
				playerData.add(this.players.get(i).getData());
			}
		}
		data.setGameId(this.id);
		data.setGameName(this.name);
		data.setPlayers(playerData.toArray(new PlayerData[playerData.size()]));
		data.setGameStartedState(this.gameStartedState);
		return data;
	}

	public synchronized Player getPlayerWithKey(String key) {
		for (Player p : this.players) {
			if (p != null && p.getUser().getKey().equals(key))
				return p;
		}
		return null;
	}

	public synchronized Player getPlayerWithName(String name) {
		for (Player p : this.players) {
			if (p.getUser().getName().equals(name))
				return p;
		}
		return null;
	}

	public synchronized void accuse(Card room, Card suspect, Card weapon) {
		Accusation accusation = new Accusation(this.currentPlayer.getUser()
				.getName(), room, suspect, weapon);
		accusation.setPlayer(this.currentPlayer.getUser().getName());
		accusation.setRoom(room);
		accusation.setWeapon(weapon);
		accusation.setSuspect(suspect);
		publish(accusation);
		if (this.suspect == suspect && this.room == room
				&& this.weapon == weapon) {
			GameOver gameOver = new GameOver(this.currentPlayer.getUser()
					.getName());
			publish(gameOver);
			this.gameStartedState = GameStartedState.ENDED;
		} else {
			this.currentPlayer.setLost(true);
			for (Player p : this.players) {
				if (p != null) {
					for (Card c : p.getHand()) {
						if (c.equals(suspect) || c.equals(room)
								|| c.equals(weapon)) {
							Disprove disprove = new Disprove(p.getUser()
									.getName());
							this.disprovingPlayer = p;
							this.proposition = accusation;
							publish(disprove);
							break;
						}
					}
				}
			}
		}
	}

	private synchronized void publish(Message m) {
		for (Player p : this.players) {
			if (p != null)
				p.publish(m);
		}
	}

	public synchronized Proposition getProposition() {
		return this.proposition;
	}

	public synchronized Player getDisprovingPlayer() {
		return this.disprovingPlayer;
	}

	public void setDisprovingPlayer(Player p) {
		this.disprovingPlayer = p;
	}

	public synchronized void join(User user, Suspect suspect)
			throws SuspectTakenException, GameStartedException,
			AlreadyJoinedException {
		if (this.gameStartedState.equals(GameStartedState.STARTED))
			throw new GameStartedException();
		for (Player p : this.players) {
			if (p != null) {
				if (p.getId().getSuspect().equals(suspect))
					throw new SuspectTakenException();
				else if (p.getUser().getKey().equals(user.getKey()))
					throw new AlreadyJoinedException();
			}
		}
		this.players.set(suspect.ordinal(), new Player(user, suspect, this.id));
	}

	public synchronized void start(Random random)
			throws NotEnoughPlayersException {
		int cachedJoinedPlayersCount = this.joinedPlayersCount();
		if (cachedJoinedPlayersCount <= 2)
			throw new NotEnoughPlayersException();
		// We don't really know how many cards will be dealt to each player (we
		// could find that out mathematically but I'm too lazy to come up with
		// the equation) so we use an dynamically sizing ArrayList.
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		// Create a new hand for each player.
		for (Player p : this.players) {
			if (p != null)
				hands.add(new ArrayList<Card>());
		}
		// We will need to remove 3 cards from the deck: the real room, suspect,
		// and weapon. Thus, it is easier to use a List.
		List<Card> deck = new LinkedList<Card>();
		// Here we add all possible cards to the deck.
		for (Card c : Card.values()) {
			deck.add(c);
		}
		// We don't want to have the same game every time so we shuffle the deck
		// here.
		Collections.shuffle(deck, random);
		// Pick the solution from the deck.
		// Note that I'm too lazy to break early once we've found the cards for
		// the solution so the solution will be the last room, weapon, and
		// suspect in the deck. This shouldn't really be a problem since we
		// shuffled the deck so the only thing we lose is time going through
		// unnecessary cards. Since we know the deck will be 21 cards, I feel it
		// is an acceptable waste computing time.
		for (Card c : deck) {
			if (c.isRoom())
				this.room = c;
			else if (c.isSuspect())
				this.suspect = c;
			else if (c.isWeapon())
				this.weapon = c;
		}
		// Pull the cards out of the deck so we don't accidentally deal the
		// solution to one (or more) of the players.
		deck.remove(this.room);
		deck.remove(this.suspect);
		deck.remove(this.weapon);
		// Finally deal out the cards.
		for (int i = 0; i < deck.size(); i++) {
			hands.get(i % hands.size()).add(deck.get(i));
		}
		// Actually give the dealt cards to the players.
		int handBeingDealtIndex = 0;
		for (Player p : this.players) {
			if (p != null) {
				p.setHand(hands.get(handBeingDealtIndex));
				handBeingDealtIndex++;
				// Tell the player about their cards.
				Cards cardsMessage = new Cards(p.getHand().toArray(
						new Card[p.getHand().size()]));
				p.publish(cardsMessage);
			}
		}

		// Find the first player.
		for (Player p : this.players) {
			if (p != null) {
				this.currentPlayer = p;
				break;
			}
		}
		this.movementAllowed = 9;
		this.gameStartedState = GameStartedState.STARTED;
		NextTurn nextTurn = new NextTurn(
				this.currentPlayer.getUser().getName(), this.movementAllowed);
		publish(nextTurn);
	}

	public synchronized void disprove(Card card) throws CheatException,
			NotYourTurnException {
		if (this.proposition == null)
			throw new NotYourTurnException();
		if (!this.proposition.getRoom().equals(card)
				&& !this.proposition.getSuspect().equals(card)
				&& !this.proposition.getWeapon().equals(card))
			throw new CheatException();
		DisprovingCard dc = new DisprovingCard(card);
		this.currentPlayer.publish(dc);
		this.proposition = null;
	}

	public synchronized void suggest(Card suspect, Card weapon)
			throws NotYourTurnException, NotInRoomException {
		if (this.proposition != null || this.suggestionMade)
			throw new NotYourTurnException();
		Card room = Grid.grid[this.currentPlayer.getPosition().x][this.currentPlayer
				.getPosition().y].toCard();
		if (room == null)
			throw new NotInRoomException();

		Suggestion suggestion = new Suggestion(this.currentPlayer.getUser()
				.getName(), room, suspect, weapon);
		this.publish(suggestion);
		for (int i = this.currentPlayer.getId().getSuspect().ordinal() + 1; i
				% this.players.size() != this.currentPlayer.getId()
				.getSuspect().ordinal(); i++) {
			if (this.players.get(i) != null) {
				for (Card c : this.players.get(i).getHand()) {
					if (c.equals(suspect) || c.equals(room) || c.equals(weapon)) {
						this.disprovingPlayer = this.players.get(i);
						Disprove disprove = new Disprove(this.disprovingPlayer
								.getUser().getName());
						this.publish(disprove);
						this.proposition = suggestion;
						return;
					}
				}
			}
		}
	}

	public synchronized int move(Point p) throws IllegalMoveException {
		int distance = Board.distance(this.players,
				this.currentPlayer.getPosition(), p);
		if (distance < 0 || this.movementAllowed < distance)
			throw new IllegalMoveException();
		this.movementAllowed -= distance;
		Move move = new Move(this.currentPlayer.getUser().getName(),
				this.currentPlayer.getPosition().x,
				this.currentPlayer.getPosition().y, p.x, p.y, distance);
		this.currentPlayer.setPosition(new Point(p.x, p.y));
		this.publish(move);
		return distance;
	}

	public synchronized void chat(String key, String message)
			throws NotYourTurnException {
		Player chattingPlayer = this.getPlayerWithKey(key);
		if (chattingPlayer == null)
			throw new NotYourTurnException();
		Chat chat = new Chat(chattingPlayer.getUser().getName(), message);
		this.publish(chat);
	}

	private int joinedPlayersCount() {
		int count = 0;
		for (Player p : this.players) {
			if (p != null)
				count++;
		}
		return count;
	}

	// The following stuff is for Hibernate to use.

	public void setSuspect(Card suspect) {
		this.suspect = suspect;
	}

	public Card getSuspect() {
		return this.suspect;
	}

	public void setRoom(Card room) {
		this.room = room;
	}

	public Card getRoom() {
		return this.room;
	}

	public void setWeapon(Card weapon) {
		this.weapon = weapon;
	}

	public Card getWeapon() {
		return this.weapon;
	}

	public void setProposedSuspect(Card suspect) {
		if (suspect == null) {
			this.proposition = null;
			return;
		}
		if (this.proposition == null) {
			this.proposition = new Proposition();
		}
		this.proposition.setSuspect(suspect);
	}

	public void setProposedRoom(Card room) {
		if (room == null) {
			this.proposition = null;
			return;
		}
		if (this.proposition == null) {
			this.proposition = new Proposition();
		}
		this.proposition.setRoom(room);
	}

	public void setProposedWeapon(Card weapon) {
		if (weapon == null) {
			this.proposition = null;
			return;
		}
		if (this.proposition == null) {
			this.proposition = new Proposition();
		}
		this.proposition.setWeapon(weapon);
	}

	public Card getProposedSuspect() {
		if (this.proposition == null) {
			return null;
		}
		return this.proposition.getSuspect();
	}

	public Card getProposedRoom() {
		if (this.proposition == null) {
			return null;
		}
		return this.proposition.getRoom();
	}

	public Card getProposedWeapon() {
		if (this.proposition == null) {
			return null;
		}
		return this.proposition.getWeapon();
	}

	public GameStartedState getGameStartedState() {
		return this.gameStartedState;
	}

	public void setGameStartedState(GameStartedState state) {
		this.gameStartedState = state;
	}

	public boolean isSuggestionMade() {
		return this.suggestionMade;
	}

	public void setSuggestionMade(boolean suggestionMade) {
		this.suggestionMade = suggestionMade;
	}

	public int getMovementAllowed() {
		return this.movementAllowed;
	}

	public void setMovementAllowed(int movementAllowed) {
		this.movementAllowed = movementAllowed;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
		for (Player p : this.players) {
			if (p != null)
				p.getId().setGameId(this.id);
		}
	}

	public Game() {
		for (int i = 0; i < Suspect.values().length; i++) {
			this.players.add(null);
		}
	}

}
