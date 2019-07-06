package org.stormrealms.stormcore.controller;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.model.SimpleEntity;

@Controller
public class DatabaseController {
	@Autowired
	private SessionFactory sessions;

	@PostConstruct
	public void getSession() {
		System.out.println("GETTING SESSION");
		try (Session session = sessions.openSession()) {
			try {
				session.beginTransaction();
				SimpleEntity e = session.get(SimpleEntity.class, UUID.randomUUID());
				System.out.println("Fetched: " + e);
				session.getTransaction().commit();
				session.flush();
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}
	}

}
