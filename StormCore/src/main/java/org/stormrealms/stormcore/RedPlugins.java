package org.stormrealms.stormcore;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RedPlugins implements Iterable<RedPlugin> {
	private static Map<Class<? extends RedPlugin>, RedPlugin> loadedMap = new ConcurrentHashMap();

	public static RedPlugin getInstance(Class<? extends RedPlugin> clazz) {
		return loadedMap.get(clazz);
	}

	public boolean isEnabled(Class<? extends RedPlugin> clazz) {
		return loadedMap.containsKey(clazz);
	}

	@Override
	public Iterator<RedPlugin> iterator() {
		return loadedMap.values().iterator();
	}

	@Override
	public void forEach(Consumer<? super RedPlugin> action) {
		loadedMap.values().forEach(action);
	}

	public void loadPlugin(RedPlugin plugin) {
		if (loadedMap.containsKey(plugin.getClass()))
			throw new IllegalStateException(
					"Tried to load plugin with class: " + plugin.getClass() + " while already loaded!");
		plugin.initialize();
		loadedMap.put(plugin.getClass(), plugin);
	}

	@SuppressWarnings("deprecation")
	public boolean loadPluginFromClass(Class<? extends RedPlugin> clazz) {
		if (loadedMap.containsKey(clazz))
			return false;
		try {
			loadedMap.put(clazz, clazz.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void unloadPlugin(Class<? extends RedPlugin> clazz) {
		loadedMap.remove(clazz);
	}

}
