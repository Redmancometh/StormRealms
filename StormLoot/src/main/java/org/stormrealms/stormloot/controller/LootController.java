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
		System.out.println("INITIALIZING BASIC LOOT CONTROLLER LISTENER!");
		Bukkit.getPluginManager().registerEvents(listener, StormCore.getInstance());
	}
}
