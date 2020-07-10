package org.stormrealms.stormloot.configuration.pojo;

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
}
