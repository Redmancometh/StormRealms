package org.stormrealms.stormstats.menus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.Icon;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class CreateCharacterMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	private GUIConfig cfg;
	@Autowired
	private AutowireCapableBeanFactory factory;

	public CreateCharacterMenu() {
		super("Create Character", 27);
	}

	@PostConstruct
	public void addButtons() {

		setButton(cfg.getSetClass().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetClass().build(), (clickType, rPlayer, player) -> {
					ClassMenu classMenu = factory.getBean(ClassMenu.class);
					classMenu.open(player, rPlayer);
				}));
		setButton(cfg.getSetName().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetName().build(), (clickType, rPlayer, player) -> {
					rPlayer.getChosenCharacter().setCharacterName("Test Character");
				}));
		setButton(cfg.getSetRace().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetRace().build(), (clickType, rPlayer, player) -> {
					// RaceMenu raceMenu = factory.getBean(RaceMenu.class);
					// raceMenu.open(player, rPlayer);
					rPlayer.getChosenCharacter().setRace("Elf");
				}));
	}

	private Icon getTestIcon() {
		Icon testIcon = new Icon();
		testIcon.setMaterial(Material.ACACIA_BUTTON);
		testIcon.setDisplayName("Confirm Character Creation");
		List<String> lore = new ArrayList();
		lore.add("Chosen Name: " + getElement().getChosenCharacter().getCharacterName());
		lore.add("Chosen Class: " + getElement().getChosenCharacter().getData().getClassName());
		lore.add("Chosen Race: " + getElement().getChosenCharacter().getRace());
		testIcon.setLore(lore);
		testIcon.setDataValue((short) 0);
		return testIcon;
	}

	public void open(Player p, RPGPlayer e) {
		setSelected(e);
		if (e.getChosenCharacter() == null) {
			RPGCharacter newChar = factory.getBean(RPGCharacter.class);
			e.setChosenCharacter(newChar);
		}
		if (getElement().getChosenCharacter().isCharacterComplete()) {
			setButton(0, new TypedMenuButton<RPGPlayer>((player, rp) -> getTestIcon().build(),
					(clickType, rPlayer, player) -> {
						System.out.println("HOORAY");
					}));
		}
		super.open(p, e);
	}

	@Override
	public boolean shouldReopen() {
		System.out.println("ASKING SHOULD REOPEN!");
		System.out.println("ELEMENT NULL ");
		System.out.println("ELEMENT NULL " + (getElement() == null));
		System.out.println("ELEMENT NULL " + (getElement() == null));
		System.out.println("CHOSEN CHAR NULL: " + (getElement().getChosenCharacter() == null));
		boolean charComplete = getElement().getChosenCharacter() != null
				&& getElement().getChosenCharacter().isCharacterComplete();
		return !charComplete;
	}

}
