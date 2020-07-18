package org.stormrealms.stormresources.configuration.pojo;

import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.stormrealms.stormcore.StormCore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HerbNode extends ResourceNode {
	private int numSpawns;

	@Override
	public void respawn(Location location) {
		Collections.shuffle(getLocations());
		getLocations().forEach((potentialSpawn) -> {
			Block b = potentialSpawn.getBlock();
			if (b.getType() != getItem().getMaterial()) {
				b.setMetadata("herbnode", new FixedMetadataValue(StormCore.getInstance(), getName()));
				b.setType(getItem().getMaterial());
			}
		});
	}

}
