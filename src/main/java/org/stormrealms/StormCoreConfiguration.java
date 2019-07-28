package org.stormrealms;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.event.Listener;
import org.json.simple.parser.JSONParser;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.storage.PluginStorage;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormcore", "org.stormrealms.stormcombat",
		"org.stormrealms.stormstats" })
@EnableJpaRepositories(basePackages = "org.stormrealms.stormcore.data")
@EntityScan(basePackages = { "org.stormrealms.stormcore.model", "org.stormrealms.stormstats.model" })
@EnableAutoConfiguration
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
	@Scope("singleton")
	public ExecutorService pool() {
		return Executors.newFixedThreadPool(7);
	}

	@Bean
	public ModuleCommand mainCommand() {
		return new ModuleCommand();
	}

	@Bean
	public JSONParser parser() {
		JSONParser parser = new JSONParser();
		return parser;
	}

	@Bean
	public StormCore plugin() {
		return StormCore.getInstance();
	}

}
