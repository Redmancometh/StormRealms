package org.stormrealms.stormmenus.controllers;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormmenus.listeners.MenuListeners;
import org.stormrealms.stormmenus.listeners.TypedMenuListeners;

@Controller
public class MenuController {
	@Autowired
	private TypedMenuListeners typedListeners;
	@Autowired
    private MenuListeners menuListeners;

	@PostConstruct
	public void registerMenus() {
		Bukkit.getPluginManager().registerEvents(typedListeners, StormCore.getInstance());
        Bukkit.getPluginManager().registerEvents(menuListeners, StormCore.getInstance());
    }
}
