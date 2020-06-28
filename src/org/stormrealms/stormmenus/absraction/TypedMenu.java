package org.stormrealms.stormmenus.absraction;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.stormrealms.stormmenus.menus.ClickType;
import org.stormrealms.stormmenus.menus.TypedMenuButton;
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormmenus.util.ItemUtil;
import org.stormrealms.stormmenus.util.PaneUtil;
import org.stormrealms.stormmenus.util.TriConsumer;
import org.stormrealms.stormmenus.MenuTemplate;
import org.stormrealms.stormmenus.Menus;

/***
 * TODO: ADD AN ON CLOSE CALLBACK FFS AND PURGE THE OBJECT FROM THE TYPE
 * SELECTOR
 * 
 * @author Redmancometh
 *
 * @param <T>
 */

public abstract class TypedMenu<T> extends BaseTypedMenu<T> {
	protected Map<Integer, TypedMenuButton<T>> actionMap = new ConcurrentHashMap<>();
	protected BiFunction<Player, T, Inventory> constructInventory;
	protected BiConsumer<Player, T> onClose;
	private T e;

	public void open(Player p, T e) {
		if (e == null)
			throw new IllegalStateException("You forgot to set the object value in a typed menu before calling open!");
		Menus.getInstance().getMenuManager().setTypedPlayerMenu(p.getUniqueId(), this);
		System.out.println("IS E NULL? " + (e == null));
		System.out.println("Template is null? " + (template == null));
		this.e = e;
		p.openInventory(constructInventory.apply(p, e));
	}

	public void onClose(Player p) {
		if (this.onClose != null)
			this.onClose.accept(p, e);
	}

	public abstract TypedSelector getSelector();

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
		this.constructInventory = (p, typedElement) -> {
			Inventory menuInv = Bukkit.createInventory(null, getSize(), this.getName());
			getSelector().select(p.getUniqueId(), typedElement);
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
			getSelector().select(p.getUniqueId(), typedElement);
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
		this.constructInventory = (p, typedElement) -> {
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			System.out.println("Template is null? " + (template == null));
			if (template != null) {
				decorateMenu();
			}
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

	public T getSelected() {
		return e;
	}

	public void selectObject(T e) {
		this.e = e;
	}
}
