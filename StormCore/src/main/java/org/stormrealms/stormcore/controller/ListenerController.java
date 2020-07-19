package org.stormrealms.stormcore.controller;

import javax.annotation.PostConstruct;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.springframework.stereotype.Controller;
import org.stormrealms.stormcore.StormCore;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * We could probably gen this class at runtime but meh this is less thinking
 * 
 * @author Redmancometh
 *
 */
@Controller
public class ListenerController implements Listener {

	private Multimap<Class, ScriptObjectMirror> events = HashMultimap.create();

	@PostConstruct
	public void register() {
		Bukkit.getPluginManager().registerEvents(this, StormCore.getInstance());
	}

	public void registerEvent(Class<? extends Event> eventClass, ScriptObjectMirror action) {
		events.put(eventClass, action);
	}

	@EventHandler
	public void craft(CraftItemEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void moveItem(InventoryMoveItemEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onEntity(PlayerInteractEntityEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void heldEvent(PlayerItemHeldEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onSneak(PlayerVelocityEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onTele(PlayerTeleportEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void preLogin(PlayerPickupItemEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void preLogin(PlayerPreLoginEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		events.get(e.getClass()).forEach((exec) -> exec.call(this, e));
	}
}
