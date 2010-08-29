package com.pocketcookies.clue.mud;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class Grid {
	private static Terrain charToTerrain(int c) {
		switch (c) {
		case 'w':
			return Terrain.WALL;
		case 'i':
			return Terrain.IMPASSABLE;
		case 'W':
			return Terrain.WHITE_START;
		case 'G':
			return Terrain.GREEN_START;
		case 'K':
			return Terrain.KITCHEN;
		case 'N':
			return Terrain.SECRET_PASSAGE_KITCHEN;
		case 'B':
			return Terrain.BALLROOM;
		case 't':
			return Terrain.TILE;
		case 'E':
			return Terrain.SECRET_PASSAGE_LOUNGE;
		case 'V':
			return Terrain.SECRET_PASSAGE_CONSERVATORY;
		case 'Y':
			return Terrain.SECRET_PASSAGE_STUDY;
		case 'D':
			return Terrain.DINING_ROOM;
		case 'I':
			return Terrain.BILLIARD_ROOM;
		case 'L':
			return Terrain.LIBRARY;
		case 'O':
			return Terrain.LOUNGE;
		case 'T':
			return Terrain.STUDY;
		case 'H':
			return Terrain.HALL;
		case 'S':
			return Terrain.SCARLETT_START;
		case 'M':
			return Terrain.MUSTARD_START;
		case 'U':
			return Terrain.PLUM_START;
		case 'P':
			return Terrain.PEACOCK_START;
		case 'C':
			return Terrain.CONSERVATORY;
		default:
			return null;
		}
	}

	public enum Terrain {
		TILE, WALL, SECRET_PASSAGE_LOUNGE, SECRET_PASSAGE_CONSERVATORY, SECRET_PASSAGE_KITCHEN, SECRET_PASSAGE_STUDY, IMPASSABLE, KITCHEN, BALLROOM, CONSERVATORY, DINING_ROOM, LOUNGE, HALL, STUDY, LIBRARY, BILLIARD_ROOM, SCARLETT_START, GREEN_START, MUSTARD_START, PLUM_START, PEACOCK_START, WHITE_START;
		public Card toCard() {
			switch (this) {
			case BALLROOM:
				return Card.BALLROOM;
			case BILLIARD_ROOM:
				return Card.BILLIARD_ROOM;
			case CONSERVATORY:
			case SECRET_PASSAGE_CONSERVATORY:
				return Card.CONSERVATORY;
			case DINING_ROOM:
				return Card.DINING_ROOM;
			case HALL:
				return Card.HALL;
			case KITCHEN:
			case SECRET_PASSAGE_KITCHEN:
				return Card.KITCHEN;
			case LIBRARY:
				return Card.LIBRARY;
			case LOUNGE:
			case SECRET_PASSAGE_LOUNGE:
				return Card.LOUNGE;
			case STUDY:
			case SECRET_PASSAGE_STUDY:
				return Card.STUDY;
			default:
				return null;
			}
		}
	}

	public static final Terrain[][] grid;
	public static final Point SECRET_PASSAGE_CONSERVATORY,
			SECRET_PASSAGE_LOUNGE, SECRET_PASSAGE_STUDY,
			SECRET_PASSAGE_KITCHEN;
	public static final Point SCARLETT_START, MUSTARD_START, GREEN_START,
			PEACOCK_START, PLUM_START, WHITE_START;
	static {
		Point tempSecretPassageConservatory = null, tempSecretPassageLounge = null, tempSecretPassageStudy = null, tempSecretPassageKitchen = null;
		Point tempScarlettStart = null, tempMustardStart = null, tempGreenStart = null, tempPeacockStart = null, tempPlumStart = null, tempWhiteStart = null;
		Terrain tempGrid[][] = null;
		try {
			InputStream stream = Grid.class.getClassLoader()
					.getResourceAsStream("board.txt");
			tempGrid = new Terrain[stream.read()][stream.read()];
			int c;

			for (int i = 0; (c = stream.read()) != -1; i++) {
				if (Character.isWhitespace(c)) {
					i--;
					continue;
				}
				int x = i % tempGrid.length;
				int y = i / tempGrid.length;
				tempGrid[x][y] = charToTerrain(c);
				if (tempGrid[x][y] == Terrain.SECRET_PASSAGE_CONSERVATORY)
					tempSecretPassageConservatory = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.SECRET_PASSAGE_KITCHEN)
					tempSecretPassageKitchen = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.SECRET_PASSAGE_LOUNGE)
					tempSecretPassageLounge = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.SECRET_PASSAGE_STUDY)
					tempSecretPassageStudy = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.SCARLETT_START)
					tempScarlettStart = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.MUSTARD_START)
					tempMustardStart = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.GREEN_START)
					tempGreenStart = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.PEACOCK_START)
					tempPeacockStart = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.PLUM_START)
					tempPlumStart = new Point(x, y);
				else if (tempGrid[x][y] == Terrain.WHITE_START)
					tempWhiteStart = new Point(x, y);
			}
		} catch (IOException e) {
		}
		SECRET_PASSAGE_CONSERVATORY = tempSecretPassageConservatory;
		SECRET_PASSAGE_LOUNGE = tempSecretPassageLounge;
		SECRET_PASSAGE_STUDY = tempSecretPassageStudy;
		SECRET_PASSAGE_KITCHEN = tempSecretPassageKitchen;
		grid = tempGrid;
		SCARLETT_START = tempScarlettStart;
		MUSTARD_START = tempMustardStart;
		GREEN_START = tempGreenStart;
		PLUM_START = tempPlumStart;
		PEACOCK_START = tempPeacockStart;
		WHITE_START = tempWhiteStart;
	}

	public static boolean isImpassable(int x, int y, Collection<Point> players) {
		return isImpassable(new Point(x, y), players);
	}

	public static boolean isImpassable(Point point, Collection<Point> players) {
		for (Point p : players == null ? new LinkedList<Point>() : players) {
			if (p.equals(point))
				return true;
		}
		Terrain t = grid[point.x][point.y];
		return t == Terrain.IMPASSABLE || t == Terrain.WALL;
	}

	public static Collection<Exit> findExits(Point start,
			Collection<Point> players) {
		Collection<Exit> exits = new LinkedList<Exit>();
		if (start.x - 1 >= 0 && start.y - 1 >= 0
				&& !isImpassable(start.x - 1, start.y - 1, players))
			exits.add(Exit.NORTH_WEST);
		if (start.y - 1 >= 0 && !isImpassable(start.x, start.y - 1, players))
			exits.add(Exit.NORTH);
		if (start.x + 1 < grid.length && start.y - 1 >= 0
				&& !isImpassable(start.x + 1, start.y - 1, players))
			exits.add(Exit.NORTH_EAST);
		if (start.x - 1 >= 0 && !isImpassable(start.x - 1, start.y, players))
			exits.add(Exit.WEST);
		if (start.x + 1 < grid.length
				&& !isImpassable(start.x + 1, start.y, players))
			exits.add(Exit.EAST);
		if (start.x - 1 >= 0 && start.y + 1 < grid[0].length
				&& !isImpassable(start.x - 1, start.y + 1, players))
			exits.add(Exit.SOUTH_WEST);
		if (start.y + 1 < grid[0].length
				&& !isImpassable(start.x, start.y + 1, players))
			exits.add(Exit.SOUTH);
		if (start.x + 1 < grid.length && start.y + 1 < grid[0].length
				&& !isImpassable(start.x + 1, start.y + 1, players))
			exits.add(Exit.SOUTH_EAST);
		return exits;
	}

	public static Point getStartingPosition(Suspect suspect) {
		switch (suspect) {
		case GREEN:
			return new Point(GREEN_START);
		case MUSTARD:
			return new Point(MUSTARD_START);
		case PEACOCK:
			return new Point(PEACOCK_START);
		case PLUM:
			return new Point(PLUM_START);
		case SCARLETT:
			return new Point(SCARLETT_START);
		case WHITE:
			return new Point(WHITE_START);
		default:
			return null;
		}
	}
}
