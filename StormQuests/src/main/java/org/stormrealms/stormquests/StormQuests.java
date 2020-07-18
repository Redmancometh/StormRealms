package org.stormrealms.stormquests;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormquests.config.context.StormQuestContext;

@Component
public class StormQuests extends StormSpringPlugin {

	@Override
	public Class<?> getConfigurationClass() {
		return StormQuestContext.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public SpringConfig getSpringConfig() {
		ConfigManager<SpringConfig> config = new ConfigManager("spring.json", SpringConfig.class);
		config.init();
		return config.getConfig();
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		super.context = context;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormquests.controllers", "org.stormrealms.stormquests.*",
				"org.stormrealms.stormquests.config.context" };
	}

}
