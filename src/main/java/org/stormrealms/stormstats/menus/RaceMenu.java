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
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormmenus.util.ItemUtil;
import org.stormrealms.stormstats.configuration.pojo.ClassConfiguration;
import org.stormrealms.stormstats.configuration.pojo.ClassInformation;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
@Scope("prototype")
public class RaceMenu extends TypedMenu<RPGPlayer> {
	private TypedSelector typeSelector = new TypedSelector<RPGPlayer>();
	@Autowired
	@Qualifier("class-config")
	ConfigManager<ClassConfiguration> confMan;

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
				player.closeInventory();
			});
			setButton(x.getAndIncrement(), button);
		});
	}

	public void setStartingStats(RPGPlayer rpPlayer, ClassInformation classInfo) {
		/*rpPlayer.setAgi(classInfo.getStartingAgi());
		rpPlayer.setStr(classInfo.getStartingStr());
		rpPlayer.setSpi(classInfo.getStartingSpi());
		rpPlayer.setIntel(classInfo.getStartingIntel());
		*/
	}

	@Override
	public TypedSelector getSelector() {
		return typeSelector;
	}

}
