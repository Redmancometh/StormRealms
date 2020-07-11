package org.stormrealms.stormcombat.configuration;

import org.bukkit.Bukkit;
import org.springframework.context.annotation.Bean;
import org.stormrealms.stormcore.StormCore;

public class StormCombatConfiguration {
	@Bean
	public StormCore stormCore() {
		return (StormCore) Bukkit.getPluginManager().getPlugin("StormCore");
	}
}
