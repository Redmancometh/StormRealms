package org.stormrealms.stormcrafting.menus;

import java.util.List;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcrafting.configuration.pojo.CraftingRecipe;
import org.stormrealms.stormcrafting.configuration.pojo.RecipeIngredient;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormcrafting.configuration.pojo.BrewingConfig;
import org.stormrealms.stormcrafting.configuration.pojo.BrewingIngredients;
import net.md_5.bungee.api.ChatColor;

@Component
@Scope("prototype")
public class BrewingMenu extends TypedMenu<Integer> {
	private ConfigManager<BrewingConfig> cfg;
	ConfigManager<BrewingIngredients> BrewingIngredients;

	public BrewingMenu(@Autowired ConfigManager<BrewingConfig> crafting) {
		super(crafting.getConfig().getMenuName(), crafting.getConfig().getTemplate(), 54);
		this.cfg = crafting;
	}

	@PostConstruct
	public void setup() {
		/*
		this.setConstructInventory((p, page) -> {
			decorateMenu();
			List<Integer> recipeLocs = cfg.getConfig().getRecipeLocs();
			List<CraftingRecipe> recipes = cfg.getConfig().getRecipes();
			Inventory menuInv = Bukkit.createInventory(null, this.getSize());
			int start = getElement() * recipeLocs.size();
			int endOfPage = start + recipeLocs.size();
			int endRecipes = recipes.size();
			for (int x = getElement() * recipeLocs.size(); x < endOfPage && x < endRecipes; x++) {
				CraftingRecipe recipe = recipes.get(x);
				ItemStack itemBase = recipe.getCreatedItem().build();
				ItemMeta meta = itemBase.getItemMeta();
				TypedMenuButton<Integer> button = new TypedMenuButton();
				button.setButtonConstructor((p2, rp) -> {
					List<String> lore = meta.getLore();
					for (RecipeIngredient ingredient : recipe.getIngredients()) {
						int amt = ingredient.hasX(p.getInventory());
						ChatColor color = amt >= ingredient.getQuantity() ? ChatColor.GREEN : ChatColor.DARK_RED;
						//lore.add(color + ingredient.getIngredient().getDisplayName() + " " + amt + "/"
							//	+ ingredient.getQty());
					}
					itemBase.setLore(lore);
					return itemBase;
				});
				button.setAction((type, rp, p2) -> {
					recipe.getIngredients().forEach((ingredient) -> ingredient.removeFromInventory(p));
					p.getInventory().addItem(recipe.getCreatedItem().build());
					p.sendMessage("MADE ITEM");
				});
			}
			this.actionMap.forEach((number, actionButton) -> menuInv.setItem(number,
					actionButton.constructButton(getElement(), this, p)));
			return menuInv;
		});*/
	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
