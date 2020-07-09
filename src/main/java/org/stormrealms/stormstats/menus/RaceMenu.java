package org.stormrealms.stormstats.menus;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.util.ItemUtil;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.ClassInformation;
import org.stormrealms.stormstats.model.ClassData;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
@Scope("prototype")
public class RaceMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	@Qualifier("class-config")
	ConfigManager<ClassConfiguration> confMan;
	private boolean raceChosen = false;

	public RaceMenu() {
		super("Class Menu", 54);
	}

	@PostConstruct
	public void addButtons() {
		AtomicInteger x = new AtomicInteger(0);
		confMan.getConfig().getClassMap().forEach((className, classInfo) -> {
			TypedMenuButton<RPGPlayer> button = new TypedMenuButton<>((p, t) -> {
				return ItemUtil.buildItem(classInfo.getClassItem(), classInfo.getClassName(), classInfo.getClassLore());
			});
			button.setAction((clickType, rpgPlayer, player) -> {
				setStartingStats(rpgPlayer, classInfo);
				raceChosen = true;
				player.closeInventory();
				CreateCharacterMenu menu = new CreateCharacterMenu();
				menu.open(player, rpgPlayer);
			});
			setButton(x.getAndIncrement(), button);
		});
	}

	@Override
	public boolean shouldReopen() {
		return raceChosen;
	}

	public void setStartingStats(RPGPlayer rpPlayer, ClassInformation classInfo) {
		rpPlayer.getChosenCharacter().setAgi(classInfo.getStartingAgi());
		rpPlayer.getChosenCharacter().setStr(classInfo.getStartingStr());
		rpPlayer.getChosenCharacter().setSpi(classInfo.getStartingSpi());
		rpPlayer.getChosenCharacter().setIntel(classInfo.getStartingIntel());
		ClassData data = rpPlayer.getChosenCharacter().getData();
		data.setCharacter(rpPlayer.getChosenCharacter());
		data.setClassName(classInfo.getClassName());
	}

}
