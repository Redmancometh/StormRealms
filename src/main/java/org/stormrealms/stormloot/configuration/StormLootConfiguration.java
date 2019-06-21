package org.stormrealms.stormloot.configuration;

import org.bukkit.Bukkit;
import org.springframework.context.annotation.Bean;
import org.stormrealms.stormcore.StormCore;

public class StormLootConfiguration {
	@Bean
	public StormCore stormCore() {
		return (StormCore) Bukkit.getPluginManager().getPlugin("StormCore");
	}
}
