package org.stormrealms.stormstats.menus;

import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
import org.stormrealms.stormstats.util.CharacterUtil;

@Component
public class CharacterMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	private GUIConfig config;
	@Autowired
	private AutowireCapableBeanFactory factory;
	@Autowired
	private CharacterUtil util;

	public CharacterMenu() {
		super("Character Menu", 18);

	}

	@PostConstruct
	public void addButtons() {
		setConstructInventory((p, rp) -> {
			Inventory i = Bukkit.createInventory(null, getSize());
			System.out.println("Config: " + config);
			System.out.println("Config Char " + config.getCreateChar());
			setNewCharButton(p, rp);
			setCharacterButtons(rp, p, i);
			actionMap.forEach((number, button) -> i.setItem(number, button.constructButton(rp, this, p)));
			return i;
		});
	}

	public void setCharacterButtons(RPGPlayer rp, Player p, Inventory i) {
		if (rp != null && rp.getCharacters() != null && rp.getCharacters().size() > 0) {
			Iterator<RPGCharacter> charIter = rp.getCharacters().iterator();
			for (int x = 0; x < rp.getCharacters().size(); x++) {
				RPGCharacter chosen = charIter.next();
				TypedMenuButton<RPGPlayer> button = getButton(chosen, rp);
				setButton(x + config.getCharsStartAt(), button);
			}
		}
	}

	public void setNewCharButton(Player p, RPGPlayer rp) {
		TypedMenuButton<RPGPlayer> newChar = new TypedMenuButton<RPGPlayer>((p2, rp2) -> config.getCreateChar().build(),
				(clickType, rPlayer, player) -> {
					CreateCharacterMenu createMenu = factory.getBean(CreateCharacterMenu.class);
					createMenu.open(p, rp);
				});
		setButton(config.getCreateChar().getIndex(), newChar);
	}

	private TypedMenuButton getButton(RPGCharacter chosen, RPGPlayer rp) {
		TypedMenuButton<RPGPlayer> button = new TypedMenuButton((p2, rp2) -> {
			ItemStack icon = config.getCharIcon().build();
			return icon;
		});
		button.setAction((type, rPlayer, player) -> chooseCharacter(rPlayer, chosen, player));
		return button;
	}

	public void chooseCharacter(RPGPlayer rp, RPGCharacter chosen, Player player) {
		player.closeInventory();
		if (rp.getChosenCharacter() != null && rp.getChosenCharacter() == chosen)
			return;
		rp.setChosenCharacter(chosen);
		player.sendMessage("Chose character named: " + chosen.getCharacterName());
		player.teleport(util.getLocation(chosen));
		util.attachPermissions(chosen, player);
	}

	@Override
	public boolean shouldReopen() {
		if (getElement().getChosenCharacter() != null)
			return false;
		return true;
	}

}
