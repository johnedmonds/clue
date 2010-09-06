package com.pocketcookies.clue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.pocketcookies.clue.Room;
import com.pocketcookies.clue.players.Suspect;

public class Board {
	private static final Map<Room, Collection<Room>> rooms = new HashMap<Room, Collection<Room>>();;
	private static final HashMap<Suspect, Room> startingPositions = new HashMap<Suspect, Room>();
	static {
		try {
			Element root;
			root = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(Board.class.getClassLoader().getResourceAsStream(
							"board.xml")).getDocumentElement();

			NodeList roomList = ((Element) root.getElementsByTagName("rooms")
					.item(0)).getElementsByTagName("room");
			for (int i = 0; i < roomList.getLength(); i++) {
				NodeList adjacentRoomList = ((Element) roomList.item(i))
						.getElementsByTagName("adjacent-room");
				ArrayList<Room> tempAdjacentRoomList = new ArrayList<Room>(
						adjacentRoomList.getLength());
				for (int j = 0; j < adjacentRoomList.getLength(); j++) {
					tempAdjacentRoomList.add(Room
							.valueOf(((Element) adjacentRoomList.item(j))
									.getAttribute("room")));
				}
				rooms.put(Room.valueOf(((Element) roomList.item(i))
						.getAttribute("room")), Collections
						.unmodifiableCollection(tempAdjacentRoomList));
			}
			NodeList startingDefinitions = ((Element) root
					.getElementsByTagName("starting-positions").item(0))
					.getElementsByTagName("start");
			for (int i = 0; i < startingDefinitions.getLength(); i++) {
				Element current = (Element) startingDefinitions.item(i);
				startingPositions.put(
						Suspect.valueOf(current.getAttribute("suspect")),
						Room.valueOf(current.getAttribute("room")));
			}
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static Collection<Room> getAdjacentRooms(Room room) {
		return rooms.get(room);
	}

	public static Room getStartingPosition(Suspect suspect) {
		return startingPositions.get(suspect);
	}
}
