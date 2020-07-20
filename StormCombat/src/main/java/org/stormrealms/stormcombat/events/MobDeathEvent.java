package org.stormrealms.stormcombat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.stormrealms.stormmobs.entity.RPGEntity;
import org.stormrealms.stormstats.model.RPGPlayer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MobDeathEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Player bukkitKiller;
	private RPGPlayer killer;
	private RPGEntity mobLevel;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
