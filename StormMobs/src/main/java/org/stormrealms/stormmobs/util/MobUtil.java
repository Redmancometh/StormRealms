package org.stormrealms.stormmobs.util;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.stormrealms.stormmobs.entity.CustomEntity;

public class MobUtil {

	public static <T> T spawnEntity(CustomEntity entity, Location loc) {
		entity.getEntity().setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		((CraftWorld) loc.getWorld()).getHandle().addEntity(entity.getEntity());
		return (T) entity;
	}
}
