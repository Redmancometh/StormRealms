package org.stormrealms.stormmenus.absraction;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.stormrealms.stormmenus.Menus;

public interface SubMenu {
	public abstract void close(Player p);

	public default void closeMenu(Player p) {
		p.setMetadata("lowermenu", new FixedMetadataValue(Menus.getInstance(), p));
		close(p);
	}
}
