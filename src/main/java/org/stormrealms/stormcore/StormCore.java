package org.stormrealms.stormcore;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.context.StormCoreConfiguration;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormcore.controller.ModuleLoaderController;

public class StormCore extends JavaPlugin {
	private static ConfigManager<SpringConfig> cfgMon = new ConfigManager("spring.json", SpringConfig.class);
	protected AnnotationConfigApplicationContext context;
	private ModuleLoaderController moduleLoaderController;


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
		this.moduleLoaderController = context.getAutowireCapableBeanFactory().getBean(ModuleLoaderController.class);

		Map<String, Object> props = context.getEnvironment().getSystemProperties();
		cfg.getProperties().forEach((key, value) -> props.put(key, value));
		context.getEnvironment().setActiveProfiles(cfg.getProfiles().toArray(new String[cfg.getProfiles().size()]));

		Logger.getLogger(StormCoreConfiguration.class.getName()).info("StormCore has started!");

		this.moduleLoaderController.loadModules();
	}
}
