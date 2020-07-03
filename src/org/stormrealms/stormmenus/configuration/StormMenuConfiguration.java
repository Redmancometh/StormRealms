package org.stormrealms.stormmenus.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.stormrealms.stormmenus.MenuManager;

@Configuration
@ComponentScan({ "org.stormrealms.stormmenus", "org.stormrealms.stormmenus.abstraction",
		"org.stormrealms.stormmenus.menus", "org.stormrealms.stormmenus.listeners",
		"org.stormrealms.stormmenus.menus" })
public class StormMenuConfiguration {
	@Bean
	public MenuManager manager() {
		return new MenuManager();
	}
}
