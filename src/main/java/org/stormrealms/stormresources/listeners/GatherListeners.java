package org.stormrealms.stormresources.listeners;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormresources.configuration.HerbNode;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class GatherListeners implements Listener {

	@EventHandler
	public void onHarvestHerb(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		if (!b.hasMetadata("resourcenode"))
			return;
		Location loc = b.getLocation();
		Location locUp = loc.add(0, 1.5, 0);
		Hologram holo = HologramsAPI.createHologram(StormCore.getInstance(), locUp);
		AtomicInteger countDown = new AtomicInteger(10);
		Bukkit.getScheduler().runTaskTimer(StormCore.getInstance(), (task) -> {
			holo.clearLines();
			StringBuilder line = new StringBuilder("■");
			for (int x = 0; x < countDown.get(); x++)
				line.append("■");
			HerbNode node = (HerbNode) b.getMetadata("resourcenode").get(0);
			e.getPlayer().getInventory().addItem(node.getItem().build());
			task.cancel();
		}, 10, 10);
	}

	@EventHandler
	public void onOreMine(BlockBreakEvent e) {
		Block b = e.getBlock();
		if (!b.hasMetadata("resourcenode"))
			return;
	}
}
