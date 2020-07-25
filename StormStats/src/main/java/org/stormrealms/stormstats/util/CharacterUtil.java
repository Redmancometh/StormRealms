package org.stormrealms.stormstats.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
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

	public Location getLocation(RPGCharacter character) {
		return new Location(Bukkit.getWorld(character.getWorld()), character.getX(), character.getY(),
				character.getZ());
	}

	public void attachPermissions(RPGCharacter character, Player p) {
		PermissionAttachment perms = p.addAttachment(StormCore.getInstance());
		character.getFinalPerms().forEach((perm) -> perms.setPermission(perm, true));
	}

}
