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

	public WeaponPrefix randomWeaponPrefix() {
		Collections.shuffle(weaponPrefixes.getWeaponPrefixes());
		return weaponPrefixes.getWeaponPrefixes().get(0);
	}

	public WeaponRoot randomWeaponRoot() {
		Collections.shuffle(weaponRoots.getRoots());
		return weaponRoots.getRoots().get(0);
	}

	public WeaponSuffix randomWeaponSuffix() {
		Collections.shuffle(weaponSuffixes.getSuffixes());
		return weaponSuffixes.getSuffixes().get(0);
	}

	public ArmorPrefix randomArmorPrefix() {
		Collections.shuffle(armorPrefixes.getPrefixes());
		return armorPrefixes.getPrefixes().get(0);
	}

	public ArmorRoot randomArmorRoot() {
		Collections.shuffle(armorRoots.getRoots());
		return armorRoots.getRoots().get(0);
	}

	public ArmorSuffix randomArmorSuffix() {
		Collections.shuffle(armorSuffixes.getSuffixes());
		return armorSuffixes.getSuffixes().get(0);
	}
}
