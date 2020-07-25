package org.stormrealms.stormcombat.combatsystem;

import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;

@Component
public class SimpleCombatProcessor implements CombatProcessor {

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
		System.out.println("HIT");
	}

	@Override
	public void giveLoot(WeaponAttackEvent e) {
		System.out.println("GIVE LOOT");
	}

}
