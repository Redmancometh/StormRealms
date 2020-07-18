package org.stormrealms.stormloot;

import javax.annotation.PostConstruct;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormloot.configuration.StormLootContext;

@Component
public class StormLoot extends StormSpringPlugin {

	@PostConstruct
	public void test() {
		for (int x = 0; x < 15; x++)
			System.out.println("TEST");
	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormLootContext.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		super.context = context;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormloot", "org.stormrealms.stormloot.controller",
				"org.stormrealms.stormloot.listeners", "org.stormrealms.stormloot.configuration",
				"org.stormrealms.stormloot.configuration.pojo" };
	}

}
