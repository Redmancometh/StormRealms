package org.stormrealms.stormstats.menus;

import javax.annotation.PostConstruct;

import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.absraction.SubMenu;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class CreateCharacterMenu extends TypedMenu<RPGPlayer> implements SubMenu {
	private TypedSelector typeSelector = new TypedSelector<RPGPlayer>();
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
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetClass().build(), (clickType, player, rPlayer) -> {
					ClassMenu classMenu = factory.getBean(ClassMenu.class);
					classMenu.open(rPlayer, player);
				}));
		setButton(cfg.getSetName().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetName().build(), (clickType, player, rPlayer) -> {
					// Set name via anvil GUI
				}));
		setButton(cfg.getSetRace().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetRace().build(), (clickType, player, rPlayer) -> {
					RaceMenu raceMenu = factory.getBean(RaceMenu.class);
					raceMenu.open(rPlayer, player);
				}));
	}

	@Override
	public TypedSelector getSelector() {
		return typeSelector;
	}

	@Override
	public void close(Player p) {
		// Go back to the CharacterMenu or back to this menu if a character isn't
		// completed.
	}

}
