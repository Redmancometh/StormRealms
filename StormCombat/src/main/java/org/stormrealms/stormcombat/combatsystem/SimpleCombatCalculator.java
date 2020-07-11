package org.stormrealms.stormcombat.combatsystem;

import org.stormrealms.stormcombat.events.WeaponAttackEvent;

public class SimpleCombatCalculator implements CombatCalculator {

	@Override
	public int calculateMeleeDamage(WeaponAttackEvent e) {
		return 0;
	}

	@Override
	public boolean isCrushingBlow(WeaponAttackEvent e) {
		return false;
	}

	@Override
	public boolean isDodged(WeaponAttackEvent e) {
		return false;
	}

	@Override
	public boolean isGlancing(WeaponAttackEvent e) {
		return false;
	}

	@Override
	public boolean isMiss(WeaponAttackEvent e) {
		return false;
	}

}
