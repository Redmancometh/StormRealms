package org.stormrealms.stormmenus;

import java.util.List;

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

	// TODO: Add data values for now it's uninmportant.
	// TODO: Add placeholders
	public ItemStack build() {
		ItemStack i = new ItemStack(material);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
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
