package org.stormrealms.stormstats.controllers;

import org.bukkit.entity.Player;
import org.stormrealms.stormstats.model.RPGPlayer;

public interface StatController {
	public RPGPlayer getRPGPlayer(Player p);
}
