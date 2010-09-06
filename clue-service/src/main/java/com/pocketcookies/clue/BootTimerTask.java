package com.pocketcookies.clue;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.pocketcookies.clue.exceptions.NotInGameException;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.players.Player;

public class BootTimerTask extends TimerTask {

	private static final Logger logger = Logger.getLogger(BootTimerTask.class);
	private int gameId;

	public BootTimerTask(int gameId) {
		this.gameId = gameId;
	}

	@Override
	public void run() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Game g = (Game) session.load(Game.class, gameId);
		if (g == null) {
			logger.warn("The game with id "
					+ this.gameId
					+ " was deleted before we got a chance to boot the players.");
			return;
		}
		for (Player p : g.getPlayers()) {
			if (p != null) {
				try {
					g.leave(p.getUser().getKey());
				} catch (NotInGameException e) {
					logger.warn("Player " + p.getUser().getName()
							+ " was not in game " + this.gameId);
				}
			}
		}
		session.flush();
		session.delete(g);
		session.getTransaction().commit();
	}
}
