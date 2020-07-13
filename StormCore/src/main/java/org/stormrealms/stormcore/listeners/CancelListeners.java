package org.stormrealms.stormcore.listeners;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.magic.EventInterceptor;

@Component
public class CancelListeners implements Listener {
	@Autowired
	private EventInterceptor test;

	@EventHandler(priority = EventPriority.LOWEST)
	public void cancelBreak(BlockBreakEvent e) {
		test.onEvent((Event) e);
		e.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}
}
