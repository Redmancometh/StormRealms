package org.stormrealms.stormloot.controller;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormloot.configuration.pojo.ItemRoot;
import org.stormrealms.stormloot.configuration.pojo.LootPrefix;
import org.stormrealms.stormloot.configuration.pojo.LootRoll;
import org.stormrealms.stormloot.configuration.pojo.LootSuffix;
import org.stormrealms.stormloot.configuration.pojo.MasterLootConfig;
import org.stormrealms.stormmenus.Icon;

@Component
public class BasicLootSelector implements LootSelector {
	@Autowired
	private MasterLootConfig masterLootCfg;

	public ItemStack itemDrop(int level) {
		ItemStack item;
		if (Math.random() > .50)
			item = new ItemStack(generateItem(level, masterLootCfg.randomArmorPrefix(), masterLootCfg.randomArmorRoot(),
					masterLootCfg.randomArmorSuffix()));
		else
			item = new ItemStack(generateItem(level, masterLootCfg.randomWeaponPrefix(),
					masterLootCfg.randomWeaponRoot(), masterLootCfg.randomWeaponSuffix()));
		return item;
	}

	public ItemStack generateItem(int level, LootPrefix prefix, ItemRoot root, LootSuffix suffix) {
		Icon icon = root.getItem();
		String newName = prefix.getPrefix() + " " + icon.getDisplayName() + " " + suffix.getSuffix();
		ItemStack item = new ItemStack(icon.getMaterial());
		ItemMeta meta = item.getItemMeta();
		LootRoll prefixRoll = prefix.rollStats(level);
		LootRoll suffixRoll = suffix.rollStats(level);
		LootRoll rootRoll = root.rollStats(level);
		RPGGearData data = makeGearData(prefixRoll, suffixRoll, rootRoll);
		meta.setLore(buildLore(level, icon, prefixRoll, suffixRoll, data));
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + newName));
		data.attachTo(meta);
		item.setItemMeta(meta);
		return item;
	}

	public RPGGearData makeGearData(LootRoll prefixRoll, LootRoll suffixRoll, LootRoll rootRoll) {
		RPGGearData data = new RPGGearData();
		prefixRoll.forEach((stat, amount) -> data.addBonus(stat, amount));
		suffixRoll.forEach((stat, amount) -> data.addBonus(stat, amount));
		rootRoll.forEach((stat, amount) -> data.addBonus(stat, amount));
		return data;
	}

	private List<String> buildLore(int level, Icon icon, LootRoll prefixRoll, LootRoll suffixRoll, RPGGearData data) {
		List<String> lore = new ArrayList();
		data.getBonuses()
				.forEach((key, value) -> {
					String symbol = value > 0 ? "+" : "";
					lore.add("Overall " + key.getName() + ": " + symbol + value);
				});
		prefixRoll.forEach((key, value) -> {
			String symbol = value > 0 ? "+" : "";
			lore.add(prefixRoll.getText() + ": " + symbol + value + " " + key.getName());
		});
		suffixRoll.forEach((key, value) -> {
			String symbol = value > 0 ? "+" : "";
			lore.add(suffixRoll.getText() + ": " + symbol + value + " " + key.getName());
		});
		if (icon.getLore() != null)
			lore.addAll(icon.getLore());
		return lore;
	}

	@PostConstruct
	public void printMaster() {
		System.out.println("Proceed to echo master loot config!");
		System.out.println(masterLootCfg);
	}
}
