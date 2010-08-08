package com.pocketcookies.clue.service.server.mud;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

public class MudPlayer implements Runnable {

	private Socket client;
	private static final Logger logger = Logger.getLogger(MudPlayer.class);

	public MudPlayer(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		try {
			PrintWriter writer = new PrintWriter(client.getOutputStream());
			writer.println("Hello client.  This is just a test to see if everything worked so we're going to close the connection now.");
			client.close();
		} catch (IOException e) {
			logger.error(
					"There was an error relating to the output stream of the client socket.",
					e);
		}
	}
}
