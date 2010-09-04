package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

/**
 * Displays the user's cards.
 * 
 * @author jack
 * 
 */
public class CardsCommand implements Command {

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays
				.asList(new String[] { "cards" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		PrintWriter writer = player.getWriter();
		if (player.getCards() == null)
			writer.println("You do not have any cards yet.");
		else {
			writer.println("Your cards:");
			for (Card c : player.getCards()) {
				writer.println("\t" + c.toString());
			}
		}
	}
}
