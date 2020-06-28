package org.stormrealms.stormcore.storage;

import java.util.Map;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;

import com.google.common.collect.Multimap;

public class PluginStorage {
	@Autowired
	@Qualifier("context-storage")
	private Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts;

	/**
	 * Will use these just in case we need to read them in some way.
	 */
	@Autowired
	@Qualifier("listener-storage")
	private Multimap<Class<? extends StormPlugin>, Listener> listenerMap;
	@Autowired
	@Qualifier("command-storage")
	private Multimap<Class<? extends StormPlugin>, ModuleCommand> commandMap;

	public void registerPlugin(Class<? extends StormPlugin> pluginClass, AnnotationConfigApplicationContext context,
			Set<Listener> listeners, Set<ModuleCommand> commands) {
		contexts.put(pluginClass, context);
		listenerMap.putAll(pluginClass, listeners);
		commandMap.putAll(pluginClass, commands);
	}

	/**
	 * 
	 * @param pluginClass
	 * @param typeNeeded
	 * @return
	 * 
	 *         TODO: Add error handling for the plugin not being found
	 * 
	 */
	public <T> T autowireByClass(Class pluginClass, Class<T> typeNeeded) {
		return contexts.get(pluginClass).getAutowireCapableBeanFactory().getBean(typeNeeded);
	}

	/**
	 * 
	 * @param pluginClass   The plugin who's context you want to pull from
	 * @param expectedClass The expected return type
	 * @param name          The bean name as specified in the bean configuration
	 * @return
	 * 
	 *         TODO: Need to add error handling for the plugin not being loaded, or
	 *         for the bean not being of the expected class.
	 */
	public <T> T autowireByName(Class pluginClass, Class<T> expectedClass, String name) {
		return (T) contexts.get(pluginClass).getAutowireCapableBeanFactory().getBean(name);
	}
}
