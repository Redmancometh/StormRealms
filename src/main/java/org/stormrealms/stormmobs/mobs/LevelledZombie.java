package org.stormrealms.stormmobs.mobs;

import net.minecraft.server.v1_14_R1.EntityZombie;
import net.minecraft.server.v1_14_R1.World;

/**
 * Base class for MMO zombies that has a level. These will have caster, melee,
 * and ranged variants.
 * 
 * @author Redmancometh
 *
 */
public class LevelledZombie extends EntityZombie {

	private int level = 5;

	public LevelledZombie(World world) {
		super(world);
	}

	public int getLevel() {
		return level;
	}

}
