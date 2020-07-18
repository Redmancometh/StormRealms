package org.stormrealms.stormcombat.combatsystem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormstats.model.RPGPlayer;

public interface CombatProcessor {
	public boolean isRPGWeapon(ItemStack weapon);

	public void dodged(WeaponAttackEvent e);

	public void parried(WeaponAttackEvent e);

	public void missed(WeaponAttackEvent e);

	public void hit(WeaponAttackEvent e);

	public void giveLoot(WeaponAttackEvent e);

	public RPGGearData getRPGWeapon(ItemStack weapon);

	public RPGPlayer getRPGPlayer(Player player);

}
