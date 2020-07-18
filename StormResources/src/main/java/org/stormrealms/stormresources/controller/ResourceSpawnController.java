package org.stormrealms.stormresources.controller;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormresources.configuration.ResourceConfigurationManager;
import org.stormrealms.stormresources.configuration.pojo.HerbNode;
import org.stormrealms.stormresources.configuration.pojo.OreNode;
import org.stormrealms.stormresources.listeners.GatherListeners;

@Controller
public class ResourceSpawnController {
	@Autowired
	private ResourceConfigurationManager confMan;
	@Autowired
	private GatherListeners gatherListener;

	@PostConstruct
	public void registerListeners() {
		Bukkit.getPluginManager().registerEvents(gatherListener, StormCore.getInstance());
	}

	@PostConstruct
	public void spawnResources() {
		System.out.println("REGISTER LISTENERS RESOURCE SPAWN CONTROLLER");
		System.out.println("Wiping resources");
		wipeResources();
		confMan.getConfig().getResources().forEach((key, resource) -> {
			System.out.println("Resource node " + resource.getClass());
			if (resource instanceof HerbNode)
				spawnHerbs(key, (HerbNode) resource);
			if (resource instanceof OreNode) {
				resource.getLocations().forEach((loc) -> {
					Block b = loc.getBlock();
					b.setType(resource.getItem().getMaterial());
					b.setMetadata("orenode", new FixedMetadataValue(StormCore.getInstance(), key));
				});
			}
		});
	}

	private void spawnHerbs(String key, HerbNode node) {
		int spawns = node.getNumSpawns();
		Collections.shuffle(node.getLocations());
		for (int x = 0; x < spawns; x++) {
			Block nodeBlock = node.getLocations().get(x).getBlock();
			nodeBlock.setType(node.getItem().getMaterial());
			nodeBlock.setMetadata("herbnode", new FixedMetadataValue(StormCore.getInstance(), key));
		}
	}

	private void wipeResources() {
		confMan.getConfig().getResources().forEach((key, resource) -> {
			if (resource instanceof OreNode)
				return;
			System.out.println(resource);
			resource.getLocations().forEach((loc) -> loc.getBlock().setType(Material.AIR));
		});

	}
}
