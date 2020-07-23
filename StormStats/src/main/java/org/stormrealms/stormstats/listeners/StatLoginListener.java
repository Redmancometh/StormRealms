package org.stormrealms.stormstats.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormstats.configuration.pojo.StatMiscConfig;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.event.PlayerInitializedEvent;
import org.stormrealms.stormstats.menus.CharacterMenu;
import org.stormrealms.stormstats.menus.CreateCharacterMenu;
import org.stormrealms.stormstats.model.RPGCharacter;

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
	@Autowired
	private AutowireCapableBeanFactory factory;

	/**
	 * 
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PlayerJoinEvent e) {
		repo.getRecord(e.getPlayer().getUniqueId()).thenAccept((rpgPlayer) -> {
			Bukkit.getScheduler().scheduleSyncDelayedTask(StormCore.getInstance(), () -> {
				System.out.println("MAIN THREAD: " + Bukkit.isPrimaryThread());
				if (rpgPlayer.getChosenCharacter() != null) {
					PlayerInitializedEvent event = new PlayerInitializedEvent(rpgPlayer.getChosenCharacter(),
							e.getPlayer());
					Bukkit.getPluginManager().callEvent(event);
					System.out.println("Has a character already!");
					return;
				} else if (rpgPlayer.getCharacters().size() == 1) {
					System.out.println("SET CHOSEN CHARACTER");
					RPGCharacter chosen = rpgPlayer.getCharacters().iterator().next();
					PlayerInitializedEvent event = new PlayerInitializedEvent(chosen, e.getPlayer());
					rpgPlayer.setChosenCharacter(rpgPlayer.getCharacters().iterator().next());
					Bukkit.getPluginManager().callEvent(event);
				} else if (rpgPlayer.getCharacters().size() > 1) {
					System.out.println("OPENING CHAR SELECT MENU");
					CharacterMenu menu = factory.getBean(CharacterMenu.class);
					menu.open(e.getPlayer(), rpgPlayer);
				} else {
					System.out.println(miscCfg.getConfig());
					e.getPlayer().teleport(miscCfg.getConfig().getCharRoomLocation());
					System.out.println("CREATE CHAR MENU");
					CreateCharacterMenu menu = factory.getBean(CreateCharacterMenu.class);
					menu.open(e.getPlayer(), rpgPlayer);
				}
			});
		});
	}
}
