package org.stormrealms.stormcore;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public interface SpringPlugin {
	public abstract Class<?> getConfigurationClass();

	/**
	 * Get packages that need to be scanned.
	 * 
	 * @return
	 */
	public abstract String[] getPackages();

	public default SpringConfig getSpringConfig() {
		return StormCore.getInstance().springCfg();
	}

	public abstract void setContext(AnnotationConfigApplicationContext context);

	public abstract ConfigurableApplicationContext getContext();

	public default <U> U getBean(Class<U> clazz) {
		return getContext().getAutowireCapableBeanFactory().getBean(clazz);
	}

}
