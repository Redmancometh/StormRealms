package org.stormrealms.stormcombat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.combatsystem.CombatCalculator;
import org.stormrealms.stormcombat.combatsystem.CombatProcessor;
import org.stormrealms.stormcombat.events.PVMEvent;
import org.stormrealms.stormcombat.events.PVPEvent;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcombat.util.CombatUtil;
import org.stormrealms.stormmobs.entity.RPGEntity;

@Component
public class CombatListeners implements Listener {
	@Autowired
	private CombatUtil util;
	@Autowired
	private CombatProcessor cProc;
	@Autowired
	private CombatCalculator cCalc;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void throwEvent(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		Entity damaged = e.getEntity();
		if (damager instanceof Player && (damaged instanceof LivingEntity)) {
			Player dPlayer = (Player) damager;
			if (damaged instanceof Player) {
				Bukkit.getPluginManager().callEvent(new PVPEvent());
				return;
			} else if (((CraftLivingEntity) damaged).getHandle() instanceof RPGEntity) {
				Bukkit.getPluginManager().callEvent(new PVMEvent(util.getRPGCharacter(dPlayer), dPlayer,
						(RPGEntity) ((CraftLivingEntity) damaged).getHandle()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void checkPVM(PVMEvent e) {
		Player bAttacker = e.getBukkitAttacker();
		ItemStack mainHand = bAttacker.getInventory().getItemInMainHand();
		if (mainHand != null && mainHand.hasItemMeta() && util.isRPGGear(mainHand))
			Bukkit.getPluginManager().callEvent(new WeaponAttackEvent(util.getRPGGearData(mainHand), e.getAttacker(),
					bAttacker, util.getOverallBonuses(bAttacker), e.getDamaged()));
		if (e.isDamagedKilled())
			return;
		ItemStack offHand = bAttacker.getInventory().getItemInOffHand();
		if (offHand != null && offHand.hasItemMeta() && util.isRPGGear(offHand))
			Bukkit.getPluginManager().callEvent(new WeaponAttackEvent(util.getRPGGearData(offHand), e.getAttacker(),
					bAttacker, util.getOverallBonuses(bAttacker), e.getDamaged()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void calculateAndProcess(WeaponAttackEvent e) {
		System.out.println("CALCULATE AND PROCESS");
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
