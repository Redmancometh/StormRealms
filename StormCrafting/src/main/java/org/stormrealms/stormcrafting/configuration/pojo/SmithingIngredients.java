package org.stormrealms.stormcrafting.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class SmithingIngredients {
	private Map<String, CraftingIngredient> ingredients;
}
