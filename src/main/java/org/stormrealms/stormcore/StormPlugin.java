package org.stormrealms.stormcore;

import java.util.Map;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public abstract class StormPlugin {
	protected AnnotationConfigApplicationContext context;

	public void init() {
		ConfigurableApplicationContext context = initializeContext();
		setContext(context);
	}

	public void enable() {
		init();
	}

	public void disable() {

	}

	public abstract Class<?> getConfigurationClass();

	public ConfigurableApplicationContext initializeContext() {
		SpringConfig cfg = getSpringConfig();
		this.context = new AnnotationConfigApplicationContext();
		// TODO: This method doesn't exist?
		this.context.setClassLoader(StormCore.class.getClassLoader());
		this.context.refresh();
		this.context.register(getConfigurationClass());
		Map<String, Object> props = context.getEnvironment().getSystemProperties();
		cfg.getProperties().forEach((key, value) -> props.put(key, value));
		return context;
	}

	public abstract SpringConfig getSpringConfig();

	public abstract void setContext(ConfigurableApplicationContext context);

	public abstract ConfigurableApplicationContext getContext();

}
