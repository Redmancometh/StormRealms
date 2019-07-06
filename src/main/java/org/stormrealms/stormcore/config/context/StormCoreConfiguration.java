package org.stormrealms.stormcore.config.context;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
@ComponentScan(basePackages = { "org.stormrealms.stormcore", "org.stormrealms.stormcombat",
		"org.stormrealms.stormstats" })
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
	public JSONParser parser() {
		JSONParser parser = new JSONParser();
		return parser;
	}

	@Bean
	public JSONObject hibernateConfig(JSONParser parser) {
		JavaPlugin stormCore = (JavaPlugin) Bukkit.getPluginManager().getPlugin("StormCore");
		File hibernateConfig = new File(stormCore.getDataFolder(), "config.json");
		if (!hibernateConfig.exists())
			stormCore.saveResource("config.json", true);
		try (FileReader scanner = new FileReader(hibernateConfig)) {
			return (JSONObject) parser.parse(scanner);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Configuration not initialized properly. Either config.json is missing, corrupted, or ill-formatted");
		}
	}

	@Bean
	public SessionFactory buildSessionFactory(JSONObject jsonConfig) {
		JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("StormCore");
		File hibernateConfig = new File(plugin.getDataFolder(), "hibernate.cfg.xml");
		if (!hibernateConfig.exists())
			plugin.saveResource("hibernate.cfg.xml", true);
		org.hibernate.cfg.Configuration config = new org.hibernate.cfg.Configuration().configure(hibernateConfig);
		JSONObject dbConfig = (JSONObject) jsonConfig.get("DB");
		config.setProperty("hibernate.hikari.dataSource.user", dbConfig.get("user").toString());
		config.setProperty("hibernate.hikari.dataSource.password", dbConfig.get("password").toString());
		config.setProperty("hibernate.hikari.dataSource.url", dbConfig.get("url").toString());
		SessionFactory sessionFactory = config.buildSessionFactory();
		return sessionFactory;
	}

	@Bean
	public StormCore plugin() {
		return StormCore.getInstance();
	}
}
