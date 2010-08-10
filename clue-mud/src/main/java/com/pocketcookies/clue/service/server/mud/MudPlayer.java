package com.pocketcookies.clue.service.server.mud;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.pocketcookies.clue.service.server.ClueServiceAPI;

public class MudPlayer implements Runnable {

	private Socket client;
	private static final Logger logger = Logger.getLogger(MudPlayer.class);
	private PrintWriter writer;
	private BufferedReader reader;
	private String key = null;
	private ClueServiceAPI service;
	private Point location = new Point();

	public MudPlayer(Socket client, ClueServiceAPI service) {
		this.client = client;
		this.service = service;
	}

	@Override
	public void run() {
		try {
			writer = new PrintWriter(client.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));

			// TODO: Description
			writer.println("Welcome to Clue.  Enter your username (or pick one if you're new).");
			writer.flush();
			while (this.key == null) {
				writer.print("Username: ");
				writer.flush();
				String username = reader.readLine();
				writer.print("Password: ");
				writer.flush();
				String password = reader.readLine();
				this.key = service.login(username, password);
				if (this.key == null) {
					writer.println("That username is already taken (or you entered the wrong password).");
				} else
					writer.println(this.key);
			}
			writer.flush();
			writer.close();
			client.close();
		} catch (IOException e) {
			logger.error(
					"There was an error relating to the output stream of the client socket.",
					e);
		}
	}

	public ClueServiceAPI getService() {
		return this.service;
	}

	public PrintWriter getWriter() {
		return this.writer;
	}
}
