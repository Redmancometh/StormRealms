package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntityCustom;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.RPGEntity;

public class CraftRPGEntity extends CraftMonster implements RPGEntity {

	public CraftRPGEntity(CraftServer server, EntityCustom entity) {
		super(server, entity);
	}

	@Override
	public EntityCustom getHandle() {
		return (EntityCustom) entity;
	}

	@Override
	public String toString() {
		// We're gonna put the thing here later.
		return "CraftRPGEntity";
	}

	@Override
	public EntityType getType() {
		return EntityType.RPG_ENTITY;
	}
}
