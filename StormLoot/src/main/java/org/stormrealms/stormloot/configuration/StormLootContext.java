package org.stormrealms.stormloot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.stormrealms.stormcore.config.ConfigManager;
import org.stormrealms.stormloot.configuration.pojo.ArmorPrefixes;
import org.stormrealms.stormloot.configuration.pojo.ArmorRoots;
import org.stormrealms.stormloot.configuration.pojo.ArmorSuffixes;
import org.stormrealms.stormloot.configuration.pojo.ItemEffects;
import org.stormrealms.stormloot.configuration.pojo.WeaponPrefixes;
import org.stormrealms.stormloot.configuration.pojo.WeaponRoots;
import org.stormrealms.stormloot.configuration.pojo.WeaponSuffixes;

/**
 * 
 * @author Redmancometh
 *
 */
@Configuration
@ComponentScan(basePackages = { "org.stormrealms.stormloot", "org.stormrealms.stormloot.controller",
		"org.stormrealms.stormloot.listeners", "org.stormrealms.stormloot.configuration",
		"org.stormrealms.stormloot.configuration.pojo" })
public class StormLootContext {

	@Bean
	public ConfigManager<WeaponRoots> weaponRootsCfg() {
		ConfigManager<WeaponRoots> weaponRootCfg = new ConfigManager("weaponroots.json", WeaponRoots.class);
		weaponRootCfg.init();
		return weaponRootCfg;
	}

	@Bean
	@Scope("prototype")
	public WeaponRoots weaponRoots(ConfigManager<WeaponRoots> confMan) {
		return confMan.getConfig();
	}

	@Bean
	public ConfigManager<ArmorRoots> armorRootsCfg() {
		ConfigManager<ArmorRoots> armorRootCfg = new ConfigManager("armorroots.json", ArmorRoots.class);
		armorRootCfg.init();
		return armorRootCfg;
	}

	@Bean
	@Scope("prototype")
	public ArmorRoots armorRoots(ConfigManager<ArmorRoots> confMan) {
		return confMan.getConfig();
	}

	@Bean
	public ConfigManager<WeaponPrefixes> weaponPrefixesCfg() {
		ConfigManager<WeaponPrefixes> weaponRootCfg = new ConfigManager("weaponprefixes.json", WeaponPrefixes.class);
		weaponRootCfg.init();
		return weaponRootCfg;
	}

	@Bean
	public ConfigManager<ArmorPrefixes> armorPrefixesCfg() {
		ConfigManager<ArmorPrefixes> weaponRootCfg = new ConfigManager("armorprefixes.json", ArmorPrefixes.class);
		weaponRootCfg.init();
		return weaponRootCfg;
	}

	@Bean
	@Scope("prototype")
	public ArmorPrefixes armorPrefixes(ConfigManager<ArmorPrefixes> weaponPrefixesCfg) {
		return weaponPrefixesCfg.getConfig();
	}

	@Bean
	@Scope("prototype")
	public WeaponPrefixes weaponPrefixes(ConfigManager<WeaponPrefixes> weaponPrefixesCfg) {
		return weaponPrefixesCfg.getConfig();
	}

	@Bean
	public ConfigManager<WeaponSuffixes> weaponSuffixesCfg() {
		ConfigManager<WeaponSuffixes> weaponRootCfg = new ConfigManager("weaponsuffixes.json", WeaponSuffixes.class);
		weaponRootCfg.init();
		return weaponRootCfg;
	}

	@Bean
	@Scope("prototype")
	public WeaponSuffixes weaponSuffixes(ConfigManager<WeaponSuffixes> weaponSuffixesCfg) {
		return weaponSuffixesCfg.getConfig();
	}

	@Bean
	public ConfigManager<ArmorSuffixes> armorSuffixesCfg() {
		ConfigManager<ArmorSuffixes> weaponRootCfg = new ConfigManager("armorsuffixes.json", ArmorSuffixes.class);
		weaponRootCfg.init();
		return weaponRootCfg;
	}

	@Bean
	@Scope("prototype")
	public ArmorSuffixes armorSuffixes(ConfigManager<ArmorSuffixes> armorSuffixesCfg) {
		return armorSuffixesCfg.getConfig();
	}

	@Bean
	@Scope("prototype")
	public ItemEffects itemEffects(ConfigManager<ItemEffects> itemEffects) {
		return itemEffects.getConfig();
	}

	@Bean
	public ConfigManager<ItemEffects> itemEffects() {
		ConfigManager<ItemEffects> weaponRootCfg = new ConfigManager("itemeffects.json", ArmorSuffixes.class);
		weaponRootCfg.init();
		return weaponRootCfg;
	}

}
