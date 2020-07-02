package org.stormrealms.stormcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.stormrealms.stormcore.command.SCommandExecutor;

public abstract class StormPlugin {
	protected AnnotationConfigApplicationContext context;
	/*
	 * @Autowired private PluginStorage pluginStorage;
	 * 
	 * @Autowired private StormCore instance;
	 * 
	 * @Autowired private StormCommandHandler commandHandler;
	 */

	@Getter
	@Setter
	private String name;

	private List<SCommandExecutor> commands = new ArrayList<>();
	private List<Listener> listeners = new ArrayList<>();
	@Setter
	@Getter
	private ClassLoader moduleLoader;

	public final void registerCommand(String cmd, SCommandExecutor executor) {
		executor.setName(cmd);
		this.commands.add(executor);
	}

	public final void registerListener(Listener l) {
		this.listeners.add(l);
		Bukkit.getPluginManager().registerEvents(l, Bukkit.getPluginManager().getPlugin("StormCore"));
	}

	public final void unregisterListeners() {
		for (Listener l : listeners) {
			HandlerList.unregisterAll(l);
		}
	}

	public void enable() {

	}

	public void disable() {
		// commands.forEach(command -> commandHandler.unregisterCommand(command));
		commands.clear();
		// contexts.remove(this.getClass());
	}

	public abstract Set<Listener> listeners();

	public void registerListeners() {
		listeners().forEach(this::registerListener);
	}
}
