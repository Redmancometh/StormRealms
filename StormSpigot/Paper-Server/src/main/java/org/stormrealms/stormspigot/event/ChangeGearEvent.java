package org.stormrealms.stormspigot.event;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ChangeGearEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private ItemStack finalItem;
	private HumanEntity player;
	private int slot;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ItemStack getInitialItem() {
		return player.getItemOnCursor();
	}
}
