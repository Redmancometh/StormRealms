package org.stormrealms.stormmobs.mobs;

import net.minecraft.server.v1_15_R1.*;

/**
 * Base class for MMO zombies that has a level. These will have caster, melee,
 * and ranged variants.
 * 
 * @author Redmancometh
 *
 */
public class LevelledZombie extends EntityZombie {

	private int level = 5;

	public LevelledZombie(EntityTypes<? extends EntityMonster> entitytypes, World world) {
		super(EntityTypes.ZOMBIE, world);
	}

	public LevelledZombie(World world) {
		super(world);
	}

	public int getLevel() {
		return level;
	}

}
