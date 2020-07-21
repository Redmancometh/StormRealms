package org.stormrealms.stormcombat.combatsystem;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcombat.util.CombatUtil;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormmobs.entity.RPGEntity;
import org.stormrealms.stormstats.model.RPGCharacter;

import io.netty.util.internal.ThreadLocalRandom;

/**
 * This is a combat calculator base interface which will define how specific
 * entity interactions go. Used a placeholder for now so we can write the events
 * without implementation.
 * 
 * @author Redmancometh
 *
 */
@Component
public class PVMCombatCalculator implements CombatCalculator {

	@Autowired
	private CombatUtil util;

	public double getMeleeAttackPower(WeaponAttackEvent e) {
		RPGCharacter player = e.getPlayer();
		Map<RPGStat, Integer> bonuses = e.getTotalBonuses();
		int str = bonuses.get(RPGStat.STR);
		int agi = bonuses.get(RPGStat.AGI);
		int level = player.getLevel();
		return (level * 2) + (str - 10) + (agi - 20);
	}

	public double calculateMeleeDamage(WeaponAttackEvent e) {
		Map<RPGStat, Integer> bonuses = e.getTotalBonuses();
		int baseMin = bonuses.get(RPGStat.DMG_MIN);
		int baseMax = bonuses.get(RPGStat.DMG_MAX);
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		int baseDamage = rand.nextInt(baseMin, baseMax);
		double ap = getMeleeAttackPower(e);
		int baseMultiplier = 1;
		double weaponSpeed = 2.4;
		double damage = baseDamage + (baseMultiplier * ap / (14 - (weaponSpeed * .1)));
		return damage;
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isCrushingBlow(WeaponAttackEvent e) {
		int crushingChance = 2 + Math.max(1, e.getEntity().getLevel() - e.getPlayer().getLevel());
		if (ThreadLocalRandom.current().nextInt(0, 100) < crushingChance)
			return true;
		return false;

	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isDodged(WeaponAttackEvent e) {
		RPGCharacter character = e.getPlayer();
		Map<RPGStat, Integer> bonuses = e.getTotalBonuses();
		// We'll get the characters class here once the init logic for that is set up
		int baseDodge = 5;
		double classAgiRatio = 20;
		int agi = bonuses.get(RPGStat.AGI);
		int addnlDodge = bonuses.get(RPGStat.DODGE);
		int mobDef = e.getEntity().getDefense();
		double dodgeChance = baseDodge + ((agi / classAgiRatio) + addnlDodge)
				+ ((mobDef - (character.getLevel() * 10) * .04));
		if (ThreadLocalRandom.current().nextInt(0, 100) < dodgeChance)
			return true;
		return false;
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isGlancing(WeaponAttackEvent e) {
		double glancingChance = .015 * Math.max(1, (e.getEntity().getLevel() - e.getPlayer().getLevel()));
		if (ThreadLocalRandom.current().nextInt(0, 100) < glancingChance)
			return true;
		return false;
	}

	/**
	 * See if this is a miss
	 * 
	 * @param e
	 * @return
	 */
	public boolean isMiss(WeaponAttackEvent e) {
		RPGEntity entity = e.getEntity();
		int wScal = util.hasRPGOffhand(e.getBukkitPlayer()) ? 17 : 7;
		int pWep = e.getPlayer().getLevel() * 10;
		double missChance = wScal
				+ ((pWep - entity.getDefense()) * (.001 * Math.max(1, (entity.getLevel() - e.getPlayer().getLevel()))));
		if (ThreadLocalRandom.current().nextInt(0, 100) < missChance)
			return true;
		return false;

	}

}
