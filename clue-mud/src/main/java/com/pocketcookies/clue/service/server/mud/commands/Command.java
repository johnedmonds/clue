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
	public abstract Collection<String> getCommandAliases();

	/**
	 * Processes the given command and returns whether the processing was
	 * successful.
	 * 
	 * This function will be given the entire string the user entered including
	 * the command. This will allow us to roll up different commands into
	 * aliases. Movement (north, northeast, ...) for example, will not need to
	 * be handled by 8 classes and can instead be handled by one.
	 * 
	 * All necessary state changes are handled by the command itself. You should
	 * never need to check the return value.
	 * 
	 * @param command
	 * @return True if the command succeeded and false otherwise.
	 */
	public abstract void process(String command, MudPlayer player);
}
