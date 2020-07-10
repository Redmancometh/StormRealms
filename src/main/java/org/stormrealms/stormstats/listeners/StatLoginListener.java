package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.util.RateLimiter;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.menus.CreateCharacterMenu;
import org.stormrealms.stormstats.model.RPGPlayer;

/**
 * 
 * @author Redmancometh
 *
 */
@Component
public class StatLoginListener implements Listener {
	@Autowired
	private AutowireCapableBeanFactory factory;
	@Autowired
	private StatRepo repo;
	@Autowired
	@Qualifier("needs-character")
	private List<UUID> characterless;

	private RateLimiter limiter = new RateLimiter(TimeUnit.SECONDS, 10, (p) -> {
		p.sendMessage("TEST");
	});

	@EventHandler
	public void onChat(PlayerJoinEvent e) {
		repo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rp) -> {
			RPGPlayer rPlayer = new RPGPlayer();
			Bukkit.getScheduler().scheduleSyncDelayedTask(StormCore.getInstance(), () -> {
				limiter.tryAction(e.getPlayer(), () -> {
					CreateCharacterMenu charmenu = factory.getBean(CreateCharacterMenu.class);
					// charmenu.open(e.getPlayer(), rPlayer);
				}, () -> System.out.println("FAIL"));
			});
		});
	}

}
