package org.stormrealms.stormresources.configuration.pojo;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormmenus.Icon;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResourceNode {
	private List<Location> locations;
	private int secondsToSpawn;
	private Icon item;
	private String name;

	public void scheduleRespawn(ResourceNode node, Location location) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(StormCore.getInstance(), () -> respawn(location),
				node.getSecondsToSpawn() * 20);
	}

	public void respawn(Location location) {
		throw new IllegalStateException("Tried to respawn a resourcenode (" + this.getClass().getName()
				+ ") SFwhos implementation has no version of respawn!");
	}
}
