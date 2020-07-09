package org.stormrealms.stormstats.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.SpecialFuture;
import org.stormrealms.stormstats.data.OtherStatRepo;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.menus.CharacterMenu;
import org.stormrealms.stormstats.menus.CreateCharacterMenu;
import org.stormrealms.stormstats.model.ClassData;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class StatLoginListener implements Listener {
	@Autowired
	private AutowireCapableBeanFactory factory;
	@Autowired
	private StatRepo repo;
	@Autowired
	@Qualifier("needs-character")
	private List<UUID> characterless;

	@EventHandler
	public void characterHandler(PlayerInteractEvent e) {
		System.out.println("PLAYER INTERACTED");
		repo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rp) -> {
			System.out.println("I JUST ACCEPTED");
				System.out.println("NOT PRESENT IN THE DB");
				RPGPlayer rPlayer = new RPGPlayer();
				CharacterMenu charmenu = factory.getBean(CharacterMenu.class);
				System.out.println("CHAR MENU " + charmenu);
				System.out.println("MAIN THREAD: " + Bukkit.isPrimaryThread());
				charmenu.open(e.getPlayer(), rPlayer);
		});
	}
	/*
	public void save(RPGPlayer rp) {
		System.out.println("SAVE RP PLAYER");
		repo.saveAndFlush(rp);
	}

	public void onLogout(PlayerQuitEvent e) {
		Location l = e.getPlayer().getLocation();
		SpecialFuture.supplyAsync(() -> repo.findById(e.getPlayer().getUniqueId())).thenAccept((rp) -> {
			System.out.println("PRESENT: " + rp.isPresent());
			if (rp.isPresent()) {
				System.out.println("FOUND IT");
				RPGPlayer rPlayer = rp.get();
				if (rp.get().getCharacters() == null) {
					Set<RPGCharacter> characters = new HashSet();
					RPGCharacter character = new RPGCharacter();
					character.setCharacterName("TEST");
					character.setX(l.getX());
					character.setY(l.getY());
					character.setZ(l.getZ());
					character.setWorld(l.getWorld().getName());
					characters.add(character);
					rPlayer.setCharacters(characters);
					ClassData classData = new ClassData();
					classData.setClassName("WARRIOR");
					character.setData(classData);
					save(rPlayer);
				}
			}
		});
	}*/

}
