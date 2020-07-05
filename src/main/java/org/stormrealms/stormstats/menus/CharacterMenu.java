package org.stormrealms.stormstats.menus;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

public class CharacterMenu extends TypedMenu<RPGPlayer> {
	private TypedSelector typeSelector = new TypedSelector<RPGPlayer>();
	@Autowired
	private GUIConfig config;

	public CharacterMenu() {
		super("Character Menu", 18);

	}

	@PostConstruct
	public void addButtons() {
		setConstructInventory((p, rp) -> {
			Inventory i = Bukkit.createInventory(null, 18);
			for (int x = 0; x < rp.getCharacters().size(); x++) {
				setButton(x + config.getCharsStartAt(), new TypedMenuButton((p2, rp2) -> {
					ItemStack icon = config.getCharIcon().build();
					return icon;
				}));
			}
			return i;
		});
	}

	public void chooseCharacter(RPGPlayer rp, RPGCharacter rpgChar) {

	}

	@Override
	public TypedSelector getSelector() {
		return typeSelector;
	}

}
