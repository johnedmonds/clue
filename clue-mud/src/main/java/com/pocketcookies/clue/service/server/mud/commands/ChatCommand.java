package com.pocketcookies.clue.service.server.mud.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class ChatCommand implements Command {

	private static final Logger logger = Logger.getLogger(ChatCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays.asList(new String[] {
				"chat", "say" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		int index = command.indexOf(' ');
		if (index < 0)
			return;
		String message = command.substring(index + 1);
		try {
			player.getService().chat(player.getKey(), player.getGameId(),
					message);
		} catch (NotLoggedInException e) {
			logger.error("Player " + player.getKey() + " is not logged in.", e);
			player.getWriter()
					.println(
							"The server does not seem to think you are logged in.  Try logging out and back in again.");
		} catch (NoSuchGameException e) {
			logger.error(
					"Game " + player.getGameId() + " for player "
							+ player.getKey() + " no longer exists.", e);
			player.getWriter()
					.println(
							"The game you were in no longer exists.  Try leaving and joining again.");
		} catch (NotInGameException e) {
			logger.error("Player " + player.getKey() + " is not in game "
					+ player.getGameId() + ".", e);
			player.getWriter()
					.println(
							"The server thinks you are not in this game.  Try leaving and joining again.");
		}
	}

}
