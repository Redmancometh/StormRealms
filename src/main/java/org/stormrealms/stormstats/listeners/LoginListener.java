package org.stormrealms.stormstats.listeners;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormstats.data.RPGPlayerRepository;
import org.stormrealms.stormstats.model.RPGPlayer;

public class LoginListener implements Listener {
	@Autowired
	private RPGPlayerRepository players;

	@EventHandler
	public void onLogin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		Optional<RPGPlayer> player = players.findById(uuid);
		if (!player.isPresent()) {
			p.kickPlayer("");
		}
	}

	@EventHandler
	public void onLogout(PlayerQuitEvent e) {

	}
}
