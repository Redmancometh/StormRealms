package org.stormrealms.stormmobs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormmobs.config.context.StormMobContext;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public class StormMobs extends StormSpringPlugin {
	
	@Override
	public Class<?> getConfigurationClass() {
		return StormMobContext.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public SpringConfig getSpringConfig() {
		ConfigManager<SpringConfig> config = new ConfigManager("spring.json", SpringConfig.class);
		config.init();
		return config.getConfig();
	}

	@Override
	public Set<Listener> listeners() {
		return new HashSet();
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {

	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormmobs" };
	}

}
