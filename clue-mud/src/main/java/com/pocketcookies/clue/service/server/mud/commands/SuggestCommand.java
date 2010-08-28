package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotInRoomException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class SuggestCommand implements Command {

	private static final Logger logger = Logger.getLogger(SuggestCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays
				.asList(new String[] { "suggest" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		String args[] = command.toUpperCase().split(" ");
		PrintWriter writer = player.getWriter();
		if (args.length != 3) {
			writer.println("That was invalid.  Use suggest like so: suggest <suspect> <weapon>");
			return;
		}
		Card suspect, weapon;
		try {
			suspect = Card.valueOf(args[1]);
		} catch (IllegalArgumentException e) {
			writer.println("No such suspect or weapon: " + args[1]);
			return;
		}
		try {
			weapon = Card.valueOf(args[2]);
		} catch (IllegalArgumentException e) {
			writer.println("No such suspect or weapon: " + args[2]);
			return;
		}
		// Swap the suspect and weapon if necessary.
		if (suspect.isWeapon()) {
			Card temp = suspect;
			suspect = weapon;
			weapon = temp;
		}
		if (!suspect.isSuspect() || !weapon.isWeapon()) {
			writer.println("You must suggest a suspect and a weapon (the room will be chosen by the room in which you are standing.");
			return;
		}

		try {
			player.getService().suggest(player.getKey(), player.getGameId(),
					suspect, weapon);
		} catch (NotLoggedInException e) {
			writer.println("The server does not think you are logged in.  Try logging out and then logging in again.");
			logger.error("Player " + player.getUsername()
					+ " is not logged in.");
		} catch (NotYourTurnException e) {
			writer.println("It is not your turn.");
		} catch (NoSuchGameException e) {
			writer.println("This game no longer exists.  Try leaving and joining another game");
			logger.error("Player " + player.getUsername() + " was not in game "
					+ player.getGameId());
		} catch (NotInRoomException e) {
			writer.println("You need to be in a room to make a suggestion.");
		}
	}

}
