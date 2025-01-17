package org.stormrealms.stormcombat.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.RPGEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.stormrealms.stormstats.model.RPGCharacter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MobAttackEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	@NonNull
	private final RPGCharacter attackingCharacter;
	@NonNull
	private final Player attackingPlayer;
	@NonNull
	private RPGEntity entity;
	private double damage;
	private boolean crushingBlow = false, isMiss = false, isDodged = false, glancingBlow = false, killingBlow = false;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
