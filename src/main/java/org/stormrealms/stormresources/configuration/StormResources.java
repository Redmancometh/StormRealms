package org.stormrealms.stormresources.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormresources.configuration.pojo.HerbsConfiguration;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormstats.listeners", "org.stormrealms.stormstats.data" })
public class StormResources {

	@Bean(name = "gui-config")
	public ConfigManager<HerbsConfiguration> resources() {
		ConfigManager<HerbsConfiguration> man = new ConfigManager("resources.json", HerbsConfiguration.class);
		man.init();
		System.out.print("CFG INITIAL " + man.getConfig());
		return man;
	}

}
