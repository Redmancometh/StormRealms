package org.stormrealms.stormloot.controller;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormloot.listeners.DropListener;

@Controller
public class LootController {
	@Autowired
	private DropListener listener;

	@PostConstruct
	public void registerListener() {
		for (int x = 0; x < 10; x++)
			System.out.println("REGISTER LISTENER");
		Bukkit.getPluginManager().registerEvents(listener, StormCore.getInstance());
	}
}
