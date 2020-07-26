package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormstats.configuration.pojo.StatMiscConfig;
import org.stormrealms.stormstats.data.StatRepo;
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
	@Qualifier("stat-config")
	private ConfigManager<StatMiscConfig> miscCfg;
	@Autowired
	private StatRepo repo;

	@PostConstruct
	public void register() {
		System.out.println("Register storm players events");
		Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("StormCore"));
	}

	/**
	 * 
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerJoinEvent e) {
		repo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> Bukkit.getScheduler()
				.scheduleSyncDelayedTask(StormCore.getInstance(), () -> trySelectCharacter(e, rpgPlayer)));
	}

	/**
	 * 
	 * @param e
	 * @param rpgPlayer
	 */
	public void trySelectCharacter(PlayerJoinEvent e, RPGPlayer rpgPlayer) {
		if (rpgPlayer.getChosenCharacter() != null)
			return;
		e.getPlayer().teleport(miscCfg.getConfig().getCharRoomLocation());
	}
}
