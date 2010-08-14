package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.exceptions.GameAlreadyExistsException;
import com.pocketcookies.clue.exceptions.NotLoggedInException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

public class CreateCommand implements Command {

	private static final Logger logger = Logger.getLogger(CreateCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		Collection<String> ret = new LinkedList<String>();
		ret.add("create");
		return ret;
	}

	@Override
	public boolean process(String command, MudPlayer player) {
		String args[] = command.split(" ");
		PrintWriter writer = player.getWriter();
		if (args.length == 2) {
			try {
				player.getService().create(player.getKey(), args[1]);
				writer.println("Your game has been successfully created.");
				return true;
			} catch (NotLoggedInException e) {
				writer.println("Something went wrong and you're not logged in.  Try logging out and logging in again.");
				logger.error(
						"The player seems to have become logged out somehow.  This is probably a more serious problem whereby they have been deleted.  Though it might just be a bug in the way the MUD server stores keys.",
						e);
			} catch (GameAlreadyExistsException e) {
				writer.println("A game by that name already exists.  Try picking a different name.");
			}
			return false;
		}
		return false;
	}

}
