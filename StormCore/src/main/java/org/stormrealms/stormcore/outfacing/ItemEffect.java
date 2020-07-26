package org.stormrealms.stormcore.outfacing;

import org.bukkit.event.Event;

public interface ItemEffect {
	public Class<? extends Event>[] eventClass();

}
