package com.pocketcookies.clue.service.server.mud.commands;

import java.util.Collection;
import java.util.LinkedList;

import com.pocketcookies.clue.service.server.mud.MudPlayer;

public interface Command {

	/**
	 * Returns all the aliases by which this command may go. It constructs a new
	 * collection before returning so you do not have to worry about modifying
	 * the returned value; it will not affect the commands held by this object.
	 * 
	 * @return A collection of aliases by which this command may go.
	 */
	public Collection<String> getCommandAliases();

	/**
	 * Processes the given command and returns whether the processing was
	 * successful.
	 * 
	 * All necessary state changes are handled by the command itself. You should
	 * never need to check the return value.
	 * 
	 * @param command
	 * @return True if the command succeeded and false otherwise.
	 */
	public boolean process(String command, MudPlayer player);
}
