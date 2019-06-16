package org.stormrealms.stormstats.configuration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormstats.model.RPGPlayer;

@Configuration
public class StormStatConfiguration {
	@Bean(name = "player-cache")
	public Map<UUID, RPGPlayer> playerCache() {
		return new ConcurrentHashMap<UUID, RPGPlayer>();
	}
}
