package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Redmancometh
 *
 */
@Component
public class StatLoginListener implements Listener {
	@Autowired
	@Qualifier("needs-character")
	private List<UUID> characterless;
	/*
	@Autowired
	private StatRepo repo;

	private RateLimiter limiter = new RateLimiter(TimeUnit.SECONDS, 10, (p) -> {
		p.sendMessage("TEST");
	});*/

	@EventHandler
	public void onChat(PlayerInteractEvent e) {

	}

}
