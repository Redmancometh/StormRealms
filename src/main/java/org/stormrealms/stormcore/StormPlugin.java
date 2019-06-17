package org.stormrealms.stormcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.config.pojo.SpringConfig;
import org.stormrealms.stormcore.util.CommandUtil;

public abstract class StormPlugin {
	protected AnnotationConfigApplicationContext context;
	@Autowired
	@Qualifier("context-storage")
	private Map<Class<? extends StormPlugin>, AnnotationConfigApplicationContext> contexts;

	@Autowired
    private CommandUtil commandUtil;

	@Autowired
    private StormCore instace;

	@Getter
    @Setter
	private String name;

	private List<BukkitCommand> commands = new ArrayList<>();
	private List<Listener> listeners = new ArrayList<>();

	public void init() {
		AnnotationConfigApplicationContext context = initializeContext();
		contexts.put(this.getClass(), context);
		setContext(context);
	}

	public final void registerCommand(BukkitCommand executor) {
	    this.commands.add(executor);
	    commandUtil.registerCommand(executor.getName(), executor);
    }

    public final void registerListener(Listener l) {
	    this.listeners.add(l);
	    Bukkit.getPluginManager().registerEvents(l, instace);
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
	    commands.forEach(command -> commandUtil.unregisterCommand(command));
	    commands.clear();
		contexts.remove(this.getClass());
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

}
