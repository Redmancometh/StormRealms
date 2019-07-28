package org.stormrealms.stormstats;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormstats.configuration.StormStatConfiguration;
import org.stormrealms.stormstats.model.ClassData;
import org.stormrealms.stormstats.model.RPGClass;
import org.stormrealms.stormstats.model.RPGPlayer;

@AutoConfigurationPackage

public class StormStats extends StormPlugin {
	private ConfigManager<SpringConfig> cfgMan = new ConfigManager<SpringConfig>("spring.json", SpringConfig.class);

	@Override
	public Set<ModuleCommand> commands() {
		return new HashSet();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormStatConfiguration.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public Class[] getEntities() {
		return new Class[] { RPGPlayer.class, RPGClass.class, ClassData.class };
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormstats.controllers", "org.stormrealms.stormstats.listeners",
				"org.stormrealms.stormstats.data", "org.stormrealms.stormstats.model" };
	}

	@Override
	public SpringConfig getSpringConfig() {
		cfgMan.init();
		return cfgMan.getConfig();
	}

	@Override
	public Set<Listener> listeners() {
		return new HashSet();
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		super.context = context;
	}

}
