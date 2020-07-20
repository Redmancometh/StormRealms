package org.stormrealms.stormcore.outfacing;

import lombok.Getter;
import lombok.Setter;

public enum RPGStat {
	DMG_MIN("Damage Minimum"), DMG_MAX("Damage Maximum"), ARMOR("Armor"), STR("Strength"), STA("Stamina"),
	AGI("Agility"), INT("Intellect"), SPI("Spirit");

	private static RPGStat[] basicStats = { STR, STA, AGI, INT, SPI };
	@Getter
	@Setter
	private String name;

	RPGStat(String name) {
		this.name = name;
	}

	public RPGStat[] basicStats() {
		return basicStats;
	}
}
