package org.stormrealms.stormcombat.gear;

import java.util.Map;
import java.util.Set;

public interface RPGArmor extends RPGItem {
	public int getArmor();

	public Set<WeaponEffect> getEffect();

	public Map<RPGStat, Integer> getStats();

	public default Integer getStat(RPGStat stat) {
		return getStats().get(stat);
	}
}
