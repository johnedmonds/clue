package com.pocketcookies.clue;

import java.util.TimerTask;

import org.hibernate.Hibernate;
import org.hibernate.classic.Session;
import org.hibernate.type.EnumType;

import com.pocketcookies.clue.hibernate.util.HibernateUtil;

/**
 * Deletes the game with the given id.
 * 
 * @author jack
 * 
 */
public class DeleteTimerTask extends TimerTask {

	private int gameId;

	public DeleteTimerTask(int gameId) {
		this.gameId = gameId;
	}

	@Override
	public void run() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.createQuery(
				"delete Game g where g.id = :id and (g.gameStartedState = :gameStartedState or (select count(*) from Player p where :id = p.id.gameId) = 0)")
				.setInteger("id", gameId)
				.setParameter(
						"gameStartedState",
						GameStartedState.ENDED,
						Hibernate
								.custom(EnumType.class,
										new String[] { "enumClass" },
										new String[] { GameStartedState.class
												.getName() })).executeUpdate();
		session.getTransaction().commit();
		System.out.println("Deleted game.");
	}

}
