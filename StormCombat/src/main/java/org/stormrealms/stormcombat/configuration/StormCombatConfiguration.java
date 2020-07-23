package org.stormrealms.stormcombat.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcombat.configuration.pojo.CombatGUIConfig;
import org.stormrealms.stormcombat.configuration.pojo.RegenConfig;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.outfacing.RPGStat;

@Configuration
public class StormCombatConfiguration {
	@Bean("stat-cache")
	public Map<UUID, Map<RPGStat, Integer>> statCache() {
		//this may end up needing a concurrent hashmap
		return new HashMap();
	}

	@Bean
	public ConfigManager<RegenConfig> cfgMan() {
		ConfigManager<RegenConfig> cfgMan = new ConfigManager("regen.json", RegenConfig.class);
		cfgMan.init();
		return cfgMan;
	}

	@Bean("combat-cfg-man")
	public ConfigManager<CombatGUIConfig> comCfgMan() {
		ConfigManager<CombatGUIConfig> cfgMan = new ConfigManager("combatgui.json", CombatGUIConfig.class);
		cfgMan.init();
		return cfgMan;
	}
}
