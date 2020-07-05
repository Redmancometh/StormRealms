package org.stormrealms.stormstats.menus;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGPlayer;

public class CreateCharacterMenu extends TypedMenu<RPGPlayer> {
	private TypedSelector typeSelector = new TypedSelector<RPGPlayer>();
	@Autowired
	private GUIConfig cfg;

	public CreateCharacterMenu() {
		super("Create Character", (p, rp) -> {
			Inventory inv = Bukkit.createInventory(null, 18);
			rp.getCharacters().forEach((character) -> {
				
			});
			return Bukkit.createInventory(p, 18);
		});
	}

	@PostConstruct
	public void addButtons() {
		setButton(cfg.getSetClass().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetClass().build(), (clickType, player, rPlayer) -> {
					
				}));
		setButton(cfg.getSetName().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetName().build(), (clickType, player, rPlayer) -> {

				}));
		setButton(cfg.getSetRace().getIndex(),
				new TypedMenuButton<RPGPlayer>((p, rp) -> cfg.getSetRace().build(), (clickType, player, rPlayer) -> {

				}));
	}

	@Override
	public TypedSelector getSelector() {
		return typeSelector;
	}

}
