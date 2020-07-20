package org.stormrealms.stormstats.util;

import org.bukkit.Location;
import org.springframework.stereotype.Component;
import org.stormrealms.stormstats.model.RPGCharacter;

@Component
public class CharacterUtil {
	/**
	 * Warning this does no null check on anything involved.
	 * 
	 * @param character
	 * @param loc
	 */
	public void setCharacterLocation(RPGCharacter character, Location loc) {
		character.setWorld(loc.getWorld().getName());
		character.setX(loc.getX());
		character.setY(loc.getY());
		character.setZ(loc.getZ());
	}
}
