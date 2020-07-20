package org.stormrealms.stormstats.menus;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class CharacterStatMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	private GUIConfig config;
	@Autowired
	private AutowireCapableBeanFactory factory;

	public CharacterStatMenu() {
		super("Stat Menu", 18);

	}

	@PostConstruct
	public void addButtons() {

	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
