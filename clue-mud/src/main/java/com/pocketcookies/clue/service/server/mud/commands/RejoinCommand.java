package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;

import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class RejoinCommand implements Command {

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays
				.asList(new String[] { "rejoin" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		String[] args = command.split(" ");
		PrintWriter writer = player.getWriter();
		ClueServiceAPI service = player.getService();
		if (args.length != 2) {
			writer.println("You must specify a game or the ID of a game to rejoin.  You can only rejoin games that you left due to a dropped connection.");
			return;
		}
		int gameId;
		// Pick the game ID.
		try {
			// Check if the player submitted an id.
			gameId = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			// Otherwise, find all the possible games.
			GameData[] tempData = service.getGames(args[1], null);
			// If there was more than one game, print out all the games.
			if (tempData.length > 1) {
				printGameOptions(writer, tempData);
				return;
			}
			// Otherwise, if there were no games, tell the player that too.
			else if (tempData.length <= 0) {
				new Formatter(writer).format(
						"There are no games with the name %s.", args[1]);
				// Create a new line using the system default.
				System.out.println();
				return;
			}
			// If we have not returned by now, we can unambiguously pick the
			// game.
			gameId = tempData[0].getGameId();
		}
		GameData data;
		try {
			data = service.getStatus(gameId);
			player.setGameId(gameId);
			player.suspect = findSuspect(data.getPlayers(),
					player.getUsername());
			player.loadPlayerPositions();
			player.startMessageConnection();
			writer.println("You have successfully rejoined the game.");
		} catch (NoSuchGameException e) {
			new Formatter(writer).format("Game %s does not exist", args[1]);
			System.out.println();
			return;
		}
	}

	private static void printGameOptions(PrintWriter writer, GameData[] tempData) {
		writer.println("There are multiple games by that name.  Use the command: \"rejoin <id>\" to specify which game you would like to join:");
		for (GameData gd : tempData) {
			writer.println("ID: " + gd.getGameId());
			writer.println("Players: ");
			for (PlayerData pd : gd.getPlayers()) {
				writer.println("\t" + pd.getPlayerName() + " - "
						+ pd.getSuspect().toString());
			}
		}
	}

	private static Suspect findSuspect(PlayerData[] data, String username) {
		for (PlayerData pd : data) {
			if (pd.getPlayerName().equals(username))
				return pd.getSuspect();
		}
		return null;
	}

}
