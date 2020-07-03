package org.stormrealms.stormmenus;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormmenus.configuration.StormMenuConfiguration;

@Component
public class Menus extends StormSpringPlugin {
	private MenuManager menuManager;

	@Override
	public void enable() {
		System.out.println("ENABLING MENUS");
		super.enable();
		this.menuManager = new MenuManager();
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	@Override
	public Set<Listener> listeners() {
		Set<Listener> listeners = new HashSet();
		return listeners;
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
