package org.stormrealms.stormstats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.StormPlugin;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormstats.configuration.StormStatConfiguration;
import org.stormrealms.stormstats.listeners.StatLoginListener;
import org.stormrealms.stormstats.menus.ClassMenu;
import org.stormrealms.stormstats.model.RPGClass;
import org.stormrealms.stormstats.model.RPGPlayer;

import com.redmancometh.redcore.DBRedPlugin;

@AutoConfigurationPackage
public class StormStats extends StormPlugin implements DBRedPlugin {
	private ConfigManager<SpringConfig> cfgMan = new ConfigManager<SpringConfig>("spring.json", SpringConfig.class);
	private SessionFactory factory;

	@Override
	public void initialize() {
		DBRedPlugin.super.initialize();
		Bukkit.getScheduler().scheduleSyncDelayedTask(StormCore.getInstance(), () -> {
			ClassMenu menu = new ClassMenu();
			getContext().getAutowireCapableBeanFactory().autowireBean(menu);
		}, 60);
	}

	@Override
	public Set<ModuleCommand> commands() {
		return new HashSet();
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
		cfgMan.init();
		return cfgMan.getConfig();
	}

	@Override
	public Set<Listener> listeners() {
		Set<Listener> listeners = new HashSet();
		listeners.add(new StatLoginListener());
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
		classes.addAll(Arrays.asList(new Class[] { RPGPlayer.class, RPGClass.class }));
		return classes;
	}

	@Override
	public void setInternalFactory(SessionFactory fac) {
		this.factory = fac;
	}

}
