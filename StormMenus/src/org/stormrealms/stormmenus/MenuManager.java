package org.stormrealms.stormmenus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.absraction.Menu;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TextPrompt;

@Component
public class MenuManager {
	/**
	 * These are mapped by title
	 */
	protected static Map<UUID, TypedMenu> typedMap = new ConcurrentHashMap();
	protected static Map<UUID, Menu> menuMap = new ConcurrentHashMap();
	protected static Map<UUID, TextPrompt> promptMap = new ConcurrentHashMap<>();
	/*
	 * public Promise<String> prompt(String title, String defaultInput, Player
	 * player) { TextPrompt textPrompt = factory.getBean(TextPrompt.class, title,
	 * defaultInput, player); return textPrompt.show(); }
	 */

	public Map<UUID, TypedMenu> map() {
		return typedMap;
	}

	public boolean playerHasTypedMenuOpen(UUID uuid) {
		return typedMap.containsKey(uuid);
	}

	public TypedMenu getTypedMenuFromUUID(UUID uuid) {
		return typedMap.get(uuid);
	}

	public void setPlayerMenu(UUID uuid, Menu menu) {
		menuMap.put(uuid, menu);
	}

	public TypedMenu setTypedPlayerMenu(UUID title, TypedMenu menu) {
		return typedMap.put(title, menu);
	}

	public Menu getMenuFromTitle(UUID uuid) {
		return menuMap.get(uuid);
	}

	public boolean manager(UUID uuid) {
		return menuMap.containsKey(uuid);
	}

	public boolean playerHasMenuOpen(UUID uuid) {
		return menuMap.containsKey(uuid);
	}

	public TextPrompt getPlayerTextPrompt(UUID uuid) {
		return promptMap.get(uuid);
	}

	public TextPrompt setPlayerTextPrompt(UUID uuid, TextPrompt textPrompt) {
		return promptMap.put(uuid, textPrompt);
	}

	public void closeMenus(UUID uuid) {
		menuMap.remove(uuid);
		typedMap.remove(uuid);
	}

	public boolean playerHasPromptOpen(UUID uuid) {
		return promptMap.containsKey(uuid);
	}
}
