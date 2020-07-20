package org.stormrealms.stormcombat.controllers;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcombat.listeners.CombatListeners;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormstats.model.RPGCharacter;

@Controller
public class CombatController {
	@Autowired
	private CombatListeners listeners;

	@PostConstruct
	public void registerListeners() {
		Bukkit.getPluginManager().registerEvents(listeners, StormCore.getInstance());
	}

	public double calculateHealthRegen(RPGCharacter character) {
		int level = character.getLevel();
		double maxHP = character.getMaxHealth();
		double baseRegen = (level * 1.35) + (.025 * maxHP);
		int spi = character.getStats().get(RPGStat.SPI);
		int sta = character.getStats().get(RPGStat.STA);
		double hp5 = ((spi * Math.sqrt(sta)) * baseRegen) * 20;
		return hp5;
	}

	public double calculateManaRegen(RPGCharacter character) {
		int level = character.getLevel();
		double maxHP = character.getMaxHealth();
		double baseRegen = (level * .135) + (.025 * maxHP);
		int spi = character.getStats().get(RPGStat.SPI);
		int sta = character.getStats().get(RPGStat.STA);
		double hp5 = ((spi * Math.sqrt(sta)) * baseRegen) * 20;
		return hp5;
	}
}
