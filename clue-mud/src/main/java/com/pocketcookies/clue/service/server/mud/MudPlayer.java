package com.pocketcookies.clue.service.server.mud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

public class MudPlayer implements Runnable {

	private Socket client;
	private static final Logger logger = Logger.getLogger(MudPlayer.class);
	private PrintWriter writer;
	private BufferedReader reader;

	public MudPlayer(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		try {
			writer = new PrintWriter(client.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			writer.println("Hello client.  This is just a test to see if everything worked.  Send us something and we'll tell you what you sent.  Then we'll close the connection.");
			writer.flush();
			writer.println("You said: \"" + reader.readLine()
					+ ".\"  Have a good day.");
			writer.flush();
			writer.close();
			client.close();
		} catch (IOException e) {
			logger.error(
					"There was an error relating to the output stream of the client socket.",
					e);
		}
	}
}
