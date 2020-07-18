package org.stormrealms.stormresources.listeners;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.util.RateLimiter;
import org.stormrealms.stormresources.configuration.ResourceConfigurationManager;
import org.stormrealms.stormresources.configuration.pojo.HerbNode;
import org.stormrealms.stormresources.configuration.pojo.OreNode;
import org.stormrealms.stormresources.configuration.pojo.ResourceNode;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.md_5.bungee.api.ChatColor;

@Component
public class GatherListeners implements Listener {
	@Autowired
	private ResourceConfigurationManager confMan;
	@Autowired
	@Qualifier("gather-limiter")
	private RateLimiter gatherLimiter;

	@EventHandler
	public void onHarvestHerb(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		if (b == null || !b.hasMetadata("herbnode")) {
			System.out.println("NO META OR NULL");
			return;
		}
		ResourceNode node = confMan.getConfig().getResources().get(b.getMetadata("herbnode").get(0).asString());
		if (!(node instanceof HerbNode)) {
			System.out.println("NOT AN HERBNODE");
			return;
		}
		gatherLimiter.tryAction(e.getPlayer(), () -> {
			System.out.println("GATHERING");
			Location loc = b.getLocation();
			Location locUp = loc.add(0, 1.5, 0);
			Hologram holo = HologramsAPI.createHologram(StormCore.getInstance(), locUp);
			holo.getVisibilityManager().setVisibleByDefault(false);
			holo.getVisibilityManager().showTo(e.getPlayer());
			AtomicInteger countDown = new AtomicInteger(8);
			Bukkit.getScheduler().runTaskTimer(StormCore.getInstance(), (task) -> {
				holo.clearLines();
				StringBuilder line = new StringBuilder("■");
				for (int x = 0; x < countDown.get(); x++)
					line.append(ChatColor.YELLOW + "■");
				holo.insertTextLine(0, line.toString());
				System.out.println("IS VISIBLE: " + holo.getVisibilityManager().isVisibleTo(e.getPlayer()));
				if (countDown.decrementAndGet() == 0) {
					e.getPlayer().getInventory().addItem(node.getItem().build());
					b.setType(Material.AIR);
					node.scheduleRespawn(node, b.getLocation());
					holo.delete();
					task.cancel();
				}
			}, 15, 15);
		}, () -> {
		});

	}

	@EventHandler
	public void onOreMine(BlockBreakEvent e) {
		Block b = e.getBlock();
		if (!b.hasMetadata("orenode") || b.getType() == Material.STONE)
			return;
		ResourceNode node = confMan.getConfig().getResources().get(b.getMetadata("orenode").get(0).asString());
		if (!(node instanceof OreNode))
			return;
		e.setCancelled(true);
		e.getPlayer().getInventory().addItem(node.getItem().build());
		b.setType(Material.STONE);
		Bukkit.getScheduler().runTaskLater(StormCore.getInstance(), () -> b.setType(node.getItem().getMaterial()),
				node.getSecondsToSpawn() * 20);
	}
}
