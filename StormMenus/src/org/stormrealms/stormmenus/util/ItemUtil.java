package org.stormrealms.stormmenus.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemUtil {
	public static List<Material> swords = Arrays.asList(Material.DIAMOND_SWORD, Material.IRON_SWORD,
			Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD);

	private static Map<EntityType, String> nameMap = new ConcurrentHashMap();

	static {
		nameMap.put(EntityType.SKELETON,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Skeleton Spawner"));
		nameMap.put(EntityType.SLIME, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Slime Spawner"));
		nameMap.put(EntityType.BLAZE, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Blaze Spawner"));
		nameMap.put(EntityType.PIG_ZOMBIE,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Zombie Pigman Spawner"));
		nameMap.put(EntityType.SQUID, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Squid Spawner"));
		nameMap.put(EntityType.MUSHROOM_COW,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Mooshroom Spawner"));
		nameMap.put(EntityType.VILLAGER,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Villager Spawner"));
		nameMap.put(EntityType.SPIDER,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Spider Spawner"));
		nameMap.put(EntityType.CREEPER,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Creeper Spawner"));
		nameMap.put(EntityType.ENDERMAN,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Enderman Spawner"));
		nameMap.put(EntityType.IRON_GOLEM,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Iron Golem Spawner"));
		nameMap.put(EntityType.ZOMBIE,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Zombie Spawner"));
		nameMap.put(EntityType.COW, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Cow Spawner"));
		nameMap.put(EntityType.CHICKEN,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Chicken Spawner"));
		nameMap.put(EntityType.WOLF, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Wolf Spawner"));
		nameMap.put(EntityType.PIG, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Pig Spawner"));
		nameMap.put(EntityType.SHEEP, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Sheep Spawner"));
		nameMap.put(EntityType.RABBIT,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Rabbit Spawner"));
		nameMap.put(EntityType.WITCH, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Witch Spawner"));
		nameMap.put(EntityType.CAVE_SPIDER,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Cave Spider Spawner"));
		nameMap.put(EntityType.OCELOT,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Ocelot Spawner"));
		nameMap.put(EntityType.HORSE, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Horse Spawner"));
		nameMap.put(EntityType.SLIME, ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Slime Spawner"));
		nameMap.put(EntityType.SILVERFISH,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Silverfish Spawner"));
		nameMap.put(EntityType.MUSHROOM_COW,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Mushroom Cow Spawner"));
		nameMap.put(EntityType.GUARDIAN,
				ChatColor.translateAlternateColorCodes('&', ChatColor.ITALIC + "Guardian Spawner"));
	}

	public static ItemStack addGlow(ItemStack is) {
		// TODO FIXME
		return is;
	}

	public static ItemStack addLore(ItemStack i, String lore) {
		ItemMeta meta = i.getItemMeta();
		List<String> loreList = i.getItemMeta().getLore();
		if (loreList == null) {
			loreList = new ArrayList<>();
		}
		loreList.add(lore);
		meta.setLore(loreList);
		i.setItemMeta(meta);
		return i;
	}

	public static void addLore(ItemStack i, String... lore) {
		ItemMeta meta = i.getItemMeta();
		List<String> loreList = i.getItemMeta().getLore();
		if (loreList == null) {
			loreList = new ArrayList();
		}
		loreList.addAll(Arrays.asList(lore));
		meta.setLore(loreList);
		i.setItemMeta(meta);
	}

	public static ItemStack buildItem(Material m, String itemName, String... lore) {
		ItemStack i = new ItemStack(m);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(itemName);
		meta.setLore(Arrays.asList(lore));
		i.setItemMeta(meta);
		return i;
	}

	public static ItemStack buildItem(Material m, String itemName, List<String> lore) {
		ItemStack i = new ItemStack(m, 1);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(itemName);
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack buildItem(Material m, String itemName, short dataValue, List<String> lore) {
		ItemStack i = new ItemStack(m, 1, dataValue);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(itemName);
		meta.setLore(lore);
		i.setItemMeta(meta);
		return i;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack buildItem(Material m, String itemName, short dataValue, String... lore) {
		ItemStack i = new ItemStack(m, 1, dataValue);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(itemName);
		meta.setLore(Arrays.asList(lore));
		i.setItemMeta(meta);
		return i;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack constructItem(Material m, String displayName, int data, String... lore) {
		ItemStack i = new ItemStack(m, 1, (short) data);
		ItemMeta iMeta = i.getItemMeta();
		iMeta.setDisplayName(displayName);
		iMeta.setLore(Arrays.asList(lore));
		i.setItemMeta(iMeta);
		return i;
	}

	public static ItemStack getSpawnerItem(EntityType type) {
		ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
		ItemMeta meta = spawnerItem.getItemMeta();
		if (meta instanceof BlockStateMeta && ((BlockStateMeta) meta).getBlockState() instanceof CreatureSpawner) {
			BlockState bs = ((BlockStateMeta) meta).getBlockState();
			((CreatureSpawner) bs).setSpawnedType(type);
			((BlockStateMeta) meta).setBlockState(bs);
		}
		meta.setDisplayName(nameMap.get(type));
		spawnerItem.setAmount(1);
		spawnerItem.setItemMeta(meta);
		return spawnerItem;
	}

	public static String getSpawnerNameForType(EntityType type) {
		return nameMap.get(type);
	}

	public static boolean isCookedFood(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			switch (item.getType()) {
			case COOKED_SALMON:
				return true;
			case COOKED_COD:
				return true;
			case COOKED_BEEF:
				return true;
			case COOKED_CHICKEN:
				return true;
			case COOKED_MUTTON:
				return true;
			case COOKED_RABBIT:
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	public static boolean isPick(ItemStack i) {
		if (i == null) {
			return false;
		}
		switch (i.getType()) {
		case WOODEN_PICKAXE:
			return true;
		case STONE_PICKAXE:
			return true;
		case IRON_PICKAXE:
			return true;
		case GOLDEN_PICKAXE:
			return true;
		case DIAMOND_PICKAXE:
			return true;
		default:
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean isRawFood(ItemStack item) {
		if (item != null && item.getType() != Material.AIR) {
			switch (item.getType()) {
			case LEGACY_RAW_FISH:
				return true;
			case LEGACY_RAW_BEEF:
				return true;
			case LEGACY_RAW_CHICKEN:
				return true;
			case MUTTON:
				return true;
			case RABBIT:
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	public static boolean isWeapon(ItemStack item) {
		return swords.contains(item.getType());
	}

	public static ItemStack setLore(ItemStack i, List<String> lore) {
		ItemMeta meta = i.getItemMeta();
		meta.setLore(lore);
		ItemStack i2 = i;
		i2.setItemMeta(meta);
		return i2;
	}

	public static void setName(ItemStack i, String name) {
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(name);
		i.setItemMeta(meta);
	}

	/**
	 * Attempt to take an item from a player taking into account first material,
	 * then displayname if a displayname is used. Then it will check lore if lore is
	 * used.
	 * 
	 * @param item      The item you're trying to remove from the inventory in
	 *                  question.
	 * @param inventory The inventory to remove from
	 * @param qty       This is the maximum amount of the item to remove
	 * @return Will return 0 if no items could be removed; otherwise this return the
	 *         quantity of items removed
	 */
	public static int takeItems(ItemStack item, Inventory inventory, Integer qty) {
		int removed = 0;
		if (item == null || item.getType() == Material.AIR)
			return 0;
		ListIterator<ItemStack> iter = inventory.iterator();
		while (iter.hasNext()) {
			ItemStack currentItem = iter.next();
			if (currentItem != null && currentItem.getType() == item.getType() && compareItems(currentItem, item)) {
				if (currentItem.getAmount() + removed > qty) {
					currentItem.setAmount(currentItem.getAmount() - (qty - removed));
					return qty;
				}
				removed += currentItem.getAmount();
				iter.set(new ItemStack(Material.AIR));
			}
		}
		return removed;
	}

	/**
	 * Compare two ItemStacks. Compares
	 * 
	 * @param item
	 * @param target
	 * @return
	 */
	public static boolean compareItems(ItemStack item, ItemStack target) {
		if (item.getType() != target.getType())
			return false;
		if ((!target.hasItemMeta())
				|| (target.getItemMeta().getDisplayName() == null && target.getItemMeta().getLore() == null))
			return true;
		if (!item.hasItemMeta())
			return false;
		ItemMeta meta = item.getItemMeta();
		if (target.getItemMeta().hasDisplayName()) {
			if (!meta.hasDisplayName())
				return false;
			if (!meta.getDisplayName().equals(target.getItemMeta().getDisplayName()))
				return false;
		}
		if (target.getItemMeta().hasLore()) {
			if (!meta.hasLore())
				return false;
			if (!target.getItemMeta().getLore().equals(meta.getLore()))
				return false;
		}
		if (meta.hasLore()) {
			if (!target.getItemMeta().hasLore())
				return false;
			if (!target.getItemMeta().getLore().equals(target.getItemMeta().getLore()))
				return false;
		}
		if (meta.hasDisplayName()) {
			if (!target.getItemMeta().hasDisplayName())
				return false;
			if (!target.getItemMeta().getDisplayName().equals(target.getItemMeta().getDisplayName()))
				return false;
		}
		return true;
	}

}
