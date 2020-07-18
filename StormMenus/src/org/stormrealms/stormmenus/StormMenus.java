package org.stormrealms.stormmenus;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormmenus.configuration.StormMenuConfiguration;

@Component
public class StormMenus extends StormSpringPlugin {

	@Override
	public void enable() {
		super.enable();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormMenuConfiguration.class;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormmenus", "org.stormrealms.stormmenus.abstraction",
				"org.stormrealms.stormmenus.menus", "org.stormrealms.stormmenus.listeners",
				"org.stormrealms.stormmenus.menus" };
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		this.context = context;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return this.context;
	}

}
