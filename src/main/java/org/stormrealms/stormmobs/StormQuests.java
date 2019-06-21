package org.stormrealms.stormmobs;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormmobs.config.context.StormQuestContext;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

public class StormQuests extends StormPlugin {

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
	public Set<Listener> listeners() {
		return new HashSet();
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		super.context = context;
	}

	@Override
	public Set<ModuleCommand> commands() {
		return new HashSet();
	}

}
