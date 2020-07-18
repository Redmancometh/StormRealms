package org.stormrealms.stormmenus.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TODO: Refactor all the names.
 * 
 * @author Redmancometh
 *
 * @param <T>
 */
public class TypedSelector<T> {
	private Map<UUID, T> selectorMap = new HashMap();

	public T get(UUID uuid) {
		return selectorMap.get(uuid);
	}

	public T getSelected(UUID uuid) {
		return selectorMap.get(uuid);
	}

	public void deSelect(UUID uuid) {
		selectorMap.remove(uuid);
	}

	public T select(UUID uuid, T e) {
		return selectorMap.put(uuid, e);
	}

	public Map<UUID, T> getSelectorMap() {
		return selectorMap;
	}

	public void setSelectorMap(Map<UUID, T> selectorMap) {
		this.selectorMap = selectorMap;
	}

}
