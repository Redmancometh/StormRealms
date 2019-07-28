package org.stormrealms.stormcore.controller;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.data.SimpleEntityRepo;
import org.stormrealms.stormcore.model.SimpleEntity;

@Controller
public class DatabaseController {
	@Autowired
	private SimpleEntityRepo repo;

	@PostConstruct
	public void fetch() {
		SimpleEntity entity = new SimpleEntity();
		entity.setName("Test");
		UUID uuid = UUID.randomUUID();
		entity.setId(uuid);
		repo.save(entity);
		System.out.println(repo.findById(uuid));
	}
}
