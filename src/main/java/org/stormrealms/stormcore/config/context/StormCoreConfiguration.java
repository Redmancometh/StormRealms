package org.stormrealms.stormcore.config.context;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.storage.PluginStorage;

@Configuration
@ComponentScan
public class StormCoreConfiguration {
	@Bean
	public PluginStorage pluginStorage() {
		return new PluginStorage();
	}

	@Bean(name = "context-storage")
	public Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts() {
		return new ConcurrentHashMap();
	}

	@Bean(name = "modules-dir")
	public File moduleDir() {
		return new File("plugins/StormCore/modules");
	}
}
