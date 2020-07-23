package org.stormrealms.stormmenus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Data;

@Data
public class Icon {
	private int index;
	private List<String> lore;
	private String displayName;
	private Material material;
	private Short dataValue = 0;

	public Icon() {

	}

	// TODO: Add placeholders
	@SuppressWarnings("deprecation")
	public ItemStack build() {
		ItemStack i = new ItemStack(material, 1, (short) dataValue);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
	}

	// TODO: Add placeholders
	@SuppressWarnings("deprecation")
	public ItemStack build(Function<String, String>... replacements) {
		List<String> newList = new ArrayList();
		newList.addAll(lore);
		ItemStack i = new ItemStack(material, 1, (short) dataValue);
		ItemMeta meta = i.getItemMeta();
		String newDisplay = displayName;
		for (int y = 0; y < replacements.length; y++)
			newDisplay = replacements[y].apply(newDisplay);
		meta.setDisplayName(newDisplay);
		meta.setLore(replaceAllLore(newList, replacements));
		i.setItemMeta(meta);
		return i;
	}

	private List<String> replaceAllLore(List<String> initialLore, Function<String, String>[] replacements) {
		for (int x = 0; x < initialLore.size(); x++) {
			String ogString = initialLore.get(x);
			for (int y = 0; y < replacements.length; y++) {
				ogString = replacements[y].apply(ogString);
			}
			initialLore.set(x, ogString);
		}
		return initialLore;
	}

}
