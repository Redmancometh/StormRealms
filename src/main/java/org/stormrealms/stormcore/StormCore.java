package org.stormrealms.stormcore;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.stormrealms.StormCoreConfiguration;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.command.StormCommandHandler;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

@ComponentScan
public class StormCore extends JavaPlugin {
	private static ConfigManager<SpringConfig> cfgMon = new ConfigManager("spring.json", SpringConfig.class);
	protected AnnotationConfigApplicationContext context;

	private static StormCore instance;

	public void onEnable() {
		instance = this;
		this.context = new AnnotationConfigApplicationContext();
		cfgMon.init();
		SpringConfig cfg = cfgMon.getConfig();
		cfg.getProperties().forEach((key, value) -> {
			Logger.getLogger(StormCoreConfiguration.class.getName() + " Properties").info(key);
			Logger.getLogger(StormCoreConfiguration.class.getName() + " Properties").info(value + "");
		});
		this.context.setClassLoader(this.getClassLoader());
		this.context.register(StormCoreConfiguration.class);
		Map<String, Object> props = context.getEnvironment().getSystemProperties();
		cfg.getProperties().forEach((key, value) -> props.put(key, value));
		context.getEnvironment().setActiveProfiles(cfg.getProfiles().toArray(new String[cfg.getProfiles().size()]));
		this.context.refresh();
		getCommand("sc").setExecutor(context.getBean(ModuleCommand.class));
		Bukkit.getPluginManager().registerEvents(context.getBean(StormCommandHandler.class), this);
		Logger.getLogger(StormCoreConfiguration.class.getName()).info("StormCore has started!");
	}

	public static StormCore getInstance() {
		return instance;
	}

	public AnnotationConfigApplicationContext getContext() {
		return context;
	}
}
