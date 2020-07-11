package org.stormrealms.stormmenus.menus;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.stormrealms.stormmenus.absraction.BaseTypedMenu;

public abstract class BaseTypedMenuButton<T> extends BaseMenuButton
{
	protected ItemStack processPlaceholders(ItemStack stack, BaseTypedMenu<?> menu, Player player, T t)
	{
		super.processPlaceholders(stack, menu, player);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(process(menu, meta.getDisplayName(), player, t));
		meta.setLore(meta.getLore().stream().map(s -> process(menu, s, player, t)).collect(Collectors.toList()));
		stack.setItemMeta(meta);
		return stack;
	}

	private String process(BaseTypedMenu<?> menu, String in, Player player, T t)
	{
		@SuppressWarnings("unchecked") // This should be safe
		Map<String, BiFunction<Player, T, String>> m = (Map<String, BiFunction<Player, T, String>>) menu.getTypedPlaceholders().entrySet();
		for (Entry<String, BiFunction<Player, T, String>> e : m.entrySet())
		{
			if (in.indexOf(e.getKey()) >= 0)
			{
				in = in.replace(e.getKey(), e.getValue().apply(player, t));
			}
		}
		return in;
	}
}
