package org.stormrealms.stormcombat.combatsystem;

import java.lang.reflect.Modifier;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stormrealms.stormcombat.events.WeaponAttackEvent;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormcore.config.ConfigManager.LocationAdapter;
import org.stormrealms.stormcore.config.ConfigManager.MaterialAdapter;
import org.stormrealms.stormcore.config.ConfigManager.RPGStatAdapter;
import org.stormrealms.stormcore.outfacing.RPGGearData;
import org.stormrealms.stormcore.outfacing.RPGStat;
import org.stormrealms.stormstats.data.StatRepo;
import org.stormrealms.stormstats.model.RPGPlayer;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class SimpleCombatProcessor implements CombatProcessor {

	@Autowired
	private StatRepo rpgPlayerRepo;

	NamespacedKey key = new NamespacedKey(StormCore.getInstance(), "rpggear");
	protected Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.registerTypeHierarchyAdapter(Material.class, new MaterialAdapter())
			.registerTypeAdapter(Location.class, new LocationAdapter())
			.registerTypeAdapter(RPGStat.class, new RPGStatAdapter()).setLenient().setPrettyPrinting().create();

	@Override
	public boolean isRPGWeapon(ItemStack weapon) {
		if (weapon.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING))
			return true;
		return false;
	}

	@Override
	public void dodged(WeaponAttackEvent e) {

		System.out.println("DODGED");
	}

	@Override
	public void parried(WeaponAttackEvent e) {
		System.out.println("PARRIED");
	}

	@Override
	public void missed(WeaponAttackEvent e) {
		System.out.println("MISSED");
	}

	@Override
	public void hit(WeaponAttackEvent e) {
		System.out.println("HIT");
	}

	@Override
	public void giveLoot(WeaponAttackEvent e) {
		System.out.println("GIVE LOOT");
	}

	@Override
	public RPGGearData getRPGWeapon(ItemStack weapon) {
		return gson.fromJson(weapon.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING),
				RPGGearData.class);
	}

	@Override
	public RPGPlayer getRPGPlayer(Player player) {
		return rpgPlayerRepo.getBlocking(player.getUniqueId());
	}

}
