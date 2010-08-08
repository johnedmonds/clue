package com.pocketcookies.clue.service.server.mud.commands.look;

import java.util.Collection;
import java.util.LinkedList;

import com.pocketcookies.clue.service.server.mud.MudPlayer;
import com.pocketcookies.clue.service.server.mud.commands.Command;

public class LookCommand implements Command {

	@Override
	public Collection<String> getCommandAliases() {
		Collection<String> aliases = new LinkedList<String>();
		aliases.add("look");
		aliases.add("l");
		return aliases;
	}

	@Override
	public boolean process(String command, MudPlayer player) {
		// TODO Auto-generated method stub
		return false;
	}

}
