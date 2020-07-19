package org.stormrealms.stormcombat.combatsystem;

import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormstats.model.RPGCharacter;

@Component
public class SimpleCombatCalculator extends CombatCalculator {

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

	@Override
	public double getMeleeAttackPower(RPGCharacter player, Player p) {
		return 0;
	}

}
