package org.stormrealms.stormmenus.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormmenus.Menus;
import org.stormrealms.stormmenus.absraction.Menu;
import org.stormrealms.stormmenus.absraction.SubMenu;
import org.stormrealms.stormmenus.menus.ClickType;

public class MenuListeners implements Listener {
	@EventHandler
	public void listenForBasicClick(InventoryClickEvent e) {
		UUID uuid = e.getWhoClicked().getUniqueId();
		if (e.getClickedInventory() != null && e.getView().getTitle() != null) {
			if (Menus.getInstance().getMenuManager().playerHasMenuOpen(uuid)) {
				e.setCancelled(true);
				Menu m = Menus.getInstance().getMenuManager().getMenuFromTitle(uuid);
				if (m != null && m.hasActionAt(e.getRawSlot())) {
					Player p = (Player) e.getWhoClicked();
					if (m.getActionAt(e.getRawSlot()) == null)
						return;
					m.getActionAt(e.getRawSlot()).accept(getClickType(e.isShiftClick(), e.isRightClick()), p);
				}
				// TODO: This cannot stay like this
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
			if (Menus.getInstance().getMenuManager().playerHasMenuOpen(uuid)) {
				Menu m = Menus.getInstance().getMenuManager().getMenuFromTitle(uuid);
				if (m instanceof SubMenu && (!e.getPlayer().hasMetadata("lowermenu"))) {
					((SubMenu) m).closeMenu((Player) e.getPlayer());
					e.getPlayer().removeMetadata("lowermenu", StormCore.getInstance());
				}
			}
		}
	}

	// TODO: Add filter for top/bottom menu clicks
	@EventHandler
	public void cancelLowerClick(InventoryClickEvent e) {
		UUID uuid = e.getWhoClicked().getUniqueId();
		if (e.getInventory() != null && e.getView().getTitle() != null) {
			if (Menus.getInstance().getMenuManager().playerHasMenuOpen(uuid)) {
				e.setCancelled(true);
				Menu m = Menus.getInstance().getMenuManager().getMenuFromTitle(uuid);
				// Player p = (Player) e.getWhoClicked();
				if (!m.allowsClickLower()) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}

}
