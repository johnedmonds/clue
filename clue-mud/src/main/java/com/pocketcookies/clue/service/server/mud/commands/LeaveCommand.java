package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.service.server.ClueServiceAPI;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class LeaveCommand implements Command {

	private static final Logger logger = Logger.getLogger(LeaveCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		Collection<String> ret = new LinkedList<String>();
		ret.add("leave");
		return ret;
	}

	@Override
	public void process(String command, MudPlayer player) {
		PrintWriter writer = player.getWriter();
		ClueServiceAPI service = player.getService();
		if (!player.isInGame()) {
			writer.println("You must join a game before you can leave one.");
			return;
		}
		try {
			service.leave(player.getKey(), player.getGameId());
			player.leave();
			player.stopMessageConnection();
			writer.println("You have successfully left the game.");
		} catch (NotLoggedInException e) {
			logger.error("Player with key " + player.getKey() + " in game "
					+ player.getGameId() + " is not logged in.", e);
			writer.println("The server doesn't seem to have you logged in.  This is probably our fault.  Try logging out and then back in again.");
		} catch (NoSuchGameException e) {
			logger.warn(
					"Player with key "
							+ player.getKey()
							+ " was in game "
							+ player.getGameId()
							+ " and tried to leave it.  However, the game did not exist.",
					e);
			player.leave();
			writer.println("The server experienced an error while trying to leave the game.  It seems it never existed.  Regardless, you are no longer in any game and may join another.");
		} catch (NotInGameException e) {
			logger.warn("Player with key " + player.getKey()
					+ " tried to leave game " + player.getGameId()
					+ " but that player was not in the game.");
			writer.println("The server does not think you were ever in a game.  Do not worry.  You may join another game now.");
		}
	}

}
