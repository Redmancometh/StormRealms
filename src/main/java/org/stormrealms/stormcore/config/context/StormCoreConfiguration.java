package org.stormrealms.stormcore.config.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.storage.PluginStorage;

@Configuration
public class StormCoreConfiguration {
	@Bean
	public PluginStorage pluginStorage() {
		return new PluginStorage();
	}

	@Bean(name = "context-storage")
	public Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts() {
		return new ConcurrentHashMap();
	}
}
