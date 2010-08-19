package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.GameStartedState;
import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class JoinCommand implements Command {

	private static final Logger logger = Logger.getLogger(JoinCommand.class);

	private static boolean isGameId(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	@Override
	public Collection<String> getCommandAliases() {
		Collection<String> ret = new ArrayList<String>();
		ret.add("join");
		return ret;
	}

	@Override
	public void process(String command, MudPlayer player) {
		String[] args = command.split(" ");
		PrintWriter writer = player.getWriter();
		ClueServiceAPI service = player.getService();
		if (args.length != 3)
			return;
		if (player.isInGame()) {
			writer.println("You are already in a game and cannot join another one without leaving this one first.");
			return;
		}
		int gameId = -1;
		// It is a number.
		if (isGameId(args[1])) {
			try {
				int tempGameId = Integer.parseInt(args[1]);
				service.join(player.getKey(), tempGameId,
						Suspect.valueOf(args[2].toUpperCase()));
				gameId = tempGameId;
				writer.println("You have successfully joined that game.");
			} catch (NumberFormatException e) {
				logger.error(
						"There was a problem parsing the number. That probably means there is an error detecting that this is a number.",
						e);
				writer.println("No game with that ID exists.");
			} catch (NotLoggedInException e) {
				logger.error(
						"The player appears to be not logged in. Hopefully this was caused by something as simple as the database being reset.",
						e);
				writer.println("The server doesn't seem to recognize you.  This is probably our fault.  Try logging out and back in again.");
			} catch (NoSuchGameException e) {
				writer.println("No game with that ID exists.");
			} catch (SuspectTakenException e) {
				writer.println(String
						.format("The suspect %s is already taken.  Try picking a different one.",
								args[1]));
			} catch (GameStartedException e) {
				writer.println("That game has already started.");
			} catch (AlreadyJoinedException e) {
				writer.println("You have already joined that game.  Perhaps using a different client.  You cannot rejoin the game.");
			}
		} else {
			GameData[] games = service.getGames(args[1],
					GameStartedState.NOT_STARTED);
			if (games.length < 1)
				writer.println("No game by that name exists (or all games by that name have already started).");
			else if (games.length > 1) {
				writer.println("There are several games by that name which you may join.  Their ids are below.  Type join <id> <suspect> to join one of those games.");
				for (GameData gd : games) {
					writer.println(gd.getGameId());
				}
			} else {
				try {
					service.join(player.getKey(), games[0].getGameId(),
							Suspect.valueOf(args[2].toUpperCase()));
					gameId = games[0].getGameId();
					writer.println("You have successfully joined the game.");
				} catch (NotLoggedInException e) {
					logger.error("The user is not logged in.", e);
					writer.println("The server doesn't appear to recognize you.  This is probably our fault.  Try logging out and back in again.");
				} catch (NoSuchGameException e) {
					logger.error("We found the game but it disappeared.", e);
					writer.println("We tried to put you in that game but the game seems to have disappeared.  Try joining again.");
				} catch (SuspectTakenException e) {
					writer.println("That suspect is already taken.");
				} catch (GameStartedException e) {
					writer.println("The game has already started.  You were too late.");
				} catch (AlreadyJoinedException e) {
					writer.println("You are already in that game.  You cannot join again.");
				}
			}
		}
		if (gameId >= 0)
			player.setGameId(gameId);
	}
}
