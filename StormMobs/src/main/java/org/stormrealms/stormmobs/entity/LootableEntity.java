package org.stormrealms.stormmobs.entity;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface LootableEntity {

	public default void dropLoot(World w, double x, double y, double z, Player p) {

	}
}