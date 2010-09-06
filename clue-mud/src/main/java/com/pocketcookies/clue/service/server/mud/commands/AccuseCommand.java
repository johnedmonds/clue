package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class AccuseCommand implements Command {

	private static final Logger logger = Logger.getLogger(AccuseCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays
				.asList(new String[] { "accuse" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		String[] args = command.toUpperCase().split(" ");
		PrintWriter writer = player.getWriter();
		if (args.length != 4) {
			writer.println("You must pick a suspect, room, and weapon when accusing.  Like so: accuse <room> <suspect> <weapon>");
			return;
		}
		Card room = null, suspect = null, weapon = null;
		for (String s : Arrays.asList(args).subList(1, args.length)) {
			try {
				Card temp = Card.valueOf(s);
				if (temp.isRoom())
					room = temp;
				else if (temp.isSuspect())
					suspect = temp;
				else if (temp.isWeapon())
					weapon = temp;
			} catch (IllegalArgumentException e) {
				writer.println(s + " is not a valid room, suspect, or weapon.");
				return;
			}
		}
		if (room == null || weapon == null || suspect == null) {
			writer.println("You must enter a room, weapon, and suspect to make an accusation.");
			return;
		}

		try {
			player.getService().accuse(player.getKey(), player.getGameId(),
					room, suspect, weapon);
		} catch (NotLoggedInException e) {
			writer.println("The server does not seem to think you are logged in.  Try logging out and then back in again.");
			logger.error("Player " + player.getUsername()
					+ " is not logged in.", e);
		} catch (NotYourTurnException e) {
			writer.println("You can only make accusations during your turn.");
		} catch (NoSuchGameException e) {
			writer.println("This game no longer exists.  Try leaving it and joining another.");
			logger.error("Player " + player.getUsername() + " in game "
					+ player.getGameId()
					+ " experienced an error where the game no longer exists.",
					e);
		}
	}

}
