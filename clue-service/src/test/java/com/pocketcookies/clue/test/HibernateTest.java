package com.pocketcookies.clue.test;

import java.util.Random;

import junit.framework.TestCase;

import com.pocketcookies.clue.Game;
import com.pocketcookies.clue.User;
import com.pocketcookies.clue.exceptions.AlreadyJoinedException;
import com.pocketcookies.clue.exceptions.GameStartedException;
import com.pocketcookies.clue.exceptions.NotEnoughPlayersException;
import com.pocketcookies.clue.exceptions.SuspectTakenException;
import com.pocketcookies.clue.hibernate.util.HibernateUtil;
import com.pocketcookies.clue.players.Suspect;

public class HibernateTest extends TestCase {

	public void testCreateUser() throws NotEnoughPlayersException,
			SuspectTakenException, GameStartedException, AlreadyJoinedException {
		HibernateUtil.getSessionFactory().getCurrentSession()
				.beginTransaction();
		Game g = new Game("Test");
		HibernateUtil.getSessionFactory().getCurrentSession().save(g);
		User u1 = new User("a", "b");
		User u2 = new User("c", "d");
		User u3 = new User("e", "f");
		HibernateUtil.getSessionFactory().getCurrentSession().save(u1);
		HibernateUtil.getSessionFactory().getCurrentSession().save(u2);
		HibernateUtil.getSessionFactory().getCurrentSession().save(u3);
		g.join(u1, Suspect.SCARLETT);
		g.join(u2, Suspect.GREEN);
		g.join(u3, Suspect.WHITE);
		HibernateUtil.getSessionFactory().getCurrentSession().flush();
		g.start(new Random(1));
		HibernateUtil.getSessionFactory().getCurrentSession().getTransaction()
				.commit();
	}
}
