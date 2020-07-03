package org.stormrealms.stormmenus;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.stormrealms.stormcore.StormPlugin;

public class Menus extends StormPlugin {
	private MenuManager menuManager;

	@Override
	public void enable() {
		super.enable();
	}

	public static Menus getInstance() {
		return (Menus) Bukkit.getPluginManager().getPlugin("StormMenus");
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	@Override
	public Set<Listener> listeners() {
		Set<Listener> listeners = new HashSet();
		return listeners;
	}

}
