package org.stormrealms.stormcombat.combatsystem;

import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;

/**
 * This is a combat calculator base interface which will define how specific
 * entity interactions go. Used a placeholder for now so we can write the events
 * without implementation.
 * 
 * @author Redmancometh
 *
 */
@Component
public interface CombatCalculator {

	public abstract int calculateMeleeDamage(WeaponAttackEvent e);

	/**
	 * 
	 * @param e
	 * @return
	 */
	public abstract boolean isCrushingBlow(WeaponAttackEvent e);

	/**
	 * 
	 * @param e
	 * @return
	 */
	public abstract boolean isDodged(WeaponAttackEvent e);

	/**
	 * 
	 * @param e
	 * @return
	 */
	public abstract boolean isGlancing(WeaponAttackEvent e);

	/**
	 * See if this is a miss
	 * 
	 * @param e
	 * @return
	 */
	public abstract boolean isMiss(WeaponAttackEvent e);

}
