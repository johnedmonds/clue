package com.pocketcookies.clue.service.server.mud.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.exceptions.NotYourTurnException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class EndTurnCommand implements Command {

	private static final Logger logger = Logger.getLogger(EndTurnCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays.asList(new String[] {
				"end", "next" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		try {
			player.getService().endTurn(player.getKey(), player.getGameId());
		} catch (NotLoggedInException e) {
			player.getWriter()
					.println(
							"The server seems to think that you are not logged in.  Try logging out and back in again.");
			logger.error("Player " + player.getKey() + " is not logged in.", e);
		} catch (NotYourTurnException e) {
			player.getWriter().println(
					"You can end your turn when it is your turn.");
		} catch (NoSuchGameException e) {
			player.getWriter()
					.println(
							"This game does not exist anymore.  Try leaving and joining another game.");
			logger.error("Player " + player.getKey()
					+ " experienced an error. That player is not in game "
					+ player.getGameId());
		}
	}
}
