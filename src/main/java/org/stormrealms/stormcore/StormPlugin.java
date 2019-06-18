package org.stormrealms.stormcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.command.ModuleCommand;
import org.stormrealms.stormcore.command.SCommandExecutor;
import org.stormrealms.stormcore.command.StormCommandHandler;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormcore.storage.PluginStorage;

public abstract class StormPlugin {
	protected AnnotationConfigApplicationContext context;
	@Autowired
	private PluginStorage pluginStorage;

	@Autowired
	private StormCore instance;

	@Autowired
    private StormCommandHandler commandHandler;

	@Getter
	@Setter
	private String name;

	private List<SCommandExecutor> commands = new ArrayList<>();
	private List<Listener> listeners = new ArrayList<>();

	public void init() {
		AnnotationConfigApplicationContext context = initializeContext();
		for (int x = 0; x < 5; x++)
			System.out.println("Context is null: " + (context == null));
		//pluginStorage.registerPlugin(this, context, listeners(), commands);
		setContext(context);
		registerListeners();
	}

	public final void registerCommand(String cmd, SCommandExecutor executor) {
	    executor.setName(cmd);
		this.commands.add(executor);
		commandHandler.registerCommand(cmd, executor);
	}

	public final void registerListener(Listener l) {
		this.listeners.add(l);
		Bukkit.getPluginManager().registerEvents(l, instance);
	}

	public final void unregisterListeners() {
		for (Listener l : listeners) {
			HandlerList.unregisterAll(l);
		}
	}

	public void enable() {
		init();
	}

	public void disable() {
		commands.forEach(command -> commandHandler.unregisterCommand(command));
		commands.clear();
		//contexts.remove(this.getClass());
	}

	public abstract Class<?> getConfigurationClass();

	public AnnotationConfigApplicationContext initializeContext() {
		SpringConfig cfg = getSpringConfig();
		this.context = new AnnotationConfigApplicationContext();
		// TODO: This method doesn't exist?
		this.context.setClassLoader(StormCore.class.getClassLoader());
		this.context.refresh();
		this.context.register(getConfigurationClass());
		Map<String, Object> props = context.getEnvironment().getSystemProperties();
		cfg.getProperties().forEach((key, value) -> props.put(key, value));
		return context;
	}

	public abstract SpringConfig getSpringConfig();

	public abstract void setContext(AnnotationConfigApplicationContext context);

	public abstract ConfigurableApplicationContext getContext();

	public abstract Set<Listener> listeners();

	public abstract Set<ModuleCommand> commands();

	public void registerListeners() {
		listeners().forEach(this::registerListener);
	}
}
