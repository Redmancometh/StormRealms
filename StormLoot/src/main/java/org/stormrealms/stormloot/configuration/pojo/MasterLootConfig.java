package org.stormrealms.stormloot.configuration.pojo;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class MasterLootConfig {
	@Autowired
	private ArmorPrefixes armorPrefixes;
	@Autowired
	private WeaponPrefixes weaponPrefixes;
	@Autowired
	private ArmorRoots armorRoots;
	@Autowired
	private WeaponRoots weaponRoots;
	@Autowired
	private ArmorSuffixes armorSuffixes;
	@Autowired
	private WeaponSuffixes weaponSuffixes;

	public LootPrefix randomWeaponPrefix() {
		Collections.shuffle(weaponPrefixes.getPrefixes());
		return weaponPrefixes.getPrefixes().get(0);
	}

	public ItemRoot randomWeaponRoot() {
		Collections.shuffle(weaponRoots.getRoots());
		return weaponRoots.getRoots().get(0);
	}

	public LootSuffix randomWeaponSuffix() {
		Collections.shuffle(weaponSuffixes.getSuffixes());
		return weaponSuffixes.getSuffixes().get(0);
	}

	public LootPrefix randomArmorPrefix() {
		Collections.shuffle(armorPrefixes.getPrefixes());
		return armorPrefixes.getPrefixes().get(0);
	}

	public ItemRoot randomArmorRoot() {
		Collections.shuffle(armorRoots.getRoots());
		return armorRoots.getRoots().get(0);
	}

	public LootSuffix randomArmorSuffix() {
		Collections.shuffle(armorSuffixes.getSuffixes());
		return armorSuffixes.getSuffixes().get(0);
	}
}
