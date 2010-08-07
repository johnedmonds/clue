package com.pocketcookies.clue;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.pocketcookies.clue.Grid.Terrain;
import com.pocketcookies.clue.players.Player;

public class Board {

	private static class GridSquare implements Comparable<GridSquare> {
		private Terrain terrain;
		private int distance;
		private boolean visited = false;
		int x, y;
		private boolean playerHere; // You cannot have a path going through a

		// player.

		public GridSquare(int x, int y, Terrain terrain, int distance) {
			this.x = x;
			this.y = y;
			this.terrain = terrain;
			this.distance = distance;
		}

		public Terrain getTerrain() {
			return this.terrain;
		}

		public int getDistance() {
			return this.distance;
		}

		public void setDistance(int distance) {
			this.distance = distance;
		}

		@Override
		public int compareTo(GridSquare o) {
			if (this.distance < 0)
				return -1;
			else
				return this.distance - o.distance;
		}

		public void setVisited(boolean visited) {
			this.visited = visited;
		}

		public void setPlayerHere(boolean playerHere) {
			this.playerHere = playerHere;
		}

		public boolean isPlayerHere() {
			return playerHere;
		}
	}

	private GridSquare[][] grid;

	private Board(Player[] players) {
		this.setup(players);
	}

	private void setup(Player[] players) {
		this.grid = new GridSquare[Grid.grid.length][Grid.grid[0].length];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[i].length; j++) {
				grid[i][j] = new GridSquare(i, j, Grid.grid[i][j], -1);
			}
		}
		for (Player p : players) {
			if (p != null)
				grid[p.getPosition().x][p.getPosition().y].setPlayerHere(true);
		}
	}

	private boolean hasLeftWall(int i, int j) {
		return i >= 1 && Grid.grid[i - 1][j] == Terrain.WALL;
	}

	private boolean hasTopWall(int i, int j) {
		return j >= 1 && Grid.grid[i][j - 1] == Terrain.WALL;
	}

	private boolean hasRightWall(int i, int j) {
		return i < Grid.grid.length - 1 && Grid.grid[i + 1][j] == Terrain.WALL;
	}

	private boolean hasBottomWall(int i, int j) {
		return j < Grid.grid[0].length - 1
				&& Grid.grid[i][j + 1] == Terrain.WALL;
	}

	private char wallSegment(int i, int j) {
		if (hasLeftWall(i, j) && hasTopWall(i, j) && hasRightWall(i, j)
				&& hasBottomWall(i, j))
			return 206;
		else if (hasLeftWall(i, j) && hasTopWall(i, j) && hasRightWall(i, j))
			return 202;
		else if (hasLeftWall(i, j) && hasTopWall(i, j) && hasBottomWall(i, j))
			return 203;
		else if (hasTopWall(i, j) && hasRightWall(i, j) && hasBottomWall(i, j))
			return 204;
		else if (hasLeftWall(i, j) && hasBottomWall(i, j) && hasRightWall(i, j))
			return 185;
		else if (hasTopWall(i, j) && hasRightWall(i, j))
			return 200;
		else if (hasLeftWall(i, j) && hasTopWall(i, j))
			return 188;
		else if (hasRightWall(i, j) && hasBottomWall(i, j))
			return 201;
		else if (hasLeftWall(i, j) && hasBottomWall(i, j))
			return 187;
		else if (hasTopWall(i, j) || hasBottomWall(i, j))
			return 186;
		else if (hasLeftWall(i, j) || hasRightWall(i, j))
			return 205;
		return '\0';
	}

	private int distance(GridSquare from, GridSquare to) {
		if (to.isPlayerHere())
			return -1; // Infinity
		if (to.getTerrain() == Terrain.WALL
				|| to.getTerrain() == Terrain.IMPASSABLE)
			return -1; // Infinity
		if (to.getTerrain() == Terrain.TILE)
			return 1;
		// We're going to make taking secret passage-ways free.
		if (to.getTerrain() == Terrain.SECRET_PASSAGE_CONSERVATORY
				|| to.getTerrain() == Terrain.SECRET_PASSAGE_LOUNGE
				|| to.getTerrain() == Terrain.SECRET_PASSAGE_KITCHEN
				|| to.getTerrain() == Terrain.SECRET_PASSAGE_STUDY)
			return 0;
		if (!to.getTerrain().equals(from.getTerrain()))
			return 1;
		return 0;
	}

	private Collection<GridSquare> getNeighbors(GridSquare start) {
		LinkedList<GridSquare> neighbors = new LinkedList<Board.GridSquare>();
		for (int i = Math.max(0, start.x - 1); i < Math.min(this.grid.length,
				start.x + 1 + 1); i++) {
			for (int j = Math.max(0, start.y - 1); j < Math.min(
					this.grid[0].length, start.y + 1 + 1); j++) {
				if (i != start.x || j != start.y)
					neighbors.add(grid[i][j]);
			}
		}
		switch (start.getTerrain()) {
		case SECRET_PASSAGE_CONSERVATORY:
			neighbors
					.add(grid[Grid.SECRET_PASSAGE_LOUNGE.x][Grid.SECRET_PASSAGE_LOUNGE.y]);
			break;
		case SECRET_PASSAGE_LOUNGE:
			neighbors
					.add(grid[Grid.SECRET_PASSAGE_CONSERVATORY.x][Grid.SECRET_PASSAGE_CONSERVATORY.y]);
			break;
		case SECRET_PASSAGE_KITCHEN:
			neighbors
					.add(grid[Grid.SECRET_PASSAGE_STUDY.x][Grid.SECRET_PASSAGE_STUDY.y]);
			break;
		case SECRET_PASSAGE_STUDY:
			neighbors
					.add(grid[Grid.SECRET_PASSAGE_KITCHEN.x][Grid.SECRET_PASSAGE_KITCHEN.y]);
			break;
		}
		return neighbors;
	}

	public static int distance(Player[] players, Point start, Point end) {
		return new Board(players).distance(start, end);
	}

	private int distance(Point start, Point end) {
		// This will short circuit and also protect us against players
		// attempting to move into their own positions.
		if (this.grid[end.x][end.y].isPlayerHere())
			return -1;
		PriorityQueue<GridSquare> nextNode = new PriorityQueue<GridSquare>();
		this.grid[start.x][start.y].setDistance(0);
		nextNode.add(this.grid[start.x][start.y]);
		while (nextNode.size() > 0) {
			GridSquare currentNode = nextNode.poll();
			for (GridSquare neighbor : getNeighbors(currentNode)) {
				if (neighbor.visited)
					continue;
				int tempDistance = distance(currentNode, neighbor);
				if (tempDistance >= 0) {
					tempDistance += currentNode.getDistance();
					if (tempDistance < neighbor.getDistance()
							|| neighbor.getDistance() < 0) {
						neighbor.setDistance(tempDistance);
						// Since the value has changed, we need to remove it
						// from
						// the queue and put it back in, otherwise it will come
						// out
						// in the wrong order.
						nextNode.remove(neighbor);
					}
					nextNode.add(neighbor);
				}
			}
			currentNode.setVisited(true);
			if (currentNode.equals(this.grid[end.x][end.y]))
				return currentNode.getDistance();
		}
		return -1;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < this.grid[0].length; j++) {
			for (int i = 0; i < this.grid.length; i++) {
				switch (this.grid[i][j].getTerrain()) {
				case TILE:
					sb.append((char) 219);// ASCII code 219
					break;
				case WALL:
					sb.append(wallSegment(i, j));
					break;
				case IMPASSABLE:
					sb.append((char) 184);
					break;
				default:
					sb.append('R');
				}
			}
			sb.append('\n');
		}
		return sb.toString();
	}
}
