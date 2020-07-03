package org.stormrealms.stormstats.configuration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.model.RPGPlayer;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormstats.listeners" })
public class StormStatConfiguration {

	@Bean(name = "player-cache")
	public Map<UUID, RPGPlayer> playerCache() {
		return new ConcurrentHashMap<UUID, RPGPlayer>();
	}

	@Bean(name = "class-config")
	public ConfigManager<ClassConfiguration> config() {
		ConfigManager<ClassConfiguration> man = new ConfigManager("classes.json", ClassConfiguration.class);
		man.init();
		System.out.print("CFG INITIAL " + man.getConfig());
		return man;
	}

}
