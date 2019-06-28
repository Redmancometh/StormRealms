package org.stormrealms.stormcore.config.context;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.Listener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.storage.PluginStorage;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Configuration
@ComponentScan(basePackages = "org.stormrealms.stormcore")
public class StormCoreConfiguration {
	@Bean
	public PluginStorage pluginStorage() {
		return new PluginStorage();
	}

	@Bean(name = "context-storage")
	public Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts() {
		return new ConcurrentHashMap();
	}

	@Bean(name = "listener-storage")
	public Multimap<Class<? extends StormPlugin>, Listener> listeners() {
		return Multimaps.synchronizedSetMultimap(HashMultimap.<Class<? extends StormPlugin>, Listener>create());
	}

	@Bean(name = "command-storage")
	public Multimap<Class<? extends StormPlugin>, ModuleCommand> commands() {
		return Multimaps.synchronizedSetMultimap(HashMultimap.<Class<? extends StormPlugin>, ModuleCommand>create());
	}

	@Bean(name = "modules-dir")
	public File moduleDir() {
		return new File("plugins/StormCore/modules");
	}

	@Bean
	public ModuleCommand mainCommand() {
		return new ModuleCommand();
	}

	@Bean
	public StormCore plugin() {
		return StormCore.getInstance();
	}
}
