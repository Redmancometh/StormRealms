package org.stormrealms.stormcore.outfacing;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager.LocationAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.RPGStatAdapter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Data;

@Data
public class RPGGearData {
	private Map<RPGStat, Integer> bonuses = new HashMap();
	protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
			.registerTypeAdapter(Location.class, new LocationAdapter())
			.registerTypeAdapter(RPGStat.class, new RPGStatAdapter()).setLenient().create();

	protected NamespacedKey key = new NamespacedKey(StormCore.getInstance(), "rpggear");

	public ItemStack attachTo(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(key, PersistentDataType.STRING, gson.toJson(this));
		item.setItemMeta(meta);
		return item;
	}

	public void addBonus(RPGStat stat, int amount) {
		bonuses.put(stat, bonuses.getOrDefault(stat, 0 + amount));
	}

	public ItemMeta attachTo(ItemMeta meta) {
		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(key, PersistentDataType.STRING, gson.toJson(this));
		return meta;
	}
}
