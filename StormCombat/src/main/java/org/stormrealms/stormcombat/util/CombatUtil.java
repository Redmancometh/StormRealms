package org.stormrealms.stormcombat.util;

import java.lang.reflect.Modifier;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager.LocationAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.RPGStatAdapter;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.model.RPGCharacter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class CombatUtil {
	private NamespacedKey key = new NamespacedKey(StormCore.getInstance(), "rpggear");
	@Autowired
	private StatRepo rpgPlayerRepo;
	protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
			.registerTypeAdapter(Location.class, new LocationAdapter())
			.registerTypeAdapter(RPGStat.class, new RPGStatAdapter()).setLenient().setPrettyPrinting().create();

	public boolean isRPGGear(ItemStack weapon) {
		if (weapon.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING))
			return true;
		return false;
	}

	public RPGGearData getRPGGearData(ItemStack weapon) {
		return gson.fromJson(weapon.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING),
				RPGGearData.class);
	}

	public RPGCharacter getRPGCharacter(Player player) {
		return rpgPlayerRepo.getBlocking(player.getUniqueId()).getChosenCharacter();
	}

	public boolean hasRPGOffhand(Player p) {
		return isRPGGear(p.getInventory().getItemInOffHand());
	}
}
