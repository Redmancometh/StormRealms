package org.stormrealms.stormcombat.gear;

import java.util.Map;
import java.util.Set;

/**
 * Hopefully we can just stick this in the NBT a la aloreable.
 * 
 * @author Redmancometh
 *
 */
public interface RPGWeapon extends RPGItem {
	public int getLow();

	public int getHigh();

	public Set<WeaponEffect> effects();

	public Map<RPGStat, Integer> getStats();

	public default Integer getStat(RPGStat stat) {
		return getStats().get(stat);
	}
}
