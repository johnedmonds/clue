package com.pocketcookies.clue.service.server.mud.commands;

import java.util.Collection;
import java.util.LinkedList;

import com.pocketcookies.clue.service.server.mud.MudPlayer;
import com.pocketcookies.clue.service.server.mud.commands.look.LookCommand;

public class CommandProcessor {
	private static Collection<Command> commands = new LinkedList<Command>();

	public static void addCommand(Command c) {
		commands.add(c);
	}

	static {
		addCommand(new LookCommand());
		addCommand(new CreateCommand());
		addCommand(new JoinCommand());
		addCommand(new LeaveCommand());
		addCommand(new StartCommand());
		addCommand(new ChatCommand());
		addCommand(new MoveCommand());
		addCommand(new EndTurnCommand());
		addCommand(new SuggestCommand());
		addCommand(new AccuseCommand());
		addCommand(new DisproveCommand());
		addCommand(new CardsCommand());
	}

	/**
	 * Processes the given command. All state changes that need to occur will be
	 * handled by the command processing.
	 * 
	 * @param sCommand
	 *            The string the user gave to the server.
	 * @param player
	 *            The player instance. This may be necessary if process() needs
	 *            to change some state.
	 * @return Whether the command succeeded.
	 */
	public static void process(String sCommand, MudPlayer player) {
		String commandWord = sCommand.split(" ")[0];
		for (Command command : commands) {
			for (String commandAlias : command.getCommandAliases()) {
				if (commandAlias.equals(commandWord)) {
					command.process(sCommand, player);
					player.getWriter().flush();
				}
			}
		}
		player.getWriter().flush();
	}
}
