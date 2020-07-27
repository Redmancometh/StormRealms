package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.RPGEntity;

import net.minecraft.server.EntityCustomMonster;

public class CraftRPGEntity extends CraftMonster implements RPGEntity {

	public CraftRPGEntity(CraftServer server, EntityCustomMonster entity) {
		super(server, entity);

	}

	@Override
	public EntityCustomMonster getHandle() {
		return (EntityCustomMonster) entity;
	}

	@Override
	public String toString() {
		// We're gonna put the thing here later.
		return "CraftRPGEntity";
	}

	@Override
	public int getEntityId() {
		return getHandle().getId();
	}

	@Override
	public EntityType getType() {
		return EntityType.RPG_ENTITY;
	}
}
