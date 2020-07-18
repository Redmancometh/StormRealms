package org.stormrealms.stormmenus.menus;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.stormrealms.stormmenus.absraction.BaseMenu;
import org.stormrealms.stormmenus.util.TriConsumer;

import lombok.Getter;
import lombok.Setter;

public class TypedMenuButton<T> extends BaseTypedMenuButton {
	@Getter
	@Setter
	private BiFunction<Player, T, ItemStack> buttonConstructor;
	private TriConsumer<ClickType, T, Player> clickAction;

	public TypedMenuButton() {

	}

	public TypedMenuButton(BiFunction<Player, T, ItemStack> buttonConstructor) {
		this.buttonConstructor = buttonConstructor;
	}

	public TypedMenuButton(BiFunction<Player, T, ItemStack> buttonConstructor,
			TriConsumer<ClickType, T, Player> clickAction) {
		this.buttonConstructor = buttonConstructor;
		this.clickAction = clickAction;

	}

	public void setAction(TriConsumer<ClickType, T, Player> action) {
		this.clickAction = action;
	}

	public ItemStack constructButton(T t, BaseMenu menu, Player p) {
		return processPlaceholders(buttonConstructor.apply(p, t), menu, p);
	}

	public TriConsumer<ClickType, T, Player> getClickAction() {
		return clickAction;
	}

	public void setClickAction(TriConsumer<ClickType, T, Player> clickAction) {
		this.clickAction = clickAction;
	}
}
