package org.stormrealms.stormmenus.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.absraction.SubMenu;
import org.stormrealms.stormmenus.absraction.TypedMenu;
import org.stormrealms.stormmenus.menus.ClickType;

@Component
public class TypedMenuListeners implements Listener {
	@Autowired
	private MenuManager manager;

	@EventHandler
	public void listenForBasicClick(InventoryClickEvent e) {
		System.out.println("IS MANAGER NULL? " + (manager == null));
		UUID uuid = e.getWhoClicked().getUniqueId();
		if (e.getClickedInventory() != null && e.getView().getTitle() != null) {
			if (manager.playerHasTypedMenuOpen(uuid)) {
				System.out.println("PLAYER HAS TYPED MENU OPEN");
				e.setCancelled(true);
				TypedMenu m = manager.getTypedMenuFromUUID(uuid);
				if (m != null && m.hasActionAt(e.getRawSlot())) {
					Player p = (Player) e.getWhoClicked();
					if (m.getActionAt(e.getRawSlot()) == null)
						return;
					m.getSelected();
					Object selected = m.getSelector().get(p.getUniqueId());
					if (selected == null)
						throw new IllegalStateException(
								"A menu of type has been clicked with no selector! Error. Type: " + m.getClass());
					ClickType type = getClickType(e.isShiftClick(), e.isRightClick());
					m.getActionAt(e.getRawSlot()).accept(type, selected, p);
				}
			} else {
				System.out.println("NO TYPED MENU DAFUQ YO");
				manager.map().forEach((id, menu) -> {
					System.out.println("UUID " + id + " has menu of type " + menu.getClass() + " open1");
				});
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
		if (e.getInventory() != null && e.getView() != null) {
			if (manager.playerHasTypedMenuOpen(uuid)) {
				TypedMenu m = manager.getTypedMenuFromUUID(uuid);
				if (m instanceof SubMenu && (!e.getPlayer().hasMetadata("lowermenu"))) {
					((SubMenu) m).closeMenu((Player) e.getPlayer());
					e.getPlayer().removeMetadata("lowermenu", StormCore.getInstance());
				}
				m.onClose((Player) e.getPlayer());
			}
		}
	}

	// TODO: Add filter for top/bottom menu clicks
	@EventHandler
	public void cancelLowerClick(InventoryClickEvent e) {
		UUID uuid = e.getWhoClicked().getUniqueId();
		if (e.getInventory() != null && e.getView().getTitle() != null) {
			if (manager.playerHasTypedMenuOpen(uuid)) {
				e.setCancelled(true);
				TypedMenu m = manager.getTypedMenuFromUUID(uuid);
				// Player p = (Player) e.getWhoClicked();
				if (!m.allowsClickLower()) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}
}
