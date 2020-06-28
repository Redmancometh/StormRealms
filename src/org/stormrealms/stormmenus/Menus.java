package org.stormrealms.stormmenus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.stormrealms.stormmenus.listeners.MenuListeners;
import org.stormrealms.stormmenus.listeners.TypedMenuListeners;

public class Menus extends JavaPlugin {
	private MenuManager menuManager;

	@Override
	public void onEnable() {
		setMenuManager(new MenuManager());
		/**
		 * This is done to ensure reloadability, but also compartmentalize the menu
		 * implementations to their respective plugins.
		 */
		Bukkit.getPluginManager().registerEvents(new MenuListeners(), this);
		Bukkit.getPluginManager().registerEvents(new TypedMenuListeners(), this);
	}

	@Override
	public void onDisable() {
		/**
		 * Tell all plugins to unregister their menus before ArkhamMenus shuts down to
		 * prevent deep GC roots.
		 */
		super.onDisable();
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
}
