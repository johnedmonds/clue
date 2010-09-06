package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class MoveCommand implements Command {

	private static final Logger logger = Logger.getLogger(MoveCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		ArrayList<String> tempCommands = new ArrayList<String>(
				Room.values().length);
		for (Room r : Room.values()) {
			tempCommands.add(r.toString());
			tempCommands.add(r.toString().toLowerCase());
		}
		return Collections.unmodifiableCollection(tempCommands);
	}

	@Override
	public void process(String command, MudPlayer player) {
		PrintWriter writer = player.getWriter();
		try {
			Room to = Room.valueOf(command.toUpperCase());
			player.getService().move(player.getKey(), player.getGameId(), to);
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
		}

	}

}
