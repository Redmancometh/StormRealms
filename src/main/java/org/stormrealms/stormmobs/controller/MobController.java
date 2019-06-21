package org.stormrealms.stormmobs.controller;

import org.bukkit.entity.LivingEntity;
import org.stormrealms.stormmobs.mobs.RPGMob;

public interface MobController {
	public RPGMob getMob(LivingEntity e);

	public boolean isMob(LivingEntity e);
}
