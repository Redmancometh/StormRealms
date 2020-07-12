package org.stormrealms.stormcore.outfacing;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public enum RPGStat {
	DMG_MIN("Damage Minimum"), DMG_MAX("Damage Maximum"), ARMOR("Armor"), STR("Strength"), STA("Stamina"),
	AGI("Agility"), INT("Intellect"), SPI("Spirit");

	@Getter
	@Setter
	private String name;

	RPGStat(String name) {
		this.name = name;
	}

	public static Set<RPGStat> characterStats() {
		Set<RPGStat> statSet = new HashSet();
		statSet.add(STR);
		statSet.add(STA);
		statSet.add(AGI);
		statSet.add(INT);
		statSet.add(SPI);
		return statSet;
	}
}
