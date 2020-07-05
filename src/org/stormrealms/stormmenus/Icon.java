package org.stormrealms.stormmenus;

import java.util.List;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Icon {
	private int index;

	private List<String> lore;
	private String displayName;
	private Material material;
	private Short dataValue;

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

	@SuppressWarnings("deprecation")
	public ItemStack build(Function<String, String>... replacements) {
		ItemStack i = new ItemStack(material, 1, (short) dataValue);
		ItemMeta meta = i.getItemMeta();
		String newDisplay = displayName;
		for (int y = 0; y < replacements.length; y++)
			newDisplay = replacements[y].apply(newDisplay);
		meta.setDisplayName(newDisplay);
		meta.setLore(replaceAllLore(lore, replacements));
		i.setItemMeta(meta);
		return i;
	}

	private List<String> replaceAllLore(List<String> lore, Function<String, String>[] replacements) {
		for (int x = 0; x < lore.size(); x++) {
			String ogString = lore.get(x);
			for (int y = 0; y < replacements.length; y++) {
				ogString = replacements[y].apply(ogString);
			}
			lore.set(x, ogString);
		}
		return lore;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<String> getLore() {
		return lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Short getDataValue() {
		return dataValue;
	}

	public void setDataValue(Short dataValue) {
		this.dataValue = dataValue;
	}

}
