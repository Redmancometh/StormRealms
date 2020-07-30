package org.stormrealms.stormcombat.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.RPGEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.combatsystem.PVMCombatCalculator;
import org.stormrealms.stormcombat.combatsystem.CombatProcessor;
import org.stormrealms.stormcombat.events.PVMEvent;
import org.stormrealms.stormcombat.events.PVPEvent;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcombat.util.CombatUtil;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormspigot.event.ChangeGearEvent;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.event.CharacterChosenEvent;

@Component
public class CombatListeners implements Listener {
	@Autowired
	private CombatUtil util;
	@Autowired
	private CombatProcessor cProc;
	@Autowired
	private PVMCombatCalculator cCalc;
	@Autowired
	@Qualifier("stat-cache")
	private Map<UUID, Map<RPGStat, Integer>> statCache;
	@Autowired
	private StatRepo repo;

	@EventHandler
	public void onEquip(ChangeGearEvent e) {
		ItemStack initialItem = e.getInitialItem();
		if (initialItem != null)
			System.out.println(initialItem.getType());
		ItemStack newItem = e.getFinalItem();
		boolean removingGear = util.isRPGGear(initialItem);
		boolean addingGear = util.isRPGGear(newItem);
		if (!removingGear && !addingGear)
			return;
		RPGGearData initialData = null;
		RPGGearData newData = null;
		if (removingGear)
			initialData = util.getRPGGearData(initialItem);
		if (addingGear)
			newData = util.getRPGGearData(newItem);
		Map<RPGStat, Integer> currentStats = statCache.getOrDefault(e.getPlayer().getUniqueId(), new HashMap());
		for (RPGStat stat : RPGStat.values()) {
			// We specifically DO NOT check currentStats.containsKey here, because if it
			// fails an item
			// was equipped, and it's bonuses weren't in the map. We want that to error very
			// loudly.
			if (removingGear && initialData.getBonuses().containsKey(stat)) {
				int initialStat = initialData.getBonuses().get(stat);
				currentStats.compute(stat, (otherStat, amt) -> amt - initialStat);
			}
			if (addingGear && newData.getBonuses().containsKey(stat)) {
				int bonus = newData.getBonuses().get(stat);
				currentStats.computeIfAbsent(stat, (otherStat) -> bonus);
				currentStats.computeIfPresent(stat, (otherStat, amt) -> bonus + amt);
			}
		}
		currentStats.forEach((stat, amt) -> System.out.println(stat + ": " + amt));
		statCache.put(e.getPlayer().getUniqueId(), currentStats);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void initialCache(CharacterChosenEvent e) {
		System.out.println("INITIAL CACHE");
		UUID uuid = e.getPlayer().getUniqueId();
		if (statCache.containsKey(uuid))
			return;
		System.out.println("Looking up record");
		repo.getRecord(uuid).thenAccept((rpgPlayer) -> {
			System.out.println(Bukkit.isPrimaryThread());
			System.out.println("Got record " + rpgPlayer);
			Map<RPGStat, Integer> statMap = new HashMap();
			ItemStack mh = e.getPlayer().getInventory().getItemInMainHand();
			ItemStack oh = e.getPlayer().getInventory().getItemInOffHand();
			List<ItemStack> items = new ArrayList(Arrays.asList(e.getPlayer().getInventory().getArmorContents()));
			if (util.isRPGGear(mh))
				items.add(mh);
			if (util.isRPGGear(oh))
				items.add(oh);
			for (ItemStack item : items) {
				if (util.isRPGGear(item)) {
					RPGGearData data = util.getRPGGearData(item);
					data.getBonuses().forEach((stat, bonus) -> {
						System.out.println("Bonus: " + stat + " " + bonus);
						statMap.computeIfAbsent(stat, (otherStat) -> bonus);
						statMap.computeIfPresent(stat, (otherStat, amt) -> bonus + amt);
					});
				}
			}
			rpgPlayer.getChosenCharacter().getStats().forEach((stat, amt) -> {
				statMap.computeIfAbsent(stat, (oldStat) -> amt);
				statMap.computeIfPresent(stat, (s, oldStat) -> amt + oldStat);
			});
			statMap.forEach((stat, amt) -> System.out.println(stat + ": " + amt));
			statCache.put(uuid, statMap);
			System.out.println("DONE ADDING PLAYER STATS");
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void throwEvent(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		Entity damaged = e.getEntity();
		if (damager instanceof Player && (damaged instanceof LivingEntity)) {
			Player dPlayer = (Player) damager;
			if (damaged instanceof Player) {
				Bukkit.getPluginManager().callEvent(new PVPEvent());
				return;
			} else if (damaged instanceof RPGEntity) {
				Bukkit.getPluginManager()
						.callEvent(new PVMEvent(util.getRPGCharacter(dPlayer), dPlayer, (RPGEntity) damaged));
				
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
		System.out.println("DMG: " + e.getDamage());
		cProc.hit(e);
	}
}
