package com.pocketcookies.clue.service.server.mud.commands.look;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

import com.pocketcookies.clue.Board;
import com.pocketcookies.clue.GameData;
import com.pocketcookies.clue.PlayerData;
import com.pocketcookies.clue.Room;
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
	public void process(String command, MudPlayer player) {
		String[] arguments = command.split(" ");
		PrintWriter writer = player.getWriter();
		if (arguments.length == 1) {
			if (player.isInGame()) {
				writer.println("Room: "
						+ player.getPlayers().get(player.getUsername())
								.toString());
				writer.println("Exits:");
				for (Room r : Board.getAdjacentRooms(player.getPlayers().get(
						player.getUsername()))) {
					writer.println("\t" + r.toString());
				}
			} else {
				GameData[] games = player.getService().getGames();
				writer.println("Games: ");
				for (GameData g : games) {
					writer.println("\t" + g.getGameId() + "\t"
							+ g.getGameName());
					writer.println("\t\tPlayers(" + g.getPlayers().length + ")");
					for (PlayerData p : g.getPlayers()) {
						writer.println("\t\t\t" + p.getPlayerName() + "\t"
								+ p.getSuspect().toString());
					}
				}
			}
		}
		writer.flush();
	}
}
