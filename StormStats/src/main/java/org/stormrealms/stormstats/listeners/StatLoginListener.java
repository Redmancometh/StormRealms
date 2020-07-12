package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.SpecialFuture;
import org.stormrealms.stormstats.data.OtherStatRepo;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

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
	@Autowired
	private StatRepo repo;
	@Autowired
	private OtherStatRepo springRepo;

	@EventHandler
	public void onChat(PlayerJoinEvent e) {
		System.out.println("JOIN");
		repo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> {
			System.out.println("RPG PLAYER: " + rpgPlayer);
			RPGCharacter character = new RPGCharacter();
			Location loc = e.getPlayer().getLocation();
			character.setX(loc.getX());
			character.setY(loc.getY());
			character.setZ(loc.getZ());
			character.setWorld(loc.getWorld().getName());
			character.setRace("Orc");
			character.setCharacterName("Test");
			System.out.println("Chars: " + (rpgPlayer.getCharacters() == null));
			rpgPlayer.getCharacters().add(character);
			character.setRpgPlayer(rpgPlayer);
			System.out.println("Saving " + rpgPlayer);
			repo.save(rpgPlayer);
		}).get();
	}

	public void onJoin(PlayerJoinEvent e) {
		System.out.println("JOIN");
		SpecialFuture.supplyAsync(() -> springRepo.findById(e.getPlayer().getUniqueId())).thenAccept((rpOp) -> {
			System.out.println("MAIN THREAD: " + Bukkit.isPrimaryThread());
			if (!rpOp.isPresent()) {
				RPGPlayer rp = new RPGPlayer();
				rp.setDefaults(e.getPlayer().getUniqueId());
				RPGCharacter character = new RPGCharacter();
				Location loc = e.getPlayer().getLocation();
				character.setX(loc.getX());
				character.setY(loc.getY());
				character.setZ(loc.getZ());
				character.setWorld(loc.getWorld().getName());
				character.setRace("Orc");
				character.setCharacterName("Test");
				System.out.println("Chars: " + (rp.getCharacters() == null));
				rp.getCharacters().add(character);
				System.out.println("Saving " + rp);
				springRepo.saveAndFlush(rp);
			} else {
				System.out.println("FOUND: " + rpOp.get());
				springRepo.saveAndFlush(rpOp.get());
			}
		});
	}

}
