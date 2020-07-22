package org.stormrealms.stormcombat.combatsystem.menus;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.configuration.pojo.CombatGUIConfig;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.GUIConfig;
import org.stormrealms.stormstats.model.RPGCharacter;

@Component
@Scope("prototype")
public class CharacterStatMenu extends TypedMenu<RPGCharacter> {
	@Autowired
	private GUIConfig config;
	@Autowired
	@Qualifier("combat-cfg-man")
	private ConfigManager<CombatGUIConfig> cfg;
	@Autowired
	private AutowireCapableBeanFactory factory;

	public CharacterStatMenu() {
		super("Stat Menu", 18);

	}

	@PostConstruct
	public void addButtons() {
		AtomicInteger index = new AtomicInteger(cfg.getConfig().getStartingIndex());
		getSelected().getStats().forEach((stat, amt) -> {
			TypedMenuButton button = new TypedMenuButton((p, c) -> {
				// TODO: Set Placeholders
				return cfg.getConfig().getStatIcon().build();
			});
			setButton(index.getAndIncrement(), button);
		});
	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
