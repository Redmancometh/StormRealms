package org.stormrealms.stormloot.controller;

import org.bukkit.inventory.ItemStack;

public interface LootSelector {
	public ItemStack armorDrop(int level);

	public ItemStack weaponDrop(int level);
}
