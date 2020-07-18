package org.stormrealms.stormcrafting.listeners;

import javax.annotation.PostConstruct;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcrafting.menus.BrewingMenu;
import org.stormrealms.stormcrafting.menus.SmithingMenu;

@Component
public class CraftMenuListeners implements Listener {

	@Autowired
	private AutowireCapableBeanFactory factory;

	@PostConstruct
	public void register() {
		Bukkit.getPluginManager().registerEvents(this, StormCore.getInstance());
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.hasBlock()) {
			Player p = e.getPlayer();
			switch (e.getClickedBlock().getType()) {
			case ANVIL:
				e.setCancelled(true);
				SmithingMenu smithMenu = factory.getBean(SmithingMenu.class);
				smithMenu.open(p, 0);
			case BREWING_STAND:
				e.setCancelled(true);
				BrewingMenu brewMenu = factory.getBean(BrewingMenu.class);
				brewMenu.open(p, 0);
			case SPRUCE_WOOD:
				e.setCancelled(true);
				SmithingMenu menu = factory.getBean(SmithingMenu.class);
				menu.open(p, 0);
			default:
				break;
			}
		}
	}
}
