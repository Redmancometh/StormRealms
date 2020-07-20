package org.stormrealms.stormstats.menus;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.ClassInformation;
import org.stormrealms.stormstats.model.ClassData;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
@Scope("prototype")
public class ClassMenu extends TypedMenu<RPGPlayer> {
	@Autowired
	@Qualifier("class-config")
	ConfigManager<ClassConfiguration> confMan;
	@Autowired
	private AutowireCapableBeanFactory factory;

	public ClassMenu() {
		super("Class Menu", 54);
	}

	@PostConstruct
	public void addButtons() {
		AtomicInteger x = new AtomicInteger(0);
		confMan.getConfig().getClassMap().forEach((className, classInfo) -> {
			TypedMenuButton<RPGPlayer> button = new TypedMenuButton<>((p, t) -> classInfo.getClassItem().build());
			button.setAction((clickType, rpgPlayer, player) -> {
				ClassData data = new ClassData();
				data.setClassName(className);
				RPGCharacter character = rpgPlayer.getConstructingChar();
				data.setCharacter(character);
				character.setData(data);
				CreateCharacterMenu charMenu = factory.getBean(CreateCharacterMenu.class);
				charMenu.open(player, rpgPlayer);
			});
			setButton(x.getAndIncrement(), button);
		});
	}

	@Override
	public void onClose(Player p) {
		super.onClose(p);
	}

	public void setStartingStats(RPGCharacter rpChar, ClassInformation classInfo) {

	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
