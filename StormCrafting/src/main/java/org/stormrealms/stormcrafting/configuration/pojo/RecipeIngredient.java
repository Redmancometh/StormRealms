package org.stormrealms.stormcrafting.configuration.pojo;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Data;

@Data
public class RecipeIngredient {
	private CraftingIngredient ingredient;
	private int qty;

	/**
	 * How many of the ingredient does the player have
	 * 
	 * @return
	 */
	public int hasX(Inventory inv) {
		int amount = 0;
		for (ItemStack item : inv) {
			if (item == null || item.getType() != ingredient.getMaterial() || !item.hasItemMeta())
				continue;
			ItemMeta meta = item.getItemMeta();
			if (meta.getDisplayName().equals(ingredient.getDisplayName()) && listsMatch(meta.getLore())) {
				amount += item.getAmount();
			}

		}
		return amount;
	}

	/**
	 * 
	 * @param lore
	 * @return
	 */
	private boolean listsMatch(List<String> lore) {

		if (lore.size() != ingredient.getLore().size())
			return false;
		for (int x = 0; x < lore.size(); x++) {
			if (lore.get(x).equals(ingredient.getLore().get(x)))
				return false;
		}

		return true;
	}

	public void removeFromInventory(Player p) {

	}

}
