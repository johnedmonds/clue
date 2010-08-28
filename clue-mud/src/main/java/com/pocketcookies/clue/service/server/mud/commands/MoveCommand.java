package com.pocketcookies.clue.service.server.mud.commands;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.IllegalMoveException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.mud.Exit;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class MoveCommand implements Command {

	private static final Logger logger = Logger.getLogger(MoveCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays.asList(new String[] {
				"n", "s", "e", "w", "ne", "nw", "se", "sw", "north",
				"northeast", "northwest", "southwest", "southeast", "south",
				"east", "west" }));
	}

	private static Exit directionToExit(String direction) {
		direction = direction.toLowerCase();
		if (direction.equals("ne") || direction.equals("northeast"))
			return Exit.NORTH_EAST;
		else if (direction.equals("se") || direction.equals("southeast"))
			return Exit.SOUTH_EAST;
		else if (direction.equals("s") || direction.equals("south"))
			return Exit.SOUTH;
		else if (direction.equals("sw") || direction.equals("southwest"))
			return Exit.SOUTH_WEST;
		else if (direction.equals("w") || direction.equals("west"))
			return Exit.WEST;
		else if (direction.equals("nw") || direction.equals("northwest"))
			return Exit.NORTH_WEST;
		else if (direction.equals("n") || direction.equals("north"))
			return Exit.NORTH;
		else if (direction.equals("e") || direction.equals("east"))
			return Exit.EAST;
		return null;
	}

	private static Point exitToPoint(Exit exit, Point start) {
		switch (exit) {
		case EAST:
			return new Point(start.x + 1, start.y);
		case NORTH:
			return new Point(start.x, start.y - 1);
		case NORTH_EAST:
			return new Point(start.x + 1, start.y - 1);
		case NORTH_WEST:
			return new Point(start.x - 1, start.y - 1);
		case SOUTH:
			return new Point(start.x, start.y + 1);
		case SOUTH_EAST:
			return new Point(start.x + 1, start.y + 1);
		case SOUTH_WEST:
			return new Point(start.x - 1, start.y + 1);
		case WEST:
			return new Point(start.x - 1, start.y);
		default:
			return null;
		}
	}

	@Override
	public void process(String command, MudPlayer player) {
		PrintWriter writer = player.getWriter();
		Point to = exitToPoint(directionToExit(command), player.getPlayers()
				.get(player.getUsername()));
		try {
			player.getService().move(player.getKey(), player.getGameId(), to.x,
					to.y);
			// There is no need to move the player here because if the move is
			// successful, we will get a message which will cause us to position
			// the player properly.
		} catch (NotLoggedInException e) {
			writer.println("The server seems to think you are not logged in.  Try logging out and back in again.");
			logger.error("Player " + player.getKey() + " is not logged in.");
		} catch (NotYourTurnException e) {
			writer.println("You can only move during your turn.");
		} catch (NoSuchGameException e) {
			writer.println("The game no longer exists.  Try leaving and rejoining.");
			logger.error("Player " + player.getKey()
					+ " experienced an error where game " + player.getGameId()
					+ " did not exist.");
		} catch (IllegalMoveException e) {
			writer.println("You cannot move in that direction (maybe you are too tired).");
		}

	}

}
