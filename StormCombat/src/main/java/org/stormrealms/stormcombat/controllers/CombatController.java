package org.stormrealms.stormcombat.controllers;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcombat.listeners.CombatListeners;
import org.stormrealms.stormcore.StormCore;

@Controller
public class CombatController {
	@Autowired
	private CombatListeners listeners;

	@PostConstruct
	public void registerListeners() {
		Bukkit.getPluginManager().registerEvents(listeners, StormCore.getInstance());
	}
}
