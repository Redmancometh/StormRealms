package org.stormrealms.stormmenus.absraction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.stormrealms.stormmenus.menus.ClickType;
import org.stormrealms.stormmenus.menus.QuadTypedButton;
import org.stormrealms.stormmenus.menus.TypedSelector;
import org.stormrealms.stormmenus.util.QuintFunction;
import org.stormrealms.stormmenus.util.SextConsumer;
import org.stormrealms.stormmenus.MenuTemplate;

public abstract class QuadTypedMenu<T, U, V, X> extends BaseTypedMenu {
	protected Map<Integer, QuadTypedButton<T, U, V, X>> actionMap = new ConcurrentHashMap<>();
	protected QuintFunction<Player, T, U, V, X, Inventory> constructInventory;

	public abstract TypedSelector getSelector();

	/**
	 * Construct a menu, and provide your own generified inventory constructor
	 * 
	 * @param name
	 * @param constructInventory
	 */
	public QuadTypedMenu(String name, QuintFunction<Player, T, U, V, X, Inventory> constructInventory) {
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
	public QuadTypedMenu(String name) {
		super(name, 18);
		this.constructInventory = (p, typedElement, typedElemnt2, typedElement3, typedElement4) -> {
			Inventory menuInv = Bukkit.createInventory(null, getSize(), this.getName());
			actionMap.forEach((number, button) -> menuInv.setItem(number,
					button.constructButton(typedElement, typedElemnt2, typedElement3, typedElement4, this, p)));
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
	public QuadTypedMenu(String name, int size) {
		super(name, size);
		this.constructInventory = (p, typedElement, typedElement2, typedElement3, typedElement4) -> {
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			actionMap.forEach((number, button) -> menuInv.setItem(number,
					button.constructButton(typedElement, typedElement2, typedElement3, typedElement4, this, p)));
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
	public QuadTypedMenu(String name, MenuTemplate template, int size) {
		super(name, size, template);
		this.constructInventory = (p, typedElement, typedElement2, typedElement3, typedElement4) -> {
			Inventory menuInv = Bukkit.createInventory(null, size, this.getName());
			actionMap.forEach((number, button) -> menuInv.setItem(number,
					button.constructButton(typedElement, typedElement2, typedElement3, typedElement4, this, p)));
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
	public QuadTypedMenu(String name, QuintFunction<Player, T, U, V, X, Inventory> constructInventory, int size) {
		super(name, size);
		this.constructInventory = constructInventory;
	}

	public void setButton(int slot, QuadTypedButton<T, U, V, X> button) {
		actionMap.put(slot, button);
	}

	public boolean hasActionAt(int slot) {
		return actionMap.containsKey(slot);
	}

	public SextConsumer<ClickType, T, U, V, X, Player> getActionAt(int slot) {
		return actionMap.get(slot).getClickAction();
	}

	public QuintFunction<Player, T, U, V, X, Inventory> getConstructInventory() {
		return constructInventory;
	}

	public void setConstructInventory(QuintFunction<Player, T, U, V, X, Inventory> constructInventory) {
		this.constructInventory = constructInventory;
	}

	public QuadTypedMenu decorateMenu() {
		// getTemplate().forEach((pane) -> pane.forEach((index) -> setButton(index, new
		// QuadTypedButton((p, t, u, v, x) ->
		// ItemUtil.buildItem(Material.STAINED_GLASS_PANE, " ", pane.getColor(), new
		// ArrayList())))));
		return this;
	}
}
