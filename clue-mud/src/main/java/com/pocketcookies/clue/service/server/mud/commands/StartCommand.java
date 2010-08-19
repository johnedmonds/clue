package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class StartCommand implements Command {

	private static final Logger logger = Logger.getLogger(StartCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		Collection<String> ret = new LinkedList<String>();
		ret.add("start");
		return ret;
	}

	@Override
	public void process(String command, MudPlayer player) {
		PrintWriter writer = player.getWriter();
		ClueServiceAPI service = player.getService();

		if (!player.isInGame()) {
			writer.println("You need to be in a game to start it.");
			return;
		}

		try {
			service.startGame(player.getKey(), player.getGameId());
			writer.println("The game has been successfully started.");
		} catch (NotLoggedInException e) {
			logger.error(
					"The player tried to start the game but was not logged in.",
					e);
			writer.println("The server does not seem to know who you are.  This is probably our fault.  Try logging out and logging back in again.");
		} catch (NoSuchGameException e) {
			logger.error(
					"The player attempted to start the game but the game didn't exist.",
					e);
			writer.println("The game you were a part of does not exist.  Try leaving that game and joining another.");
		} catch (GameStartedException e) {
			writer.println("The game has already started.  You cannot start it again.");
		} catch (NotEnoughPlayersException e) {
			writer.println("There needs to be at least 3 players in the game before the game can be started.  Try getting more players.");
		}
	}
}
