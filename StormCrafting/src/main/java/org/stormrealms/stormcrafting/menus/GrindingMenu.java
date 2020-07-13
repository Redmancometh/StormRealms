package org.stormrealms.stormcrafting.menus;

import java.util.List;

import javax.annotation.PostConstruct;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcrafting.configuration.pojo.RecipeIngredient;
import org.stormrealms.stormcrafting.configuration.pojo.SmithingConfig;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.model.RPGPlayer;

import net.md_5.bungee.api.ChatColor;

public class GrindingMenu extends TypedMenu<RPGPlayer> {
	private ConfigManager<SmithingConfig> cfg;

	public GrindingMenu(@Autowired ConfigManager<SmithingConfig> crafting) {
		super(crafting.getConfig().getMenuName());
		this.cfg = crafting;
	}

	@PostConstruct
	public void setup() {
		cfg.getConfig().getRecipes().forEach((recipe) -> {
			ItemStack itemBase = recipe.getCreatedItem().build();
			ItemMeta meta = itemBase.getItemMeta();
			TypedMenuButton<RPGPlayer> button = new TypedMenuButton();
			button.setButtonConstructor((p, rp) -> {
				List<String> lore = meta.getLore();
				for (RecipeIngredient ingredient : recipe.getIngredients()) {
					int amt = ingredient.hasX(p.getInventory());
					ChatColor color = amt >= ingredient.getQty() ? ChatColor.GREEN : ChatColor.DARK_RED;
					lore.add(color + ingredient.getIngredient().getDisplayName() + " " + amt + "/"
							+ ingredient.getQty());
				}
				itemBase.setLore(lore);
				return itemBase;
			});
			button.setAction((type, rp, p) -> {
				recipe.getIngredients().forEach((ingredient) -> ingredient.removeFromInventory(p));
				p.getInventory().addItem(recipe.getCreatedItem().build());
				p.sendMessage("MADE ITEM");
			});
		});
	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
