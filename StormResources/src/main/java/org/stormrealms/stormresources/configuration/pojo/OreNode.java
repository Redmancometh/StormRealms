package org.stormrealms.stormresources.configuration.pojo;

import org.bukkit.Location;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OreNode extends ResourceNode {
	@Override
	public void respawn(Location location) {
		location.getBlock().setType(getItem().getMaterial());
	}

}
