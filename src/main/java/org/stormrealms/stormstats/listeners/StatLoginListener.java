package org.stormrealms.stormstats.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.util.SpecialFuture;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.menus.ClassMenu;
import org.stormrealms.stormstats.model.RPGCharacter;
import org.stormrealms.stormstats.model.RPGPlayer;

@Component
public class StatLoginListener implements Listener {
	@Autowired
	private StatRepo statRepo;
	@Autowired
	private AutowireCapableBeanFactory factory;

	@EventHandler
	public void onLogin(PlayerJoinEvent e) {
		System.out.println("JOIN EVENT");
		SpecialFuture<RPGPlayer> pF = statRepo.getRecord(e.getPlayer().getUniqueId());
		pF.thenAccept((p) -> {
			System.out.println(p);
		});
	}

	@EventHandler
	public void onClick(PlayerItemHeldEvent e) {
		statRepo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> {
			ClassMenu menu = factory.getBean(ClassMenu.class);
			menu.selectObject(rpgPlayer);
			menu.open(e.getPlayer(), rpgPlayer);
		});
	}

	@EventHandler
	public void onLogout(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Location l = p.getLocation();
		statRepo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> {
			System.out.println(rpgPlayer);
			Set<RPGCharacter> characters = new HashSet();
			RPGCharacter character = new RPGCharacter();
			character.setCharacterName("TEST");
			character.setX(l.getX());
			character.setY(l.getY());
			character.setZ(l.getZ());
			character.setWorld(l.getWorld().getName());
			characters.add(character);
			rpgPlayer.setCharacters(characters);
			statRepo.saveAndPurge(rpgPlayer, e.getPlayer().getUniqueId())
					.thenRun(() -> System.out.println("DONE SAVING"));
		});
	}

}
