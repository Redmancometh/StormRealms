package org.stormrealms.stormmenus.absraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.stormrealms.stormmenus.menus.ClickType;
import org.stormrealms.stormmenus.menus.TriTypedButton;
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormmenus.util.QuadFunction;
import org.stormrealms.stormmenus.util.QuintConsumer;
import org.stormrealms.stormmenus.MenuTemplate;

public abstract class TriTypedMenu<T, U, V> extends BaseTypedMenu {
	protected Map<Integer, TriTypedButton<T, U, V>> actionMap = new ConcurrentHashMap<>();
	protected QuadFunction<Player, T, U, V, Inventory> constructInventory;

	public abstract TypedSelector getSelector();

	/**
	 * Construct a menu, and provide your own generified inventory constructor
	 * 
	 * @param name
	 * @param constructInventory
	 */
	public TriTypedMenu(String name, QuadFunction<Player, T, U, V, Inventory> constructInventory) {
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
	public TriTypedMenu(String name) {
		super(name, 18);
		this.constructInventory = (p, typedElement, typedElemnt2, typedElement3) -> {
			Inventory menuInv = Bukkit.createInventory(null, getSize(), this.getName());
			actionMap.forEach((number, button) -> menuInv.setItem(number,
					button.constructButton(typedElement, typedElemnt2, typedElement3, this, p)));
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
	public TriTypedMenu(String name, int size) {
		super(name, size);
		this.constructInventory = (p, typedElement, typedElement2, typedElement3) -> {
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			actionMap.forEach((number, button) -> menuInv.setItem(number,
					button.constructButton(typedElement, typedElement2, typedElement3, this, p)));
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
	public TriTypedMenu(String name, MenuTemplate template, int size) {
		super(name, size, template);
		this.constructInventory = (p, typedElement, typedElement2, typedElement3) -> {
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			actionMap.forEach((number, button) -> menuInv.setItem(number,
					button.constructButton(typedElement, typedElement2, typedElement3, this, p)));
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
	public TriTypedMenu(String name, QuadFunction<Player, T, U, V, Inventory> constructInventory, int size) {
		super(name, size);
		this.constructInventory = constructInventory;
	}

	public void setButton(int slot, TriTypedButton<T, U, V> button) {
		actionMap.put(slot, button);
	}

	public boolean hasActionAt(int slot) {
		return actionMap.containsKey(slot);
	}

	public QuintConsumer<ClickType, T, U, V, Player> getActionAt(int slot) {
		return actionMap.get(slot).getClickAction();
	}

	public QuadFunction<Player, T, U, V, Inventory> getConstructInventory() {
		return constructInventory;
	}

	public void setConstructInventory(QuadFunction<Player, T, U, V, Inventory> constructInventory) {
		this.constructInventory = constructInventory;
	}

	public TriTypedMenu decorateMenu() {
		// getTemplate().forEach((pane) -> pane.forEach((index) -> setButton(index, new
		// TriTypedButton((p, t, u, v) ->
		// ItemUtil.buildItem(Material.STAINED_GLASS_PANE, " ", pane.getColor(), new
		// ArrayList())))));
		return this;
	}
}
