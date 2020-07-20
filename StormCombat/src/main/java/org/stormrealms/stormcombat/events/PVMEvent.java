package org.stormrealms.stormcombat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.stormrealms.stormmobs.entity.RPGEntity;
import org.stormrealms.stormstats.model.RPGCharacter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PVMEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@NonNull
	private final RPGCharacter attacker;
	@NonNull
	private final Player bukkitAttacker;
	@NonNull
	private final RPGEntity damaged;
	private int damage = 0;
	private boolean damagedKilled = false;
}
