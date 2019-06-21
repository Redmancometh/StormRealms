package org.stormrealms.stormcombat;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcombat.configuration.StormCombatConfiguration;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public class StormCombat extends StormPlugin {

	@Override
	public Set<ModuleCommand> commands() {
		return new HashSet();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormCombatConfiguration.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return null;
	}

	@Override
	public SpringConfig getSpringConfig() {
		return null;
	}

	@Override
	public Set<Listener> listeners() {
		return null;
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		this.context = context;
	}

}
