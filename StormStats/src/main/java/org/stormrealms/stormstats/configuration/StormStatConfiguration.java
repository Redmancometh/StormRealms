package org.stormrealms.stormstats.configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormmenus.MenuTemplate;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.configuration.pojo.RaceConfig;
import org.stormrealms.stormstats.configuration.pojo.StatMiscConfig;
import org.stormrealms.stormstats.model.RPGPlayer;

@Configuration
public class StormStatConfiguration {

	@Bean
	public Set<StormPlugin> enabled() {
		Set<StormPlugin> enabledPlugins = new HashSet();
		return enabledPlugins;
	}

	@Bean(name = "needs-character")
	public List<UUID> characterless() {
		return new ArrayList();
	}

	@Bean(name = "player-cache")
	public Map<UUID, RPGPlayer> playerCache() {
		return new ConcurrentHashMap<UUID, RPGPlayer>();
	}

	@Bean("create-char-template")
	public MenuTemplate createCharTemplate(GUIConfig guiCfg) {
		return guiCfg.getCreateCharTemplate();
	}

	@Bean(name = "class-config")
	public ConfigManager<ClassConfiguration> config() {
		ConfigManager<ClassConfiguration> man = new ConfigManager("classes.json", ClassConfiguration.class);
		man.init();
		return man;
	}

	@Bean(name = "race-config")
	public ConfigManager<RaceConfig> raceConfig() {
		ConfigManager<RaceConfig> man = new ConfigManager("races.json", RaceConfig.class);
		man.init();
		return man;
	}

	@Bean(name = "race-cfg")
	@Scope("prototype")
	public RaceConfig raceCfg(@Qualifier("race-config") ConfigManager<RaceConfig> guiConfig) {
		return guiConfig.getConfig();
	}

	@Bean(name = "gui-config")
	public ConfigManager<GUIConfig> guiConfig() {
		ConfigManager<GUIConfig> man = new ConfigManager("statgui.json", GUIConfig.class);
		man.init();
		return man;
	}

	@Bean(name = "stat-config")
	public ConfigManager<StatMiscConfig> statConfig() {
		ConfigManager<StatMiscConfig> man = new ConfigManager("statsmisc.json", StatMiscConfig.class);
		man.init();
		return man;
	}

	@Bean(name = "gui-cfg")
	@Scope("prototype")
	public GUIConfig guiCfg(@Qualifier("gui-config") ConfigManager<GUIConfig> guiConfig) {
		return guiConfig.getConfig();
	}

}
