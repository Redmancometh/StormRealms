package org.stormrealms.stormmobs.config.context;

import org.bukkit.Bukkit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.StormCore;

@Configuration
public class StormMobContext {
	@Bean
	public StormCore stormCore() {
		return (StormCore) Bukkit.getPluginManager().getPlugin("StormCore");
	}
}
