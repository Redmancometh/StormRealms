package org.stormrealms.stormcombat;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcombat.configuration.StormCombatConfiguration;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public class StormCombat extends StormSpringPlugin {

	@Override
	public Class<?> getConfigurationClass() {
		return StormCombatConfiguration.class;
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

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormcombat.*", "org.stormrealms.stormcombat.configuration",
				"org.stormrealms.stormcombat.controllers", "org.stormrealms.stormcombat.listeners" };
	}

}
