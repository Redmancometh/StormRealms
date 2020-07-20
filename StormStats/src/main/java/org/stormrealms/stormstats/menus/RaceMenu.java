package org.stormrealms.stormstats.menus;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.RaceConfig;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
@Scope("prototype")
public class RaceMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	private RaceConfig cfg;
	@Autowired
	private AutowireCapableBeanFactory factory;
	private boolean raceChosen = false;

	public RaceMenu() {
		super("Race Menu", 54);
	}

	@PostConstruct
	public void addButtons() {
		AtomicInteger x = new AtomicInteger(0);
		cfg.getRaces().forEach((name, race) -> {
			TypedMenuButton<RPGPlayer> button = new TypedMenuButton<>((p, t) -> race.getRaceIcon().build());
			button.setAction((clickType, rpgPlayer, player) -> {
				rpgPlayer.getConstructingChar().setRace(name);
				// TODO: Add bonus to stats from race.
				CreateCharacterMenu menu = factory.getBean(CreateCharacterMenu.class);
				menu.open(player, rpgPlayer);
			});
			setButton(x.getAndIncrement(), button);
		});

	}

	@Override
	public boolean shouldReopen() {
		return raceChosen;
	}

}
