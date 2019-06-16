package org.stormrealms.stormcore.storage;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormPlugin;

public class PluginStorage {
	@Autowired
	@Qualifier("context-storage")
	private Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts;

	/**
	 * 
	 * @param pluginClass
	 * @param typeNeeded
	 * @return
	 * 
	 * 		TODO: Add error handling for the plugin not being found
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
	 * 		TODO: Need to add error handling for the plugin not being loaded, or
	 *         for the bean not being of the expected class.
	 */
	public <T> T autowireByName(Class pluginClass, Class<T> expectedClass, String name) {
		return (T) contexts.get(pluginClass).getAutowireCapableBeanFactory().getBean(name);
	}
}
