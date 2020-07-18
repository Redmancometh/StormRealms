package org.stormrealms.stormcrafting.configuration.pojo;

import java.util.List;

import org.bukkit.Material;

import lombok.Data;

@Data
public class CraftingIngredient {
	private String displayName;
	private List<String> lore;
	private Material material;
}
