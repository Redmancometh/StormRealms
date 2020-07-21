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
	/**
	 * Calculate a players current AP
	 * 
	 * @param e
	 * @return
	 */
	public double getMeleeAttackPower(WeaponAttackEvent e);

	/**
	 * Calculate overall melee damage for an attack event
	 * 
	 * @param e
	 * @return
	 */
	public double calculateMeleeDamage(WeaponAttackEvent e);

	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isCrushingBlow(WeaponAttackEvent e);

	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isDodged(WeaponAttackEvent e);

	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isGlancing(WeaponAttackEvent e);

	/**
	 * See if this is a miss
	 * 
	 * @param e
	 * @return
	 */
	public boolean isMiss(WeaponAttackEvent e);

}
