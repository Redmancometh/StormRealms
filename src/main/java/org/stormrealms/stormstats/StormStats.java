package org.stormrealms.stormstats;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormstats.configuration.StormStatConfiguration;

public class StormStats extends StormPlugin {

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
		return null;
	}

}
