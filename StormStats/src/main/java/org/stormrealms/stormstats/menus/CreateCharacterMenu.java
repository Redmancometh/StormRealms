package org.stormrealms.stormstats.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormmenus.Icon;
import org.stormrealms.stormmenus.MenuTemplate;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.ClassInformation;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.configuration.pojo.Race;
import org.stormrealms.stormstats.configuration.pojo.RaceConfig;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;
import org.stormrealms.stormstats.util.CharacterUtil;

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
	private StatRepo repo;
	@Autowired
	private AutowireCapableBeanFactory factory;
	@Autowired
	private CharacterUtil util;

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
					rPlayer.getConstructingChar().setCharacterName(UUID.randomUUID().toString());
					this.openInstead(player, rPlayer);
				}));
		setButton(cfg.getConfig().getSetRace().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> getRaceIcon().build(), (clickType, rPlayer, player) -> {
					RaceMenu raceMenu = factory.getBean(RaceMenu.class);
					raceMenu.openInstead(player, rPlayer);
				}));
		setButton(43, new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getConfig().getSetRace().build(),
				(clickType, rPlayer, player) -> { player.kickPlayer("Aborted character creation!"); }));
	}

	public void setStats() {
		RPGCharacter character = getElement().getConstructingChar();
		Race race = character.getRace();
		ClassInformation classInfo = confMan.getConfig().getClassMap().get(character.getClassData().getClassName());
		Map<RPGStat, Integer> statMap = new HashMap();
		statMap.putAll(classInfo.getStartingStats());
		race.getBonusStats().forEach((stat, amt) -> statMap.merge(stat, amt, (v1, v2) -> v1 + v2));
	}

	private Icon getRaceIcon() {
		Race race = getSelected().getConstructingChar().getRace();
		if (race == null)
			return cfg.getConfig().getSetRace();
		return race.getRaceIcon();
	}

	private Icon getClassIcon() {
		ClassInformation data = getSelected().getConstructingChar().getClassData();
		if (data == null || data.getClassName() == null)
			return cfg.getConfig().getSetClass();
		String clazz = getSelected().getConstructingChar().getClassData().getClassName();
		if (clazz != null)
			return confMan.getConfig().getClassMap().get(clazz.toLowerCase()).getClassItem();
		return cfg.getConfig().getSetClass();
	}

	private Icon getTestIcon() {
		Icon testIcon = new Icon();
		testIcon.setMaterial(Material.ACACIA_BUTTON);
		testIcon.setDisplayName("Confirm Character Creation");
		List<String> lore = new ArrayList();
		lore.add("Chosen Name: " + getElement().getConstructingChar().getCharacterName());
		lore.add("Chosen Class: " + getElement().getConstructingChar().getClassData().getClassName());
		lore.add("Chosen Race: " + getElement().getConstructingChar().getRace());
		testIcon.setLore(lore);
		testIcon.setDataValue((short) 0);
		return testIcon;
	}

	public void open(Player p, RPGPlayer e) {
		System.out.println("Constructing char: " + e.getConstructingChar());
		setSelected(e);
		if (e.getConstructingChar() == null) {
			RPGCharacter newChar = factory.getBean(RPGCharacter.class);
			e.setConstructingChar(newChar);
			return;
		}
		if (getElement().getConstructingChar().isCharacterComplete()) {
			setButton(42, new TypedMenuButton<RPGPlayer>((player, rp) -> getTestIcon().build(),
					(clickType, rPlayer, player) -> {
						player.closeInventory();
						player.sendMessage("Created Character!");
						setStats();
						util.setCharacterLocation(rPlayer.getConstructingChar(), player.getLocation());
						rPlayer.getCharacters().add(rPlayer.getConstructingChar());
						repo.save(rPlayer);
						rPlayer.setConstructingChar(null);
					}));
		}
		super.open(p, e);
	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
