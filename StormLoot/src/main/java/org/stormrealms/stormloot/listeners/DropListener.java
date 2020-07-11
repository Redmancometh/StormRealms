package org.stormrealms.stormloot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormloot.controller.LootSelector;

import io.netty.util.internal.ThreadLocalRandom;

@Component
public class DropListener implements Listener {
	@Autowired
	private LootSelector selector;

	@EventHandler
	public void dropItem(PlayerInteractEvent e) {
		System.out.println("INTERACT");
		e.getPlayer().getInventory().addItem(selector.armorDrop(ThreadLocalRandom.current().nextInt(1, 50)));
		e.getPlayer().getInventory().addItem(selector.weaponDrop(ThreadLocalRandom.current().nextInt(1, 50)));
	}
}
