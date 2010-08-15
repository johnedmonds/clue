package com.pocketcookies.clue.test;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import junit.framework.TestCase;

import com.pocketcookies.clue.Board;
import com.pocketcookies.clue.User;
import com.pocketcookies.clue.players.Player;
import com.pocketcookies.clue.players.Suspect;

//TODO: Test players block entrances.

public class BoardTest extends TestCase {
	public void testSingleDistance() {
		assertEquals(1, Board.distance(new LinkedList<Player>(),
				new Point(0, 7), new Point(1, 7)));
	}

	public void testMoveToRoom() {
		assertEquals(4, Board.distance(new LinkedList<Player>(), new Point(2,
				16), new Point(6, 15)));
	}

	public void testMoveIntoRoom() {
		assertEquals(4, Board.distance(new LinkedList<Player>(), new Point(2,
				16), new Point(4, 11)));
	}

	public void testMoveAroundRoom() {
		assertEquals(0, Board.distance(new LinkedList<Player>(), new Point(1,
				14), new Point(6, 11)));
	}

	public void testKitchenToStudy() {
		assertEquals(1, Board.distance(new LinkedList<Player>(),
				new Point(1, 3), new Point(18, 22)));
	}

	public void testStudyToKitchen() {
		assertEquals(1, Board.distance(new LinkedList<Player>(), new Point(18,
				22), new Point(1, 3)));
	}

	public void testConservatoryToLounge() {
		assertEquals(1, Board.distance(new LinkedList<Player>(), new Point(22,
				2), new Point(3, 21)));
	}

	public void testLoungeToConservatory() {
		assertEquals(1, Board.distance(new LinkedList<Player>(), new Point(3,
				21), new Point(22, 2)));
	}

	public void testPlayersBlockEntrance() {
		// First check the cost without a player blocking the door.
		ArrayList<Player> players = new ArrayList<Player>();
		assertEquals(2,
				Board.distance(players, new Point(6, 17), new Point(6, 14)));
		Player p = new Player(new User("a", "test player"), Suspect.SCARLETT, 0);
		p.setPosition(new Point(6, 15));
		players.add(p);
		assertEquals(-1,
				Board.distance(players, new Point(6, 17), new Point(6, 14)));

	}

	// Checks if positioning players at entrances actually causes us to use a
	// different entrance.
	// This is probably covered by testPlayersBlockEntrance but this test is
	// mainly to make sure that using a list of players doesn't just make
	// moving anywhere impossible.
	public void testPlayersImpedeEntrance() {
		ArrayList<Player> players = new ArrayList<Player>();
		Player p;
		assertEquals(2,
				Board.distance(players, new Point(17, 5), new Point(11, 2)));
		p = new Player(new User("a", "clue test 1"), Suspect.SCARLETT, 0);
		p.setPosition(new Point(15, 5));
		players.add(p);
		assertEquals(4,
				Board.distance(players, new Point(17, 5), new Point(11, 2)));
		p = new Player(new User("b", "clue test 2"), Suspect.GREEN, 0);
		p.setPosition(new Point(14, 7));
		players.add(p);
		assertEquals(9,
				Board.distance(players, new Point(17, 5), new Point(11, 2)));
		p = new Player(new User("c", "clue test 3"), Suspect.PEACOCK, 0);
		p.setPosition(new Point(9, 7));
		players.add(p);
		assertEquals(13,
				Board.distance(players, new Point(17, 5), new Point(11, 2)));
		p = new Player(new User("d", "clue test 4"), Suspect.PLUM, 0);
		p.setPosition(new Point(8, 5));
		players.add(p);
		assertEquals(-1,
				Board.distance(players, new Point(17, 5), new Point(11, 2)));
	}
}
