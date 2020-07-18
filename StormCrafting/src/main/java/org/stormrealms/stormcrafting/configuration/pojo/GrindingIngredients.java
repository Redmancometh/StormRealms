package org.stormrealms.stormcrafting.configuration.pojo;

import java.util.Map;

import lombok.Data;

@Data
public class GrindingIngredients {
	private Map<String, CraftingIngredient> ingredients;
}
