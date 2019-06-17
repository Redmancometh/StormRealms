package org.stormrealms.stormcore;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.context.StormCoreConfiguration;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

@ComponentScan
public class StormCore extends JavaPlugin {
	private static ConfigManager<SpringConfig> cfgMon = new ConfigManager("spring.json", SpringConfig.class);
	protected AnnotationConfigApplicationContext context;

	public void onEnable() {
		this.context = new AnnotationConfigApplicationContext();
		cfgMon.init();
		SpringConfig cfg = cfgMon.getConfig();
		cfg.getProperties().forEach((key, value) -> {
			Logger.getLogger(StormCoreConfiguration.class.getName() + " Properties").info(key);
			Logger.getLogger(StormCoreConfiguration.class.getName() + " Properties").info(value + "");
		});
		this.context.setClassLoader(StormCore.class.getClassLoader());
		this.context.register(StormCoreConfiguration.class);
		this.context.refresh();
		Map<String, Object> props = context.getEnvironment().getSystemProperties();
		cfg.getProperties().forEach((key, value) -> props.put(key, value));
		context.getEnvironment().setActiveProfiles(cfg.getProfiles().toArray(new String[cfg.getProfiles().size()]));

		Logger.getLogger(StormCoreConfiguration.class.getName()).info("StormCore has started!");
	}
}
