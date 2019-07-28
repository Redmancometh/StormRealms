package org.stormrealms.stormstats.controllers;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormstats.listeners.StatLoginListener;

@Controller
public class MasterController {
	@Autowired
	private StatLoginListener listener;

	@PostConstruct
	public void registerListener() {
		for (int x = 0; x < 2; x++)
			System.out.println("FOUND");
		Bukkit.getPluginManager().registerEvents(listener, Bukkit.getPluginManager().getPlugin("StormCore"));
	}
}
