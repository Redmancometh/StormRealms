package org.stormrealms.stormresources.controller;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormresources.configuration.HerbNode;
import org.stormrealms.stormresources.configuration.OreNode;
import org.stormrealms.stormresources.configuration.ResourceNode;
import org.stormrealms.stormresources.configuration.pojo.HerbsConfiguration;
import org.stormrealms.stormresources.listeners.GatherListeners;

@Controller
public class ResourceSpawnController {
	@Autowired
	@Qualifier("resources-config")
	private ConfigManager<HerbsConfiguration> confMan;
	@Autowired
	private GatherListeners gatherListener;

	@PostConstruct
	public void registerListeners() {
		Bukkit.getPluginManager().registerEvents(gatherListener, StormCore.getInstance());
	}

	@PostConstruct
	public void spawnResources() {
		wipeResources();
		for (ResourceNode resource : confMan.getConfig().getResources()) {
			if (resource instanceof HerbNode)
				spawnHerbs((HerbNode) resource);
			if (resource instanceof OreNode)
				resource.getLocations().forEach((loc) -> {
					Block b = loc.getBlock();
					b.setType(resource.getItem().getMaterial());
					b.setMetadata("resourcenode", new FixedMetadataValue(StormCore.getInstance(), true));
				});
		}
	}

	private void spawnHerbs(HerbNode node) {
		int spawns = node.getNumSpawns();
		Collections.shuffle(node.getLocations());
		for (int x = 0; x < spawns; x++) {
			Block nodeBlock = node.getLocations().get(x).getBlock();
			nodeBlock.setType(node.getItem().getMaterial());
			nodeBlock.setMetadata("resourcenode", new FixedMetadataValue(StormCore.getInstance(), true));
		}
	}

	private void wipeResources() {
		for (ResourceNode resource : confMan.getConfig().getResources()) {
			if (resource instanceof OreNode)
				continue;
			resource.getLocations().forEach((loc) -> loc.getBlock().setType(Material.AIR));
		}
	}
}
