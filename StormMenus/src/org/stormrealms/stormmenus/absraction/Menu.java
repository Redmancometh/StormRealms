package org.stormrealms.stormmenus.absraction;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormmenus.menus.ClickType;
import org.stormrealms.stormmenus.menus.MenuButton;
import org.stormrealms.stormmenus.util.ItemUtil;
import org.stormrealms.stormmenus.util.PaneUtil;
import org.stormrealms.stormmenus.MenuManager;
import org.stormrealms.stormmenus.MenuTemplate;

import net.md_5.bungee.api.ChatColor;

public abstract class Menu extends BaseMenu {
	protected Map<Integer, MenuButton> actionMap = new ConcurrentHashMap<>();
	protected Function<Player, Inventory> constructInventory;
	@Autowired
	private MenuManager manager;

	public void open(Player p) {
		constructInventory.apply(p);
		manager.setPlayerMenu(p.getUniqueId(), this);
	}

	public void closeMenu(Player p) {

	}

	/**
	 * Construct a menu, and provide your own generified inventory constructor
	 * 
	 * @param name
	 * @param constructInventory
	 */
	public Menu(String name, Function<Player, Inventory> constructInventory) {
		super(ChatColor.translateAlternateColorCodes('&', name), 18);
		this.constructInventory = constructInventory;
	}

	/**
	 * Construct a menu with completely default parameters. This well default to
	 * size 9, and the default generified inventory constructor.
	 * 
	 * @param name
	 * @param size
	 */
	public Menu(String name) {
		super(name, 18);
		this.constructInventory = (p) -> {
			Inventory menuInv = Bukkit.createInventory(null, getSize(),
					ChatColor.translateAlternateColorCodes('&', this.getName()));
			actionMap.forEach((number, button) -> menuInv.setItem(number, button.constructButton(this, p)));
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
	public Menu(String name, int size) {
		super(name, size);
		this.constructInventory = (p) -> {
			Inventory menuInv = Bukkit.createInventory(null, size,
					ChatColor.translateAlternateColorCodes('&', this.getName()));
			actionMap.forEach((number, button) -> menuInv.setItem(number, button.constructButton(this, p)));
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
	public Menu(String name, MenuTemplate template, int size) {
		super(name, size, template);
		this.constructInventory = (p) -> {
			Inventory menuInv = Bukkit.createInventory(null, size,
					ChatColor.translateAlternateColorCodes('&', this.getName()));
			actionMap.forEach((number, button) -> menuInv.setItem(number, button.constructButton(this, p)));
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
	public Menu(String name, Function<Player, Inventory> constructInventory2, int size) {
		super(name, size);
		this.constructInventory = constructInventory2;
	}

	/**
	 * Construct a menu with completely default parameters. This well default to
	 * size 9, and the default generified inventory constructor.
	 * 
	 * @param name
	 * @param size
	 */
	public Menu(String name, Function<Player, Inventory> constructInventory2, MenuTemplate template, int size) {
		super(name, size, template);
		this.constructInventory = (p) -> {
			Inventory menuInv = Bukkit.createInventory(null, size,
					ChatColor.translateAlternateColorCodes('&', this.getName()));
			actionMap.forEach((number, button) -> menuInv.setItem(number, button.constructButton(this, p)));
			return menuInv;
		};
		decorateMenu();
	}

	public void setButton(int slot, MenuButton button) {
		actionMap.put(slot, button);
	}

	public boolean hasActionAt(int slot) {
		return actionMap.containsKey(slot);
	}

	public BiConsumer<ClickType, Player> getActionAt(int slot) {
		return actionMap.get(slot).getClickAction();
	}

	public Function<Player, Inventory> getConstructInventory() {
		return constructInventory;
	}

	public void setConstructInventory(Function<Player, Inventory> constructInventory) {
		this.constructInventory = constructInventory;
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

	public Menu decorateMenu() {
		getTemplate().forEach((pane) -> pane.forEach((index) -> setButton(index,
				new MenuButton((p) -> ItemUtil.buildItem(PaneUtil.getPaneFor(pane.getColor()), "", new ArrayList())))));
		return this;
	}
}
