package org.stormrealms.stormresources.configuration;

import java.util.List;

import org.bukkit.Location;
import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class ResourceNode {
	private List<Location> locations;
	private int secondsToSpawn;
	private Icon item;
}
