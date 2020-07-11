package org.stormrealms.stormmenus.menus;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.stormrealms.stormmenus.absraction.BaseMenu;

public abstract class BaseMenuButton {
	protected ItemStack processPlaceholders(ItemStack stack, BaseMenu menu, Player player) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(process(menu, meta.getDisplayName(), player));
		if (stack.getItemMeta().getLore() == null)
			return stack;
		meta.setLore(meta.getLore().stream().map(s -> process(menu, s, player)).collect(Collectors.toList()));
		stack.setItemMeta(meta);
		return stack;
	}

	private String process(BaseMenu menu, String in, Player player) {
		for (Entry<String, Function<Player, String>> e : menu.getPlaceholders().entrySet()) {
			if (in.indexOf(e.getKey()) >= 0) {
				in = in.replace(e.getKey(), e.getValue().apply(player));
			}
		}
		return in;
	}
}
