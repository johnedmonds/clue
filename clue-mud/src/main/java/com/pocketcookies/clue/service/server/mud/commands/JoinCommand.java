package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.mud.Grid;
import com.pocketcookies.clue.players.Suspect;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class JoinCommand implements Command {

	private static final Pattern joinCommandParser = Pattern
			.compile("[^\\s]+ (.*) ([^\\s]+)");
	private static final Pattern rejoinCommandParser = Pattern
			.compile("[^\\s]+ (.*)");

	private static final Logger logger = Logger.getLogger(JoinCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		Collection<String> ret = new ArrayList<String>();
		ret.add("join");
		return ret;
	}

	@Override
	public void process(String command, MudPlayer player) {
		final PrintWriter writer = player.getWriter();
		if (player.isInGame()) {
			writer.println("You are already in a game and cannot join another one without leaving this one first.");
			return;
		}
		Matcher matcher = null;
		if ((matcher = joinCommandParser.matcher(command)).matches())
			join(player, matcher.group(1), matcher.group(2));
		else if ((matcher = rejoinCommandParser.matcher(command)).matches())
			rejoin(player, matcher.group(1));
		else {
			writer.println("To join a game, use the syntax: join <game name> <suspect>");
			return;
		}
	}

	private static void setupPlayer(int gameId, Suspect suspect,
			MudPlayer player) {
		player.setGameId(gameId);
		player.suspect = suspect;
		// Position ourselves.
		player.getPlayers().put(player.getUsername(),
				Grid.getStartingPosition(suspect));
		player.startMessageConnection();
	}

	private static void join(MudPlayer player, String gameName,
			String suspectName) {
		final ClueServiceAPI service = player.getService();
		final PrintWriter writer = player.getWriter();
		Suspect suspect;
		try {
			suspect = Suspect.valueOf(suspectName);
		} catch (IllegalArgumentException e) {
			writer.println("That is not a valid suspect.");
			return;
		}
		GameData data = null;
		try {
			data = service.getStatusByName(gameName);
			final int gameId = data.getGameId();
			service.join(player.getKey(), gameId, suspect);
			setupPlayer(gameId, suspect, player);
		} catch (NoSuchGameException e) {
			writer.println("There is no game with that name.");
		} catch (NotLoggedInException e) {
			writer.println("The server seems to think you are not logged in.  Try logging out and back in again.");
			logger.error("Player " + player.getUsername()
					+ " is not logged in.", e);
		} catch (SuspectTakenException e) {
			writer.println("That suspect is already in use by another player.  Try choosing a different suspect.");
		} catch (GameStartedException e) {
			writer.println("This game has already started.  You cannot join a game that is already in progress.");
		} catch (AlreadyJoinedException e) {
			// gameId will be valid here because we will either fail to get it
			// and cause a different exception, or succeed in getting it and be
			// able to use it here.
			rejoin(player, data);
		}
	}

	private static void rejoin(MudPlayer player, String gameName) {
		try {
			rejoin(player, player.getService().getStatusByName(gameName));
		} catch (NoSuchGameException e) {
			player.getWriter().println("No game by that name exists.");
		}
	}

	private static Suspect getPlayerSuspect(String playerName, GameData data) {
		for (PlayerData pd : data.getPlayers()) {
			if (pd.getPlayerName().equals(playerName))
				return pd.getSuspect();
		}
		return null;
	}

	private static void rejoin(MudPlayer player, GameData data) {
		setupPlayer(data.getGameId(),
				getPlayerSuspect(player.getUsername(), data), player);
	}
}
