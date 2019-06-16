package org.stormrealms.stormcore;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.context.StormCoreConfiguration;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public class StormCore extends JavaPlugin {
	private static ConfigManager<SpringConfig> cfgMon = new ConfigManager("spring.json", SpringConfig.class);

	public void onEnable() {
		cfgMon.init();
		SpringConfig cfg = cfgMon.getConfig();
		SpringApplication application = new SpringApplication(StormCoreConfiguration.class);
		application.setDefaultProperties(cfg.getProperties());
		cfg.getProperties().forEach((key, value) -> {
			Logger.getLogger(StormCoreConfiguration.class.getName() + " Properties").info(key);
			Logger.getLogger(StormCoreConfiguration.class.getName() + " Properties").info(value + "");
		});
		ConfigurableEnvironment environment = new StandardEnvironment();
		environment.setActiveProfiles(cfg.getProfiles().toArray(new String[cfg.getProfiles().size()]));
		application.setEnvironment(environment);
		application.run();
		Logger.getLogger(StormCoreConfiguration.class.getName()).info("StormCore has started!");
	}
}
