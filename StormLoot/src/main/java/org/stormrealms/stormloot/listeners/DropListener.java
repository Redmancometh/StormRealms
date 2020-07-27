package org.stormrealms.stormloot.listeners;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager.LocationAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.RPGStatAdapter;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormloot.controller.LootSelector;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class DropListener implements Listener {
	@Autowired
	protected LootSelector selector;
	protected AtomicInteger totalRunning = new AtomicInteger(0);
	protected NamespacedKey key = new NamespacedKey(StormCore.getInstance(), "rpggear");
	protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
			.registerTypeAdapter(Location.class, new LocationAdapter())
			.registerTypeAdapter(RPGStat.class, new RPGStatAdapter()).setLenient().setPrettyPrinting().create();

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getPlayer().isSneaking()) {
			e.getPlayer().getInventory().addItem(selector.itemDrop(10));
		}
	}

}
