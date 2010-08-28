package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.exceptions.CheatException;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class DisproveCommand implements Command {

	private static final Logger logger = Logger
			.getLogger(DisproveCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays
				.asList(new String[] { "disprove" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		String args[] = command.toUpperCase().split(" ");
		PrintWriter writer = player.getWriter();
		if (args.length != 2) {
			writer.println("You need to say which card you are using to disprove.");
			return;
		}
		Card disprovingCard = null;
		try {
			disprovingCard = Card.valueOf(args[1]);
		} catch (IllegalArgumentException e) {
			writer.println(args[1] + " is not a valid card.");
			return;
		}
		try {
			player.getService().disprove(player.getKey(), player.getGameId(),
					disprovingCard);
		} catch (NotLoggedInException e) {
			writer.println("The server does not seem to think you are logged in.  Try logging out and then back in again.");
			logger.error("Player " + player.getUsername()
					+ " is not logged in.", e);
		} catch (NotYourTurnException e) {
			writer.println("You cannot disprove unless someone has made a suggestion and you are the one who is supposed to be disproving it.");
		} catch (NoSuchGameException e) {
			writer.println("This game has ceased to exist.  Try leaving it and joining another.");
			logger.error("Player " + player.getUsername()
					+ " experienced an error whereby game "
					+ player.getGameId() + " has ceased to exist.");
		} catch (CheatException e) {
			writer.println("That card was never proposed.  Try another.");
		}
	}

}
