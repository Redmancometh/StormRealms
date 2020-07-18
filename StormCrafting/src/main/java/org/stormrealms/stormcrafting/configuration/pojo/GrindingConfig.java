package org.stormrealms.stormcrafting.configuration.pojo;

import java.util.List;

import org.stormrealms.stormmenus.MenuTemplate;

import lombok.Data;

@Data
public class GrindingConfig {
	private List<CraftingRecipe> recipes;
	private String menuName;
	private MenuTemplate template;
	private List<Integer> recipeLocs;
}
