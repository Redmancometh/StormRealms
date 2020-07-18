package org.stormrealms.stormcore;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class StormPlugin {
	protected AnnotationConfigApplicationContext context;
	@Getter
	@Setter
	private String name;
	private List<Listener> listeners = new ArrayList<>();
	@Setter
	@Getter
	private URLClassLoader moduleLoader;

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
		System.out.println("Calling stormplugin.enable");
		if (this instanceof SpringPlugin) {
			System.out.println("ON ENABLE CONTEXT " + (this.context == null));
			System.out.println(this.context);
		}
	}

}
