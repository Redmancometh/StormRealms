package org.stormrealms.stormcore.scripting;

import org.bukkit.event.Event;

public interface StormEventHandler<T extends Event> {
	void accept(T event);
}
