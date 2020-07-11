package org.stormrealms.stormcombat.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcombat.combatsystem.CombatCalculator;
import org.stormrealms.stormcombat.combatsystem.CombatProcessor;

public class CombatListeners implements Listener {
	@Autowired
	private CombatProcessor cProc;
	@Autowired
	private CombatCalculator cCalc;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void throwEvent(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		Entity damaged = e.getEntity();
		if (damager instanceof Player && (damaged instanceof LivingEntity)) {
			if (damaged instanceof Player) {
				Bukkit.getPluginManager().callEvent(new PVPEvent());
				return;
			}
			Player dPlayer = (Player) damager;
			Bukkit.getPluginManager()
					.callEvent(new PVMEvent(cProc.getRPGPlayer(dPlayer), dPlayer, (LivingEntity) damager));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void checkPVM(PVMEvent e) {
		Player bAttacker = e.getBukkitAttacker();
		ItemStack mainHand = bAttacker.getInventory().getItemInMainHand();
		if (mainHand != null && cProc.isRPGWeapon(mainHand))
			Bukkit.getPluginManager()
					.callEvent(new WeaponAttackEvent(cProc.getRPGWeapon(mainHand), e.getAttacker(), bAttacker));
		if (e.isDamagedKilled())
			return;
		ItemStack offHand = bAttacker.getInventory().getItemInMainHand();
		if (offHand != null && cProc.isRPGWeapon(offHand))
			Bukkit.getPluginManager()
					.callEvent(new WeaponAttackEvent(cProc.getRPGWeapon(offHand), e.getAttacker(), bAttacker));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void calculateAndProcess(WeaponAttackEvent e) {
		if (cCalc.isMiss(e)) {
			cProc.missed(e);
			return;
		}
		if (cCalc.isDodged(e)) {
			cProc.dodged(e);
			return;
		}
		if (cCalc.isCrushingBlow(e))
			e.setCrushingBlow(true);
		if (cCalc.isGlancing(e))
			e.setGlancingBlow(true);
		e.setDamage(cCalc.calculateMeleeDamage(e));
		cProc.hit(e);
	}
}
