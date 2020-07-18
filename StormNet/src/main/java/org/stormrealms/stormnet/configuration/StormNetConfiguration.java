package org.stormrealms.stormnet.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.stormrealms.stormmenus", "org.stormrealms.stormmenus.abstraction",
		"org.stormrealms.stormmenus.menus", "org.stormrealms.stormmenus.listeners",
		"org.stormrealms.stormmenus.menus" })
public class StormNetConfiguration {

}
