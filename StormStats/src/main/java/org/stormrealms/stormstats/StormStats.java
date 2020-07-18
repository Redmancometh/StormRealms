package org.stormrealms.stormstats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.event.Listener;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.DBRedPlugin;
import org.stormrealms.stormcore.StormSpringPlugin;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormstats.configuration.StormStatConfiguration;
import org.stormrealms.stormstats.model.ClassData;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class StormStats extends StormSpringPlugin implements DBRedPlugin {
	private ConfigManager<SpringConfig> cfgMon = new ConfigManager<SpringConfig>("spring.json", SpringConfig.class);
	private SessionFactory factory;
	
	@Override
	public void initialize() {
		DBRedPlugin.super.initialize();
	}

	@Override
	public void disable() {
		super.disable();

	}

	@Override
	public Class<?> getConfigurationClass() {
		return StormStatConfiguration.class;

	}

	@Override
	public ConfigurableApplicationContext getContext() {
		return super.context;
	}

	@Override
	public String[] getPackages() {
		return new String[] { "org.stormrealms.stormstats.controllers", "org.stormrealms.stormstats.listeners",
				"org.stormrealms.stormstats.data", "org.stormrealms.stormstats.model",
				"org.stormrealms.stormstats.menus" };
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

	@Override
	public SessionFactory getInternalFactory() {
		return factory;
	}

	@Override
	public List<Class> getMappedClasses() {
		List<Class> classes = new ArrayList();
		classes.addAll(Arrays.asList(new Class[] { RPGPlayer.class, RPGCharacter.class, ClassData.class }));
		return classes;
	}

	@Override
	public void setInternalFactory(SessionFactory fac) {
		this.factory = fac;
	}

}
