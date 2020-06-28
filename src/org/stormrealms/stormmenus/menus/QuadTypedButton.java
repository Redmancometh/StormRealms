package org.stormrealms.stormmenus.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stormrealms.stormmenus.absraction.BaseMenu;
import org.stormrealms.stormmenus.util.QuintFunction;
import org.stormrealms.stormmenus.util.SextConsumer;

public class QuadTypedButton<T, U, V, X> extends BaseTypedMenuButton {
	private QuintFunction<Player, T, U, V, X, ItemStack> buttonConstructor;
	private SextConsumer<ClickType, T, U, V, X, Player> clickAction;

	public QuadTypedButton() {

	}

	public QuadTypedButton(QuintFunction<Player, T, U, V, X, ItemStack> buttonConstructor) {
		this.buttonConstructor = buttonConstructor;
	}

	public QuadTypedButton(QuintFunction<Player, T, U, V, X, ItemStack> buttonConstructor,
			SextConsumer<ClickType, T, U, V, X, Player> clickAction) {
		this.buttonConstructor = buttonConstructor;
		this.clickAction = clickAction;

	}

	public void setAction(SextConsumer<ClickType, T, U, V, X, Player> action) {
		this.clickAction = action;
	}

	public ItemStack constructButton(T t, U u, V v, X x, BaseMenu menu, Player p) {
		return processPlaceholders(buttonConstructor.apply(p, t, u, v, x), menu, p);
	}

	public SextConsumer<ClickType, T, U, V, X, Player> getClickAction() {
		return clickAction;
	}

	public void setClickAction(SextConsumer<ClickType, T, U, V, X, Player> clickAction) {
		this.clickAction = clickAction;
	}
}
