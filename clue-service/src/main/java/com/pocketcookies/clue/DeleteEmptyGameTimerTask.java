package com.pocketcookies.clue;

import java.util.TimerTask;

import org.hibernate.classic.Session;

import com.pocketcookies.clue.hibernate.util.HibernateUtil;

/**
 * Deletes the game with the given id.
 * 
 * @author jack
 * 
 */
public class DeleteEmptyGameTimerTask extends TimerTask {

	private int gameId;

	public DeleteEmptyGameTimerTask(int gameId) {
		this.gameId = gameId;
	}

	@Override
	public void run() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.createQuery(
				"delete Game g where g.id = :id and (select count(*) from Player p where :id = p.id.gameId) = 0")
				.setInteger("id", gameId).executeUpdate();
		session.getTransaction().commit();
	}
}
