package org.stormrealms.stormstats;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormstats.configuration.StormStatConfiguration;
import org.stormrealms.stormstats.listeners.LoginListener;

public class StormStats extends StormPlugin {
	private ConfigManager<SpringConfig> cfgMan = new ConfigManager<SpringConfig>("spring.json", SpringConfig.class);

	@Override
	public void enable() {
		super.enable();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormStatConfiguration.class;
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		this.context = context;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return context;
	}

	@Override
	public SpringConfig getSpringConfig() {
		cfgMan.init();
		return cfgMan.getConfig();
	}

	@Override
	public Set<Listener> listeners() {
		Set listeners = new HashSet();
		listeners.add(new LoginListener());
		return listeners;
	}

	@Override
	public Set<ModuleCommand> commands() {
		return new HashSet();
	}

}
