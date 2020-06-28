package org.stormrealms.stormmenus.absraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.stormrealms.stormmenus.MenuTemplate;

import net.md_5.bungee.api.ChatColor;

public abstract class BaseMenu {
	private Map<String, Function<Player, String>> placeholders = new ConcurrentHashMap<>();
	private String name;
	private boolean lowerMenu = false;
	private MenuTemplate template;
	private boolean allowLower = false;
	private int size = 18;

	public BaseMenu(String name, int size) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.size = size;
	}

	public BaseMenu(String name, int size, MenuTemplate template) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.template = template;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
	}

	public MenuTemplate getTemplate() {
		return template;
	}

	public void setTemplate(MenuTemplate template) {
		this.template = template;
	}

	public boolean isLowerMenu() {
		return lowerMenu;
	}

	public void setLowerMenu(boolean lowerMenu) {
		this.lowerMenu = lowerMenu;
	}

	public boolean allowsClickLower() {
		return allowLower;
	}

	public void setAllowClickLower(boolean letClickLower) {
		this.allowLower = letClickLower;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void addPlaceholder(String key, Function<Player, String> replace) {
		placeholders.put(key, replace);
	}

	public Map<String, Function<Player, String>> getPlaceholders() {
		return placeholders;
	}
}
