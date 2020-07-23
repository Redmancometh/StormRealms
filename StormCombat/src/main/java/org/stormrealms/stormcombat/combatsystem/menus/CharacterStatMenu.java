package org.stormrealms.stormcombat.combatsystem.menus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.configuration.pojo.CombatGUIConfig;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormstats.model.RPGCharacter;

@Component
@Scope("prototype")
public class CharacterStatMenu extends TypedMenu<RPGCharacter> {
	@Autowired
	@Qualifier("combat-cfg-man")
	private ConfigManager<CombatGUIConfig> cfg;
	@Autowired
	@Qualifier("stat-cache")
	private Map<UUID, Map<RPGStat, Integer>> statCache;

	public CharacterStatMenu() {
		super("Stat Menu", 18);
	}

	@PostConstruct
	public void addButtons() {
		AtomicInteger index = new AtomicInteger(cfg.getConfig().getStartingIndex());
		setConstructInventory((p, rp) -> {
			Map<RPGStat, Integer> cached = statCache.get(p.getUniqueId());
			Inventory i = Bukkit.createInventory(null, getSize());
			for (RPGStat stat : RPGStat.values()) {
				AtomicInteger amt = new AtomicInteger(0);
				if (cached.containsKey(stat))
					amt.set(cached.get(stat));
				TypedMenuButton button = new TypedMenuButton((player, c) -> cfg.getConfig().getStatIcon().build(
						(string) -> string.replace("%a", stat.getName()),
						(string2) -> string2.replace("%n", amt.get() + "")));
				setButton(index.getAndIncrement(), button);
			}
			actionMap.forEach((number, button) -> i.setItem(number, button.constructButton(rp, this, p)));
			return i;
		});
	}

	@Override
	public boolean shouldReopen() {
		return false;
	}

}
