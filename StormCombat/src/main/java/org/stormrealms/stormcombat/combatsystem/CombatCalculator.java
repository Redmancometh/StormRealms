package org.stormrealms.stormcombat.combatsystem;

import org.stormrealms.stormcombat.events.WeaponAttackEvent;

/**
 * This is a combat calculator base interface which will define how specific
 * entity interactions go. Used a placeholder for now so we can write the events
 * without implementation.
 * 
 * @author Redmancometh
 *
 */
public interface CombatCalculator {
	public abstract int calculateMeleeDamage(WeaponAttackEvent e);

	public abstract boolean isCrushingBlow(WeaponAttackEvent e);

	public abstract boolean isDodged(WeaponAttackEvent e);

	public abstract boolean isGlancing(WeaponAttackEvent e);

	public abstract boolean isMiss(WeaponAttackEvent e);

}
