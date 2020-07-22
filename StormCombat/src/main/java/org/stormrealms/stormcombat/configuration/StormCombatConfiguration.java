package org.stormrealms.stormcombat.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcombat.configuration.pojo.CombatGUIConfig;
import org.stormrealms.stormcombat.configuration.pojo.RegenConfig;
import org.stormrealms.stormcore.config.ConfigManager;

@Configuration
public class StormCombatConfiguration {
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
