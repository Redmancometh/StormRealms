package org.stormrealms.stormcrafting.configuration.pojo;

import java.util.List;

import org.stormrealms.stormmenus.Icon;

import lombok.Data;

@Data
public class CraftingRecipe {
	private List<RecipeIngredient> ingredients;
	private Icon createdItem;
}
