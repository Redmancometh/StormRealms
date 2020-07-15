package org.stormrealms.stormcore.magic;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.springframework.stereotype.Component;

@Component
public class EventInterceptor {

	@BeginEvent(eventClass = BlockBreakEvent.class)
	public void onEvent(Event e) {
		System.out.println("ON BREAK");
	}
}
