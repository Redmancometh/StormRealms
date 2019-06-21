package org.stormrealms.stormcombat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.stormrealms.stormcombat.gear.RPGWeapon;
import org.stormrealms.stormstats.model.RPGPlayer;

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
	private final RPGWeapon weapon;
	@NonNull
	private final RPGPlayer player;
	@NonNull
	private final Player bukkitPlayer;
	private int damage;
	private boolean crushingBlow = false, glancingBlow = false, killingBlow = false;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
