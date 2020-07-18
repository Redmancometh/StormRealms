package org.stormrealms.stormloot.controller;

import org.bukkit.inventory.ItemStack;

public interface LootSelector {
	public ItemStack itemDrop(int level);
}
