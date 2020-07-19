package org.stormrealms.stormcombat.events;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormstats.model.RPGCharacter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WeaponAttackEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	@NonNull
	private final RPGGearData weapon;
	@NonNull
	private final RPGCharacter player;
	@NonNull
	private final Player bukkitPlayer;
	@NonNull
	private Map<RPGStat, Integer> totalBonuses;
	private int damage;
	private boolean crushingBlow = false, isMiss = false, isDodged = false, glancingBlow = false, killingBlow = false;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
