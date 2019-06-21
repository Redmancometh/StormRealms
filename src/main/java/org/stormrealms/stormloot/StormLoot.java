package org.stormrealms.stormloot;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormloot.configuration.StormLootConfiguration;

public class StormLoot extends StormPlugin {

	@Override
	public Set<ModuleCommand> commands() {
		return new HashSet();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormLootConfiguration.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public SpringConfig getSpringConfig() {
		return null;
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
