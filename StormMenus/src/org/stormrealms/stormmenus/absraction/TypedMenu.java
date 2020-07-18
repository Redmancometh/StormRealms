package org.stormrealms.stormmenus.absraction;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormmenus.menus.ClickType;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.util.ItemUtil;
import org.stormrealms.stormmenus.util.PaneUtil;
import org.stormrealms.stormmenus.util.TriConsumer;

import lombok.Getter;
import lombok.Setter;

import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.MenuTemplate;

/***
 * TODO: ADD AN ON CLOSE CALLBACK FFS AND PURGE THE OBJECT FROM THE TYPE
 * SELECTOR
 * 
 * @author Redmancometh
 *
 * @param <T>
 */

public abstract class TypedMenu<T> extends BaseTypedMenu<T> {
	@Autowired
	private MenuManager manager;
	protected Map<Integer, TypedMenuButton<T>> actionMap = new ConcurrentHashMap<>();
	protected BiFunction<Player, T, Inventory> constructInventory;
	protected BiConsumer<Player, T> onClose;
	@Getter
	@Setter
	private T selected;
	@Getter
	@Setter
	protected boolean shouldReopen = false;
	@Getter
	@Setter
	protected boolean openingNewMenu = false;

	public void openInstead(Player p, T e) {
		p.setMetadata("opening", new FixedMetadataValue(StormCore.getInstance(), true));
		open(p, e);
		p.removeMetadata("opening", StormCore.getInstance());

	}

	public void open(Player p, T e) {
		if (e == null) {
			throw new IllegalStateException("You forgot to set the object value in a typed menu before calling open!");
		}
		this.selected = e;
		manager.setTypedPlayerMenu(p.getUniqueId(), this);
		this.selected = e;
		p.openInventory(constructInventory.apply(p, e));
	}

	public void onClose(Player p) {
		System.out.println("on close");
		if (openingNewMenu) {
			System.out.println("Opening new menu");
			return;
		}
		System.out.println("ON CLOSE CALLBACK CALLED");
		if (this.shouldReopen()) {
			System.out.println("SHOULD REOPEN");
			this.openingNewMenu = true;
			Bukkit.getScheduler().scheduleSyncDelayedTask(StormCore.getInstance(), () -> {
				this.open(p, selected);
				p.updateInventory();
				this.openingNewMenu = false;
			});
			return;
		}
		System.out.println("NVM SHOULDNT OPEN");
		if (this.onClose != null)
			this.onClose.accept(p, selected);
	}

	public T getElement() {
		return selected;
	}

	/**
	 * Construct a menu, and provide your own generified inventory constructor
	 * 
	 * @param name
	 * @param constructInventory
	 */
	public TypedMenu(String name, BiFunction<Player, T, Inventory> constructInventory) {
		super(name, 18);
		this.constructInventory = constructInventory;
	}

	/**
	 * Construct a menu with completely default parameters. This well default to
	 * size 9, and the default generified inventory constructor.
	 * 
	 * @param name
	 * @param size
	 */
	public TypedMenu(String name) {
		super(name, 18);
		decorateMenu();
		this.constructInventory = (p, typedElement) -> {
			Inventory menuInv = Bukkit.createInventory(null, getSize(), this.getName());
			this.selected = typedElement;
			actionMap.forEach((number, button) -> {
				menuInv.setItem(number, button.constructButton(typedElement, this, p));
			});
			return menuInv;
		};
	}

	/**
	 * Construct a menu with the default inventory constructor. This constructor
	 * will call each MenuButton on the menu, and set the inventory's items from
	 * MenuButton.constructButton(T t, Player p)
	 * 
	 * @param name
	 * @param size
	 */
	public TypedMenu(String name, int size) {
		super(name, size);
		this.constructInventory = (p, typedElement) -> {
			this.selected = typedElement;
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			actionMap.forEach(
					(number, button) -> menuInv.setItem(number, button.constructButton(typedElement, this, p)));
			return menuInv;
		};
	}

	/**
	 * Construct a menu with the default inventory constructor. This constructor
	 * will call each MenuButton on the menu, and set the inventory's items from
	 * MenuButton.constructButton(T t, Player p)
	 * 
	 * @param name
	 * @param size
	 */
	public TypedMenu(String name, MenuTemplate template, int size) {
		super(name, size, template);
		System.out.println("Template is null? " + (template == null));
		System.out.println("Called typed menu constructor with template: " + template);
		this.constructInventory = (p, typedElement) -> {
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			actionMap.forEach(
					(number, button) -> menuInv.setItem(number, button.constructButton(typedElement, this, p)));
			return menuInv;
		};
		decorateMenu();
	}

	/**
	 * Construct an inventory with both a custom constructor function, and
	 * non-default size
	 * 
	 * @param name
	 * @param size
	 */
	public TypedMenu(String name, BiFunction<Player, T, Inventory> constructInventory, int size) {
		super(name, size);
		this.constructInventory = constructInventory;
	}

	public void setButton(int slot, TypedMenuButton<T> button) {
		actionMap.put(slot, button);
	}

	public boolean hasActionAt(int slot) {
		return actionMap.containsKey(slot);
	}

	public TriConsumer<ClickType, T, Player> getActionAt(int slot) {
		return actionMap.get(slot).getClickAction();
	}

	public BiFunction<Player, T, Inventory> getConstructInventory() {
		return constructInventory;
	}

	public void setConstructInventory(BiFunction<Player, T, Inventory> constructInventory) {
		this.constructInventory = constructInventory;
	}

	public TypedMenu decorateMenu() {
		System.out.println("Is template null: " + (getTemplate() == null));
		getTemplate().forEach((pane) -> pane.forEach((index) -> setButton(index, new TypedMenuButton(
				(p, t) -> ItemUtil.buildItem(PaneUtil.getPaneFor(pane.getColor()), "", new ArrayList())))));
		return this;
	}

	/**
	 * THIS MUST ONLY be called menus utilizing the template system THIS MUST ONLY
	 * be called AFTER decorateMenu has been called, so that the action map is
	 * already populated!
	 */
	public int getNextBlankIndex() {
		if (this.actionMap.isEmpty() || this.actionMap == null)
			throw new IllegalStateException("Call only on templated menus AFTER decorateMenu is called!");
		for (int x = 0; x < this.getSize(); x++) {
			if (this.actionMap.containsKey(x))
				continue;
			return x;
		}
		return -1;
	}

	public abstract boolean shouldReopen();
}
