package org.stormrealms.stormmenus.absraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.bukkit.entity.Player;
import org.stormrealms.stormmenus.MenuTemplate;

public abstract class BaseTypedMenu<T> extends BaseMenu {
	private Map<String, BiFunction<Player, T, Object>> placeholders = new ConcurrentHashMap<>();

	public BaseTypedMenu(String name, int size) {
		super(name, size);
	}

	public BaseTypedMenu(String name, int size, MenuTemplate template) {
		super(name, size, template);
	}

	public <U> void addPlaceholder(String key, BiFunction<Player, T, Object> replace) {
		placeholders.put(key, replace);
	}

	public Map<String, BiFunction<Player, T, Object>> getTypedPlaceholders() {
		return placeholders;
	}
}
