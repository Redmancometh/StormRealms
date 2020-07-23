package org.stormrealms.stormstats.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.stormrealms.stormstats.model.RPGCharacter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
/**
 * This event is to be thrown after a player has their stats initialized, and a
 * character has been selected.
 * 
 * This event will be called in 2 cases:
 * 
 * A) A player logs in who already has a character selected as a flag to other
 * plugins to cache things.
 * 
 * B) A player who didn't have a character selected selects a character or
 * confirms their character creation.
 * 
 * 
 * This event occurs after the player has been teleported to the last location
 * their character has been saved in.
 * 
 * @author Redmancometh
 *
 */
public class PlayerInitializedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private RPGCharacter character;
	private Player player;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
