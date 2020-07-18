package org.stormrealms.stormstats.menus;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class CharacterMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	private GUIConfig config;
	@Autowired
	private AutowireCapableBeanFactory factory;

	public CharacterMenu() {
		super("Character Menu", 18);

	}

	@PostConstruct
	public void addButtons() {
		setConstructInventory((p, rp) -> {
			Inventory i = Bukkit.createInventory(null, 18);
			System.out.println("Config: " + config);
			System.out.println("Config Char " + config.getCreateChar());
			TypedMenuButton<RPGPlayer> newChar = new TypedMenuButton<RPGPlayer>(
					(p2, rp2) -> config.getCreateChar().build(), (clickType, rPlayer, player) -> {
						CreateCharacterMenu createMenu = factory.getBean(CreateCharacterMenu.class);
						createMenu.open(p, rp);
					});
			setButton(config.getCreateChar().getIndex(), newChar);
			if (rp != null && rp.getCharacters() != null && rp.getCharacters().size() > 0) {
				for (int x = 0; x < rp.getCharacters().size(); x++) {
					setButton(x + config.getCharsStartAt(), new TypedMenuButton((p2, rp2) -> {
						ItemStack icon = config.getCharIcon().build();
						return icon;
					}));
				}
			}
			actionMap.forEach((number, button) -> {
				i.setItem(number, button.constructButton(rp, this, p));
			});
			return i;
		});
	}

	public void chooseCharacter(RPGPlayer rp, RPGCharacter rpgChar) {

	}

	@Override
	public boolean shouldReopen() {
		if (getElement().getChosenCharacter() != null && getElement().getChosenCharacter().isCharacterComplete())
			return false;
		return true;
	}

}
