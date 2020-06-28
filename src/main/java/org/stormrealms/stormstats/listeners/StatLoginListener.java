package org.stormrealms.stormstats.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.menus.ClassMenu;
import org.stormrealms.stormstats.model.RPGPlayer;

import com.redmancometh.redcore.util.SpecialFuture;

@Component
public class StatLoginListener implements Listener {
	@Autowired
	private StatRepo statRepo;
	@Autowired
	private AutowireCapableBeanFactory factory;

	@EventHandler
	public void onLogin(PlayerJoinEvent e) {
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
		statRepo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> {
			System.out.println(rpgPlayer);
			statRepo.saveAndPurge(rpgPlayer, e.getPlayer().getUniqueId())
					.thenRun(() -> System.out.println("DONE SAVING"));
		});
	}

}
