package com.pocketcookies.clue.service.server.mud.commands;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.Card;
import com.pocketcookies.clue.exceptions.NoSuchGameException;
import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.service.server.mud.MudPlayer;

/**
 * Displays the user's cards.
 * 
 * @author jack
 * 
 */
public class CardsCommand implements Command {

	Logger logger = Logger.getLogger(CardsCommand.class);

	@Override
	public Collection<String> getCommandAliases() {
		return Collections.unmodifiableCollection(Arrays
				.asList(new String[] { "cards" }));
	}

	@Override
	public void process(String command, MudPlayer player) {
		PrintWriter writer = player.getWriter();
		try {
			Card[] cards = player.getService().getCards(player.getKey(),
					player.getGameId());
			if (cards == null)
				writer.println("You do not have any cards yet.");
			else {
				writer.println("Your cards:");
				for (Card c : cards) {
					writer.println("\t" + c.toString());
				}
			}
		} catch (NoSuchGameException e) {
			logger.error("The player thinks there is a game but there is not.",
					e);
			writer.println("The game you were in no longer exists.");
		} catch (NotInGameException e) {
			logger.error("The player was not in the game.", e);
			writer.println("The server seems to think you are not in this game.  Try leaving and rejoining.");
		}
	}
}
