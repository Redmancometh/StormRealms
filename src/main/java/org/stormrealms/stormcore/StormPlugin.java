package org.stormrealms.stormcore;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public abstract class StormPlugin {
	protected AnnotationConfigApplicationContext context;
	@Autowired
	@Qualifier("context-storage")
	private Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts;

	@Getter
    @Setter
	private String name;

	public void init() {
		AnnotationConfigApplicationContext context = initializeContext();
		contexts.put(this.getClass(), context);
		setContext(context);
	}

	public void enable() {
		init();
	}

	public void disable() {
		contexts.remove(this.getClass());
	}

	public abstract Class<?> getConfigurationClass();

	public AnnotationConfigApplicationContext initializeContext() {
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

	public abstract void setContext(AnnotationConfigApplicationContext context);

	public abstract ConfigurableApplicationContext getContext();

}
