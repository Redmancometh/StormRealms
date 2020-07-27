package org.stormrealms.stormspigot.entities;

import net.minecraft.server.EntityMonster;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.World;

public class CustomMonster extends EntityMonster {
	private int entityId;

	protected CustomMonster(EntityTypes<? extends EntityMonster> entitytypes, World world, int entityId) {
		super(entitytypes, world);
	}

	@Override
	public int getId() {
		return this.entityId;
	}
}
