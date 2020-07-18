package org.stormrealms.stormcombat.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormcombat.*", "org.stormrealms.stormcombat.configuration",
		"org.stormrealms.stormcombat.controllers", "org.stormrealms.stormcombat.listeners" })
public class StormCombatConfiguration {
	
}
