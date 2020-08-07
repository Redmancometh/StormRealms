package org.stormrealms.stormmobs.goals;

import org.stormrealms.stormmobs.skills.MobSpell;

public abstract class SummonerGoal extends CasterGoal {

	public SummonerGoal(MobSpell spell) {
		super(spell);
	}

	@Override
	public boolean shouldCast() {
		boolean superCast = super.shouldCast();
		if (superCast) {
			if (mobsLeft() >= maxMobs())
				return false;
			return true;
		}
		return superCast;
	}

	/**
	 * How many of the entities summoned mobs remain
	 * 
	 * @return
	 */
	public abstract int mobsLeft();

	/**
	 * The maximum amount of summoned mobs that should ever exist for a given
	 * summoner
	 * 
	 * @return
	 */
	public abstract int maxMobs();

}
