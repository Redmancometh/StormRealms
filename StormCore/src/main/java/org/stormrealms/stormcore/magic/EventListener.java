package org.stormrealms.stormcore.magic;

import org.bukkit.event.Event;
import org.springframework.stereotype.Component;

@Component
public abstract class EventListener<T extends Event> {
	public abstract Class<T> getType();

}
