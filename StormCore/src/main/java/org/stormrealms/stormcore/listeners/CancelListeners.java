package org.stormrealms.stormcore.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.springframework.stereotype.Component;

@Component
public class CancelListeners implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void cancelBreak(BlockBreakEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}
}
