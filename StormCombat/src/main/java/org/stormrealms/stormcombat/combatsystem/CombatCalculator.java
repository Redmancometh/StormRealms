package org.stormrealms.stormcombat.combatsystem;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcombat.util.CombatUtil;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormstats.model.RPGCharacter;

/**
 * This is a combat calculator base interface which will define how specific
 * entity interactions go. Used a placeholder for now so we can write the events
 * without implementation.
 * 
 * @author Redmancometh
 *
 */
@Component
public abstract class CombatCalculator {

	@Autowired
	private CombatUtil util;

	/**
	 * Probably a good idea to periodically calculate this or write an equip event
	 * listener to know when to update it.
	 * 
	 * @param stat
	 * @param p
	 * @return
	 */
	public Map<RPGStat, Integer> getOverallBonuses(Player p) {
		Map<RPGStat, Integer> stats = new HashMap();
		for (ItemStack i : p.getInventory().getArmorContents()) {
			if (util.isRPGGear(i)) {
				RPGGearData data = util.getRPGGearData(i);
				stats.putAll(data.getBonuses());
			}
		}
		RPGCharacter player = util.getRPGCharacter(p);
		stats.putAll(player.getStats());
		return stats;
	}

	public double getMeleeAttackPower(RPGCharacter player, Player p) {
		Map<RPGStat, Integer> bonuses = getOverallBonuses(p);
		int str = bonuses.get(RPGStat.STR);
		int agi = bonuses.get(RPGStat.AGI);
		int level = player.getLevel();
		return (level * 2) + (str - 10) + (agi - 20);
	}

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
	public boolean isMiss(WeaponAttackEvent e) {
		int wScal = util.hasRPGOffhand(e.getBukkitPlayer()) ? 17 : 7;
		return false;
		
	}

}
