package org.stormrealms.stormmenus.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stormrealms.stormmenus.absraction.BaseMenu;
import org.stormrealms.stormmenus.util.QuadFunction;
import org.stormrealms.stormmenus.util.QuintConsumer;

public class TriTypedButton<T, U, V> extends BaseTypedMenuButton {
	private QuadFunction<Player, T, U, V, ItemStack> buttonConstructor;
	private QuintConsumer<ClickType, T, U, V, Player> clickAction;

	public TriTypedButton() {

	}

	public TriTypedButton(QuadFunction<Player, T, U, V, ItemStack> buttonConstructor) {
		this.buttonConstructor = buttonConstructor;
	}

	public TriTypedButton(QuadFunction<Player, T, U, V, ItemStack> buttonConstructor,
			QuintConsumer<ClickType, T, U, V, Player> clickAction) {
		this.buttonConstructor = buttonConstructor;
		this.clickAction = clickAction;

	}

	public void setAction(QuintConsumer<ClickType, T, U, V, Player> action) {
		this.clickAction = action;
	}

	public ItemStack constructButton(T t, U u, V v, BaseMenu menu, Player p) {
		return processPlaceholders(buttonConstructor.apply(p, t, u, v), menu, p);
	}

	public QuintConsumer<ClickType, T, U, V, Player> getClickAction() {
		return clickAction;
	}

	public void setClickAction(QuintConsumer<ClickType, T, U, V, Player> clickAction) {
		this.clickAction = clickAction;
	}
}
