package org.stormrealms.stormloot.controller;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormloot.configuration.pojo.ArmorPrefix;
import org.stormrealms.stormloot.configuration.pojo.ArmorRoot;
import org.stormrealms.stormloot.configuration.pojo.ArmorSuffix;
import org.stormrealms.stormloot.configuration.pojo.LootRoll;
import org.stormrealms.stormloot.configuration.pojo.MasterLootConfig;
import org.stormrealms.stormloot.configuration.pojo.WeaponPrefix;
import org.stormrealms.stormloot.configuration.pojo.WeaponRoot;
import org.stormrealms.stormloot.configuration.pojo.WeaponSuffix;
import org.stormrealms.stormmenus.Icon;

@Component
public class BasicLootSelector implements LootSelector {
	@Autowired
	private MasterLootConfig masterLootCfg;

	public ItemStack armorDrop(int level) {
		ArmorPrefix prefix = masterLootCfg.randomArmorPrefix();
		ArmorRoot root = masterLootCfg.randomArmorRoot();
		ArmorSuffix suffix = masterLootCfg.randomArmorSuffix();
		ItemStack item = new ItemStack(generateArmor(level, prefix, root, suffix));
		return item;
	}

	public ItemStack generateArmor(int level, ArmorPrefix prefix, ArmorRoot root, ArmorSuffix suffix) {
		Icon icon = root.getItem();
		String newName = prefix.getPrefix() + icon.getDisplayName() + suffix.getSuffix();
		ItemStack item = new ItemStack(icon.getMaterial());
		ItemMeta meta = item.getItemMeta();
		LootRoll prefixRoll = prefix.rollStats(level);
		LootRoll suffixRoll = suffix.rollStats(level);
		meta.setLore(buildLore(level, icon, prefixRoll, suffixRoll));
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + newName));
		RPGArmorData data = makeArmorData(prefixRoll, suffixRoll, root);
		data.attachTo(meta);
		item.setItemMeta(meta);
		return item;
	}

	public RPGArmorData makeArmorData(LootRoll prefixRoll, LootRoll suffixRoll, ArmorRoot root) {
		RPGArmorData data = new RPGArmorData();
		data.setAgi(prefixRoll.get(RPGStat.AGI) + suffixRoll.get(RPGStat.AGI));
		data.setStr(prefixRoll.get(RPGStat.STR) + suffixRoll.get(RPGStat.STR));
		data.setSta(prefixRoll.get(RPGStat.STA) + prefixRoll.get(RPGStat.STA));
		data.setIntel(prefixRoll.get(RPGStat.INT) + prefixRoll.get(RPGStat.INT));
		data.setSpi(prefixRoll.get(RPGStat.SPI) + prefixRoll.get(RPGStat.SPI));
		data.setArmor(prefixRoll.get(RPGStat.ARMOR) + root.getArmor() + suffixRoll.get(RPGStat.ARMOR));
		return data;
	}

	public ItemStack weaponDrop(int level) {
		WeaponPrefix prefix = masterLootCfg.randomWeaponPrefix();
		WeaponRoot root = masterLootCfg.randomWeaponRoot();
		WeaponSuffix suffix = masterLootCfg.randomWeaponSuffix();
		return generateWeapon(level, prefix, root, suffix);
	}

	public ItemStack generateWeapon(int level, WeaponPrefix prefix, WeaponRoot root, WeaponSuffix suffix) {
		Icon icon = root.getItem();
		String newName = prefix.getPrefix() + icon.getDisplayName() + suffix.getSuffix();
		ItemStack item = new ItemStack(icon.getMaterial());
		ItemMeta meta = item.getItemMeta();
		LootRoll prefixRoll = prefix.rollStats(level);
		LootRoll suffixRoll = suffix.rollStats(level);
		meta.setLore(buildLore(level, icon, prefixRoll, suffixRoll));
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + newName));
		RPGWeaponData data = makeWeaponData(prefixRoll, suffixRoll, root);
		data.attachTo(meta);
		item.setItemMeta(meta);
		return item;
	}

	public RPGWeaponData makeWeaponData(LootRoll prefixRoll, LootRoll suffixRoll, WeaponRoot root) {
		RPGWeaponData data = new RPGWeaponData();
		data.setAgi(prefixRoll.get(RPGStat.AGI) + suffixRoll.get(RPGStat.AGI));
		data.setStr(prefixRoll.get(RPGStat.STR) + suffixRoll.get(RPGStat.STR));
		data.setSta(prefixRoll.get(RPGStat.STA) + prefixRoll.get(RPGStat.STA));
		data.setIntel(prefixRoll.get(RPGStat.INT) + prefixRoll.get(RPGStat.INT));
		data.setSpi(prefixRoll.get(RPGStat.SPI) + prefixRoll.get(RPGStat.SPI));
		data.setLow(prefixRoll.get(RPGStat.DMG_MIN) + root.getLowDmg() + suffixRoll.get(RPGStat.DMG_MIN));
		data.setLow(prefixRoll.get(RPGStat.DMG_MAX) + root.getHighDmg() + suffixRoll.get(RPGStat.DMG_MAX));
		return data;
	}

	private List<String> buildLore(int level, Icon icon, LootRoll prefixRoll, LootRoll suffixRoll) {
		List<String> lore = new ArrayList();
		prefixRoll.forEach((key, value) -> {
			String symbol = value > 0 ? "+" : "-";
			lore.add(prefixRoll.getText() + ": " + symbol + value);
		});
		suffixRoll.forEach((key, value) -> {
			String symbol = value > 0 ? "+" : "-";
			lore.add(suffixRoll.getText() + ": " + symbol + value);
		});
		lore.addAll(icon.getLore());
		return lore;
	}

	@PostConstruct
	public void printMaster() {
		System.out.println("Proceed to echo master loot config!");
		System.out.println(masterLootCfg);
	}
}
