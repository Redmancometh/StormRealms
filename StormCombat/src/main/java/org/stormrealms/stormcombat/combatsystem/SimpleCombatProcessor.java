package org.stormrealms.stormcombat.combatsystem;

import java.util.Collection;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcore.outfacing.RPGGearData;

import com.google.common.collect.Multimap;

@Component
public class SimpleCombatProcessor implements CombatProcessor {
	@Autowired
	@Qualifier("gear-cache")
	private Multimap<UUID, RPGGearData> gearCache;

	@Override
	public void dodged(WeaponAttackEvent e) {

		System.out.println("DODGED");
	}

	@Override
	public void parried(WeaponAttackEvent e) {
		System.out.println("PARRIED");
	}

	@Override
	public void missed(WeaponAttackEvent e) {
		System.out.println("MISSED");
	}

	@Override
	public void hit(WeaponAttackEvent e) {
		Collection<RPGGearData> gear = gearCache.get(e.getBukkitPlayer().getUniqueId());
		if (gear != null) {
			for (RPGGearData data : gear) {
				
			}
		}
		System.out.println("HIT");
	}

	@Override
	public void giveLoot(WeaponAttackEvent e) {
		System.out.println("GIVE LOOT");
	}

}
