package org.stormrealms.stormstats.listeners;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormstats.data.RPGPlayerRepository;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class StatLoginListener implements Listener {
	@Autowired
	private RPGPlayerRepository players;
	@Autowired
	@Qualifier(value = "player-cache")
	private Map<UUID, RPGPlayer> cache;

	@EventHandler
	@Transactional
	public void onLogin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		Optional<RPGPlayer> player = players.findById(uuid);
		if (player.isPresent()) {
			System.out.println("FOUND");
			System.out.println(player);
		} else {
			System.out.println("MAKING NEW");
			RPGPlayer newPlayer = new RPGPlayer();
			newPlayer.setPlayerId(uuid);
			newPlayer.setAgi(5);
			RPGPlayer rP = players.saveAndFlush(newPlayer);
			System.out.println("Size: " + players.findAll().size());
			System.out.println(rP);
		}
	}

	@EventHandler
	public void onLogout(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		Optional<RPGPlayer> player = players.findById(uuid);
		RPGPlayer rPlayer = player.get();
		rPlayer.setAgi(rPlayer.getAgi() + 1);

	}
}
