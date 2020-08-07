package org.stormrealms.stormmobs.goals;

import java.util.concurrent.TimeUnit;

import org.bukkit.entity.RPGEntity;
import org.spigotmc.event.entity.Goal;
import org.stormrealms.stormmobs.skills.MobSpell;

public abstract class CasterGoal implements Goal {

	public CasterGoal(MobSpell spell) {
		
	}

	@Override
	public void tick(RPGEntity entity) {
		if (shouldCast()) {
			cast();
			setLastCast(System.currentTimeMillis());
		}

	}

	public boolean shouldCast() {
		long timeSince = sinceLastSpell();
		long target = timeBetweenCasts();
		long timeDiff = target - timeSince;
		long maxTime = maxTimeBetweenCasts();
		long maxDiff = maxTime - timeDiff;
		if (maxDiff <= 0)
			return true;
		double scalar = maxDiff / maxTime;
		if (Math.random() > scalar)
			return true;
		return false;
	}

	public abstract void cast();

	/**
	 * This is the time that there should be between casting spells.
	 * 
	 * @return
	 */
	public abstract long timeBetweenCasts();

	/**
	 * The maximum time between casts for the spell this goal uses
	 * 
	 * @return
	 */
	public abstract long maxTimeBetweenCasts();

	/**
	 * 
	 * @return
	 */
	public abstract void setLastCast(long lastCast);

	public abstract long getLastSpell();

	public long sinceLastSpell() {
		return (System.currentTimeMillis() - getLastSpell()) / 1000;
	}

	public double getTimeSinceLastSpell() {
		return TimeUnit.MILLISECONDS.toSeconds((getLastSpell() - System.currentTimeMillis()));
	}
}
