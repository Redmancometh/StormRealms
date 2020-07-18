package org.stormrealms.stormstats.menus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormmenus.Icon;
import org.stormrealms.stormmenus.MenuTemplate;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.configuration.pojo.Race;
import org.stormrealms.stormstats.configuration.pojo.RaceConfig;
import org.stormrealms.stormstats.model.ClassData;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class CreateCharacterMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	@Qualifier("gui-config")
	private ConfigManager<GUIConfig> cfg;
	@Autowired
	@Qualifier("race-config")
	private ConfigManager<RaceConfig> raceCfg;
	@Autowired
	@Qualifier("class-config")
	ConfigManager<ClassConfiguration> confMan;

	@Autowired
	private AutowireCapableBeanFactory factory;

	public CreateCharacterMenu(@Autowired @Qualifier("create-char-template") MenuTemplate template) {
		super("Create Character", template, 54);
		// decorateMenu();
	}

	@PostConstruct
	public void addButtons() {
		setButton(cfg.getConfig().getSetClass().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> getClassIcon().build(), (clickType, rPlayer, player) -> {
					ClassMenu classMenu = factory.getBean(ClassMenu.class);
					classMenu.openInstead(player, rPlayer);
				}));
		setButton(cfg.getConfig().getSetName().getIndex(), new TypedMenuButton<RPGPlayer>(
				(p, rp) -> cfg.getConfig().getSetName().build(), (clickType, rPlayer, player) -> {
					rPlayer.getChosenCharacter().setCharacterName("Test Character");
					this.openInstead(player, rPlayer);
				}));
		setButton(cfg.getConfig().getSetRace().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> getRaceIcon().build(), (clickType, rPlayer, player) -> {
					RaceMenu raceMenu = factory.getBean(RaceMenu.class);
					raceMenu.openInstead(player, rPlayer);
				}));
		setButton(43, new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getConfig().getSetRace().build(),
				(clickType, rPlayer, player) -> {
					player.kickPlayer("Aborted character creation!");
				}));
	}

	private Icon getRaceIcon() {
		String race = getSelected().getChosenCharacter().getRace();
		if (race != null) {
			for (Race cfgRace : raceCfg.getConfig().getRaces()) {
				if (cfgRace.getName().equals(race))
					return cfgRace.getRaceIcon();
			}
		}
		return cfg.getConfig().getSetRace();
	}

	private Icon getClassIcon() {
		ClassData data = getSelected().getChosenCharacter().getData();
		if (data == null || data.getClassName() == null)
			return cfg.getConfig().getSetClass();
		String clazz = getSelected().getChosenCharacter().getData().getClassName();
		if (clazz != null)
			return confMan.getConfig().getClassMap().get(clazz.toLowerCase()).getClassItem();
		return cfg.getConfig().getSetClass();
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
		boolean charComplete = getElement().getChosenCharacter() != null
				&& getElement().getChosenCharacter().isCharacterComplete();
		return !charComplete;
	}

}
