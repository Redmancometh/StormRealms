package org.stormrealms.stormmobs.mobs;

import java.util.List;

public interface RPGMob {
	public int getLowDmg();

	public int getHighDmg();

	public boolean isElite();

	public int getLevel();

	public int getArmor();

	public abstract List<MobAbility> getAbilities();
}
