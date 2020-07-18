package org.stormrealms.stormcore.controller;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.listeners.CancelListeners;

@Controller
public class CancelController {
	@Autowired
	private CancelListeners cancels;

	@PostConstruct
	public void registerListener() {
		Bukkit.getPluginManager().registerEvents(cancels, StormCore.getInstance());
	}
}
