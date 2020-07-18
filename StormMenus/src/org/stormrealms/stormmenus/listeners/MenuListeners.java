package org.stormrealms.stormmenus.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.absraction.Menu;
import org.stormrealms.stormmenus.menus.ClickType;

@Component
public class MenuListeners implements Listener {
	@Autowired
	private MenuManager manager;

	@EventHandler
	public void listenForBasicClick(InventoryClickEvent e) {
		UUID uuid = e.getWhoClicked().getUniqueId();
		if (e.getClickedInventory() != null && e.getView().getTitle() != null) {
			if (manager.playerHasMenuOpen(uuid)) {
				e.setCancelled(true);
				Menu m = manager.getMenuFromTitle(uuid);
				if (m != null && m.hasActionAt(e.getRawSlot())) {
					Player p = (Player) e.getWhoClicked();
					if (m.getActionAt(e.getRawSlot()) == null)
						return;
					m.getActionAt(e.getRawSlot()).accept(getClickType(e.isShiftClick(), e.isRightClick()), p);
				}
			}
		}
	}

	public ClickType getClickType(boolean isShift, boolean isRight) {
		if (isShift) {
			if (isRight) {
				return ClickType.SHIFT_RIGHT;
			}
			return ClickType.SHIFT_LEFT;
		}
		if (isRight) {
			return ClickType.RIGHT;
		}
		return ClickType.LEFT;
	}

	@EventHandler
	public void cancelLowerClick(InventoryCloseEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		if (e.getInventory() != null && e.getView().getTitle() != null) {
			if (manager.playerHasMenuOpen(uuid)) {
				Menu m = manager.getMenuFromTitle(uuid);
				m.closeMenu((Player) e.getPlayer());
			}
		}
	}

	// TODO: Add filter for top/bottom menu clicks
	@EventHandler
	public void cancelLowerClick(InventoryClickEvent e) {
		UUID uuid = e.getWhoClicked().getUniqueId();
		if (e.getInventory() != null && e.getView().getTitle() != null) {
			if (manager.playerHasMenuOpen(uuid)) {
				e.setCancelled(true);
				Menu m = manager.getMenuFromTitle(uuid);
				// Player p = (Player) e.getWhoClicked();
				if (!m.allowsClickLower()) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}

}
