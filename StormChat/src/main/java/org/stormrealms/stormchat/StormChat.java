package org.stormrealms.stormchat;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.Listener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormchat.configuration.ChatContext;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;

@Component
public class StormChat extends StormSpringPlugin {
	private ConfigManager<SpringConfig> cfgMon = new ConfigManager<SpringConfig>("spring.json", SpringConfig.class);

	@Override
	public void disable() {
		super.disable();
	}

	@Override
	public Class<?> getConfigurationClass() {
		return ChatContext.class;
	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormresources.*", "org.stormrealms.stormresources.configuration",
				"org.stormrealms.stormresources.controller", "org.stormrealms.stormresources.listeners" };
	}

	@Override
	public SpringConfig getSpringConfig() {
		cfgMon.init();
		return cfgMon.getConfig();
	}

	@Override
	public Set<Listener> listeners() {
		Set<Listener> listeners = new HashSet();
		return listeners;
	}

	@Override
	public void setContext(AnnotationConfigApplicationContext context) {
		super.context = context;
	}

}
